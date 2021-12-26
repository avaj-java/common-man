package jaemisseo.man

import org.junit.After
import org.junit.Before
import org.junit.Test

class VariableManTest_if {

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
                listEmptyTest: []
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
    void usage1(){
        println varman.parse('site-option-${if(_lib.dir, starts, "D:/dev/workspaces1/").add("test").elseif(_lib.dir, starts, "D:/dev_by_sj/workspaces2/").add("real").else().add("dev")}.properties')
    }

    @Test
    void if_conditionFunction(){
        /** IF and ELSEIF **/
        assert varman.parse('Hello ${if("1", equals, "1").add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if("1", equals, "2").add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${if("1", equals, "3").add(ADD1).elseif("2", equals, "3").add(ADD2).elseif("3", equals, "3").add(ADD3).elseif("4", equals, "3").add(ADD4).elseif("5", equals, "3").add(ADD5)}') == 'Hello ADD3'
        assert varman.parse('Hello ${if("1", equals, "3").add(ADD1).elseif("2", equals, "3").add(ADD2).elseif("3", equals, "3").add(ADD3).elseif("4", equals, "4").add(ADD4).elseif("5", equals, "5").add(ADD5)}') == 'Hello ADD3'
        assert varman.parse('Hello ${if("1", notequals, "3").add(ADD1).elseif("2", notequals, "3").add(ADD2).elseif("3", notequals, "3").add(ADD3).elseif("4", equals, "4").add(ADD4).elseif("5", equals, "5").add(ADD5).else().add(else)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if("1", notequals, "3").add(ADD1).elseif("2", notequals, "3").add(ADD2).elseif("3", notequals, "3").add(ADD3).elseif("4", equals, "4").add(ADD4).elseif("5", equals, "5").add(ADD5).else().add(else).endif().add(" ReHello")}') == 'Hello ADD1 ReHello'
        assert varman.parse('Hello ${if("3", notequals, "3").add(ADD1).elseif("3", notequals, "3").add(ADD2).elseif("3", notequals, "3").add(ADD3).elseif("4", equals, "3").add(ADD4).elseif("5", equals, "5").add(ADD5).else().add(else).endif().add(" ReHello")}') == 'Hello ADD5 ReHello'
        assert varman.parse('Hello ${if("3", notequals, "3").add(ADD1).elseif("3", notequals, "3").add(ADD2).elseif("3", notequals, "3").add(ADD3).elseif("4", equals, "3").add(ADD4).elseif("5", equals, "3").add(ADD5).else().add(else).endif().add(" ReHello")}') == 'Hello else ReHello'

        /** IF and ELSEIF and IF AGAIN **/
        assert varman.parse('Hello ${if("b", starts, "b").add(Variable . Man).elseif("a",equals,"a").add("Addition")}') == 'Hello Variable . Man'
        assert varman.parse('Hello ${if("a", starts, "b").add(Variable . Man).elseif("a",equals,"a").add("Addition")}') == 'Hello Addition'
        assert varman.parse('Hello ${if("a", starts, "a").add(Variable . Man).elseif("a",equals,"a").add("Addition").if(a,equals,a)}') == 'Hello Variable . Man'
        assert varman.parse('Hello ${if("a", starts, "b").add(Variable . Man).elseif("a",equals,"a").add("Addition").if(a,equals,a)}') == 'Hello Addition'
        assert varman.parse('Hello ${if("abc", starts, "ab").add(ADD1).elseif("a",equals,"a").add("ADD2").elseif("aa",equals,"aa").add("ADD3").if("a",equals,"a").add("newIf1")}') == 'Hello ADD1newIf1'
        assert varman.parse('Hello ${if("abc", starts, "bc").add(ADD1).elseif("abc",starts,"c").add("ADD2").else().add("else ").if("a",equals,"b").add("newIf1")}') == 'Hello else '

        /** IF and ELSEIF and IF AGAIN **/
        assert varman.parse('Hello ${if("b", starts, "b").add(Variable . Man).add(" doubleAdd").add(" tripleAdd").elseif("a",equals,"a").add("Addition")}') == 'Hello Variable . Man doubleAdd tripleAdd'
        assert varman.parse('Hello ${if("a", starts, "b").add(Variable . Man).add(" doubleAdd").add(" tripleAdd").elseif("a",equals,"a").add("Addition")}') == 'Hello Addition'

        /** IF - Equals / Starts / Ends / Contains / Matches **/
        assert varman.parse('Hello ${hello().if(equals, "hello").add(ADD1)}') == 'Hello helloADD1'
        assert varman.parse('Hello ${hello().if(equals, "ello").add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(starts, "he").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(starts, "el").addBefore(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(ends, "llo").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(ends, "ll").addBefore(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(contains, "ll").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(contains, "el").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(contains, "rel").addBefore(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(contains, "").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(matches, "^hello").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(matches, "\\w*").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(matches, "\\d*").addBefore(ADD1)}') == 'Hello hello'

        /** IF - Exists / Empty / Null **/
        // hello = 'hello'
        assert varman.parse('Hello ${hello().if(exists).add(ADD1)}') == 'Hello helloADD1'
        assert varman.parse('Hello ${hello().if(notexists).add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(empty).add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(notempty).add(ADD1)}') == 'Hello helloADD1'
        assert varman.parse('Hello ${hello().if(null).add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(notnull).add(ADD1)}') == 'Hello helloADD1'
        // nothing = ''
        assert varman.parse('Hello ${nothing().if(exists).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${nothing().if(notexists).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${nothing().if(empty).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${nothing().if(notempty).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${nothing().if(null).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${nothing().if(notnull).add(ADD1)}') == 'Hello ADD1'
        // thereIsNoVariable = null
        assert varman.parse('Hello ${thereIsNoVariable().if(exists).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${thereIsNoVariable().if(notexists).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${thereIsNoVariable().if(empty).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${thereIsNoVariable().if(notempty).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${thereIsNoVariable().if(null).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${thereIsNoVariable().if(notnull).add(ADD1)}') == 'Hello '
    }

