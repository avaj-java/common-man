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
    Map<String, Object> variableMap

    @Before
    void beforeTest(){
        // VariableMan 생성
        varman = new VariableMan('EUC-KR')
//        .setModeDebug(true)

        variableMap = [
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
        ]

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
    void simpleVariable() {
        // Test empty string
        assert varman.parse('', variableMap) == ""

        // Test - left() and right()
        assert varman.parse('${USERNAME(8).left()}asfd', variableMap) == "하이하이asfd"
        assert varman.parse('${USERNAME()}asfd', variableMap) == "하이하이하이asfd"
        assert varman.parse('${USERNAME}asfd', variableMap) == "하이하이하이asfd"
        assert varman.parse('${USERNAME(8)}asfd', variableMap) == "하이하이asfd"
        assert varman.parse('${USERNAME(8).right()}asfd', variableMap) == "하이하이asfd"
        assert varman.parse('${USERNAME(14).right()}asfd', variableMap) == "하이하이하이  asfd"

        // Test - lower() and upper()
        assert varman.parse('${USERNAME(8).lower()}asfd', variableMap) == "하이하이asfd"
        assert varman.parse('${lowerChar()}asfd', variableMap) == "hi everybodyasfd"
        assert varman.parse('${lowerChar().upper()}asfd', variableMap) == "HI EVERYBODYasfd"
        assert varman.parse('${upperChar(15).left(0).lower()}asfd', variableMap) == "000hi everybodyasfd"
        assert varman.parse('${upperChar().lower()}asfd', variableMap) == "hi everybodyasfd"

        // Test - ${}
        assert varman.parse('${}asdf', variableMap) == '${}asdf'
        assert varman.parse('${}asdf${}', variableMap) == '${}asdf${}'
        assert varman.parse('${}as ${ } ${  } ${  ddfd } ${ddfd} ${syntaxTest}  d   f${}', variableMap) == '${}as ${ } ${  }   HI ${}EVERYBODY${}  d   f${}'
        assert varman.parse('${s()} ${s()}///${thisIsEmpty(50).left( )}///${}', variableMap) == '하하하\\n하하하 하하하\\n하하하///                                                  ///${}'
        assert varman.parse('${s2()}///${thisIsEmpty(50).left( )}///${}', variableMap) == 'ㅋㅋㅋ\nㅋㅋl///                                                  ///${}'

        // Test - numberOnly()
        assert varman.parse('[${num(15)}] / [${num().numberOnly()}] / [${num(15).numberOnly()}] / [${num(15).numberOnly().right()}]', variableMap) == '[010-9911-0321  ] / [01099110321] / [01099110321    ] / [01099110321    ]'

        // Test - nvl()
        assert varman.parse('${nvl.test().nvl()} hahaha ${nvl.test().nvl(hohoho)} ${USERNAME().nvl()} ${USERNAME().nvl(hehehe)}', variableMap) == ' hahaha hohoho 하이하이하이 하이하이하이'

        // Test - length
        assert varman.parse('${installer.level.1.file.path}', variableMap) == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path()}', variableMap) == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path(3)}', variableMap) == '/fo'

        // Test - date (내장된 변수)
        println varman.parse('${}as ${date()} ${}', variableMap)
        println varman.parse('${}as ${date(yyyy-MM-dd HH:mm:ssSSS)} ${}', variableMap)
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SSS)}${}', variableMap)
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SS)}${}', variableMap)
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(S)}${}', variableMap)
        println varman.parse('${}as ${date(long)}${}', variableMap)
        println varman.parse('${}as ${date(long)}${}', variableMap)
        println varman.parse('[${date(yyyyMMddHHmmssSSS)}${random(5)}]', variableMap)

        // Test - parseDefaultVariableOnly
        String tempCode = '${USERNAME} ${notExistsVariable} [${date(yyyyMMddHHmmssSSS)}${random(5)}]'
        println varman.setModeExistCodeOnly(true).parse(tempCode, variableMap)

        assert varman.setModeExistCodeOnly(true).parse(tempCode, variableMap).startsWith('하이하이하이 ${notExistsVariable} [')
        println varman.setModeExistCodeOnly(false).parse(tempCode, variableMap)

        assert varman.setModeExistCodeOnly(false).parse(tempCode, variableMap).startsWith('하이하이하이  [')
        println varman.setModeExistCodeOnly(true).parseDefaultVariableOnly(tempCode)
        assert varman.setModeExistCodeOnly(true).parseDefaultVariableOnly(tempCode).startsWith('${USERNAME} ${notExistsVariable} [')

        println varman.setModeExistCodeOnly(false).parseDefaultVariableOnly(tempCode)
        assert varman.setModeExistCodeOnly(false).parseDefaultVariableOnly(tempCode).startsWith('${USERNAME} ${notExistsVariable} [')

        varman.setModeExistCodeOnly(true)
    }

    @Test
    void userSetLength(){
        assert varman.parse('${like()}', variableMap) == 'I LIKE BANANA.'
        assert varman.parse('${like}', variableMap) == 'I LIKE BANANA.'
        assert varman.parse('${like(1)}', variableMap) == 'I'
        assert varman.parse('${like(2)}', variableMap) == 'I '
        assert varman.parse('${like(16)}', variableMap) == 'I LIKE BANANA.  '
        assert varman.parse('${like(16).right(0)}', variableMap) == 'I LIKE BANANA.00'
        assert varman.parse('${like(16).left(0)}', variableMap) == '00I LIKE BANANA.'
        //- UserSetLength with variable
        assert varman.parse('${number1}', variableMap) == '1'
        assert varman.parse('${number2}', variableMap) == '2'
        assert varman.parse('${number3}', variableMap) == '3'
        assert varman.parse('${like(number1)}', variableMap) == 'I'
        assert varman.parse('${like(number4)}', variableMap) == 'I LI'
        assert varman.parse('${like(number2)} / ${like(number3)}', variableMap) == 'I  / I L'
        //
        assert varman.parse('${not.exist.value(5).left(0)}', variableMap)  == '00000'
        assert varman.parse('${nothing(5).left(0)}', variableMap)  == '00000'
        assert varman.parse('${not.exist.value(0).left(0)}', variableMap)  == ''
        assert varman.parse('${nothing(0).left(0)}', variableMap)  == ''
    }

    @Test
    void syntexTest(){
        println varman.parse('${random(3)}', variableMap)
        println varman.parse('${random()}', variableMap)
        println varman.parse('${random}', variableMap)
        println varman.parse('${date()}', variableMap)
        println varman.parse('${date}', variableMap)
        println varman.parse('${enter()}1${enter(2)}2${enter(3)}3', variableMap)
        println varman.parse('${enter}1${enter}2${enter}3', variableMap)

        println varman.parse('${my.date().dateformat(yyyy MM dd )}', variableMap)
        println varman.parse('${my.date().dateformat(yyyy MM dd, yy/MM/dd/HH/mm/ss)}', variableMap)
        println varman.parse('${my.date2().dateformat(yyyy/MM/dd)}', variableMap)
        println varman.parse('${my.date2().dateformat(yyyy/MM/dd HH:mm:ss)}', variableMap)
        println varman.parse('${my.date2().dateformat(yyyy/MM/dd HH:mm:ss, yy/MM/dd/HH/mm/ss)}', variableMap)
        println varman.parse('${my.date2().dateformat("yyyy/MM/dd HH:mm:ss", "yy/MM/dd/HH/mm/ss")}', variableMap)

        assert varman.parse('${random(3)}', variableMap).length() == 3
    }

    @Test
    void systemVariableTest() {
        // Test - left() and right()
        println '_USER.NAME: ' + varman.parse('${_user.name}', variableMap)
        println '_USER.DIR: ' + varman.parse('${_user.dir}', variableMap)
        println '_USER.HOME: ' + varman.parse('${_user.home}', variableMap)
        println '_OS.NAME: ' + varman.parse('${_os.name}', variableMap)
        println '_OS.VERSION: ' + varman.parse('${_os.version}', variableMap)
        println '_JAVA.VERSION: ' + varman.parse('${_java.version}', variableMap)
        println '_JAVA.HOME: ' + varman.parse('${_java.home}', variableMap)
        println '_HOSTNAME: ' + varman.parse('${_hostname}', variableMap)
        println '_IP: ' + varman.parse('${_ip}', variableMap)
        println '_LIB.DIR: ' + varman.parse('${_lib.dir}', variableMap)
        println '_LIB.PATH: ' + varman.parse('${_lib.path}', variableMap)
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
        assert varman.parse('${installer.level.1.file.path().add(VariableMan)}', variableMap) == '/foo/barVariableMan'
        assert varman.parse('${installer.level.1.file.path().add(I\'m VariableMan)}', variableMap) == '/foo/barI\'m VariableMan'
        assert varman.parse('${installer.level.1.file.path().addBefore(I\'m VariableMan)}', variableMap) == 'I\'m VariableMan/foo/bar'
        assert varman.parse('${installer.level.1.file.path().add( enter )}', variableMap) == '/foo/bar\n'
        assert varman.parse('${installer.level.1.file.path().add( . )}', variableMap) == '/foo/bar.'
        assert varman.parse('${installer.level.1.file.path().add( . )}', variableMap) == '/foo/bar.'

        assert varman.parse('${installer.level.1.file.path().add( "," )}', variableMap) == '/foo/bar,'
        assert varman.parse('${installer.level.1.file.path().add(",")}', variableMap) == '/foo/bar,'
        assert varman.parse('${installer.level.1.file.path().add(" GRANT SELECT, INSERT, UPDATE, DELETE ON ").add(tableName).add(" TO RR_").add(tableName).add(_WW)}', variableMap) == '/foo/bar GRANT SELECT, INSERT, UPDATE, DELETE ON TBHOHO0012 TO RR_TBHOHO0012_WW'
    }

    @Test
    void existFunction(){
        // Test - replace
        assert varman.parse('Hello ${nothing().exist().add(VariableMan)}', variableMap) == 'Hello '
        assert varman.parse('Hello ${nothing().exist().add(VariableMan)}', variableMap) == 'Hello '
        assert varman.parse('Hello ${nothing().exist().addBefore(VariableMan)}', variableMap) == 'Hello '

        assert varman.parse('Hello ${nothing().add(VariableMan)}', variableMap) == 'Hello VariableMan'
        assert varman.parse('Hello ${nothing().add(Variable . Man)}', variableMap) == 'Hello Variable . Man'
        assert varman.parse('Hello ${nothing().addBefore( . Variable . Man . )}', variableMap) == 'Hello . Variable . Man .'
        assert varman.parse('Hello ${nothing().addBefore(" . Variable . Man . ")}', variableMap) == 'Hello  . Variable . Man . '

        assert varman.parse('Hello ${hello().add(" VariableMan ")}', variableMap) == 'Hello hello VariableMan '
        assert varman.parse('Hello ${hello().add(" VariableMan ")}', variableMap) == 'Hello hello VariableMan '
        assert varman.parse('Hello ${hello().addBefore(" VariableMan ")}', variableMap) == 'Hello  VariableMan hello'
    }

    @Test
    void replaceFunction(){
        // Test - replace
        assert varman.parse('${installer.level.1.file.path(3).replace(f,o)}', variableMap) == '/oo'
        assert varman.parse('${installer.level.1.file.path(3).replace(/,o)}', variableMap) == 'ofo'
        assert varman.parse('${installer.level.1.file.path(3).replace(/,o).replace(f,o)}', variableMap) == 'ooo'

        // Test - replace
        assert varman.parse('${tab().replace("\t","")}', variableMap) == 'hellotab1tab3tab4'
        assert varman.parse('${tab().replace("\t"," ")}', variableMap) == 'hello tab1   tab3    tab4'

        // Test - replace_regex
        assert varman.parse('${installer.level.1.file.path().replaceAll(/,o)}', variableMap) == 'ofooobar'
        assert varman.parse('${installer.level.1.file.path().replaceAll(^/,"s")}', variableMap) == 'sfoo/bar'
//        assert varman.parse('${installer.level.1.file.path().replace_regex([/]$,s)}', variableMap) == '/foosbar'
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
        assert "application-site-op.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml', variableMap)
        assert "application-site-op.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml', variableMap)

        //- not exists
        assert "application-site-.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml', variableMap)
        assert "application-site-.yml" == varman.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml', variableMap)
    }


    @Test
    void "Properties 스타일 Variable로 변환"(){
        // Exist Variable
        assert "Hello, i am foo.bar" == varman.parse('Hello, ${foo.bar}', variableMap)
        assert "Hello, i am foo.var" == varman.parse('Hello, ${foo.var}', variableMap)
        assert "Hello, i am foofoo.bar" == varman.parse('Hello, ${foofoo.bar}', variableMap)
        assert "Hello, i am foofoo.barbar.var" == varman.parse('Hello, ${foofoo.barbar.var}', variableMap)
        assert "Hello, i am foofoo.barbar.varvar.end" == varman.parse('Hello, ${foofoo.barbar.varvar.end}', variableMap)

        // No Exist Variable
        assert "Hello, " == varman.parse('Hello, ${foo.no.bar}', variableMap)
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
        def parsedObject = varman.parse(machingParameter, variableMap)
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
        VariableMan varman = new VariableMan('EUC-KR')
        Map<String, Object> variableMap = [
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
        ]

        varman.setModeExistFunctionOnly(false)

        // Test empty string
        varman.parsedDataList('', variableMap) == ""

        // Test - left() and right()
        list = varman.parsedDataList('${USERNAME(8).left(jjjj)}asfd', variableMap)
        list = varman.parsedDataList('${USERNAME()}asfd', variableMap)
        list = varman.parsedDataList('${USERNAME}asfd', variableMap)
        list = varman.parsedDataList('${USERNAME(8)}asfd', variableMap)
        list = varman.parsedDataList('${USERNAME(8).right()}asfd', variableMap)
        list = varman.parsedDataList('${USERNAME(14).right()}asfd', variableMap)

        list = varman.parsedDataList('${USERNAME(14).nvl()}asfd', variableMap)
        assert list[0].hasFunc('nvl')
        assert list[0].getMember('nvl') == [""] as String[]
        assert list[0].getMember('nvl', 0) == ""
        assert list[0].getMember('nonefunc') == null
        assert list[0].getMember('nonefunc', 0) == null

        list = varman.parsedDataList('${USERNAME(14).nonefunc(dddd)}asfd', variableMap)
        assert list[0].hasFunc('nonefunc')
        assert list[0].getMember('nonefunc') == ['dddd'] as String[]
        assert list[0].getMember('nonefunc', 0) == 'dddd'
    }

    @Test
    void signChange(){
        VariableMan varman = new VariableMan()
        Map<String, Object> variableMap = [
                'a1': 'a',
                'a2': 'aa',
                'a3': 'aaa',
        ]
        varman.setVariableSign('#')
        assert varman.parse('${a1} ${a2} ${a3}', variableMap) == '${a1} ${a2} ${a3}'
        assert varman.parse('#{a1} #{a2} #{a3}', variableMap) == 'a aa aaa'
        assert varman.parse('#{a1} ${a2} #{a3}', variableMap) == 'a ${a2} aaa'

        varman.setVariableSign('%')
        assert varman.parse('${a1} ${a2} ${a3}', variableMap) == '${a1} ${a2} ${a3}'
        assert varman.parse('%{a1} %{a2} %{a3}', variableMap) == 'a aa aaa'
        assert varman.parse('%{a1} ${a2} %{a3}', variableMap) == 'a ${a2} aaa'

        varman.setVariableSign('')
        assert varman.parse('${a1} ${a2} ${a3}', variableMap) == '$a $aa $aaa'
        assert varman.parse('#{a1} #{a2} #{a3}', variableMap) == '#a #aa #aaa'
        assert varman.parse('#{a1} ${a2} #{a3}', variableMap) == '#a $aa #aaa'
        assert varman.parse('{a1} {a2} {a3}', variableMap) == 'a aa aaa'
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
        println variableMan.parseString('${b().split-join(/)}', variableMap)
        println variableMan.parseString('${c().split-join(/)}', variableMap)

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
        assert "application-site-op.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml', variableMap)
        assert "application-site-op.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ ', "system")}.yml', variableMap)

        //- not exists
        assert "application-site-.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml', variableMap)
        assert "application-site-.yml" == variableMan.parse('application-site-${prop(' +testFile.getPath()+ '.temp, "system")}.yml', variableMap)
    }



}