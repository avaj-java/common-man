package jaemisseo.man

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by sujkim on 2017-06-01.
 */
class ThreadMan{

    static final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Thread Pool **/
    static int DEFAULT_THREAD_CNT = 20
//    ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT)
//    ExecutorService threadPool = Executors.newCachedThreadPool();
    ThreadPoolExecutor threadPool
    Map<String, Thread> threadMap = [:]

    /** Event Closure **/
    Closure onErrorClosure
    Closure onStopClosure
    Closure onFinallyClosure

    ThreadMan() {
//        this.threadPool = newThreadPool(DEFAULT_THREAD_CNT)
    }

    ThreadMan(int THREAD_CNT){
        this.threadPool = newThreadPool(THREAD_CNT)
    }



    /**************************************************
     * Inner Class
     **************************************************/
    class CustomUncaughtHandler implements Thread.UncaughtExceptionHandler{
        @Override
        void uncaughtException(Thread t, Throwable e) {
            println t.name
            e.printStackTrace()
            println AnsiMan.testRedBg(t.name)
            println ""
            println ""
            println ""
        }
    }

    class CustomThreadPoolExecutor extends ThreadPoolExecutor {
        public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }
        @Override
        public void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            // If submit() method is called instead of execute()
            if (t == null && r instanceof Future<?>) {
                try {
                    Object result = ((Future<?>) r).get();
                } catch (CancellationException e) {
                    t = e;
                } catch (ExecutionException e) {
                    t = e.getCause();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (t != null) {
                // Exception occurred
                System.err.println("Uncaught exception is detected! " + t
                        + " st: " + Arrays.toString(t.getStackTrace()));
                // ... Handle the exception
                // Restart the runnable again
                execute(r);
            }
            // ... Perform cleanup actions
        }
    }



    /**************************************************
     * STATIC
     **************************************************/
    static ThreadMan newThread(){
        return new ThreadMan()
    }

    static ThreadMan newThread(Closure startClosure){
        return new ThreadMan().start(startClosure)
    }

    static ThreadPoolExecutor newThreadPool(){
        newThreadPool(DEFAULT_THREAD_CNT)
    }

    static ThreadPoolExecutor newThreadPool(int THREAD_CNT){
        logger.info "Create New Thread Pool.  ==> ${THREAD_CNT}"
//        return new ThreadPoolExecutor(THREAD_CNT, THREAD_CNT, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        return new CustomThreadPoolExecutor(THREAD_CNT, THREAD_CNT, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    }



    /*************************
     * ADD
     *************************/
    ThreadMan addThread(Closure startClosure){
        return start(startClosure)
    }

    ThreadMan addThread(boolean useExistEvent, Closure startClosure){
        if (!useExistEvent)
            clearEvent()
        return start(startClosure)
    }



    /*************************
     * EVENT
     *************************/
    ThreadMan onError(Closure onErrorClosure){
        this.onErrorClosure = onErrorClosure
        return this
    }

    ThreadMan onStop(Closure onStopClosure){
        this.onStopClosure = onStopClosure
        return this
    }

    ThreadMan onFinally(Closure onFinallyClosure){
        this.onFinallyClosure = onFinallyClosure
        return this
    }

    ThreadMan clearEvent(){
        this.onErrorClosure     = null
        this.onStopClosure      = null
        this.onFinallyClosure   = null
        return this
    }



    /*************************
     * START
     *************************/
    ThreadMan start(Closure startClosure){
        Thread thread = generateThread(startClosure)
        /** Start Thread **/
        thread.start()
        return this
    }

    Thread generateThread(Closure startClosure){
        /** Setup Thread **/
        Closure onStopClosure       = this.onStopClosure
        Closure onErrorClosure      = this.onErrorClosure
        Closure onFinallyClosure    = this.onFinallyClosure
        Thread thread = new Thread(new Runnable(){void run(){
            try{
                startClosure()
            }catch(InterruptedException ie){
                if (onStopClosure)
                    onStopClosure(ie)
            }catch(Exception e){
                if (onErrorClosure)
                    onErrorClosure(e)
                else
                    throw e
            }finally{
                try{
                    if (onFinallyClosure)
                        onFinallyClosure()
                }catch(e){
                    throw new InterruptedException()
                }
            }
        }})
        thread.setUncaughtExceptionHandler(new CustomUncaughtHandler())
        return thread
    }

    /*************************
     * POOL
     *************************/
    ThreadMan pool(Closure startClosure){
        Thread thread = generateThread(startClosure)
        /** Pool Thread **/
        return pool(thread)
    }

    ThreadMan pool(Thread thread){
        String id = String.valueOf(thread.id)
        if (threadMap[id]){
            logger.warn "!! Already exists Thread ID. (${id})"
            logger.debug "- Checking ThreadMap => ${threadMap}"
        }
        threadMap[id] = thread
        threadPool = (!threadPool || threadPool.isTerminated()) ? newThreadPool() : threadPool
        threadPool.execute(thread)
        logger.debug "Starting new thread.  - ID: ${id}"
        logger.debug " - active   : ${threadPool.activeCount}  "
        logger.debug " - complete : ${threadPool.completedTaskCount}/${threadPool.taskCount}"
        logger.debug " - Checking ThreadMap  ==> ${threadMap}"
        return this
    }


    /*************************
     * STOP
     * you need to add  Thread.sleep(n) in your closure
     *************************/
    ThreadMan stop(){
        return shutdown()
    }

    ThreadMan interrupt(){
        return shutdown()
    }

    ThreadMan shutdown(){
        logger.info "Shutdown all threads."
        threadPool.shutdownNow()
        threadMap.each{ String key, Thread thread ->
            logger.debug "- Trying to close thread (${key})"
            if (thread && !thread.isInterrupted())
                thread.interrupt()
        }
//        while (threadMap.findAll{ String key, Thread thread -> !thread.isInterrupted() }){
//        }
        logger.debug " - Checking ThreadMap  ==> ${threadMap}"
        logger.debug " - task: ${threadPool.taskCount}, active: ${threadPool.activeCount}"
        return this
    }



    boolean hasRunningThread(){
        int aliveCount = threadMap.findAll{ String key, Thread thread -> thread?.isAlive() }.size()
        logger.debug "Alive: ${aliveCount}"
        return (aliveCount > 0)
    }

}
