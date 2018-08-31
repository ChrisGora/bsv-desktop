package client.handler;

import java.util.concurrent.*;

/**
 * A special executor created for debugging.
 * Allows exception encountered by threads to be thrown, and then investigated.
 * Behaviour should in theory be identical / very similar to {@link Executors#newFixedThreadPool(int)}.
 * Nevertheless, this class should only be used for debugging.
 *
 * Adapted from stackoverflow.com/questions/2248131/handling-exceptions-from-java-executorservice-tasks
 *
 * @author Chris Gora, Charlie Haslam
 * @version 1.0, 23.04.2018
 */
class DebuggingExecutor extends ThreadPoolExecutor {

    public DebuggingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Object result = ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null) {
            System.out.println("EXTENDED EXECUTOR ERROR");
            System.out.println(t);
            try {
                throw t;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}

