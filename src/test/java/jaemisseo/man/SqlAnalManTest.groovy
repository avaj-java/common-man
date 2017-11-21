package jaemisseo.man

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.regex.Matcher

/**
 * Created by sujkim on 2017-06-08.
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

    List<Integer> patternCodeList = [SqlMan.CREATE_JAVA, SqlMan.CREATE_FUNCTION, SqlMan.CREATE_TABLE, SqlMan.CREATE_INDEX, SqlMan.CREATE_VIEW, SqlMan.CREATE_USER]

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




    @Test
    @Ignore
    void matchingTest(){
        String query = queryCreateFunction
//        String pattern = "CREATE\\s{1,2}.{0,50}\\s{0,2}JAVA\\s{0,2}SOURCE\\s{1,2}[^/]{1,20000}\\s*[/]{1}"
        String pattern = "CREATE\\s+[^/]+FUNCTION\\s+(?:[^/']|(?:'[^']*'))+RETURN(?:[^/'\"]|(?:'[^']*')|(?:\"[^\"]*\"))+[/]{1}"
//        String pattern = "CREATE\\s+[^/]{0,40}\\s+JAVA\\s+(?:SOURCE|RESOURCE|CLASS)(?:[^/\"]|(?:\"[^\"]*\"))+[/]{1}"
        '(?:[^;\']|(?:\'[^\']+\'))'

        Matcher matcher = new SqlAnalMan().getMatchedList(query, pattern)
        matcher.each{
            println it
        }
        assert matcher.size() > 0
    }

    @Test
    @Ignore
    void matchingTest2(){
//        SqlAnalMan.SqlObject sqlObj = new SqlAnalMan().getAnalyzedObject(queryCreateJava)
//        println sqlObj

        assert checkMatching(queryCreateJava, SqlMan.CREATE_JAVA)
        assert checkMatching(queryCreateFunction, SqlMan.CREATE_FUNCTION)
        assert checkMatching(queryCreateFunction2, SqlMan.CREATE_FUNCTION)
        assert checkMatching(queryGrant, SqlMan.GRANT)
    }


    boolean checkMatching(String query, int matchingPatternCode){
        patternCodeList.each{
            if (matchingPatternCode == it)
                assert getMatchingList(query, it)
            else
                assert !getMatchingList(query, it)
        }
        return true
    }

    List<String> getMatchingList(String query, int command){
        String createJavaPattern = new SqlAnalMan().getSqlPattern(command)
        Matcher matcher = new SqlAnalMan().getMatchedList(query, createJavaPattern)
        return matcher.findAll()
    }


}
