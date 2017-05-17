package jaemisseo.man

import jaemisseo.man.bean.SyncTestBean
import jaemisseo.man.code.ChangeStatusCode
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/13/16
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
class CompareManTest {


    @Before
    void init(){
    }

    @After
    void after(){
    }


    Map<String, SyncTestBean> findAllOrigin(){
        Map result = [:]
        return result
    }

    Map<String, SyncTestBean> findAllTarget(){
        Map result = [:]
        return result
    }


    @Test             // 메인 프린트 테스트 육안 확인하기 - , 생성, 수정, 삭제
    void mainTest(){
        println '//////////////////////'
        println '///// TEST START /////'
        println '//////////////////////'

        // OPTION
        List matchingAttributes = []
        boolean initialUserActivate = true

        //1. Standard, Target 데이터 추출
        Map<String, SyncTestBean> standardUserMap = findAllOrigin()
        Map<String, SyncTestBean> targetUserMap = findAllTarget()

        //2. 신규시 기본값 설정
        String defaultUseYN = (initialUserActivate) ? '1' : '0'
        String defaultGroup = 'PUBLIC'

        //3. 동기화 설정
        CompareMan compareMan = new CompareMan()
                .setStandardObject(standardUserMap)
                .setTargetObject(targetUserMap)
                .setCompareField(matchingAttributes)
                .setStatusFieldName('status')
                .setStatusNone(ChangeStatusCode.NONE)
                .setStatusNew(ChangeStatusCode.NEW)
                .setStatusModified(ChangeStatusCode.MODIFIED)
                .setStatusRemoved(ChangeStatusCode.REMOVED)
                .eachNewObject{ SyncTestBean standard, SyncTestBean target ->
                    // 신규시, 사용여부와 사용자그룹은 매핑이 없더라도, 기본값을 넣는다.
                    target.useYN = target.useYN ?: defaultUseYN
                    target.userGroup = target.userGroup ?: defaultGroup
                }
                .eachModifiedObject{ SyncTestBean standard, SyncTestBean target ->
                    // 수정시, 동기화를 위해, 같은 USERID의 객체에 objectId 부여한다.
                    target.objectId = standard.objectId
                }
                .eachRemovedObject{ SyncTestBean standard ->
                    // 삭제시, 이미 사용여부(useYN)가 0 이면 삭제대상에서 제외
                    if (standard.useYN.equals('0'))
                        standard.status = ChangeStatusCode.NONE
                    else
                        standard.useYN = '0'
                }

        //4. 동기화 설정 적용
        compareMan.inputStatus()

        //5. 변화된 데이터만 추출
        List<SyncTestBean> resultList = compareMan.getChangedList()
        List<SyncTestBean> newList = resultList.findAll{ it.status == ChangeStatusCode.NEW }
        List<SyncTestBean> modList = resultList.findAll{ it.status == ChangeStatusCode.MODIFIED }
        List<SyncTestBean> delList = resultList.findAll{ it.status == ChangeStatusCode.REMOVED }
        List<SyncTestBean> nonList = resultList.findAll{ it.status == ChangeStatusCode.NONE }

        println """
        <<< 신규 ${newList.size()} >>>
        ${ newList.collect{ "${it}\n" } }                

        <<< 수정 ${modList.size()} >>>
        ${ modList.collect{ "${it}\n" } }

        <<< 삭제 ${delList.size()} >>>
        ${ delList.collect{ "${it}\n" } }        

        <<< 동일 ${nonList.size()} >>>
        ${ nonList.collect{ "${it}\n" } }
        """

        println '///////////////////////'
        println '///// TEST FINISH /////'
        println '///////////////////////'
    }






}