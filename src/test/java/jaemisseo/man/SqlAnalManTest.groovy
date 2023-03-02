package jaemisseo.man

import jaemisseo.man.bean.SqlSetup
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by sujkim on 2017-06-08.
 *
 *      - It needs to set "-Xss50m"
 *
 *      - Ex)
 *          java -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:56208,suspend=y,server=n -Xss50m -Didea.test.cyclic.buffer.size=1048576 -javaagent:C:\Users\soulj\AppData\Local\JetBrains\IntelliJIdea2022.2\groovyHotSwap\gragent.jar -javaagent:C:\Users\soulj\AppData\Local\JetBrains\IntelliJIdea2022.2\captureAgent\debugger-agent.jar -Dfile.encoding=UTF-8 -classpath "C:\Program Files\JetBrains\IntelliJ IDEA 2020.3.3\lib\idea_rt.jar;C:\Program Files\JetBrains\IntelliJ IDEA 2020.3.3\plugins\junit\lib\junit5-rt.jar;C:\Program Files\JetBrains\IntelliJ IDEA 2020.3.3\plugins\junit\lib\junit-rt.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\access-bridge-64.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\cldrdata.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\jaccess.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\jfxrt.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\nashorn.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\sunpkcs11.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\ext\zipfs.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\jce.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\jfr.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\jfxswt.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\resources.jar;C:\Program Files\Java\jdk1.8.0_211\jre\lib\rt.jar;D:\dev_by_sj\workspaces_for_souljungkim\@avaj-java\installer-maker\job\hoya\hoya-core\common-man\build\classes\java\test;D:\dev_by_sj\workspaces_for_souljungkim\@avaj-java\installer-maker\job\hoya\hoya-core\common-man\build\classes\groovy\test;D:\dev_by_sj\workspaces_for_souljungkim\@avaj-java\installer-maker\job\hoya\hoya-core\common-man\build\classes\groovy\main;D:\dev_by_sj\workspaces_for_souljungkim\@avaj-java\installer-maker\job\hoya\hoya-core\common-man\external_lib_dev\mysql-connector-java-5.1.35.jar;D:\dev_by_sj\workspaces_for_souljungkim\@avaj-java\installer-maker\job\hoya\hoya-core\common-util\build\classes\groovy\main;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\ch.qos.logback\logback-classic\1.2.3\7c4f3c474fb2c041d8028740440937705ebb473a\logback-classic-1.2.3.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.slf4j\slf4j-api\1.7.25\da76ca59f6a57ee3102f8f9bd9cee742973efa8a\slf4j-api-1.7.25.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\junit\junit\4.11\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\junit-4.11.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\ch.qos.logback\logback-core\1.2.3\864344400c3d4d92dfeb0a305dc87d953677c03c\logback-core-1.2.3.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.codehaus.groovy\groovy-sql\2.1.3\9b9ff904a7f63c12fc6838ed1e583ad1ed4622ed\groovy-sql-2.1.3.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.codehaus.groovy\groovy-json\2.1.3\844d1caf87fd6871b736e1f481d1136720bb1e4d\groovy-json-2.1.3.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.codehaus.groovy\groovy\2.1.3\534f38c53d5c887bf3d73c06b80265b7d8ce2f80\groovy-2.1.3.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.hamcrest\hamcrest-core\1.3\42a25dc3219429f0e5d060061f71acb49bf010a0\hamcrest-core-1.3.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\antlr\antlr\2.7.7\83cd2cd674a217ade95a4bb83a8a14f351f48bd0\antlr-2.7.7.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-commons\4.0\a839ec6737d2b5ba7d1878e1a596b8f58aa545d9\asm-commons-4.0.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-util\4.0\d7a65f54cda284f9706a750c23d64830bb740c39\asm-util-4.0.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-analysis\4.0\1c45d52b6f6c638db13cf3ac12adeb56b254cdd7\asm-analysis-4.0.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-tree\4.0\67bd266cd17adcee486b76952ece4cc85fe248b8\asm-tree-4.0.jar;C:\Users\soulj\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm\4.0\659add6efc75a4715d738e73f07505246edf4d66\asm-4.0.jar" com.intellij.rt.junit.JUnitStarter -ideVersion5 -junit4 jaemisseo.man.SqlAnalManTest
 *
 */
