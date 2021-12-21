package jaemisseo.man

import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

class VariableManTest_stream {

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
    void test_listValue(){
        println varman.parse('test__${listTest}__test')
        assert varman.parse('-t ${listTest().join(" -t ")} _test') == '-t 1 -t 2 -t 3 -t 4 -t 111 -t 2222 -t 1155 -t Hehehe _test'
    }

    @Test
    void stream_join(){
        String result = "";
        result = varman.parse('123 ${stream(listTest).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} 321')
        assert result == '123 <a href="#/search?search=%231">#1</a>, <a href="#/search?search=%232">#2</a>, <a href="#/search?search=%233">#3</a>, <a href="#/search?search=%234">#4</a>, <a href="#/search?search=%23111">#111</a>, <a href="#/search?search=%232222">#2222</a>, <a href="#/search?search=%231155">#1155</a>, <a href="#/search?search=%23Hehehe">#Hehehe</a> 321'

        result = varman.parse('456 ${listTest().stream().add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} 654')
        assert result == '456 <a href="#/search?search=%231">#1</a>, <a href="#/search?search=%232">#2</a>, <a href="#/search?search=%233">#3</a>, <a href="#/search?search=%234">#4</a>, <a href="#/search?search=%23111">#111</a>, <a href="#/search?search=%232222">#2222</a>, <a href="#/search?search=%231155">#1155</a>, <a href="#/search?search=%23Hehehe">#Hehehe</a> 654'
    }

    @Test
    void stream_join_null(){
        assert '123  321' == varman.parse('123 ${stream(null_value).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} 321')
        assert '123  321' == varman.parse('123 ${stream(null_value).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join("")} 321')
        assert '' == varman.parse('${null_value().stream().add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")}')
    }

    @Test
    void stream_join_emptyList(){
        assert '123  321' == varman.parse('123 ${stream(listEmptyTest).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} 321')
        assert '123  321' == varman.parse('123 ${stream(listEmptyTest).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join("")} 321')
        assert '' == varman.parse('${listEmptyTest().stream().add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")}')
    }

    @Test
    void stream_join_with_if(){
        String result = "";

        //Just if
        result = varman.parse('^^ ${stream(nullOneNullTwoList).if( it ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
        assert result == '^^ , <a href="#/search?search=%231">#1</a>, , <a href="#/search?search=%232">#2</a> ^^'

        //Just if2
        result = varman.parse('^^ ${stream(nullOneNullTwoList).add("HI").if( it ).add("A").add(it).add("B").endstream().join(", ")} ^^')
        assert result == '^^ HI, HIA1B, HI, HIA2B ^^'

        //Just if3
        result = varman.parse('^^ ${stream(nullOneNullTwoList).add("HI").if( it == 1 ).add("A").add(it).else().add(it).add("B").add("C").add("D").endstream().join(", ")} ^^')
        assert result == '^^ HIitBCD, HIA1, HIitBCD, HI2BCD ^^'

        //Just if4
        result = varman.parse('^^ ${stream(nullOneNullTwoList).add("HI").if(it == 1).add("A").add(it).elseif(it == 2).add(it).add("B").else().add(it).add("C").endif().add(it).add("Z").endstream().join(", ")} ^^')
        assert result == '^^ HIitCitZ, HIA11Z, HIitCitZ, HI2B2Z ^^'
    }

    @Test
    void stream_join_with_filter(){
        String result = "";

        //Filter List
        result = varman.parse('^^ ${stream(nullOneNullTwoList).filter( it ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
        assert result == '^^ <a href="#/search?search=%231">#1</a>, <a href="#/search?search=%232">#2</a> ^^'

        //Filter List: it == 1
        result = varman.parse('^^ ${stream(nullOneNullTwoList).filter( it == 1 ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
        assert result == '^^ <a href="#/search?search=%231">#1</a> ^^'

        //Filter List: it == 2
        result = varman.parse('^^ ${stream(nullOneNullTwoList).filter( it == 2 ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
        assert result == '^^ <a href="#/search?search=%232">#2</a> ^^'

        //Filter List<Map>

        //Filter List<Map>: [].a == 1

        //Filter List<Map>: [].b == 2


        //Filter Map

        //Filter Map: it.key == 1

        //Filter Map: it.value == 2
    }

//    @Test
//    void stream_join_with_sorted(){
//        String result = "";
//
//        //Sort List
//        result = varman.parse('^^ ${stream(nullOneNullTwoList).filter( it ).sorted( a > b ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
//        assert result == '^^ <a href="#/search?search=%231">#1</a>, <a href="#/search?search=%232">#2</a> ^^'
//
//        //Just List: it == 1
//        result = varman.parse('^^ ${stream(nullOneNullTwoList).filter( it == 1 ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
//        assert result == '^^ <a href="#/search?search=%231">#1</a> ^^'
//
//        //Just List: it == 2
//        result = varman.parse('^^ ${stream(nullOneNullTwoList).filter( it == 2 ).add("<a href="#/search?search=%23").add(it).add("\"").add(">#").add(it).add("</a>").endstream().join(", ")} ^^')
//        assert result == '^^ <a href="#/search?search=%232">#2</a> ^^'
//    }

    @Test
    void stream_virtual_area(){
        //TODO: Develop virtual grammar
        assert 1 == 1
//        assert varman.parse('${stream(listTest)}<a href="#/search?search=%23${it}">#${it}</a>${/endstream().join(", ")},')
//                == '<a href="#/search?search=%231">#1</a>, <a href="#/search?search=%232">#2</a>, <a href="#/search?search=%233">#3</a>, <a href="#/search?search=%234">#4</a>, <a href="#/search?search=%23111">#111</a>, <a href="#/search?search=%232222">#2222</a>, <a href="#/search?search=%231155">#1155</a>, <a href="#/search?search=%23Hehehe _test">#Hehehe _test</a>, '
    }

}