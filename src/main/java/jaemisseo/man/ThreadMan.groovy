package jaemisseo.man

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by sujkim on 2017-06-01.
 */
class ThreadMan{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Thread Pool **/
    int THREAD_CNT = 10
//    ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT)
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(THREAD_CNT, THREAD_CNT, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    /** Event Closure **/
    Closure onErrorClosure
    Closure onStopClosure
    Closure onFinallyClosure

    Thread thread

    ThreadMan(){
    }

    /*************************
     * STATIC
     *************************/
    static ThreadMan newThread(){
        return new ThreadMan()
    }

    static ThreadMan newThread(Closure startClosure){
        return new ThreadMan().start(startClosure)
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
        //Setup Thread
        Closure onStopClosure       = this.onStopClosure
        Closure onErrorClosure      = this.onErrorClosure
        Closure onFinallyClosure    = this.onFinallyClosure
        thread = new Thread(new Runnable(){void run(){
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
                if (onFinallyClosure)
                    onFinallyClosure()
            }
        }})
        //Pool Thread
        //Start Thread
        return pool(thread)
    }

    ThreadMan pool(Thread thread){
        threadPool.execute(thread)
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
        threadPool.shutdownNow()
        return this
    }



}