class SqlAnalManTest {

    @Before
    void init(){
    }

    @After
    void after(){
    }

    static void main(String[] args) {
    }


    String queryCreateJava = '''CREATE OR REPLACE AND RESOLVE JAVA SOURCE NAMED "GenerateUUID" as import java.sql.*;
                        import java.io.*;
                        import java.util.UUID;
                        
                        public class GenerateUUID {
                            public static String getId(){
                                return UUID.randomUUID().toString();
                            }
                        }
                        /
        '''

    String queryCreateFunction = '''CREATE OR REPLACE FUNCTION META3.SF_CHK_MY_OBJECT_CO
            (
                iv_class_id         varchar2
               ,iv_object_id        varchar2
               ,iv_usr_object_id    varchar2
               ,iv_oi_id            varchar2
            )
                RETURN varchar2 IS ov_return varchar2(10);
                v_obj_seq       MOBJ_INV.OBJ_SEQ%TYPE;
                v_obj_ind2      MOBJ_INV.OBJ_IND2%TYPE;
                v_obj_ind4      MOBJ_INV.OBJ_IND4%TYPE;
                v_object_id2    MOBJ_INV.OBJECT_ID2%TYPE;
                v_object_id4    MOBJ_INV.OBJECT_ID4%TYPE;
                
                rv_obj_ind2     MOBJ_INV.OBJ_IND2%TYPE;
                rv_my_obj_ind2  MOBJ_INV.OBJ_IND2%TYPE;
                rv_seq_chek     MOBJ_INV.OBJ_IND2%TYPE;
            
                CURSOR  c_215500 (p_class_id varchar2, p_object_id varchar2, p_oi_id varchar2) Is
                    SELECT   MI.OBJ_SEQ        --결재순서
                            ,MI.OBJ_IND2       --결재상태 1:결재신청 , 2:결재승인, 3:결재반려
                            ,MI.OBJ_IND4       --결재방식 1:순차, 2:합의
                            ,MI.OBJECT_ID2     --결재자
                            ,(SELECT OBJECT_ID FROM META_OBJECT WHERE CLASS_ID = 101 AND ABBR_NAME = (SELECT OBJ_RMK5 FROM META_OBJECT WHERE OBJECT_ID = iv_usr_object_id)) ALTER_ID
                    FROM    MOBJ_INV MI
                    WHERE   MI.CLASS_ID     = iv_class_id
                    AND     MI.OBJECT_ID3   = iv_object_id
                    AND     MI.OBJECT_ID4   = iv_oi_id
                    ORDER   BY MI.OBJ_SEQ; 
                    
            BEGIN
                
                ov_return       := 'N';
                rv_obj_ind2     := 'N';
                rv_my_obj_ind2  := '1';
                rv_seq_chek     := '0';
                    
                IF  iv_class_id = 215500 THEN
                    
                    FOR c1 IN c_215500(iv_class_id, iv_object_id, iv_oi_id) LOOP
                        
                    IF c1.obj_ind4 = '1' AND c1.obj_ind2 = '1' THEN -- 순차인경우 결제가 진행 되었는지를 파악한다. 어차피 order by 기준으로 seq 되어 있기 때문에 위에서 부터 순차 체크한다.
                    rv_seq_chek := '1';
                    END IF;
            
                        IF  iv_usr_object_id = c1.object_id2 OR c1.alter_id = c1.object_id2 THEN
                            
                            IF  c1.obj_ind4 = '2' AND rv_seq_chek = '0' THEN --  합의일 경우에는 무조건 'Y\'
                                rv_my_obj_ind2  :=  c1.obj_ind2;
                                rv_obj_ind2     :=  '2';
                            ELSIF c1.obj_ind4 = '1'  THEN
                                rv_my_obj_ind2  :=   c1.obj_ind2;
                            END IF;
                            
                            EXIT;
                            
                        ELSE
                            IF  c1.obj_ind4 = '1'  THEN
                                rv_obj_ind2 :=  c1.obj_ind2;   
                            END IF;                 
                        END IF;
                        
                    END LOOP;
                            
                END IF;
                
                IF  rv_my_obj_ind2 = '1' AND rv_obj_ind2 <> '1' THEN
                    ov_return :=    rv_my_obj_ind2 || 'Y';
                ELSE
                    ov_return :=    rv_my_obj_ind2 || 'N';                
                END IF;
                
                RETURN ov_return;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                   RETURN   '1N';
                    
            END SF_CHK_MY_OBJECT_CO;
            /
    '''

