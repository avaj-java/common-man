package jaemisseo.man

import com.sun.nio.sctp.IllegalReceiveException
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

class VariableManTest_matcher {

    File testDir
    File testFile

    VariableMan varman

    @Before
    void beforeTest(){
        // VariableMan 생성
        varman = new VariableMan('EUC-KR', [
                noContent                    : "",
                nullContent                  : null,
                USERNAME                     : "하이하이하이",
                lowerChar                    : "hi everybody",
                upperChar                    : "HI EVERYBODY",
                syntaxTest                   : 'HI ${}EVERYBODY${}',
                s                            : '하하하\\n하하하',
                s2                           : 'ㅋㅋㅋ\nㅋㅋl',
                num                          : '010-9911-0321',
                'installer.level.1.file.path': '/foo/bar',
                'nvl.test'                   : '',
                'foo.bar': "i am maybe foo.bar",
                'foo': [
                        bar: "i am foo.bar",
                        var: "i am foo.var",
                ],
                'foofoo': [
                        barbar: [
                                varvar: [
                                        end: "i am foofoo.barbar.varvar.end"
                                ],
                                var: "i am foofoo.barbar.var",
                        ],
                        bar: "i am foofoo.bar"
                ],
                search: "HELLO SEARCH !!!",
                hello: 'hello',
                like: 'I LIKE BANANA.',
                nothing: '',
                null: null,
                null_value: null,
                zero: 0,
                tab: 'hello\ttab1\t\t\ttab3\t\t\t\ttab4',
                'my.date': '2010 01 01',
                'my.date2': '2012/07/21 13:55:41',
                tableName: 'TBHOHO0012',
                indexSeq: '3',
                indexString: 'b',
                number1: 1,
                number2: '2',
                number3: '3',
                number4: 4,
                listTest: [
                    1,2,3,4,111,2222,1155,'Hehehe'
                ],
                listEmptyTest: [],
                nullOneNullTwoList: [
                        null, 1, null, 2
                ],
        ])
//        .setModeDebug(true)

        Long time = new Date().getTime()
        String dirPath = "/temp"
        String filePName = "_test_temp_${time}.properties"
        testDir = new File(dirPath)
        testFile = new File(dirPath, filePName)
        //- Check directories
        testDir.mkdirs()
        if (!testDir.exists())
            return
        //- Make temporary test file
        testFile.write("system=op")
        if (!testFile.exists())
            return
    }

    @After
    void after(){
        testFile.delete()
    }

    boolean checkTestFile(){
        return testDir.exists() && testFile.exists()
    }


    @Test
    void test_java_matcher(){
        Matcher matcher1 = Pattern.compile('(?<n1>test__)(?<n2>.*)(?<n3>__test)').matcher("test__hhhh__test")
        assert matcher1.groupCount() == 3
        assert matcher1.matches() == true
        assert matcher1.group("n1").equals("test__")
        assert matcher1.group("n2").equals("hhhh")
        assert matcher1.group("n3").equals("__test")

        Matcher matcher2 = Pattern.compile('test__(?<n2>.*)__test').matcher("test__hhhh__test")
        assert matcher2.groupCount() == 1
        assert matcher2.matches() == true
        assert matcher2.group("n2").equals("hhhh")

        Matcher matcher22 = Pattern.compile('test__(?<n2>.*)1(?<n3>.*)__test').matcher("test__hhh1h__test")
        assert matcher22.groupCount() == 2
        assert matcher22.matches() == true
        assert matcher22.group("n2").equals("hhh")
        assert matcher22.group("n3").equals("h")

        Matcher matcher23 = Pattern.compile('test__(?<n2>.*)1(?<n3>.*)__test').matcher("test__hhhh1__test")
        assert matcher23.groupCount() == 2
        assert matcher23.matches() == true
        assert matcher23.group("n2").equals("hhhh")
        assert matcher23.group("n3").equals("")

        Matcher matcher3 = Pattern.compile('test__(?<n2>.*)__test').matcher("test__hhhh__test_")
        assert matcher3.groupCount() == 1
        assert matcher3.matches() == false
    }



    @Test
    void test_listValue(){
        Map<String, Object> variableMap = null;

        variableMap = new VariableMan().matchVariableMap('test__${var1}__test', 'test__SomeThinghah__test')
        assert variableMap["VAR1"].equals("SomeThinghah")

        variableMap = new VariableMan().matchVariableMap('${var2}__${var1}__test', 'test__SomeThinghah__test')
        assert variableMap["VAR1"].equals("SomeThinghah")
        assert variableMap["VAR2"].equals("test")

        variableMap = new VariableMan().matchVariableMap('${var2}__${var1}_${var3}', 'test__SomeThinghah__test')
        assert variableMap["VAR1"].equals("SomeThinghah_")
        assert variableMap["VAR2"].equals("test")
        assert variableMap["VAR3"].equals("test")
    }

    @Test
    void test_some(){
        Map<String, Object> variableMap = null;

        variableMap = new VariableMan().matchVariableMap('test__${var1}__test', 'test__SomeThinghah__test')
        assert variableMap["VAR1"].equals("SomeThinghah")

        variableMap = new VariableMan().matchVariableMap('${var2}${var1}__test', 'test__SomeThinghah__test')
        assert variableMap["VAR1"].equals("")
        assert variableMap["VAR2"].equals("test__SomeThinghah")
    }



    @Test(expected = NullPointerException.class)
    void codeRule_is_null(){
        Map<String, Object> variableMap = null;

        variableMap = new VariableMan().matchVariableMap(null, 'test__SomeThinghah__test')
    }

    @Test(expected = NullPointerException.class)
    void targetString_is_null(){
        Map<String, Object> variableMap = null;

        variableMap = new VariableMan().matchVariableMap('${var2}__${var1}_${var3}', null)
    }

    @Test(expected = java.util.regex.PatternSyntaxException.class)
    void codeRule_is_empty(){
        Map<String, Object> variableMap = null;

        variableMap = new VariableMan().matchVariableMap('', 'test__SomeThinghah__test')
        assert variableMap != null
        assert variableMap.keySet().size() == 0

        variableMap = new VariableMan().matchVariableMap(' ', 'test__SomeThinghah__test')
        assert variableMap != null
        assert variableMap.keySet().size() == 0

        variableMap = new VariableMan().matchVariableMap(' ${ } ', 'test__SomeThinghah__test')
        assert variableMap != null
        assert variableMap.keySet().size() == 0
    }

    void targetString_is_empty(){
        Map<String, Object> variableMap = null;

        variableMap = new VariableMan().matchVariableMap('${var2}__${var1}_${var3}', '')
        assert variableMap != null
        assert variableMap.keySet().size() == 0

        variableMap = new VariableMan().matchVariableMap('${var2}__${var1}_${var3}', ' ')
        assert variableMap != null
        assert variableMap.keySet().size() == 0

        variableMap = new VariableMan().matchVariableMap('${var2}__${var1}_${var3}', ' ${ } ')
        assert variableMap != null
        assert variableMap.keySet().size() == 0
    }

}