package io.tpourjalali.reflexgame;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by ProfessorTaha on 3/7/2018.
 */

public class SingleTaskThread extends Thread {
    private static final String TAG = "SingleTaskThread";
    private BlockingDeque<Runnable> taskQ;
    private InterruptReason mInterruptReason = null;
    private Runnable currentTask;

    public SingleTaskThread() {
        taskQ = new LinkedBlockingDeque<>();
    }

    @Override
    public void run() {
        Runnable task;
//        while(true) //TODO: uncomment this
        {
            if (isInterrupted()) {
                if (Objects.isNull(mInterruptReason) || mInterruptReason == InterruptReason.INTERRUPT_RUNNABLE) {
                    interrupted();//clear the interrupt and keep the thread running;
                } else {
                    //interrupt is because we want to shutdown the thread.
                    return;
                }
            }
            try {
                task = taskQ.takeFirst();
                task.run();
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted while waiting for more tasks", e);
                return;
            }
        }
    }

    public void enqueRunnable(@NonNull Runnable task) {
        taskQ.addLast(task);
    }

    public void shutdown() {
        mInterruptReason = InterruptReason.SHUTDOWN;
        interrupt();
    }

    public void stopTask() {
        mInterruptReason = InterruptReason.INTERRUPT_RUNNABLE;
        interrupt();
    }

    private static enum InterruptReason {
        INTERRUPT_RUNNABLE, SHUTDOWN;
    }
}