    String queryCreateFunction2 = '''CREATE OR REPLACE FUNCTION GET_UU_ID
           RETURN VARCHAR2
        AS
           LANGUAGE JAVA
           NAME 'GenerateUUID.getId() return java.lang.String';
        /
    '''

    String queryGrant = '''GRANT DBA TO TEST_META_171103_00162 WITH ADMIN OPTION ; ALTER USER META3 DEFAULT ROLE ALL ;
    '''




    @Test //TODO: Values 의 요소들이 개행되면 인식이 안된다. (예: CLOB)
    @Ignore
    void matchingTest_clob_but_cannot(){
        String query = '''
INSERT    INTO  SOME_TABLE (   a,   b,   c   ) VALUES (    ' insert into values (1,2,3)',1,2.3,'--;a,'     );

-- insert into fff_Table (asdf) values ('asdf');
INSERT INTO SOME_TABLE (ee,bb,cc) VALUES (aa,234,'a  sj',2.3);
-- insert into sss_Table(asdf) values('asdf')
INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('c2c41376-ba13-494b-8588-a4b65a9701e7', 'ko', 'is_term_0548', '추가 &gt;&gt; 0548 = -- = ', null, 'IMPACT_MESSAGE', null, null, null, null);
        ---
INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('c2c41376-ba13-494b-8588-a4b65a9701e7', 'ko', 'is_term_0548', '추가 &gt;&gt; 0548 = -- = ', null, 'IMPACT_MESSAGE', null, null, null, null);
--------------------------------------------------
-- 3. Some Class SOme SOme 
--------------------------------------------------
-- Something Something
INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('c2c41376-ba13-494b-8588-a4b65a9701e7', 'ko', 'is_term_0548', '추가 &gt;&gt; 0548 = -- = ', null, 'IMPACT_MESSAGE', null, null, null, null);

INSERT INTO dobj_desc (property_id, object_id, obj_seq, property_val, property_lval, property_pval) VALUES (1040001, '108e35a9-388c-4bd0-8562-52eb19f4c8bc', 0, null, 'search,
index,
server: _some_db,

keyword: [
    globalName, name, definition, query
],

include: [
    globalName, domainDataType, domainName,
    tableName, tableId,
    system, business, tableOwner, structureName,
    standardUrl, columnProfiling, errorRate,
],

sort: [
    globalName, domainDataType, domainName,
    tableName, tableId,
    system, business, tableOwner, structureName,
    standardUrl, columnProfiling, errorRate,
],



/*************************
 * 결과값 다루기
 *************************/
value: {

    _name: ${name},
    dataType: <label style="color: rgb(255, 127, 0);"><strong>${dataType}</strong></label>,
    columnType: <label style="color: rgb(255, 127, 0);"><strong>${columnType}</strong></label>,
    _modifyDt: ${updated().dateformat(long, yyyy-MM-dd HH:mm:ss)},
    /**
    _createDt: ${created().dateformat(long, yyyy-MM-dd HH:mm:ss)},
    **/
    _definition: ${definition},


    standardUrl: ${if(stdTerm).add(_code.mdosa.plugin.option.metastream.web-url).add(''/standard/term/termMain?objid='').add(stdTerm).else().add(''N'')},

    columnProfiling: ${if(columnProfiling).add(_code.mdosa.plugin.option.qualitystream.web-url).add(''/main#columnAnalysis/OBJ_RMK5='').add(tableOwner).add(''&OBJECT_NAME_510='').add(''tableId'').add(''&OBJECT_NAME_520='').add(globalName).add(''&IFSUBMIT=TRUE&PAGE_GUBUN=1&PAGE_ROW=50'').add(globalName).add(''&IFSUBMIT=TRUE&PAGE_GUBUN=1&PAGE_ROW=50'').else().add(''N'')},

    brAnalysis: ${if(brAnalysis).add(_code.mdosa.plugin.option.qualitystream.web-url).add(''/main#businessRule/tableName='').add(tableId).add(''&columnName='').add(globalName).add(''&dbOwner='').add(tableOwner).add(''&IFSUBMIT=TRUE&PAGE_GUBUN=1&PAGE_ROW=50'').else().add(''N'')},

    errorRate: ${if(errorRate).add(errorRate).else().add(''-'')},




    /***** FrontEnd 활용 정보 *****/
    _details: {

        /***** 링크 *****/
        link: [

            <a href="${_code.mdosa.plugin.option.metastream.web-url}/database/table/tableMain.page?sys=${sysCode}&biz=${bizCode}&dbname=${instanceName}&dbcode=500100&entityname=${tableId}" target="_blank" rel="noopener noreferrer" style="color: #1a73e8; margin-left: 3px;">${msg(search.meta.information.details)}<i aria-hidden="true" class="iruda-details-icon meta-icon"></i></a>,

            /**
            <a href="${if(qtrackUse).add(_code.mdosa.plugin.option.qtrack.web-url).add(''/main/mte#lineage/col/CL_A/[owner_undefined]/'').add(sysCode).add(''_'').add(bizCode).add(''/'').add(tableId).add(''/'').add(globalName).add(''/tbl/1/1'')}" rel="noopener noreferrer" style="color: #1a73e8; margin-left: 3px;">${msg(search.qtrack.information.details)}<i aria-hidden="true" class="iruda-details-icon qtrack-icon"></i></a>,
            **/

        ],

    }

},', null);
INSERT INTO dobj_desc (property_id, object_id, obj_seq, property_val, property_lval, property_pval) VALUES (1040001, '7f4a08dc-b9c9-4c95-9c0f-2208387b44a3', 0, null, 'search,
index,
server: _some_db,

keyword: [
    globalName, name, definition, query
],

/**
include: [
    globalName, dataType, dataStructure,
    wordType,
    relatedTerm
],

sort: [
    globalName, dataType, dataStructure,
    wordType,
    relatedTerm
],
**/


include: [
    globalName,
    relatedTerm,
],

sort: [
    globalName,
    relatedTerm,
],




/*************************
 * 결과값 다루기
 *************************/
value: {

    _name: ${objectName},
    dataType: ''<label style="color: rgb(255, 127, 0);"><strong>${dataType}</strong></label>'',
    _modifyDt: ${updated().dateformat(long, yyyy-MM-dd HH:mm:ss)},
    /**
    _createDt: ${created().dateformat(long, yyyy-MM-dd HH:mm:ss)},
    **/
    _definition: ${definition},



    /***** FrontEnd 활용 정보 *****/
    _details: {

        /** 링크 **/
        link: [

            <a href="${_code.mdosa.plugin.option.metastream.web-url}/standard/word/wordMain?objid=${objectId}" target="_blank" rel="noopener noreferrer" style="color: #1a73e8; margin-left: 3px;">${msg(search.meta.information.details)}<i aria-hidden="true" class="iruda-details-icon meta-icon"></i></a>,

        ],


        objectId: ${objectId},

    },

},
', null);
INSERT INTO dobj_desc (property_id, object_id, obj_seq, property_val, property_lval, property_pval) VALUES (1040001, 'cd3e093b-4eff-4bc0-a59b-c400150249f9', 0, null, 'search,
index,
server: _meta_db,

keyword: [
    globalName, name, definition, query
],


/**
include: [
    globalName, dataType, dataStructure,
    system, business,
    domainDataType, domainName, termType, protection
],

sort: [
    globalName, dataType, dataStructure,
    system, business,
    domainDataType, domainName, termType, protection
],
**/




include: [
    globalName, domainDataType, domainName,
    system, business,
],

sort: [
    globalName, domainDataType, domainName,
    system, business,
],



/*************************
 * 결과값 다루기
 *************************/
value: {

    _name: ${name},
    dataType: <label style="color: rgb(255, 127, 0);"><strong>${dataType}</strong></label>,
    termType: <label style="color: rgb(255, 127, 0);"><strong>${termType}</strong></label>,
    _modifyDt: ${updated().dateformat(long, yyyy-MM-dd HH:mm:ss)},
    /**
    _createDt: ${created().dateformat(long, yyyy-MM-dd HH:mm:ss)},
    **/
    _definition: ${definition},



    /***** FrontEnd 활용 정보 *****/
    _details: {

        /***** 링크 *****/
        link: [

            <a href="${_code.mdosa.plugin.option.metastream.web-url}/standard/businessterm/businesstermMain?objectId=${objectId}" target="_blank" rel="noopener noreferrer" style="color: #1a73e8; margin-left: 3px;">${msg(search.meta.information.details)}<i aria-hidden="true" class="iruda-details-icon meta-icon"></i></a>,


        ],

    },

},

', null);
        '''
        String pattern = "INSERT[\\s]+INTO[\\s]+\\S+[\\s]*\\([\\s]*(?<columns>.*?)[\\s]*\\)[\\s]*VALUES[\\s]*\\((?<values>.*?)[\\s]*\\);"
//        pattern = "INSERT\\s+[^;]{0,40}\\s+(?:[^;']|(?:'[^']*'))+[;]{1}"
        Matcher matcher  = new SqlAnalMan().getMatchedList(query, pattern)
        matcher.each{
            println it
        }
        assert matcher.size() == 8
    }



