package jaemisseo.man

import jaemisseo.man.util.Util
import org.junit.Ignore
import org.junit.Test

/**
 * Created by sujkim on 2017-06-01.
 */
class ThreadManTest {

//    static void main(String[] args) {
//        new ThreadManTest().thread_stop()
//        new ThreadManTest().thread_multi()
//    }


    @Test
    @Ignore
    void thread_stop(){
        //Simple Thread
        ThreadMan t1 = ThreadMan.newThread{
            progress('[Thread1]')
        }

        //+onStop Event
        ThreadMan t2 = ThreadMan.newThread().onStop{
            println "[Thread2] STOP"
        }.onFinally{
            println "[Thread2] Finally"
        }.start{
            progress('[Thread2]')
        }

        //+onError Event
        ThreadMan t3 = ThreadMan.newThread().onError{
            println "[Thread3] Error"
        }.onFinally{
            println "[Thread3] Finally"
        }.start{
            throw new Exception('Error HaHaHa(Thread3)')
            progress('[Thread3]')
        }

        //+Error
        ThreadMan t4 = ThreadMan.newThread{
            throw new Exception('Error HaHaHa(Thread4)')
            progress('[Thread4]')
        }

        Thread.sleep(1000)
        t1.stop()
        t2.stop()
        t3.stop()
        t4.stop()
    }

    @Test
    @Ignore
    void thread_multi(){
        //Simple Thread
        ThreadMan t1 = ThreadMan.newThread{
            progress('[Oneka]')
        }.addThread {
            progress('[Oneka-multi-1]')
        }.addThread {
            progress('[Oneka-multi-2]')
        }.addThread {
            progress('[Oneka-multi-3]')
        }

        //+onStop Event
        ThreadMan t2 = ThreadMan.newThread().onStop{
            println "[Tuubi] STOP"
        }.onFinally{
            println "[Tuubi] Finally"
        }.start{
            progress('[Tuubi-multi-1]')
        }.addThread{
            progress('[Tuubi-multi-2]')
        }
//
        //+onError Event
        ThreadMan t3 = ThreadMan.newThread().onError{
            println "[Thread3] Error"
        }.onFinally{
            println "[Thread3] Finally"
        }.start{
            throw new Exception('Error HaHaHa(Thread3)')
            progress('[Thread3]')
        }.addThread{
            progress('[Thread3-multi-1]')
        }.addThread(false){
            throw new Exception('Error HaHaHa(Thread3-multi-2)')
            progress('[Thread3-multi-2]')
        }

        Thread.sleep(1500)
        t1.stop()
        t2.stop()
        Thread.sleep(1500)
        t3.stop()
    }



    void progress(String name){
        int total = 20
        int barSize = 20
        //Loop - method1
        println "Start Just "
        (0..total).each{ int idx ->
            Thread.sleep(180)
            Util.withProgressBar(idx, total, barSize){
                println "$name - $idx"
            }
        }
    }

}