    @Test
    void if_naturalConditionFunction(){
        /** IF and ELSEIF **/
        assert varman.parse('Hello ${if("1" == "1").add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if("1" == "2").add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${if("1" == "3").add(ADD1).elseif("2"=="3").add(ADD2).elseif("3"=="3").add(ADD3).elseif("4"=="3").add(ADD4).elseif("5"=="3").add(ADD5)}') == 'Hello ADD3'
        assert varman.parse('Hello ${if("1"=="3").add(ADD1).elseif("2"=="3").add(ADD2).elseif("3"=="3").add(ADD3).elseif("4"=="4").add(ADD4).elseif("5"=="5").add(ADD5)}') == 'Hello ADD3'
        assert varman.parse('Hello ${if("1" != "3").add(ADD1).elseif("2"!="3").add(ADD2).elseif("3"!="3").add(ADD3).elseif("4"=="4").add(ADD4).elseif("5"=="5").add(ADD5).else().add(else)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if("1"!= "3").add(ADD1).elseif("2"!="3").add(ADD2).elseif("3"!="3").add(ADD3).elseif("4"=="4").add(ADD4).elseif("5"=="5").add(ADD5).else().add(else).endif().add(" ReHello")}') == 'Hello ADD1 ReHello'
        assert varman.parse('Hello ${if("3" !="3").add(ADD1).elseif("3"!="3").add(ADD2).elseif("3"!="3").add(ADD3).elseif("4"=="3").add(ADD4).elseif("5"=="5").add(ADD5).else().add(else).endif().add(" ReHello")}') == 'Hello ADD5 ReHello'
        assert varman.parse('Hello ${if("3"!="3").add(ADD1).elseif("3"!="3").add(ADD2).elseif("3"!="3").add(ADD3).elseif("4"=="3").add(ADD4).elseif("5"=="3").add(ADD5).else().add(else).endif().add(" ReHello")}') == 'Hello else ReHello'

        /** IF and ELSEIF and IF AGAIN **/
        assert varman.parse('Hello ${if("b" ^ "b").add(Variable . Man).elseif("a" == "a").add("Addition")}') == 'Hello Variable . Man'
        assert varman.parse('Hello ${if("a" ^ "b").add(Variable . Man).elseif("a" == "a").add("Addition")}') == 'Hello Addition'
        assert varman.parse('Hello ${if("a" ^ "a").add(Variable . Man).elseif("a" == "a").add("Addition").if("a" == "a")}') == 'Hello Variable . Man'
        assert varman.parse('Hello ${if("a" ^ "b").add(Variable . Man).elseif("a" == "a").add("Addition").if("a" == "a")}') == 'Hello Addition'
        assert varman.parse('Hello ${if("abc" ^ "ab").add("ADD1").elseif(a == a).add("ADD2").elseif("aa" == "aa").add("ADD3").if("a" == "a").add("newIf1")}') == 'Hello ADD1newIf1'
        assert varman.parse('Hello ${if("abc" ^ "bc").add(ADD1).elseif("abc" ^ "c").add("ADD2").else().add("else ").if("a" == "b").add("newIf1")}') == 'Hello else '

        /** IF and ELSEIF and IF AGAIN **/
        assert varman.parse('Hello ${if("b" ^ "b").add(Variable . Man).add(" doubleAdd").add(" tripleAdd").elseif(a == a).add("Addition")}') == 'Hello Variable . Man doubleAdd tripleAdd'
        assert varman.parse('Hello ${if("a" ^ "b").add(Variable . Man).add(" doubleAdd").add(" tripleAdd").elseif(a == a).add("Addition")}') == 'Hello Addition'

        /** IF - Equals / Starts / Ends / Contains / Matches **/
        assert varman.parse('Hello ${hello().if(== "hello").add(ADD1)}') == 'Hello helloADD1'
        assert varman.parse('Hello ${hello().if(== "ello").add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(^ "he").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(^ "el").addBefore(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if($ "llo").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if($ "ll").addBefore(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(~ "ll").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(~ "el").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(~ "rel").addBefore(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(~ "").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(==~ "^hello").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(==~ "\\w*").addBefore(ADD1)}') == 'Hello ADD1hello'
        assert varman.parse('Hello ${hello().if(==~ "\\d*").addBefore(ADD1)}') == 'Hello hello'

        /** IF - Exists / Empty / Null **/
        // hello = 'hello'
        assert varman.parse('Hello ${hello().if(exists).add(ADD1)}') == 'Hello helloADD1'
        assert varman.parse('Hello ${hello().if(notexists).add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(== "").add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(!= "").add(ADD1)}') == 'Hello helloADD1'
        assert varman.parse('Hello ${hello().if(== null).add(ADD1)}') == 'Hello hello'
        assert varman.parse('Hello ${hello().if(!= null).add(ADD1)}') == 'Hello helloADD1'

        // nothing = ''
        assert varman.parse('Hello ${nothing().if(exists).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${nothing().if(notexists).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${nothing().if(== "").add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${nothing().if(!= "").add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${nothing().if(== null).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${nothing().if(!= notnull).add(ADD1)}') == 'Hello ADD1'

        // thereIsNoVariable = null

        //TODO:.. It cannot recognize pre-inputed-value inner system. so recommand to use below
//        assert varman.parse('Hello ${thereIsNoVariable().if(!!).add(ADD1)}') == 'Hello '
//        assert varman.parse('Hello ${thereIsNoVariable().if(!).add(ADD1)}') == 'Hello ADD1'
//        assert varman.parse('Hello ${thereIsNoVariable().if(== "").add(ADD1)}') == 'Hello '
//        assert varman.parse('Hello ${thereIsNoVariable().if(!= "").add(ADD1)}') == 'Hello ADD1'
//        assert varman.parse('Hello ${thereIsNoVariable().if(== null).add(ADD1)}') == 'Hello ADD1'
//        assert varman.parse('Hello ${thereIsNoVariable().if(!= notnull).add(ADD1)}') == 'Hello '

        assert varman.parse('Hello ${thereIsNoVariable().if(exists).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${thereIsNoVariable().if(notexists).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${thereIsNoVariable().if(empty).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${thereIsNoVariable().if(notempty).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${thereIsNoVariable().if(null).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${thereIsNoVariable().if(notnull).add(ADD1)}') == 'Hello '
    }

