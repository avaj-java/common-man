package jaemisseo.man

import org.junit.Before
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 16. 11. 14
 * Time: 오후 7:08
 * To change this template use File | Settings | File Templates.
 */
class VariableManTest {

    String codeRuleH1 = '${biz}${seq(4).left(0).error(over)}'
    String codeRuleH2 = '${biz}${class}${seq(3).left(0).error(over)}'
    String codeRuleS = '${biz(4).right(0)}${class(1)}${seq(4).left(0).error(over)}'

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
                search: "HELLO SEARCH !!!"
        ])
//        .setModeDebug(true)
    }

    @Test
    void "간단 테스트"() {

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

}