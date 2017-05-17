package jaemisseo.man

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


    @Test
    void "간단 테스트"() {

        //VariableMan 생성
        VariableMan varman = new VariableMan('EUC-KR', [
                USERNAME                     : "하이하이하이",
                lowerChar                    : "hi everybody",
                upperChar                    : "HI EVERYBODY",
                syntaxTest                   : 'HI ${}EVERYBODY${}',
                s                            : '하하하\\n하하하',
                s2                           : 'ㅋㅋㅋ\nㅋㅋl',
                num                          : '010-9911-0321',
                'installer.level.1.file.path': '/foo/bar',

        ])
//        .setModeDebug(true)

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

        assert varman.parse('${installer.level.1.file.path}') == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path()}') == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path(3)}') == '/fo'

        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(yyyy-MM-dd HH:mm:ssSSS)} ${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SSS)}${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SS)}${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(S)}${}')
        // Test - random (내장된변수)
        println varman.parse('[${date(yyyyMMddHHmmssSSS)}${random(5)}]')

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