    @Test
    void if_nullCheck(){
        // null_value = null
        assert varman.parse('Hello ${if(null_value == null).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if(null_value != null).add(ADD1)}') == 'Hello '

        // hello = 'hello'
        assert varman.parse('Hello ${if(hello == null).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${if(hello != null).add(ADD1)}') == 'Hello ADD1'

        // nothing = ''
        assert varman.parse('Hello ${if(nothing == null).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${if(nothing != notnull).add(ADD1)}') == 'Hello ADD1'

        // like = 'I LIKE BANANA.'
        assert varman.parse('Hello ${if(like == null).add(ADD1)}') == 'Hello '
        assert varman.parse('Hello ${if(like != null).add(ADD1)}') == 'Hello ADD1'


        varman.putVariables([stdTerm: null])
        String testValue = "hi \${if(stdTerm != null).add('/standard/term/termMain?objid=').add(stdTerm).elseif(stdDomain != null).add('/standard/domain/domainMain?objectId=').add(stdDomain).elseif(stdWord != null).add('standard/word/wordMain?objid=').add(stdWord).else().add('N')}"
        assert varman.parse(testValue) == "hi N"
    }

    @Test
    void if_computeNumbers(){
        assert varman.parse('Hello ${if(1 == 1).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if(1 < 2).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if(1 < 512).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if(0 < 0.1).add(ADD1)}') == 'Hello ADD1'
        assert varman.parse('Hello ${if(-1 > -3).add(ADD1)}') == 'Hello ADD1'
    }

