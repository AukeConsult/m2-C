package no.auke.m2.task;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
// task that wait before execute first time
public abstract class TaskWaitFirst extends Task {
	// private static final Logger logger = LoggerFactory.getLogger(Task.class);
	public TaskWaitFirst(int serverId, int frequency) {
		super(serverId, frequency);
	}
	@Override
	public void execute(long time) {
		if (isRunning() && !dostop.get()) {
			// set frequency before execute in case next execute time is set
			// inside on execute
			// make a test for this
			// calculate next execution time
			nextExcute.set(time + frequency - 1);
			onExecute();
			if (frequency < 0) {
				// run stop if frequency set to zero
				if (!stopped.getAndSet(true)) {
					onStop();
				}
			}
		}
		if (!started.getAndSet(true)) {
			onStart();
		}
		// this
		if (dostop.get()) {
			// do stop execution
			if (!stopped.getAndSet(true)) {
				onStop();
			}
		}
	}
}
