package no.auke.m2.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
public class TaskMonitor implements Runnable {
	// private static final Logger logger =
	// LoggerFactory.getLogger(TaskMonitor.class);
	private long frequency = 1000; // 1 second default
	public long getFrequency() {
		return frequency;
	}
	private AtomicLong sinceStarted = new AtomicLong();
	private String monitorName = "";
	public String getMonitorName() {
		return monitorName;
	}
	private AtomicBoolean isrunning = new AtomicBoolean();
	private Object wait = new Object();
	ExecutorService executor;
	private Map<Long, Task> tasklist = new ConcurrentHashMap<Long, Task>();
	private Queue<Task> executelist = new LinkedBlockingQueue<Task>();
	public List<Task> getRunningTasks() {
		return new ArrayList<Task>(tasklist.values());
	}
	public TaskMonitor(ExecutorService executor, int frequency, String monitorName) {
		if (frequency > 0) {
			this.frequency = frequency;
		}
		this.executor = executor;
		this.monitorName = monitorName;
		isrunning.set(false);
	}
	public void execute(Task task) {
		tasklist.put(task.getId(), task);
		task.start(this);
	}
	public void removeTask(Task task) {
		tasklist.remove(task.getId());
	}
	// direct execute
	public void addExcute(Task task) {
		if (task != null) {
			executelist.add(task);
			if (!isrunning.getAndSet(true)) {
				try {
					executor.execute(this);
					sinceStarted.set(System.currentTimeMillis());
				} catch (Exception ex) {
					isrunning.set(false);
				}
			}
			// start run execute loop
			synchronized (wait) {
				wait.notify();
			}
		}
	}
	@Override
	public void run() {
		List<Long> tasklistStopped = new ArrayList<Long>();
		while (isrunning.get()) {
			long executiontime = System.currentTimeMillis(); // execution time
			// execute waiting tasks
			while (executelist.size() > 0) {
				Task task = executelist.poll();
				// Execute Task
				if (task != null && !task.isStopped())
					task.execute(executiontime);
				sinceStarted.set(System.currentTimeMillis());
			}
			// check and reschedule tasks
			for (Task task : tasklist.values()) {
				if (task.isStarted() && task.isStopped()) {
					tasklistStopped.add(task.getId());
				} else if (task.schedule(executiontime)) {
					executelist.add(task);
				}
			}
			if (tasklistStopped.size() > 0) {
				for (Long taskid : tasklistStopped) {
					tasklist.remove(taskid);
				}
				tasklistStopped.clear();
			}
			if (tasklist.size() == 0 && executelist.size() == 0) {
				if (System.currentTimeMillis() - sinceStarted.get() > (frequency * 2)) {
					isrunning.set(false);
				}
			}
			// wait if execution list is empty
			if (isrunning.get() && executelist.size() == 0) {
				// waiting to execute
				synchronized (wait) {
					try {
						long waittime = (executiontime - System.currentTimeMillis() + frequency);
						if (waittime > 0 && executelist.size() == 0) {
							wait.wait(frequency);
						}
					} catch (InterruptedException e) {}
				}
			}
			Thread.yield();
		}
		executelist.clear();
	}
	public void stopMonitor() {
		isrunning.set(false);
	}
	public void stopMonitor(int serverId) {
		for (Task task : new ArrayList<Task>(tasklist.values())) {
			if (task.getServerId() == serverId) {
				tasklist.remove(task.getId());
			}
		}
		synchronized (wait) {
			wait.notify();
		}
	}
	public boolean isRunning() {
		return isrunning.get();
	}
}
