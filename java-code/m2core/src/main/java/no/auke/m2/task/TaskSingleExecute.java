package no.auke.m2.task;
public abstract class TaskSingleExecute extends Task {
	public TaskSingleExecute() {
		super(0);
	}
	@Override
	public final void onStart() {}
	@Override
	public final void onStop() {}
	@Override
	public void onExecute() {
		if (onExecuteSingle()) {
			stop();
		} else {
			waitFor(25); // wait and reschedule after 25 MS if not possible
							// execute
		}
	}
	public abstract boolean onExecuteSingle();
}
