package no.auke.gui.general;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledRunnable
{
    public static void main(final String[] args)
    {
        final int numTasks = 10;
        final ScheduledExecutorService ses = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < numTasks; i++)
        {
            ses.scheduleAtFixedRate(new MyRunnable(i), 0, 10, TimeUnit.SECONDS);
        }
    }

    private static class MyRunnable implements Runnable
    {
        private int id;
        private int numRuns;

        private MyRunnable(final int id)
        {
            this.id = id;
            this.numRuns = 0;
        }

        @Override
        public void run()
        {
            this.numRuns += 1;
            System.out.format("%d - %d\n", this.id, this.numRuns);
        }
    }
}