    @Test
    void if_file(){
        if (!testFile.exists()){
            println "!!! testFile ${testFile?.getPath()} does not exists. !!!"
            return
        }


        //File
        assert "application-site-op.yml" == varman.parse('application-site-${if(file, "' +testFile.getPath()+ '").add("op").else().add("dev")}.yml')
        assert "application-site-op.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '", file~, "system").add("op").else().add("dev")}.yml')

        //Prop
        assert "application-site-op.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '!system", prop=, "op").add("op").else().add("dev")}.yml')
        assert "application-site-op.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '!system", !prop=, "op1").add("op").else().add("dev")}.yml')
        assert "application-site-dev.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '!system", prop=, "op1").add("op").else().add("dev")}.yml')

    }

    @Test
    void additional_conditionFuncClosure(){
        if (!checkTestFile())
            return

        VariableMan variableMan = new VariableMan().setCharset("utf-8").putConditionComputerFuncs([
                /***************
                 * - A: value
                 * - B: condition value
                 * - EX) if(A, computer, B)
                 ***************/
                ["FILE-EXISTS", "FILE"]: {  valA, valB ->
                    return new File(valB).exists()
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX) if(A, computer, B)
                 ***************/
                ["FILE-CONTAINS", "FILE~"]: { valA, valB ->
                    if (valA == null)
                        return false
                    String text = new File(valA).getText()
                    return (valB != null) ? text.contains(valB) : (text == valB)
                },

                /***************
                 * - A: ! 으로 split하여 앞은 파일경로 뒤는 property key
                 * - B: condition value
                 * - EX) if(A, computer, B)
                 ***************/
                ["PROP-EQUALS", "PROP="]: { valA, valB ->
                    String[] pathAndPropKey = valA.split("!")
                    String filePath = pathAndPropKey[0]
                    String checkPropKey = pathAndPropKey[1]
                    Properties prop = new Properties()
                    prop.load(new File(filePath).newInputStream())
                    String value = prop.get(checkPropKey)
                    return valB.equals(value)
                },
        ])

        //File
        assert "application-site-op.yml" == variableMan.parse('application-site-${if(file, "' +testFile.getPath()+ '").add("op").else().add("dev")}.yml')
        assert "application-site-op.yml" == variableMan.parse('application-site-${if("' +testFile.getPath()+ '", file~, "system").add("op").else().add("dev")}.yml')

        //Prop
        assert "application-site-op.yml" == variableMan.parse('application-site-${if("' +testFile.getPath()+ '!system", prop=, "op").add("op").else().add("dev")}.yml')
        assert "application-site-op.yml" == variableMan.parse('application-site-${if("' +testFile.getPath()+ '!system", !prop=, "op1").add("op").else().add("dev")}.yml')
        assert "application-site-dev.yml" == variableMan.parse('application-site-${if("' +testFile.getPath()+ '!system", prop=, "op1").add("op").else().add("dev")}.yml')
    }

}