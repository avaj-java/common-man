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
                println EventMan.getMain().isAliveEvent('test1')
                println EventMan.getMain().isAliveEvent('test2')
            }
        })
        EventMan.getMain().runAsSingeThread()

        int cnt = 0
        while(++cnt < 3){
            sleep(1000)
        }
        EventMan.getMain().stopAsSingeThread()

        cnt = 0
        while(++cnt < 3){
            sleep(1000)
        }
        EventMan.getMain().runAsSingeThread()

        while(true){
            sleep(1000)
        }
    }


}

