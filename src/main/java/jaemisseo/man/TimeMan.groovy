package jaemisseo.man

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 16. 7. 14
 * Time: 오전 1:52
 * To change this template use File | Settings | File Templates.
 */
class TimeMan {

    private static Logger logger = LoggerFactory.getLogger(this.getClass())



    static Map<String, Closure> timeManExpressionMethod
    static List<List<String>> sameMeaningDataForDayOfWeekList = [
            ['1', '일', '일요일', 'Sun', 'Sunday', 'Dom', 'Domingo', '日', '日曜日'],
            ['2', '월', '월요일', 'Mon', 'Monday', 'Lun', 'Lunes', '月', '月曜日'],
            ['3', '화', '화요일', 'Tue', 'Tuesday', 'Mar', 'Martes', '火', '火曜日'],
            ['4', '수', '수요일', 'Wed', 'Wednesday', 'Mié', 'Miércoles', '水', '水曜日'],
            ['5', '목', '목요일', 'Thu', 'Thursday', 'Jue', 'Jueves', '木', '木曜日'],
            ['6', '금', '금요일', 'Fri', 'Friday', 'Vie', 'Viernes', '金', '金曜日'],
            ['7', '토', '토요일', 'Sat', 'Saturday', 'Sáb', 'Sábado', '土', '土曜日'],
    ]
    long totalTime = 0
    long recentStartTime = 0



    /**************************************************
     *
     * Stopwatch
     *
     **************************************************/
    TimeMan init(){
        totalTime = 0
        recentStartTime = 0
        return this
    }

    TimeMan start(){
        recentStartTime = System.currentTimeMillis()
        return this
    }

    TimeMan pause(){
        long pauseTime = System.currentTimeMillis()
        long takenTime = pauseTime - recentStartTime
        recentStartTime = 0
        totalTime += takenTime
        return this
    }

    TimeMan stop(){
        if (recentStartTime != 0) pause()
        return this
    }

    double getTime(){
        long ingTime = 0
        if (recentStartTime != 0){  // START 상태 경우 그 시점까지 더해주기
            long pauseTime = System.currentTimeMillis()
            long takenTime = pauseTime - recentStartTime
            ingTime = takenTime
        }
        return (totalTime + ingTime) / 1000
    }

    TimeMan log(){
        logger.info "- Elapsed Time: ${getTime().toString()}"
    }



    /**************************************************
     *
     * Time Range Checker
     *
     *  TimeManDateExpression을 사용한다. (기본단위는 time / time은 점점(..)으로 범위지정 가능 / 쉼표(,) 로 기본 OR조건 열거 / 중괄호({}) 안에서 쉼표(,)는 AND조건 열거 / dayOfWeek()로 특정요일 조건 / time()으로 특정시간 조건 )
     *    - 시: 0 ~ 23
     *    - 분: 0 ~ 60
     *    - 요일: (일, 월, 화, 수, 목, 금, 토) or (SUN, MON, TUE, WED, THU, FRI, SAT) or (1, 2, 3, 4 ,5 ,6, 7) or (日, 月, 火, 水, 木, 金, 土) or ...
     *    - 예(특정시간)>
     *       11, 12, 13:10..15:30, 19..20, 21, 22
     *
     *    - 예(매일일과시간 + 주말은점심도)>
     *       time(9..11, 13..18), {dayOfWeek(토,일), time(11..13)}
     *
     *    - 예(월요일은무조건 + 화수목은일과시간 + 금요일은아침만)>
     *       dayOfWeek(월), {dayOfWeek(화,수,목), time(09:00..17:00)}, {dayOfWeek(금), time(09:00..11:00)}
     *
     *    - 예(주말은무조건 + 21,22시는항상 + 월수금은아침 + 화목은오후)>
     *       dayOfWeek(토,일), 21..22, {dayOfWeek(월,수,금), time(08:00..12:00)}, {dayOfWeek(화,목), time(14:00..18:00)}
     ***************************************************/
    static boolean isInTime(String ranges){
        Calendar nowDate = Calendar.getInstance()
        return isInTime(ranges, nowDate)
    }

    static boolean isInTime(String ranges, Calendar targetDate){
        return timeManExpressionAnyMatch(ranges, targetDate)
    }

    static boolean timeManExpressionAnyMatch(String ranges, Calendar targetDate){
        List rangeList = makeArgumentListByComma(ranges)
        return !rangeList || rangeList.any{ String range ->
            if (validateTimeManExpression(range)){
                if (range.contains('{') && range.contains('}')){
                    return timeManExpressionEveryMatch(range.substring(1, range.length() -1), targetDate)
                }else{
                    return runMethod(range, targetDate)
                }
            }else{
                return false
            }
        }
    }

    static boolean timeManExpressionEveryMatch(String ranges, Calendar targetDate){
        List rangeList = makeArgumentListByComma(ranges)
        return !rangeList || rangeList.every{ String range ->
            if (validateTimeManExpression(range)){
                if (range.contains('{') && range.contains('}')){
                    return timeManExpressionEveryMatch(range.substring(1, range.length() -1), targetDate)
                }else{
                    return runMethod(range, targetDate)
                }
            }else{
                return false
            }
        }
    }

    static List<String> makeArgumentListByComma(arguments){
        arguments = arguments ?: ''
//        return arguments.split("\\s*,\\s*").toList()
        return arguments.split("\\s*,(?![^(]*\\))(?![^{]*\\})\\s*").toList()
    }