    @Test
    @Ignore
    void matchingTest(){
        String query = queryCreateFunction
//        String pattern = "CREATE\\s{1,2}.{0,50}\\s{0,2}JAVA\\s{0,2}SOURCE\\s{1,2}[^/]{1,20000}\\s*[/]{1}"
        String pattern = "CREATE\\s+[^/]+FUNCTION\\s+(?:[^/']|(?:'[^']*'))+RETURN(?:[^/'\"]|(?:'[^']*')|(?:\"[^\"]*\"))+[/]{1}"
//        String pattern = "CREATE\\s+[^/]{0,40}\\s+JAVA\\s+(?:SOURCE|RESOURCE|CLASS)(?:[^/\"]|(?:\"[^\"]*\"))+[/]{1}"

        Matcher matcher = new SqlAnalMan().getMatchedList(query, pattern)
        matcher.each{
            println it
        }
        assert matcher.size() > 0
    }

    @Test
    @Ignore
    void some(){
        String test = "INSERT INTO table_name (c1,c2,c3) VALUES (abc,def,ghi) , (jkl,mno,pqr)";
        String regex = "((?<=(INSERT\\sINTO\\s))[\\w\\d_]+(?=\\s+))|((?<=\\()([\\w\\d_,]+)+(?=\\)))";
        Pattern re = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = re.matcher(test);
        while (m.find()) {
            System.out.println(m.group(0));
        }
    }

