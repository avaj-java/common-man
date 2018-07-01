package jaemisseo.man

import org.junit.Test

class TimeManTest {



    @Test
    void tempTest(){
        String ranges = "{dayOfWeek(토,일), time(11, 12)}, {13:00..15:50, 19, 20}, 22, 23..01, 02:20..05, {13:00..15:50, 19, 20}, time(11:00,24), {3,5,6,76,34:00..34:00}, 5,6,7,"
        List rangeList = ranges.split("\\s*,(?![^(]*\\))(?![^{]*\\})\\s*").toList()
        rangeList.each{ println it }
    }

    @Test
    void tempTest2(){
        String ranges = "{dayOfWeek(토,일), time(11, 12, 18)}, {(13:00..15:50, 17, 20)}, 20, 22, 23..01, 02:20..05, {13:00..15:50, 19, 20}, time(11:00,24), dayOfWeek(2), {3,5,6,76,34:00..34:00}, 5,6,7,"

        /** OK **/
        Calendar targetDate001 = Calendar.getInstance()
        targetDate001.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        targetDate001.set(Calendar.HOUR_OF_DAY, 18)
        assert TimeMan.isInTime(ranges, targetDate001)

        Calendar targetDate002 = Calendar.getInstance()
        targetDate002.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        targetDate002.set(Calendar.HOUR_OF_DAY, 23)
        assert TimeMan.isInTime(ranges, targetDate002)

        Calendar targetDate003 = Calendar.getInstance()
        targetDate003.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        targetDate003.set(Calendar.HOUR_OF_DAY, 11)
        assert TimeMan.isInTime(ranges, targetDate003)

        Calendar targetDate004 = Calendar.getInstance()
        targetDate004.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        targetDate004.set(Calendar.HOUR_OF_DAY, 8)
        assert TimeMan.isInTime(ranges, targetDate004)

        Calendar targetDate005 = Calendar.getInstance()
        targetDate005.set(Calendar.HOUR_OF_DAY, 00)
        assert TimeMan.isInTime(ranges, targetDate005)

        Calendar targetDate006 = Calendar.getInstance()
        targetDate006.set(Calendar.HOUR_OF_DAY, 24)
        assert TimeMan.isInTime(ranges, targetDate006)

        /** Failed **/
        Calendar targetDateFailed001 = Calendar.getInstance()
        targetDateFailed001.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        targetDateFailed001.set(Calendar.HOUR_OF_DAY, 18)
        assert !TimeMan.isInTime(ranges, targetDateFailed001)

        Calendar targetDateFailed002 = Calendar.getInstance()
        targetDateFailed002.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        targetDateFailed002.set(Calendar.HOUR_OF_DAY, 19)
        assert !TimeMan.isInTime(ranges, targetDateFailed002)
    }

    @Test
    void sameMeaningTest(){
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, 'FRI', '금')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, 'FRI', '6')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, 'FRI', 'fRi')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '金曜日', 'fri')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '목요일', '5')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, 'sun', '1')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '1', '1')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '일', '일')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '일', '日')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '2', 'Lun')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '6', '金')
        assert TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '금', '금')

        assert !TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '2', '日')
        assert !TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, 'FRI', '1')
        assert !TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '2', 'Mond')
        assert !TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '2', 'Wed')
        assert !TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '3', 'Wede')
        assert !TimeMan.isSameMeaning(TimeMan.sameMeaningDataForDayOfWeekList, '3', '金')
    }

}
