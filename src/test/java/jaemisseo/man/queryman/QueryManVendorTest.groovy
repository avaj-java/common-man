package jaemisseo.man.queryman

import jaemisseo.man.QueryMan
import jaemisseo.man.util.ConnectionGenerator
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.sql.Connection

class QueryManVendorTest {

    @Before
    void init(){
    }

    Connection conn(String vendor){
        switch (vendor?.toUpperCase()){
            case 'MYSQL':
                return generateConnection([
                        vendor: 'mysql',
                        ip: '127.0.0.1',
                        port: 3306,
                        db: 'mysql',
                        user: 'root',
                        password: 'mysql'
                ])
                break
            default:
                return generateConnection([
                        vendor: 'oracle',
                        ip: '127.0.0.1',
                        port: 1521,
                        db: 'orcl',
                        user: 'system',
                        password: 'oracle'
                ])
                break
        }
    }

    Connection generateConnection(Map dbInfo){
        ConnectionGenerator connGen = new ConnectionGenerator(dbInfo)
        return connGen.generate()
    }



    @Test
    @Ignore
    void mainTest(){
        String queryForOracle = """
        SELECT 1 AS ONE, 2 AS TWO, 3 AS THREE FROM DUAL 
        UNION ALL 
        SELECT 11 AS ONE, 12 AS TWO, 13 AS THREE FROM DUAL 
        UNION ALL 
        SELECT 21 AS ONE, 22 AS TWO, 23 AS THREE FROM DUAL
        """
        String queryForMySql = """
        SELECT 1 AS ONE, 2 AS TWO, 3 AS THREE  
        UNION ALL 
        SELECT 11 AS ONE, 12 AS TWO, 13 AS THREE 
        UNION ALL 
        SELECT 21 AS ONE, 22 AS TWO, 23 AS THREE
        """

        ['ORACLE', 'MYSQL'].each { String vendor ->
            String query
            if (vendor == 'ORACLE')
                query =  queryForOracle
            else if (vendor == 'MYSQL')
                query =  queryForMySql

            //All
            def result1 = new QueryMan(conn(vendor), query).selectList()
            assert result1.size() == 3

            //Pagination
            def result2 = new QueryMan(conn(vendor), query).setPage(1, 1).selectList()
            assert result2.size() == 1

            //Count
            Long resultCount = new QueryMan(conn(vendor), query).selectCount()
            assert resultCount == 3

            //Min
            Long result3 = new QueryMan(conn(vendor), query).selectMin('one')
            Long result3_1= new QueryMan(conn(vendor), query).selectMin('two')
            Long result3_2 = new QueryMan(conn(vendor), query).selectMin('three')
            assert result3 == 1
            assert result3_1 == 2
            assert result3_2 == 3

            //Max
            Long result4 = new QueryMan(conn(vendor), query).selectMax('one')
            Long result4_1= new QueryMan(conn(vendor), query).selectMax('two')
            Long result4_2 = new QueryMan(conn(vendor), query).selectMax('three')
            assert result4 == 21
            assert result4_1 == 22
            assert result4_2 == 23

            println result2
        }
    }

}