    @Test
    @Ignore
    void matchingTest2(){
        SqlAnalMan.SqlObject sqlObj = new SqlAnalMan().getAnalyzedObject(queryCreateJava)
        println sqlObj

        assert checkMatching(queryCreateJava, SqlMan.CREATE_JAVA)
        assert checkMatching(queryCreateFunction, SqlMan.CREATE_FUNCTION)
        assert checkMatching(queryCreateFunction2, SqlMan.CREATE_FUNCTION)
        assert checkMatching(queryGrant, SqlMan.GRANT)
    }






    @Test
    @Ignore
    void matching_database(){
        String query = "CREATE DATABASE SOME_DATABASE_12345;"
        assert checkMatching(query, SqlMan.CREATE_DATABASE)

        SqlAnalMan.SqlObject sqlObj = new SqlAnalMan().getAnalyzedObject(query)
        assert sqlObj.commandType.equals("CREATE")
        assert sqlObj.objectType.equals("DATABASE")
        assert sqlObj.objectName.equals("SOME_DATABASE_12345")
    }

    @Test
    @Ignore
    void matching_schema(){
        String query = "CREATE SCHEMA SOME_SCHEMA_12345;"
        assert checkMatching(query, SqlMan.CREATE_SCHEMA)

        SqlAnalMan.SqlObject sqlObj = new SqlAnalMan().getAnalyzedObject(query)
        assert sqlObj.commandType.equals("CREATE")
        assert sqlObj.objectType.equals("SCHEMA")
        assert sqlObj.objectName.equals("SOME_SCHEMA_12345")
    }


