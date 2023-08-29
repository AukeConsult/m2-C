package no.auke.m2.task;
public abstract class TaskExecute extends Task {
	public TaskExecute(int serverId, int frequency) {
		super(serverId, frequency);
	}
	public final void onStart() {};
	public final void onStop() {};
}
