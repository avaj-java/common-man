package jaemisseo.man

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

class VariableManTest {

    String codeRuleH1 = '${biz}${seq(4).left(0).error(over)}'
    String codeRuleH2 = '${biz}${class}${seq(3).left(0).error(over)}'
    String codeRuleS = '${biz(4).right(0)}${class(1)}${seq(4).left(0).error(over)}'

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
                ]
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
    void test_listValue(){
        println varman.parse('test__${listTest}__test')
        assert varman.parse('-t ${listTest().join(" -t ")} _test') == '-t 1 -t 2 -t 3 -t 4 -t 111 -t 2222 -t 1155 -t Hehehe _test'
    }


    @Test
    void simpleVariable() {
        // Test empty string
        assert varman.parse('') == ""

        // Test - left() and right()
        assert varman.parse('${USERNAME(8).left()}asfd') == "하이하이asfd"
        assert varman.parse('${USERNAME()}asfd') == "하이하이하이asfd"
        assert varman.parse('${USERNAME}asfd') == "하이하이하이asfd"
        assert varman.parse('${USERNAME(8)}asfd') == "하이하이asfd"
        assert varman.parse('${USERNAME(8).right()}asfd') == "하이하이asfd"
        assert varman.parse('${USERNAME(14).right()}asfd') == "하이하이하이  asfd"

        // Test - lower() and upper()
        assert varman.parse('${USERNAME(8).lower()}asfd') == "하이하이asfd"
        assert varman.parse('${lowerChar()}asfd') == "hi everybodyasfd"
        assert varman.parse('${lowerChar().upper()}asfd') == "HI EVERYBODYasfd"
        assert varman.parse('${upperChar(15).left(0).lower()}asfd') == "000hi everybodyasfd"
        assert varman.parse('${upperChar().lower()}asfd') == "hi everybodyasfd"

        // Test - ${}
        assert varman.parse('${}asdf') == '${}asdf'
        assert varman.parse('${}asdf${}') == '${}asdf${}'
        assert varman.parse('${}as ${ } ${  } ${  ddfd } ${ddfd} ${syntaxTest}  d   f${}') == '${}as ${ } ${  }   HI ${}EVERYBODY${}  d   f${}'
        assert varman.parse('${s()} ${s()}///${empty(50).left( )}///${}') == '하하하\\n하하하 하하하\\n하하하///                                                  ///${}'
        assert varman.parse('${s2()}///${empty(50).left( )}///${}') == 'ㅋㅋㅋ\nㅋㅋl///                                                  ///${}'

        // Test - numberOnly()
        assert varman.parse('[${num(15)}] / [${num().numberOnly()}] / [${num(15).numberOnly()}] / [${num(15).numberOnly().right()}]') == '[010-9911-0321  ] / [01099110321] / [01099110321    ] / [01099110321    ]'

        // Test - nvl()
        assert varman.parse('${nvl.test().nvl()} hahaha ${nvl.test().nvl(hohoho)} ${USERNAME().nvl()} ${USERNAME().nvl(hehehe)}') == ' hahaha hohoho 하이하이하이 하이하이하이'

        // Test - length
        assert varman.parse('${installer.level.1.file.path}') == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path()}') == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path(3)}') == '/fo'

        // Test - date (내장된 변수)
        println varman.parse('${}as ${date()} ${}')
        println varman.parse('${}as ${date(yyyy-MM-dd HH:mm:ssSSS)} ${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SSS)}${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SS)}${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(S)}${}')
        println varman.parse('${}as ${date(long)}${}')
        println varman.parse('${}as ${date(long)}${}')
        println varman.parse('[${date(yyyyMMddHHmmssSSS)}${random(5)}]')

        // Test - parseDefaultVariableOnly
        String tempCode = '${USERNAME} ${notExistsVariable} [${date(yyyyMMddHHmmssSSS)}${random(5)}]'
        println varman.setModeExistCodeOnly(true).parse(tempCode)

        assert varman.setModeExistCodeOnly(true).parse(tempCode).startsWith('하이하이하이 ${notExistsVariable} [')
        println varman.setModeExistCodeOnly(false).parse(tempCode)

        assert varman.setModeExistCodeOnly(false).parse(tempCode).startsWith('하이하이하이  [')
        println varman.setModeExistCodeOnly(true).parseDefaultVariableOnly(tempCode)
        assert varman.setModeExistCodeOnly(true).parseDefaultVariableOnly(tempCode).startsWith('${USERNAME} ${notExistsVariable} [')

        println varman.setModeExistCodeOnly(false).parseDefaultVariableOnly(tempCode)
        assert varman.setModeExistCodeOnly(false).parseDefaultVariableOnly(tempCode).startsWith('${USERNAME} ${notExistsVariable} [')

        varman.setModeExistCodeOnly(true)
    }

    @Test
    void userSetLength(){
        assert varman.parse('${like()}') == 'I LIKE BANANA.'
        assert varman.parse('${like}') == 'I LIKE BANANA.'
        assert varman.parse('${like(1)}') == 'I'
        assert varman.parse('${like(2)}') == 'I '
        assert varman.parse('${like(16)}') == 'I LIKE BANANA.  '
        assert varman.parse('${like(16).right(0)}') == 'I LIKE BANANA.00'
        assert varman.parse('${like(16).left(0)}') == '00I LIKE BANANA.'
        //- UserSetLength with variable
        assert varman.parse('${number1}') == '1'
        assert varman.parse('${number2}') == '2'
        assert varman.parse('${number3}') == '3'
        assert varman.parse('${like(number1)}') == 'I'
        assert varman.parse('${like(number4)}') == 'I LI'
        assert varman.parse('${like(number2)} / ${like(number3)}') == 'I  / I L'
        //
        assert varman.parse('${not.exist.value(5).left(0)}')  == '00000'
        assert varman.parse('${nothing(5).left(0)}')  == '00000'
        assert varman.parse('${not.exist.value(0).left(0)}')  == ''
        assert varman.parse('${nothing(0).left(0)}')  == ''
    }

    @Test
    void syntexTest(){
        println varman.parse('${random(3)}')
        println varman.parse('${random()}')
        println varman.parse('${random}')
        println varman.parse('${date()}')
        println varman.parse('${date}')
        println varman.parse('${enter()}1${enter(2)}2${enter(3)}3')
        println varman.parse('${enter}1${enter}2${enter}3')

        println varman.parse('${my.date().dateformat(yyyy MM dd )}')
        println varman.parse('${my.date().dateformat(yyyy MM dd, yy/MM/dd/HH/mm/ss)}')
        println varman.parse('${my.date2().dateformat(yyyy/MM/dd)}')
        println varman.parse('${my.date2().dateformat(yyyy/MM/dd HH:mm:ss)}')
        println varman.parse('${my.date2().dateformat(yyyy/MM/dd HH:mm:ss, yy/MM/dd/HH/mm/ss)}')
        println varman.parse('${my.date2().dateformat("yyyy/MM/dd HH:mm:ss", "yy/MM/dd/HH/mm/ss")}')

        assert varman.parse('${random(3)}').length() == 3
    }

    @Test
    void systemVariableTest() {
        // Test - left() and right()
        println '_USER.NAME: ' + varman.parse('${_user.name}')
        println '_USER.DIR: ' + varman.parse('${_user.dir}')
        println '_USER.HOME: ' + varman.parse('${_user.home}')
        println '_OS.NAME: ' + varman.parse('${_os.name}')
        println '_OS.VERSION: ' + varman.parse('${_os.version}')
        println '_JAVA.VERSION: ' + varman.parse('${_java.version}')
        println '_JAVA.HOME: ' + varman.parse('${_java.home}')
        println '_HOSTNAME: ' + varman.parse('${_hostname}')
        println '_IP: ' + varman.parse('${_ip}')
        println '_LIB.DIR: ' + varman.parse('${_lib.dir}')
        println '_LIB.PATH: ' + varman.parse('${_lib.path}')
    }

    @Test
    void patternTest(){
        //TODO: 추후 괄호 매칭패턴 더욱 연구 필요
        String patternToGetMembers = '(?:[(][\'][\'](?:\\(\\))[)]|[(][^()]*[)])'
        String content = '${enter()}1${enter(2)}2${enter(3)}3'
        Matcher m = Pattern.compile(patternToGetMembers).matcher(content)
        m.each{
            println it
        }
    }


    @Test
    void addTypeFunction(){
        // Test - replace
        assert varman.parse('${installer.level.1.file.path().add(VariableMan)}') == '/foo/barVariableMan'
        assert varman.parse('${installer.level.1.file.path().add(I\'m VariableMan)}') == '/foo/barI\'m VariableMan'
        assert varman.parse('${installer.level.1.file.path().addBefore(I\'m VariableMan)}') == 'I\'m VariableMan/foo/bar'
        assert varman.parse('${installer.level.1.file.path().add( enter )}') == '/foo/bar\n'
        assert varman.parse('${installer.level.1.file.path().add( . )}') == '/foo/bar.'
        assert varman.parse('${installer.level.1.file.path().add( . )}') == '/foo/bar.'

        assert varman.parse('${installer.level.1.file.path().add( "," )}') == '/foo/bar,'
        assert varman.parse('${installer.level.1.file.path().add(",")}') == '/foo/bar,'
        assert varman.parse('${installer.level.1.file.path().add(" GRANT SELECT, INSERT, UPDATE, DELETE ON ").add(tableName).add(" TO RR_").add(tableName).add(_WW)}') == '/foo/bar GRANT SELECT, INSERT, UPDATE, DELETE ON TBHOHO0012 TO RR_TBHOHO0012_WW'
    }

    @Test
    void existFunction(){
        // Test - replace
        assert varman.parse('Hello ${nothing().exist().add(VariableMan)}') == 'Hello '
        assert varman.parse('Hello ${nothing().exist().add(VariableMan)}') == 'Hello '
        assert varman.parse('Hello ${nothing().exist().addBefore(VariableMan)}') == 'Hello '

        assert varman.parse('Hello ${nothing().add(VariableMan)}') == 'Hello VariableMan'
        assert varman.parse('Hello ${nothing().add(Variable . Man)}') == 'Hello Variable . Man'
        assert varman.parse('Hello ${nothing().addBefore( . Variable . Man . )}') == 'Hello . Variable . Man .'
        assert varman.parse('Hello ${nothing().addBefore(" . Variable . Man . ")}') == 'Hello  . Variable . Man . '

        assert varman.parse('Hello ${hello().add(" VariableMan ")}') == 'Hello hello VariableMan '
        assert varman.parse('Hello ${hello().add(" VariableMan ")}') == 'Hello hello VariableMan '
        assert varman.parse('Hello ${hello().addBefore(" VariableMan ")}') == 'Hello  VariableMan hello'
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
        //File
        assert "application-site-op.yml" == varman.parse('application-site-${if(file, "' +testFile.getPath()+ '").add("op").else().add("dev")}.yml')
        assert "application-site-op.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '", file~, "system").add("op").else().add("dev")}.yml')

        //Prop
        assert "application-site-op.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '!system", prop=, "op").add("op").else().add("dev")}.yml')
        assert "application-site-op.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '!system", !prop=, "op1").add("op").else().add("dev")}.yml')
        assert "application-site-dev.yml" == varman.parse('application-site-${if("' +testFile.getPath()+ '!system", prop=, "op1").add("op").else().add("dev")}.yml')

    }

    @Test
    void replaceFunction(){
        // Test - replace
        assert varman.parse('${installer.level.1.file.path(3).replace(f,o)}') == '/oo'
        assert varman.parse('${installer.level.1.file.path(3).replace(/,o)}') == 'ofo'
        assert varman.parse('${installer.level.1.file.path(3).replace(/,o).replace(f,o)}') == 'ooo'

        // Test - replace
        assert varman.parse('${tab().replace("\t","")}') == 'hellotab1tab3tab4'
        assert varman.parse('${tab().replace("\t"," ")}') == 'hello tab1   tab3    tab4'

        // Test - replace_regex
        assert varman.parse('${installer.level.1.file.path().replaceAll(/,o)}') == 'ofooobar'
        assert varman.parse('${installer.level.1.file.path().replaceAll(^/,"s")}') == 'sfoo/bar'
//        assert varman.parse('${installer.level.1.file.path().replace_regex([/]$,s)}') == '/foosbar'
    }

    @Test
    void pickOne(){
        //특정 번호에 맞는 것을 선택
        println varman.parse('X${tableName().replaceFirst(^T,"")}${pick(indexSeq, 간,나,하,호,a,b,c,d,e,f,A..Z, 1..100, AA..AZ)}')
        //특정 문자에 맞는 것 다음 것을 선택
        println varman.parse('X${tableName().replaceFirst(^T,"")}${pickNextOf(indexString, A..Z)}')
    }

    @Test
    void useValueByKeyFromProperties(){
        if (!checkTestFile())
            return

        //- exists
        assert "application-site-op.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml')
        assert "application-site-op.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml')

        //- not exists
        assert "application-site-.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml')
        assert "application-site-.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml')
    }


    @Test
    void "Properties 스타일 Variable로 변환"(){
        // Exist Variable
        assert "Hello, i am foo.bar" == varman.parse('Hello, ${foo.bar}')
        assert "Hello, i am foo.var" == varman.parse('Hello, ${foo.var}')
        assert "Hello, i am foofoo.bar" == varman.parse('Hello, ${foofoo.bar}')
        assert "Hello, i am foofoo.barbar.var" == varman.parse('Hello, ${foofoo.barbar.var}')
        assert "Hello, i am foofoo.barbar.varvar.end" == varman.parse('Hello, ${foofoo.barbar.varvar.end}')

        // No Exist Variable
        assert "Hello, " == varman.parse('Hello, ${foo.no.bar}')
    }

    @Test
    void "Object타입의 CodeRule을 Parsing하기"(){
        def machingParameter = [
                catalog: [
                        dataBaseName: true,
                        schemaName: true,
                        tableName: true
                ],
                managerList: [
                        [
                                name: '*${search}*',
                                id: '*${search}*'
                        ]
                ]
        ]

        // Exist Variable
        def parsedObject = varman.parse(machingParameter)
        assert parsedObject.managerList.size() == parsedObject.managerList.findAll{ it.name.equals '*HELLO SEARCH !!!*' }.size()
        assert parsedObject.managerList.size() == parsedObject.managerList.findAll{ it.id.equals '*HELLO SEARCH !!!*' }.size()
    }



    @Test
    void "분석 데이터 테스트"() {
        String codeRule = '${USERNAME} ${notExistsVariable} [${date(yyyyMMddHHmmssSSS)}${random(5)}]'
        List<VariableMan.OnePartObject> list = new VariableMan().parsedDataList(codeRule, [
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
        ])
        println list
        println codeRule
        println codeRule.size()
        println list.collect{ return it.partValue }.join('')
        println list.collect{ return it.partValue }.join('').size()
        assert codeRule == list.collect{ return it.partValue }.join('')
    }

    @Test
    void parsedDataList() {
        List<VariableMan.OnePartObject> list

        //VariableMan 생성
        VariableMan varman = new VariableMan('EUC-KR', [
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
        ])

        varman.setModeExistFunctionOnly(false)

        // Test empty string
        varman.parsedDataList('') == ""

        // Test - left() and right()
        list = varman.parsedDataList('${USERNAME(8).left(jjjj)}asfd')
        list = varman.parsedDataList('${USERNAME()}asfd')
        list = varman.parsedDataList('${USERNAME}asfd')
        list = varman.parsedDataList('${USERNAME(8)}asfd')
        list = varman.parsedDataList('${USERNAME(8).right()}asfd')
        list = varman.parsedDataList('${USERNAME(14).right()}asfd')

        list = varman.parsedDataList('${USERNAME(14).nvl()}asfd')
        assert list[0].hasFunc('nvl')
        assert list[0].getMember('nvl') == [""] as String[]
        assert list[0].getMember('nvl', 0) == ""
        assert list[0].getMember('nonefunc') == null
        assert list[0].getMember('nonefunc', 0) == null

        list = varman.parsedDataList('${USERNAME(14).nonefunc(dddd)}asfd')
        assert list[0].hasFunc('nonefunc')
        assert list[0].getMember('nonefunc') == ['dddd'] as String[]
        assert list[0].getMember('nonefunc', 0) == 'dddd'
    }

    @Test
    void signChange(){
        VariableMan varman = new VariableMan([
                'a1': 'a',
                'a2': 'aa',
                'a3': 'aaa',
        ])
        varman.setVariableSign('#')
        assert varman.parse('${a1} ${a2} ${a3}') == '${a1} ${a2} ${a3}'
        assert varman.parse('#{a1} #{a2} #{a3}') == 'a aa aaa'
        assert varman.parse('#{a1} ${a2} #{a3}') == 'a ${a2} aaa'

        varman.setVariableSign('%')
        assert varman.parse('${a1} ${a2} ${a3}') == '${a1} ${a2} ${a3}'
        assert varman.parse('%{a1} %{a2} %{a3}') == 'a aa aaa'
        assert varman.parse('%{a1} ${a2} %{a3}') == 'a ${a2} aaa'

        varman.setVariableSign('')
        assert varman.parse('${a1} ${a2} ${a3}') == '$a $aa $aaa'
        assert varman.parse('#{a1} #{a2} #{a3}') == '#a #aa #aaa'
        assert varman.parse('#{a1} ${a2} #{a3}') == '#a $aa #aaa'
        assert varman.parse('{a1} {a2} {a3}') == 'a aa aaa'
    }


    @Test
    void "통합메세지 메세지채번 테스트"() {
        // h 1
        assert new VariableMan().parse(codeRuleH1, [sys: 'SSSSSSS', biz: 'BBBBBBB', class: 'classabcd', seq: 1]) == 'BBBBBBB0001'
        // h 2
        assert new VariableMan().parse(codeRuleH2, [sys: 'SSSSSSS', biz: 'BBBBBBB', class: 'classabcd', seq: 1]) == 'BBBBBBBclassabcd001'
        // s
        assert new VariableMan().parse(codeRuleS, [sys: 'SSSSSSS', biz: 'BBBBBBB', class: 'classabcd', seq: 1]) == 'BBBBc0001'
    }

    @Test(expected = Exception.class)
    void ErrorTest() {
        String h1 = new VariableMan().parse(codeRuleH1, [sys: 'SSSSSSS', biz: 'BBBBBBB', class: 'classabcd', seq: 10000])
    }

    @Test(expected = Exception.class)
    void ErrorTest2() {
        String h2 = new VariableMan().parse(codeRuleH2, [sys: 'SSSSSSS', biz: 'BBBBBBB', class: 'classabcd', seq: 1000])
    }

    @Test(expected = Exception.class)
    void ErrorTest3() {
        String s = new VariableMan().parse(codeRuleS, [sys: 'SSSSSSS', biz: 'BBBBBBB', class: 'classabcd', seq: 10000])
    }




    @Test
    void additional_funcClosure(){
        Map<String, Closure> funcClosures = [
                'split-join': { VariableMan.OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.substitutes && it.members){
                        String joiner = it.members[0]
                        it.substitutes = it.substitutes.trim().split("")?.join(joiner)
//                        it.length = it.substitutes.length()
                        it.length = it.substitutes.getBytes().length
                    }
                }
        ]
        Map<String, Object> variables = [
                a:123,
                b:'ABCD',
                c:'월화수목금'
        ]
        VariableMan variableMan = new VariableMan().setCharset('utf-8').putVariables(variables).putFuncs(funcClosures)
        println variableMan.parseString('${b().split-join(/)}')
        println variableMan.parseString('${c().split-join(/)}')

    }

    @Test
    void additional_variableClosure(){
        if (!checkTestFile())
            return

        VariableMan variableMan = new VariableMan().setCharset('utf-8').putVariableClosures([
                'prop': { VariableMan.OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String propFilePath = VariableMan.parseMember(it.members[0], vsMap, vcMap)
                        String checkPropKey = VariableMan.parseMember(it.members[1], vsMap, vcMap)
                        Properties prop
                        String value = ""
                        try{
                            prop = new Properties()
                            prop.load(new File(propFilePath).newInputStream())
                            value = prop.get(checkPropKey)
                        }catch(e){
//                            logger.warn("Error ocurred during loading file ", e)
                        }
                        it.substitutes = value
//                        it.length = it.substitutes.length()
                        it.length = it.substitutes.getBytes().length
                    }
                }
        ])

        //- exists
        assert "application-site-op.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml')
        assert "application-site-op.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml')

        //- not exists
        assert "application-site-.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml')
        assert "application-site-.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml')
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