    @Test
    void matching_insert(){
        String query = """
INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('d2a64e02-757f-489f-acd5-79a6b4fff867', 'ko', 'biz.WBT', '02.IT운영지원_프레임워크', 'Y', '국민카드 시스템/업무 데이터', null, null, null, null);
INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('c2c41376-ba13-494b-8588-a4b65a9701e7', 'ko', 'is_term_0548', '추가 &gt;&gt; 0548 = -- = ', null, 'IMPACT_MESSAGE', null, null, null, null); INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('2140a02a-0a15-4b24-bb06-290c127295e4', 'ko', 'qt.message.tbl', '테이블', 'Y', 'QTRACK_MESSAGE', null, null, null, null); INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('e655e6c4-11da-4b2c-8c04-b8c59916159d', 'ko', 'is_term_0537', '화', null, 'IMPACT_MESSAGE', null, null, null, null); INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('c9c83a3f-43a3-4680-b4b5-737e3a9ed41f', 'ko', 'alert_select_delete_parser_yn', '선택한 파서를 삭제하겠습니까?', null, 'IMPACT_MESSAGE', null, null, null, null);
INSERT INTO MSG_OBJECT (object_id, i18n_code, msg_code, msg_name, use_yn, description, create_dt, cusr, modify_dt, musr) VALUES ('6df42177-31c1-42a0-bc64-d2fe5701e685', 'ko', 'is_term_0279', 'MaxHeap', null, 'IMPACT_MESSAGE', null, null, null, null);
"""

        SqlMan sqlman = new SqlMan()
        sqlman.init()
                .query(query)
                .command([SqlMan.ALL])
                .replace(new SqlSetup())

        println sqlman
//        SqlAnalMan.SqlObject sqlObj = new SqlAnalMan().getAnalyzedObject(query)
//        assert sqlObj.commandType.equals("CREATE")
//        assert sqlObj.objectType.equals("SCHEMA")
//        assert sqlObj.objectName.equals("SOME_SCHEMA_12345")

    }




    static boolean checkMatching(String query, int matchingPatternCode){
        List<String> matched = getMatchingList(query, matchingPatternCode)
        return matched.size() > 0
    }

    static List<String> getMatchingList(String query, int matchingPatternCode){
        String createJavaPattern = new SqlAnalMan().getSqlPattern(matchingPatternCode)
        Matcher matcher = new SqlAnalMan().getMatchedList(query, createJavaPattern)
        return matcher.findAll()
    }


}
