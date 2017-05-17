package jaemisseo.man

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 16. 7. 14
 * Time: 오전 1:52
 * To change this template use File | Settings | File Templates.
 */
class TimeMan {


    long totalTime = 0
    long recentStartTime = 0

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


}
