package jaemisseo.man

import org.junit.Ignore
import org.junit.Test


class EventManTest {

    @Test
    @Ignore
    void mainTest(){
        EventMan.getMain().addEvent('test1', new EventMan.EventHandler(){
            boolean checkDoable(EventMan.EventStarter eventStarter, Long index) {
                return true
            }

            void doEvent(EventMan.EventStarter eventStarter, Long index) {
                println "hi"
            }
        })
        EventMan.getMain().runAsSingeThread()
        println "Start"
        sleep(5000)


        int cnt = 0
        while(++cnt < 30){
            sleep(100)
            assert EventMan.getMain().isAliveEvent('test1')
            assert !EventMan.getMain().isAliveEvent('test2')
        }
        EventMan.getMain().stopAsSingeThread()
        println "Stop"
        sleep(5000)

        cnt = 0
        while(++cnt < 30){
            assert !EventMan.getMain().isAliveEvent('test1')
            assert !EventMan.getMain().isAliveEvent('test2')
            sleep(100)
        }
        EventMan.getMain().runAsSingeThread()
        println "Start"
        sleep(5000)

        cnt = 0
        while(++cnt <30){
            assert EventMan.getMain().isAliveEvent('test1')
            assert !EventMan.getMain().isAliveEvent('test2')
            sleep(100)
        }
    }


}

