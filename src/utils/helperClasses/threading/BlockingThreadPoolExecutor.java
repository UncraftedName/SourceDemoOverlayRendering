package utils.helperClasses.threading;

import java.util.concurrent.*;


// A custom thread pool executor that has a max capacity to prevent excessive array usage, which is nice to have since
// the image saving requires storing all images in the queue, which is no fun for the memory.
// And importantly: when the queue is full, this executor will block until space is made.
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    public static final int threadCount = Runtime.getRuntime().availableProcessors();
    public static final int queueCapacity = 25;


    // funny meme, i trick the executor to make more threads by telling it the queue is full (which it is),
    // and then when it throw an exception to complain about that i just do nothing ¯\_(ツ)_/¯
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
                        return isFull; // tell the executor if the queue is full is it can launch
                    }
                },
                (r, executor) -> {}); // rejection handler does nothing, i assume the element is properly inserted
    }
}
