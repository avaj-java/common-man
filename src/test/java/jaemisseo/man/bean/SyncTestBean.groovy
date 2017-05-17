package jaemisseo.man.bean

import jaemisseo.man.annotation.QueryColumn
import jaemisseo.man.annotation.QueryTable
import jaemisseo.man.code.ChangeStatusCode

@QueryTable("META_OBJECT")
class SyncTestBean {

    @QueryColumn("CLASS_ID")
    String classId = 100

    @QueryColumn("OBJECT_ID")
    String objectId         // 메타의 object_id





    @QueryColumn("OBJECT_NAME")
    String userId           // 사번/아이디   (A02856)

    @QueryColumn("ABBR_NAME")
    String userName         // 이름   (A00822)

    @QueryColumn("OBJ_NO2")
    String wrongPwCnt       // 비밀번호 틀린 횟수

    @QueryColumn("OBJ_IND6")
    String wannaReceiveEmail// 이메일수신여부 Y:수신 N:미수신   (email.receive.yn)

    @QueryColumn("OBJ_IND1")
    String isAdmin          // 관리자여부 Y:관리자      (A00311)

    @QueryColumn("OBJ_IND3")
    String useYN            // 사용여부  1:사용, 0:사용안함        (A00720)

    @QueryColumn("OBJ_IND4")
    String system           // 시스템구분 Y:멀티, N:단일

    @QueryColumn("OBJ_IND9")
    String isExternal       // 외부 사용자 동기화 데잍여부  ex:외부사용자

    @QueryColumn("OBJ_RMK3")
    String dept             // 소속/부서   (A00828)

    @QueryColumn("OBJ_RMK4")
    String phone            // 전화번호      (A01022)

    @QueryColumn("OBJ_RMK5")
    String userGroup        // 사용자그룹    (A00733)

    @QueryColumn("OBJ_RMK6")
    String email            // 이메일      (A01158)

    @QueryColumn("OBJ_RMK7")
    String password         // 패스워드     (password)

    @QueryColumn("OBJ_RMK8")
    String expireDt         // 만료일자     (A00512)

    @QueryColumn("OBJ_RMK9")
    String damdangName      //담당자

    @QueryColumn("OBJ_RMK10")
    String damdangPhone     //담당자 연락처





    @QueryColumn("CUSR")
    String cusr

    @QueryColumn("MUSR")
    String musr

    @QueryColumn("CREATE_DT")
    String createDt

    @QueryColumn("MODIFY_DT")
    String modifyDt



    /*** 동기화시 필요한 정보 ***/
    ChangeStatusCode status


}