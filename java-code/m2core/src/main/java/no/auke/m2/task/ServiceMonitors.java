package no.auke.m2.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceMonitors {
	
	private String name;
	private ExecutorService threadpool;
	public ExecutorService getCoreExecutor() {
		if (threadpool == null || threadpool.isTerminated()) {
			threadpool = Executors.newFixedThreadPool(6);
		}
		return threadpool;
	}
	// Slow running ordinary ping
	private TaskMonitor pingMonitor;
	public TaskMonitor getPingMonitor() {return pingMonitor;}
	
	// fast running connects and sending checks
	private TaskMonitor connectMonitor;
	public TaskMonitor getConnectMonitor() {return connectMonitor;}
	// fast running connects and sending checks
	private TaskMonitor sendMonitor;
	public TaskMonitor getSendMonitor() {return sendMonitor;}
	// slow running for long time worker tasks (trigger once a second)
	private TaskMonitor workerMonitor;
	public TaskMonitor getWorkerMonitor() {return workerMonitor;}
	
	// fast running connects and sending checks
	private TaskMonitor singleTaskMonitor;
	public TaskMonitor getSingleTaskMonitor() {return singleTaskMonitor;}
	// handling message sending
	private TaskMonitor messageMonitor;
	public TaskMonitor getMessageMonitor() {return messageMonitor;}
	public ServiceMonitors(String name) {
		this.name = name;
		if (threadpool == null) {
			start();
		}
	}
	public void start() {
		if (threadpool != null) {
			// check if still alive
			threadpool.execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
				}
			});
		}
		if (threadpool == null) {
			// for regular pings
			pingMonitor = new TaskMonitor(getCoreExecutor(), 1000, "pingMonitor " + name);
			// for fast connect tasks
			connectMonitor = new TaskMonitor(getCoreExecutor(), 100, "connectMonitor " + name);
			// for sending tasks
			sendMonitor = new TaskMonitor(getCoreExecutor(), 25, "sendMonitor " + name);
			// for slow worker tasks
			workerMonitor = new TaskMonitor(getCoreExecutor(), 10000, "workerMonitor " + name);
			// for single tasks
			singleTaskMonitor = new TaskMonitor(getCoreExecutor(), 10000, "singleTaskMonitor " + name);
			// for message sending
			messageMonitor = new TaskMonitor(getCoreExecutor(), 250, "messageMonitor " + name);
		}
	}
	public void stop() {
		workerMonitor.stopMonitor();
		pingMonitor.stopMonitor();
		connectMonitor.stopMonitor();
		sendMonitor.stopMonitor();
		messageMonitor.stopMonitor();
		singleTaskMonitor.stopMonitor();
		threadpool.shutdownNow();
		threadpool = null;
	}
	public void stop(int serverId) {
		workerMonitor.stopMonitor(serverId);
		pingMonitor.stopMonitor(serverId);
		connectMonitor.stopMonitor(serverId);
		sendMonitor.stopMonitor(serverId);
		messageMonitor.stopMonitor(serverId);
		singleTaskMonitor.stopMonitor(serverId);
	}
}