    static boolean validateTimeManExpression(String range){
        return (
                //Brace are paired ???
                range.count('{') == range.count('}')
                //Brace are paired ???
                && range.count('(') == range.count(')')
        )
    }

    static boolean runMethod(String range, Calendar targetDate){
        String trimedRange = range?.trim()
        int startBraceIndex = trimedRange.indexOf('(')
        int endBraceIndex = trimedRange.indexOf(')')
        String methodName
        String arguments
        String defaultMethodName = 'time'
        //Get Method Name
        //Get Arguments
        if (startBraceIndex != -1){
            methodName = trimedRange?.substring(0, startBraceIndex) ?: defaultMethodName
            arguments = range.substring(startBraceIndex +1, endBraceIndex)
        }else{
            methodName = defaultMethodName
            arguments = trimedRange
        }
        //Run
        return runMethod(methodName, targetDate, makeArgumentListByComma(arguments))
    }

    static boolean runMethod(String name, Object data, List argumentList){
        Closure closure = makeTimeManExpressionMethodClosureMap()[name]
        if (closure){
            return closure(data, argumentList)
        }else{
            logger.error("There is no method [${name}]")
            return false
        }
    }

    static Map<String, Closure> makeTimeManExpressionMethodClosureMap(){
        if (!timeManExpressionMethod){
            timeManExpressionMethod = [
                    time: { Object data, List<String> timeRangeArgumentList ->
                        //Get Now
                        Calendar targetDate = (Calendar) data
                        String timeExpression = "${targetDate.get(Calendar.HOUR_OF_DAY)}:${targetDate.get(Calendar.MINUTE)}:${targetDate.get(Calendar.SECOND)}.${targetDate.get(Calendar.MILLISECOND)} "
                        Calendar compareDate = generateCalendarTimeOnToday(timeExpression)
                        //Check Now is in range
                        timeRangeArgumentList.any{ range ->
                            if (range.contains("..")){
                                List timeList = range.split("\\.\\.").toList()
                                Calendar fromDate = generateCalendarTimeOnToday(timeList[0])
                                Calendar toDate = generateCalendarTimeOnToday(timeList[1])
                                if (fromDate.before(toDate)){
                                    //Example) 06:00..11:00
                                    return compareDate.after(fromDate) && compareDate.before(toDate)
                                }else{
                                    //Example) 08:00..03:00
                                    return compareDate.after(fromDate) || compareDate.before(toDate)
                                }
                            }else{
                                Calendar specificDate = generateCalendarTimeOnToday(range)
                                return specificDate.get(Calendar.HOUR_OF_DAY) == targetDate.get(Calendar.HOUR_OF_DAY)
                            }
                        }
                    },
                    dayOfWeek: { Object data, List<String> dayOfWeekArgumentList ->
                        //Get Now
                        Calendar date = (Calendar) data
                        int nowDayOfWeek = date.get(Calendar.DAY_OF_WEEK)
                        //Check Now is in range
                        return dayOfWeekArgumentList.any{ String dayOfWeek ->
                            isSameMeaning(sameMeaningDataForDayOfWeekList, String.valueOf(nowDayOfWeek), dayOfWeek)
                        }
                    }
            ]
        }
        return timeManExpressionMethod
    }

    static Calendar generateCalendarTimeOnToday(String hourMinuteString){
        int hour = 0
        int minute = 0
        int second = 0
        int millisecond = 0
        int colonCount = hourMinuteString.count(":")
        if (colonCount == 1) {
            String[] hourMinuteArray = hourMinuteString.split("[:]")
            hour = Integer.parseInt hourMinuteArray[0].trim()
            minute = Integer.parseInt hourMinuteArray[1].trim()
        }else if (colonCount == 2){
            String[] hourMinuteArray = hourMinuteString.split("[:]")
            hour = Integer.parseInt hourMinuteArray[0].trim()
            minute = Integer.parseInt hourMinuteArray[1].trim()
            if (hourMinuteArray[2].contains('.')){
                String[] secondMillisecondArray = hourMinuteArray[2].split('\\.')
                second = Integer.parseInt secondMillisecondArray[0].trim()
                millisecond = Integer.parseInt secondMillisecondArray[1].trim()
            }else{
                second = Integer.parseInt hourMinuteArray[2].trim()
            }
        }else{
            hour = Integer.parseInt hourMinuteString
        }
        Calendar date = Calendar.getInstance()
        date.set( Calendar.HOUR_OF_DAY, hour );
        date.set( Calendar.MINUTE, minute );
        date.set( Calendar.SECOND, second );
        date.set( Calendar.MILLISECOND, millisecond );
        return date
    }


    static boolean isSameMeaning(Object dataListCollection, String a, String b){
        a = a.toLowerCase()
        b = b.toLowerCase()
        if (dataListCollection instanceof List){
            return dataListCollection.any{ List<String> sameMeaningList ->
                sameMeaningList.find{ it.toLowerCase().equals(a) } && sameMeaningList.find{ it.toLowerCase().equals(b) }
            }

        }else if (dataListCollection instanceof Map){
            return dataListCollection.any{ String key, List<String> sameMeaningList ->
                sameMeaningList.find{ it.toLowerCase().equals(a) } && sameMeaningList.find{ it.toLowerCase().equals(b) }
            }
        }
        return false
    }

}
