package no.auke.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


// TODO: Make better thread wrapping and startup
// http://download.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/ExecutorService.html
// may change to runnable and start with a ExecutorService pool 

//HUYDO: we treat this as a worker thread, there is a default executor inside, but we possibly set executor from outside
//But when you set from outside, be carefull when you stop, you will shutdown all other worker threads if you call this.executor.shutdownNow();
//call stopThread() -> fine, 
//call forceStopThread() -> shutdown now immediately.

public abstract class BaseRunable implements Runnable {

	protected int WAITPERIOD = 1000; 
	protected ExecutorService executor; 

	public void setExecutor(ExecutorService exec)
	{
		this.executor = exec;
	}


	protected String threadname = "";
	private AtomicBoolean isrunning = new AtomicBoolean();
	private AtomicBoolean isstopping = new AtomicBoolean();

	public boolean isRunning() {
		return isrunning.get();
	}

	protected String getThreadname ()
	{
		return threadname;
	}
	protected abstract int getWaitperiod();

	// Thread main;
	public void startThread() {

		if (!isrunning.getAndSet(true)) {

			if (executor == null) {
				executor = Executors.newCachedThreadPool();
			}

			onStart();
			
			this.executor.execute(this);

		}
	}

	public void stopThread() {

        // FIXME: LHA, controlled stop on all procedures i.e stop peer server controlled
	    
        onStopped();
		if (!isstopping.getAndSet(true)) {
			
			if (executor != null && !this.executor.isShutdown()) {
			    this.executor.shutdownNow();
			}
			this.executor = null;		
		}
	}

	public void forceStopThread() {

	    // FIXME: LHA, controlled stop on all procedures i.e stop peer server controlled
	    
	    onStopped();
		if (!isstopping.getAndSet(true)) {
			
			if (executor != null && !this.executor.isShutdown()) {
				
				this.executor.shutdownNow();
			}
			this.executor = null;
		}
	}

	public BaseRunable() {
	}

	public BaseRunable(String threadname) {
		this.threadname = threadname;
	}

	public BaseRunable(ExecutorService executor, String threadname) {
		this(threadname);
		this.executor = executor;
	}

	protected abstract void execute();

	protected abstract boolean doExecute();

	protected abstract void onStart();

	protected abstract void onStarted();

	protected abstract void onStopped();

	String getStackTrace(Throwable t) {
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	public void run() {

		onStarted();

		while (isrunning.get() && !isstopping.get()) {
			
			try {

				if (doExecute()) {
					execute();
				}
				Thread.sleep(getWaitperiod());

			} catch (InterruptedException e) {
				
			} catch (Exception e) {
				
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		}
		
		
		onStopped();
		isrunning.set(false);
		isstopping.set(false);

	}
}
