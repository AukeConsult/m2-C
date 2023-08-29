package no.auke.gui.general;

import java.util.LinkedList;
import java.util.Queue;
//import java.util.concurrent.Semaphore;

public class ThreadPool  {
	
	private final static int DEFAULT_SIZE = 4;
	
	private MyThread[] threads;
	private Queue<Runnable> tasks;
	//private Semaphore sem;
	
	public ThreadPool() {
		this(DEFAULT_SIZE);
	}
	
	public ThreadPool(int size) {
		//sem = new Semaphore(1, true);
		threads = new MyThread[size];
		tasks = new LinkedList<Runnable>();
		for (int i = 0 ; i < size ; i++) {
			MyThread mt = new MyThread(tasks);
			threads[i]=mt;
			mt.start();
			System.out.println("Started thread: " + mt.getId());
		}
	}


	public synchronized void add(Runnable task) {
		tasks.add(task);
		notify();
	}


	public void stopPool() {
		for (Thread t : threads) {
			t.interrupt();
		}
	}

	private class MyThread extends Thread{
		
		public MyThread(Queue<Runnable> taskList) {
			super();
		}
		
		public synchronized void run() {
			System.out.println("Running thread " + this.getId());
			Runnable job = null;
			while (true) {
			//	System.out.println(getId());
				if (!tasks.isEmpty()) {
					System.out.println("Thread " + getId() + " is getting the job");
					job = tasks.remove();
				}
				if (job != null) job.run();
			}
			
		}
	}
}