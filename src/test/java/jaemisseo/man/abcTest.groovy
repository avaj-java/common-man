package jaemisseo.man

import org.junit.Test

import java.text.SimpleDateFormat

/**
 * Created by sujkim on 2017-02-26.
 */
class abcTest {


    @Test
    void "hello Test"(){
    }

    @Test
    void "test temp"(){
        List<String> funcs = ['d','s','j','k(','i']
        int funcStartIndex = funcs.findIndexOf{ it.indexOf('(') != -1 }
        String variable = funcs[0..funcStartIndex-1].join('.')
        List list = [variable] + funcs[funcStartIndex..funcs.size()-1]
        println list

    }

    @Test
    void "date format"(){
        Date date = new Date()

        String javaFormated = date.format('yyyyMMddHHmmssSSS')
        String groovyFormated = new SimpleDateFormat('yyyyMMddHHmmssSSS').format(date)

        assert javaFormated == groovyFormated
    }


    @Test
    void "groovy range"(){
        assert (14..20).collect{ it } == [14, 15, 16, 17, 18, 19, 20]
        assert ('f4'..'f9').collect{ it } == ['f4', 'f5', 'f6', 'f7', 'f8', 'f9']
    }
   
}
