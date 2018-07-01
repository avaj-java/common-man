package jaemisseo.man

import org.slf4j.LoggerFactory

class MemoryMan {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass())

    static MemoryMan memoryman = new MemoryMan()

    class MemoryItem{
        long availableProcessors
        long freeMem
        long maxMem
        long totalMem
        long usageMem
        Integer rate

        Integer highestRate
        Long highestMemory

        String progressBarText
        String memoryText
        String logText

        String toString(){ return "$usageMem / $maxMem ($rate)" }
    }

    MemoryItem highestMemoryItem = getInitMemoryItem()
    Map<String, MemoryItem> applicantNameAndHighestMemoryItemMap = [:]



    Thread thread
    Long intervalTime = 1000
    Long startTime = -1
    Long aliveTime = -1
    List<String> applicantList = []
    int progressBarSize = 50
    Integer warnRate = 80
    Integer errorRate

    MemoryItem beforeMemoryItem = new MemoryItem()

    Closure afterCheckMemoryClosure

    boolean modeUntilNoRequire = false
    boolean modeSaveFile = false
    boolean modeDebugEnabled = true
    boolean modePrint = true




    /**************************************************
     *
     * Save Memory
     *
     **************************************************/
    MemoryItem makeMemoryItemNow(){
        return makeMemoryItemNow(20)
    }

    MemoryItem makeMemoryItemNow(int progressBarSize){
        MemoryItem o = new MemoryItem()
        o.availableProcessors = getAvailableProcessors()
        o.freeMem = getFreeMemory()
        o.maxMem = getMaximumMemory()
        o.totalMem = getTotalMemory()
        o.usageMem = o.totalMem - o.freeMem
        o.rate = ((o.usageMem / o.totalMem) * 100)
        //Improvement
        String barAnsiColor = (errorRate && o.rate > errorRate) ? AnsiMan.ANSI_RED_BACKGROUND : (warnRate && o.rate > warnRate) ? AnsiMan.ANSI_YELLOW_BACKGROUND : AnsiMan.ANSI_BLACK_BACKGROUND
        o.progressBarText = (progressBarSize > 0) ? generateProgressBarText(o.usageMem, o.totalMem, progressBarSize) : ''
        o.memoryText = "usage: ${getNumberMegaLevel(o.usageMem, 1)}MB | max: ${getNumberMegaLevel(o.maxMem, 1)}MB (Cores: ?/${availableProcessors})"
        o.logText = "${paintGageText(o.progressBarText, '>', barAnsiColor)} ${AnsiMan.testCyan(o.memoryText)}"
        return o
    }

    /**************************************************
     *
     * Get Memory
     *
     **************************************************/
    MemoryItem getHighestMemoryItem(){
        return highestMemoryItem
    }

    MemoryItem getHighestMemoryItem(String name){
        return applicantNameAndHighestMemoryItemMap[name]
    }

    MemoryItem getInitMemoryItem(){
        return new MemoryItem(usageMem:0)
    }

    /**************************************************
     *
     * Print Memory
     *
     **************************************************/
    MemoryMan printMemory(MemoryItem memoryItem){
        return printMemory('', memoryItem)
    }

    MemoryMan printMemory(String title, MemoryItem memoryItem) {
        String log = "${title} ${memoryItem.logText}"
        if (errorRate && memoryItem.rate > errorRate){
            logger.error log
        }else if (warnRate && memoryItem.rate > warnRate){
            logger.warn log
        }else if (logger.isDebugEnabled() || logger.isTraceEnabled()){
            logger.debug log
        }else{
            logger.info log
        }
        return this
    }

    static double getNumberMegaLevel(Long number, Integer decimalPointLimit){
        Long asist = 1
        double computedNumber = number / 1024 / 1024
        if (decimalPointLimit == null){

        }else if (decimalPointLimit == 0){
            computedNumber = Math.floor(computedNumber)

        }else if (decimalPointLimit > 0){
            (1..decimalPointLimit).each{ asist *= 10 }
            computedNumber = Math.floor(computedNumber * (double)asist) / (double)asist
        }

        return computedNumber
    }

    static String toNumberWithComma(def number){
        return String.format("%,d", number);
    }

    static String paintGageText(String text, String gageChar, String ansiColor){
        String result = ''
        int startGageIndex = text.indexOf(gageChar)
        int findIndex = startGageIndex
        if (findIndex != -1){
            while (text.substring(++findIndex, findIndex + 1) == gageChar){}
            int endGageIndex = findIndex
            String gageText = text.substring(startGageIndex, endGageIndex)
            result = text.replace(gageText, ansiColor + gageText + AnsiMan.ANSI_RESET)
        }
        return result
    }

    static int getAvailableProcessors(){
        return Runtime.getRuntime().availableProcessors()
    }

    static long getFreeMemory(){
        return Runtime.getRuntime().freeMemory()
    }

    static long getMaximumMemory(){
        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        return maxMemory == Long.MAX_VALUE ? -1 : maxMemory
    }

    static long getTotalMemory(){
        return Runtime.getRuntime().totalMemory()
    }



    /**************************************************
     *
     * Print FileSystemSpace
     *
     **************************************************/
    static String printFileSystemSpace(){
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        /* For each filesystem root, print some info */
        for (File root : roots) {
            println("File system root: " + root.getAbsolutePath());
            println("Total space (bytes): " + root.getTotalSpace());
            println("Free space (bytes): " + root.getFreeSpace());
            println("Usable space (bytes): " + root.getUsableSpace());
        }
        return ""
    }



    /**************************************************
     *
     * Static Main Watcher
     *
     **************************************************/
    static MemoryMan getMain() {
        return memoryman
    }




    /**************************************************
     *
     * Watcher Controller
     *
     **************************************************/
    /*****
     * Start
     *****/
    MemoryMan startWatcher(String applicantName){
        return startWatcher(applicantName, null)
    }

    MemoryMan startWatcher(String applicantName, Closure afterCheckMemoryClosure){
        setAfterCheckMemoryClosure(afterCheckMemoryClosure)
        apply(applicantName)
        if (!isAlive()){
            setModeUntilNoRequire(true)
            logger.debug "[Memory Watcher] Monitoring by ${applicantName}"
        }
        return runWatcher()
    }

    MemoryMan startWatcher(Long aliveTime){
        return startWatcher(aliveTime, null)
    }

    MemoryMan startWatcher(Long aliveTime, Closure afterCheckMemoryClosure){
        setAfterCheckMemoryClosure(afterCheckMemoryClosure)
        setAliveTimeFromNow(aliveTime)
        if (!isAlive()){
            setModeUntilNoRequire(true)
            logger.debug "[Memory Watcher] Monitoring in ${aliveTime}"
        }
        return runWatcher()
    }

    MemoryMan startWatcher() {
        setModeUntilNoRequire(false)
        logger.debug "[Memory Watcher] Continuous monitoring"
        return runWatcher()
    }

    MemoryMan startWatcher(Closure afterCheckMemoryClosure){
        setAfterCheckMemoryClosure(afterCheckMemoryClosure)
        setModeUntilNoRequire(false)
        logger.debug "[Memory Watcher] Continuous monitoring"
        return runWatcher()
    }

    synchronized MemoryMan runWatcher(){
        MemoryMan that = this
        if (that.isAlive()){
            logger.warn "!! Start sign to Memory Watcher. But Memory Watcher is already working."
            return this
        }
        logger.debug "Start Memory Watcher"
        if (!that.isWatcherEnabled()){
            logger.warn "!! Start sign to Memory Watcher. But (modeDeubgEnalbed:${that.modeDebugEnabled}, LoggerLevel: ${logger.isDebugEnabled()}  "
            return this
        }
        logger.debug "- Interval: ${intervalTime} ms"
        thread = newThread('Stoped Watcher'){
            try{
                int index = 0
                while(++index > 0){
                    //- Get now memory
                    MemoryItem nowMemoryItem = that.makeMemoryItemNow(that.progressBarSize)
                    //- Save highest memory
                    highestMemoryItem = (highestMemoryItem.rate < nowMemoryItem.rate) ? nowMemoryItem : highestMemoryItem
                    //- Save highest memory by applicant
                    applicantNameAndHighestMemoryItemMap.each{ String applicantName, MemoryItem applicantHighestMemoryItem ->
                        applicantNameAndHighestMemoryItemMap[applicantName] = (applicantHighestMemoryItem.rate < nowMemoryItem.rate) ? nowMemoryItem : applicantHighestMemoryItem
                    }
                    //- Save before memory
                    that.beforeMemoryItem = nowMemoryItem
                    //- Do watch
                    if (that.isWatcherEnabled()){
                        if (that.modePrint)
                            that.printMemory("[${index}]", nowMemoryItem)
                        if (that.afterCheckMemoryClosure)
                            that.afterCheckMemoryClosure(index, nowMemoryItem)
                        if (that.modeUntilNoRequire && !that.checkSustainability()){
                            that.setModeUntilNoRequire(false)
                            that.stopWatcher()
                        }
                    }
                    Thread.sleep(that.intervalTime)
                }

            }catch(InterruptedException ie){
                startTime = -1
                aliveTime = -1
                throw ie
            }
        }
        return this
    }

    /*****
     * Clear
     *****/
    MemoryMan clearHighestMemoryRecord() {
//        this.highestMemoryItem = getInitMemoryItem()
        this.highestMemoryItem = this.beforeMemoryItem
        return this
    }

    MemoryMan clearHighestMemoryRecord(String name) {
        if (this.applicantNameAndHighestMemoryItemMap[name])
//            this.applicantNameAndHighestMemoryItemMap[name] = getInitMemoryItem()
            this.applicantNameAndHighestMemoryItemMap[name] = this.beforeMemoryItem

        return this
    }

    /*****
     * Stop
     *****/
    MemoryMan stopWatcher(String name) {
        return complete(name)
    }

    MemoryMan stopWatcher(){
        if (!thread){
            logger.warn "!! Stop sign to Memory Watcher. But Memory Watcher is not working."
            return
        }
        logger.debug "Shutdown Memory Watcher"
        thread.interrupt()
        return this
    }



    boolean isOverThan(Integer megaByte){
        return makeMemoryItemNow().usageMem > megaByte
    }

    boolean isOverThanRate(Integer rate){
        return makeMemoryItemNow().rate > rate
    }

    boolean isAlive(){
        return thread && thread.isAlive()
    }

    boolean isWatcherEnabled(){
        return !modeDebugEnabled || (modeDebugEnabled && logger.isDebugEnabled())
    }

    boolean checkSustainability(){
        boolean existsApplicant = (applicantList.size() > 0)
        boolean timeOver = (startTime == -1 && aliveTime == -1) || (((startTime + aliveTime) - getNow()) < 0)
        return  (existsApplicant || !timeOver)
    }

    MemoryMan setIntervalTime(Long milisecond){
        this.intervalTime = milisecond ?: 1000
        return this
    }

    MemoryMan setWarnRate(Integer warnRate){
        this.warnRate = warnRate
        return this
    }

    MemoryMan setErrorRate(Integer errorRate){
        this.errorRate = errorRate
        return this
    }

    synchronized MemoryMan apply(String name){
        if (this.applicantList.contains(name)){
            logger.warn "!! ${name} is already Memory Watcher Applicant!"
            return this
        }
        this.applicantList << name
        this.applicantNameAndHighestMemoryItemMap[name] = new MemoryItem(usageMem:0)
        logger.debug "New Memory Watcher Applicant: ${name}"
        logger.debug "Memory Watcher Applicant: ${applicantList.size()} ${applicantList.toListString()}"
        return this
    }

    synchronized MemoryMan complete(String name){
        if (!this.applicantList.contains(name)){
            logger.warn "!! ${name} is not Memory Watcher Applicant!"
            return this
        }
        this.applicantList -= name
        this.applicantNameAndHighestMemoryItemMap.remove(name)
        logger.debug "Left Memory Watcher Applicant: ${name}"
        logger.debug "Memory Watcher Applicant: ${applicantList.size()} ${applicantList.toListString()}"
        return this
    }

    MemoryMan setAliveTimeFromNow(Long milisecond){
        this.startTime = getNow()
        this.aliveTime = milisecond ?: -1
        logger.debug "New Time Limit    : ${this.aliveTime} ms"
        logger.debug "Start Time        : ${new Date(this.startTime)}"
        logger.debug "End Time (maybe)  : ${new Date(this.startTime + this.aliveTime)}"
        return this
    }

    MemoryMan setModeUntilNoRequire(boolean modeUntilNoRequire){
        this.modeUntilNoRequire = modeUntilNoRequire
        return this
    }

    MemoryMan setModeDebugEnabled(boolean modeDebugEnabled){
        this.modeDebugEnabled = modeDebugEnabled
        return this
    }

    MemoryMan setModePrint(boolean modePrint){
        this.modePrint = modePrint
        return this
    }

    MemoryMan setAfterCheckMemoryClosure(Closure afterCheckMemoryClosure){
        this.afterCheckMemoryClosure = afterCheckMemoryClosure
        return this
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



    /*************************
     * Re-print PROGRESS BAR
     *************************/
    static boolean withProgressBar(long currentCount, long totalSize, int barSize){
        if (currentCount > 1000000 || totalSize > 1000000){
            currentCount = currentCount / 1000000
            totalSize = totalSize / 1000000
        }
        return withProgressBar(currentCount.toInteger(), totalSize.toInteger(), barSize)
    }

    static boolean withProgressBar(int currentCount, int totalSize, int barSize){
        return withProgressBar(currentCount, totalSize, barSize, null)
    }

    static boolean withProgressBar(int currentCount, int totalSize, int barSize, Closure progressClosure){
        return withTimeProgressBar(currentCount, totalSize, barSize, 0, progressClosure)
    }

    static boolean withTimeProgressBar(int currentCount, int totalSize, int barSize, long startTime){
        return withTimeProgressBar(currentCount, totalSize, barSize, startTime, null)
    }

    static boolean withTimeProgressBar(int currentCount, int totalSize, int barSize, long startTime, Closure progressClosure){
        //Clear
        clearProgressBar(barSize)
        //print
        boolean result = (progressClosure) ? progressClosure() : true
        //Print
        printProgressBar(currentCount, totalSize, barSize, startTime)
        //Delay
        Thread.sleep(1)
        return result
    }

    static void printProgressBar(int currentCount, int totalSize, int barSize){
        printProgressBar(currentCount, totalSize, barSize, 0)
    }

    static void printProgressBar(int currentCount, int totalSize, int barSize, long startTime){
        print '\r' + generateProgressBarText(currentCount, totalSize, barSize, startTime)
    }

    static String generateProgressBarText(long currentCount, long totalSize, int barSize){
        return generateProgressBarText(currentCount, totalSize, barSize, 0)
    }

    static String generateProgressBarText(long currentCount, long totalSize, int barSize, long startTime){
        String progressBarText = ''
        //Calculate
        int curCntInBar = (currentCount / totalSize) * barSize
        int curPercent = (currentCount / totalSize) * 100

        //Print Start
        progressBarText += '['
        //Print Progress
        if (curCntInBar > 0 )
            progressBarText += ((1..curCntInBar).collect{ '>' }.join('') as String)
        //Print Remain
        if ( (barSize - curCntInBar) > 0 )
            progressBarText += ((curCntInBar..barSize-1).collect{' '}.join('') as String)

        //Print Last
        // Progressing...
        progressBarText += "] ${curPercent}%"

        //Print Time
        if (startTime){
            long endTime = new Date().getTime()
            Integer elapseTime = (endTime - startTime) / 1000
            progressBarText += " ${elapseTime}s"
        }

        //Print Finish
//        if (curCntInBar >= barSize)
//            progressBarText += ' DONE   \n'
        return progressBarText
    }

    static void clearProgressBar(int barSize){
        //Delete
        print "\r ${(1..barSize).collect{' '}.join('') as String}"
        //Init
        print "\r"
    }


}