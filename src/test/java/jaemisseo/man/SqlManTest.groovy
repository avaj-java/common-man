package jaemisseo.man

import jaemisseo.man.util.SqlSetup
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Created by sujkim on 2017-06-08.
 */
class SqlManTest {

    @Before
    void init(){
    }

    @After
    void after(){
    }

//    static void main(String[] args) {
//        new SqlManTest().simpleTest()
//    }

    @Test
    @Ignore
    void simpleTest(){
        SqlMan sqlman = new SqlMan()
        String filePath = ''
        SqlSetup sqlSetup = new SqlSetup(ip:'localhost', port:'1521', vendor:'oracle', db:'orcl', user:'META_SANGBEOM', password:'META_SANGBEOM')

        sqlman.init()
                .queryFromFile(filePath)
                .command([SqlMan.ALL])
                .replace(sqlSetup)
                .checkBefore(sqlSetup)

    }


}
