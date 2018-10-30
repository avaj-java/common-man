package jaemisseo.man

import jaemisseo.man.EventMan.EventStarter
import org.slf4j.LoggerFactory

class EventMan {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass())

    static EventMan eventman



    Map<String, EventStarter> eventNameEventItemMap = [:]

    EventMan eventmanAsSingleThread
    boolean modeSingleThread = false
    boolean modeASync = false






    /**************************************************
     *
     * Event Item
     *
     **************************************************/
    class EventStarter {

        EventStarter(EventHandler eventHandler) {
            this.eventHandler = eventHandler
        }

        EventHandler eventHandler
        Thread thread
        List<String> applicantList = []

        String name
        Long intervalTime = -1
        Long startTime = -1
        Long aliveTime = -1
        Long latestWorkStartDate = -1
        Long index = -1
        boolean modeUntilNoRequire = false
        boolean running = false

        void run(){
            running = true
            while (running){
                try {
                    if (modeUntilNoRequire && !checkSustainability()) {
                        setModeUntilNoRequire(false)
                        stop()
                    }
                    doEvent()
                    Thread.sleep(intervalTime)
                } catch (InterruptedException ie) {
                    running = false
                    startTime = -1
                    aliveTime = -1
                    throw ie
                }
            }
        }

        void doEvent(){
            if (eventHandler.checkDoable(this, ++index)){
                updateDate()
                eventHandler.doEvent(this, index)
            }
        }

        Long updateDate(){
            this.latestWorkStartDate = new Date().getTime()
            return this.latestWorkStartDate
        }

        void stop(String name) {
            complete(name)
        }

        void stop(){
            if (!thread){
                logger.warn "!! Stop sign to ${name}. But ${name} is not working."
                return
            }
            logger.debug "Shutdown ${name}"
            thread.interrupt()
        }

        boolean isAlive(){
            return thread && thread.isAlive()
        }

        boolean checkSustainability(){
            boolean existsApplicant = (applicantList.size() > 0)
            boolean timeOver = (startTime == -1 && aliveTime == -1) || (((startTime + aliveTime) - getNow()) < 0)
            return  (existsApplicant || !timeOver)
        }

        void setIntervalTime(Long milisecond){
            this.intervalTime = milisecond ?: 1000
        }

        synchronized EventMan apply(String applicantName){
            if (this.applicantList.contains(applicantName)){
                logger.warn "!! ${applicantName} is already ${name} Applicant!"
                return this
            }
            this.applicantList << applicantName
            logger.debug "[${this.name}] New Applicant : ${applicantName}"
            logger.debug "[${this.name}] Applicant List: ${applicantList.size()} ${applicantList.toListString()}"
            return this
        }

        synchronized EventMan complete(String name){
            if (!this.applicantList.contains(name)){
                logger.warn "!! ${name} is not ${name} Applicant!"
                return this
            }
            this.applicantList -= name
            logger.debug "[${this.name}] Left Applicant: ${name}"
            logger.debug "[${this.name}] Applicant List: ${applicantList.size()} ${applicantList.toListString()}"
            return this
        }

        void setAliveTimeFromNow(Long milisecond){
            this.startTime = getNow()
            this.aliveTime = milisecond ?: -1
            logger.debug "New Time Limit    : ${this.aliveTime} ms"
            logger.debug "Start Time        : ${new Date(this.startTime)}"
            logger.debug "End Time (maybe)  : ${new Date(this.startTime + this.aliveTime)}"
        }

        void setModeUntilNoRequire(boolean modeUntilNoRequire){
            this.modeUntilNoRequire = modeUntilNoRequire
        }

    }



    /**************************************************
     *
     * Event Handler
     *
     **************************************************/
    interface EventHandler{
        boolean checkDoable(EventStarter eventStarter, Long index)
        void doEvent(EventStarter eventStarter, Long index)
    }


    


    /**************************************************
     *
     * Event Controller
     *
     **************************************************/
    /*************************
     * Start Event
     *************************/
    EventMan startEvent(String eventName, String applicantName){
        EventStarter eventItem = eventNameEventItemMap[eventName]
        if (!eventItem){
            logger.error("No Event Item ${eventName}")
            return this
        }
        eventItem.apply(applicantName)
        if (!eventItem.isAlive()){
            eventItem.setModeUntilNoRequire(true)
            logger.debug "[${eventItem.name}] ${applicantName} applied"
        }
        return runEvent(eventItem)
    }

    EventMan startEvent(String eventName, Long aliveTime){
        EventStarter eventItem = eventNameEventItemMap[eventName]
        if (!eventItem){
            logger.error("No Event Item ${eventName}")
            return this
        }
        eventItem.setAliveTimeFromNow(aliveTime)
        if (!eventItem.isAlive()){
            eventItem.setModeUntilNoRequire(true)
            logger.debug "[${eventItem.name}] ${aliveTime}"
        }
        return runEvent(eventItem)
    }

    EventMan startEvent(String eventName) {
        EventStarter eventItem = eventNameEventItemMap[eventName]
        if (!eventItem){
            logger.error("No Event Item ${eventName}")
            return this
        }
        eventItem.setModeUntilNoRequire(false)
        logger.debug "[${eventItem.name}]] Start"
        return runEvent(eventItem)
    }

    /*************************
     * Start All Event
     *************************/
    EventMan startEventAll(){
        this.eventNameEventItemMap.each{ String eventName, EventStarter eventItem ->
            startEvent(eventName)
        }
        return this
    }

    EventMan startEventAll(String applicant){
        this.eventNameEventItemMap.each{ String eventName, EventStarter eventItem ->
            startEvent(eventName, applicant)
        }
        return this
    }

    EventMan startEventAll(Long aliveTime){
        this.eventNameEventItemMap.each{ String eventName, EventStarter eventItem ->
            startEvent(eventName, aliveTime)
        }
        return this
    }



    private synchronized EventMan runEvent(EventStarter eventItem){
        if (modeSingleThread){

        }else{
            if (eventItem.isAlive()){
                logger.warn "!! Start sign to ${eventItem.name}. But ${eventItem.name} is already working."
                return this
            }
            logger.debug "Start Event [${eventItem.name}]"
            logger.debug "- Interval: ${eventItem.intervalTime} ms"
            eventItem.thread = newThread('Stoped Watcher'){
                eventItem.run()
            }
        }
        return this
    }



    /*************************
     * Start Event as Single Thread
     *************************/
    void runAsSingeThread(){
        runAsSingeThread(100)
    }

    void runAsSingeThread(Long checkingIntervalTime){
        makeEventManAsSingeThread(checkingIntervalTime).startEvent('SingleThread')
    }

    void runAsSingeThread(Long checkingIntervalTime, Long aliveTime){
        makeEventManAsSingeThread(checkingIntervalTime).startEvent('SingleThread', aliveTime)
    }

    void runAsSingeThread(Long checkingIntervalTime, String applicant){
        makeEventManAsSingeThread(checkingIntervalTime).startEvent('SingleThread', applicant)
    }

    EventMan makeEventManAsSingeThread(Long checkingIntervalTime){
        modeSingleThread = true
        if (!eventmanAsSingleThread){
            EventMan that = this
            this.eventmanAsSingleThread = new EventMan().addEvent('SingleThread', new EventHandler(){

                Map<String, EventStarter> eventNameEventItemForSingleMap = that.eventNameEventItemMap

                @Override
                boolean checkDoable(EventStarter eventStarter, Long index) {
                    return true
                }

                @Override
                void doEvent(EventStarter eventStarter, Long index) {
                    eventNameEventItemForSingleMap.each{ String eventName, EventStarter eventItem ->
                        if (checkOverIntervalTime(eventItem)){
                            if (that.modeASync){
                                eventItem.thread = newThread('Stoped Watcher'){
                                    eventItem.doEvent()
                                }
                            }else{
                                eventItem.doEvent()
                            }
                        }
                    }
                }

                boolean checkOverIntervalTime(EventStarter eventItem){
                    long elapsedTime = new Date().getTime() - eventItem.latestWorkStartDate
                    return elapsedTime >= eventItem.intervalTime
                }

            }, checkingIntervalTime)
        }
        return this.eventmanAsSingleThread
    }



    /*************************
     * Stop Event
     *************************/
    EventMan stopEvent(String eventName, String applicant) {
        if (!hasEvent(eventName)){
            logger.error("No Event Item ${eventName}")
            return this
        }
        EventStarter eventItem = eventNameEventItemMap[eventName]
        eventItem.stop(applicant)
        return this
    }

    EventMan stopEvent(String eventName) {
        if (!hasEvent(eventName)){
            logger.error("No Event Item ${eventName}")
            return this
        }
        EventStarter eventItem = eventNameEventItemMap[eventName]
        eventItem.stop()
        return this
    }

    /*************************
     * Stop All Event
     *************************/
    EventMan stopEventAll() {
        eventNameEventItemMap.each{ String eventName, EventStarter eventItem ->
            eventItem.stop()
        }
        return this
    }

    /*************************
     * Stop Event as Single Thread
     *************************/
    void stopAsSingeThread(String applicant){
        this.eventmanAsSingleThread.stopEvent('SingleThread', applicant)
    }

    void stopAsSingeThread(){
        this.eventmanAsSingleThread.stopEvent('SingleThread')
    }



    /*************************
     * Add
     *************************/
    EventMan addEvent(String eventName, EventHandler eventHandler) {
        return addEvent(eventName, eventHandler, 1000)
    }

    synchronized EventMan addEvent(String eventName, EventHandler eventHandler, Long intervalTime){
        if (hasEvent(eventName)){
            logger.error('Already exists event name.')
            return this
        }
        EventStarter eventItem = new EventStarter(eventHandler)
        eventItem.name = eventName
        eventItem.intervalTime = intervalTime
        this.eventNameEventItemMap[eventName] = eventItem
        return this
    }


    /*************************
     * Has
     *************************/
    boolean hasEvent(String eventName){
        return eventNameEventItemMap.containsKey(eventName)
    }


    /*************************
     * Del
     *************************/
    synchronized EventMan delEvent(String eventName){
        stopEvent(eventName)
        eventNameEventItemMap.remove(eventName)
        return this
    }


    /*************************
     * Check alive event
     *************************/
    boolean isAliveEvent(String eventName){
        EventStarter eventItem = eventNameEventItemMap[eventName]
        return (eventItem && eventItem.isAlive()) || (eventmanAsSingleThread && eventItem)
    }




    /**************************************************
     *
     * Thread
     *
     **************************************************/
    static Thread newThread(Closure threadRunClosure){
        //Create Thread
        Thread thread = new Thread(new Runnable(){void run(){
            threadRunClosure()
        }})
        //Start Thread
        thread.start()
        //Return Thread
        return thread
    }

    static Thread newThread(String interruptMessage, Closure threadRunClosure){
        return newThread{
            try{
                threadRunClosure()
            }catch(InterruptedException e){
                if (interruptMessage)
                    logger.debug interruptMessage
            }finally{
                logger.debug "[Finish] Thread"
            }
        }
    }

    static Long getNow(){
        return new Date().getTime()
    }



    /**************************************************
     *
     * Static Main
     *
     **************************************************/
    static EventMan getMain() {
        if (!eventman)
            eventman = new EventMan()
        return eventman
    }

}
