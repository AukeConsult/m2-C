package no.auke.m2.task;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Task {
	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	private static Random rnd = new Random();
	protected int frequency = 0;
	protected AtomicLong nextExcute = new AtomicLong();
	protected AtomicBoolean started = new AtomicBoolean();
	protected AtomicBoolean stopped = new AtomicBoolean();
	protected AtomicBoolean dostop = new AtomicBoolean();
	protected AtomicBoolean isstopping = new AtomicBoolean();
	private AtomicLong startExcute = new AtomicLong();
	// watch frequency
	protected long lastMeasure = 0;
	protected int counter = 0;
	public boolean isLongRunning() {
		// TODO: if task longer that 10 seconds, its aborted from queue in task
		// monitor (not implemented yet)
		return startExcute.get() > 0 & System.currentTimeMillis() - startExcute.get() > 10000;
	}
	protected TaskMonitor monitor;
	public TaskMonitor getTaskMonitor() {
		return monitor;
	}
	private long id = rnd.nextLong();
	public long getId() {
		return id;
	}
	public Task(int serverId, int frequency) {
		this.frequency = frequency;
		this.serverId = serverId;
		stopped.set(true);
		started.set(false);
		nextExcute.set(System.currentTimeMillis() + frequency - 1);
		lastMeasure = System.currentTimeMillis();
	}
	protected int serverId = 0;
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public Task(int serverId) {
		this(serverId, Integer.MAX_VALUE);
	}
	public void start(TaskMonitor monitor) {
		if (stopped.get()) {
			dostop.set(false);
			stopped.set(false);
			isstopping.set(false);
			started.set(false);
			nextExcute.set(System.currentTimeMillis() + frequency - 1);
		}
		if (monitor != null) {
			this.monitor = monitor;
			this.monitor.addExcute(this);
		} else {
			if (!started.getAndSet(true)) {
				onStart();
			}
		}
	}
	public void stop() {
		if (!stopped.get()) {
			if (monitor != null) {
				dostop.set(true);
				isstopping.set(true);
				monitor.addExcute(this);
			} else {
				forceStop();
			}
		}
	}
	// force stopping
	// don't use monitor execution to fire stop
	public void forceStop() {
		// remove from monitor queue
		if (monitor != null) {
			monitor.removeTask(this);
		}
		if (!stopped.getAndSet(true)) {
			onStop();
		}
	}
	public boolean isStarted() {
		return started.get();
	}
	public boolean isStopped() {
		return stopped.get();
	}
	public boolean isCompleted() {
		return started.get() && stopped.get();
	}
	public void waitStopped() {
		int wait = 0;
		while (wait < this.frequency && isstopping.get()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			wait += 10;
		}
	}
	public boolean isRunning() {
		return started.get() && !stopped.get();
	}
	public void waitFor(long time) {
		nextExcute.set(System.currentTimeMillis() + time);
	}
	public boolean schedule(long time) {		
		if ((time - nextExcute.get()) >= 0) {
			return true;
		}
		return false;
	}
	public void execute() {
		if (monitor != null)
			monitor.addExcute(this);
	}
	public void execute(long time) {
		
		if ((System.currentTimeMillis() - lastMeasure) > getTaskMonitor().getFrequency() * 10) {
			if (counter > 20) {
				logger.warn(getTaskMonitor().getMonitorName() + " taskid " + getId() + " to many executions " + counter);
			}
			counter = 0;
		}
		counter++;
		startExcute.set(System.currentTimeMillis());
		if (!started.getAndSet(true)) {
			onStart();
		}
		if (dostop.get()) {
			// do stop execution
			if (!stopped.getAndSet(true)) {
				onStop();
				isstopping.set(false);
				started.set(false);
			}
		}
		
		if (isRunning()) {
			// set frequency before execute in case next execute time is set
			// inside on execute
			// make a test for this
			// calculate next execution time
			nextExcute.set(time + frequency - 1);
			// to make sure stop signal get at close to execute as possible
			if (!stopped.get()) {
				onExecute();
				if (frequency < 0 || dostop.get()) {
					// run stop if frequency set to zero or is stopped inside
					// execute
					if (!stopped.getAndSet(true)) {
						monitor = null;
						onStop();
						isstopping.set(false);
					}
				}
			}
		}
		startExcute.set(0);
	}
	public abstract void onStart();
	public abstract void onExecute();
	public abstract void onStop();
}
