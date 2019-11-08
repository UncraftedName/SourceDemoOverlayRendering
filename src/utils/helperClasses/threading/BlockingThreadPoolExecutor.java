package utils.helperClasses.threading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


// A custom thread pool executor that has a max capacity to prevent excessive array usage, which is nice to have since
// the image saving requires storing all images in the queue, which is no fun for the memory.
// And importantly: when the queue is full, this executor will block until space is made.
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    // rounds up to nearest 5 cuz ocd or whatever
    private static final int threadCount = (Runtime.getRuntime().availableProcessors() + 4) / 5 * 5;
    public static final int queueCapacity = 25;


    // Funny meme - when the queue is full I block offer() until the queue has space, but still return saying
    // that offer() failed so that the executor will spawn more threads (if it can).
    // And then when it throws an exception to complain about that i just do nothing ¯\_(ツ)_/¯
    // since I know that the task was actually put in the queue successfully.
    public BlockingThreadPoolExecutor() {
        super(
                1, threadCount,
                5, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity, true) {
                    @Override
                    public boolean offer(Runnable e) {
                        boolean isFull = super.remainingCapacity() == 0;
                        try {
                            super.put(e); // offer will just call put() which blocks until space is available
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        return isFull; // tell the executor if the queue is full so it can start more threads
                    }
                },
                (r, executor) -> {}); // rejection handler does nothing; i assume the task is properly inserted into the queue
    }
}
