package jaemisseo.man

import jaemisseo.man.bean.ManColTestBean
import jaemisseo.man.bean.ManTestBean
import jaemisseo.man.util.ConnectionGenerator
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.sql.Connection

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/13/16
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
class QueryManTest {

    String selectQuery
    String insertQuery
    String updateQuery
    String deleteQuery

    Connection conn
    Connection conn2

    Map dbInfo
    Map dbInfo2

    Closure closure(){
        Closure closure = { QueryMan qman ->
            String tableName = qman.tableName
            String command = qman.command
            Map resultMap = qman.resultMap

            if (tableName.equals('META_OBJECT')) {
                if (command.equals("insert")) {
                    // 1. Required Column AutoSet
                    qman.addAttributeByColumnNames(['OBJECT_ID', 'CLASS_ID', 'MUSR', 'CUSR', 'CREATE_DT', 'MODIFY_DT'])
                    // 2. Value AutoSet
                    qman.setValueByColumnName("CREATE_DT", "SYSDATE")
                    qman.setValueByColumnName("MODIFY_DT", "SYSDATE")
                    // 3. Batch Closure By One Step
                    qman.setBatchClosureByOneStep { param ->
                        resultMap.findAll { it.value.equals("OBJECT_ID") }.each {
                            param["${it.key}"] = getmaxidOftable.getmaxid(IdGenerator._METAOBJECT)
                        }
                    }

                    //UPDATE할 경우 자동 값
                } else if (command.equals("update")) {
                    // 1. Required Column AutoSet
                    qman.addAttributeByColumnNames(['MUSR', 'MODIFY_DT'])
                    // 2. Value AutoSet
                    qman.setValueByColumnName("MODIFY_DT", "SYSDATE")
                }
            } else if (tableName.equals('META_DEVELOPER')) {
                if (command.equals("insert")) {
                    // 1. Required Column AutoSet
                    qman.addAttributeByColumnNames(['CREATE_DT'])
                    // 2. Value AutoSet
                    qman.setValueByColumnName("OBJECT_ID", "(select max(nvl(OBJECT_ID, 0)) +1 from meta_developer)")
                    qman.setValueByColumnName("CREATE_DT", "SYSDATE")
                }
            }
        }
        return closure
    }

    @Before
    void init(){
        selectQuery = 'SELECT * from insa_jb_dqm'
        insertQuery = "insert into insa_jb_dqm values( 'QueryMan_TEST', 'QueryMan_TEST', 'QueryMan_TEST', 'QueryMan_TEST', 'QueryMan_TEST@mail.com', 010-1234-5678, null, null)"
        updateQuery = "update insa_jb_dqm set email = 'QueryMan_TEST@mail.com' where swbeonho='new1' and name='뉴사용자1' "
        deleteQuery = "delete from insa_jb_dqm where swbeonho='new1' and name='뉴사용자1'"

//        dbInfo = [ip:'127.0.0.1', db:'orcl', id:'tester', pw:'tester']
//        dbInfo2 = [ip:'127.0.0.1', db:'orcl', id:'tester2', pw:'tester2']

        dbInfo = [ip:'192.168.0.158', db:'da', id:'spusr', pw:'spusr']
        dbInfo2 = [ip:'192.168.0.158', db:'da', id:'spusr', pw:'spusr']

        ConnectionGenerator connGen = new ConnectionGenerator()
        conn = connGen.generate(dbInfo)
        conn2 = connGen.generate(dbInfo2)
    }

    @After
    void after(){
    }

    /**
     * LOG TITLE
     */
    void printTitle(String title){
        println """
        ==================================================
        = ${title}
        =================================================="""
    }



    /*************************
     * SELECT & INSERT & UPDATE
     *************************/
    @Test
    void select(){
        printTitle 'SelectString'
        new QueryMan(dbInfo, 'SELECT * from tab').selectString()
        new QueryMan(dbInfo, 'SELECT * from tab').selectString('TNAME')

        printTitle 'SelectInteger'
        new QueryMan(dbInfo, 'SELECT 1,2,3 from dual').selectInteger()
        new QueryMan(dbInfo, 'SELECT 1,2,3 from tab').selectInteger('3')

        printTitle 'SelectList'
        new QueryMan(dbInfo).selectList(new ManTestBean())
        new QueryMan(dbInfo, 'SELECT * from tab').selectList()

        printTitle 'SelectMap'
        new QueryMan(dbInfo).selectMap(new ManTestBean())
        new QueryMan(dbInfo, 'SELECT * from tab').selectMap()
        new QueryMan(dbInfo, 'SELECT * from tab').selectMap('TNAME')
        new QueryMan(dbInfo, 'SELECT * from tab').setResultMap(['tn':'TNAME']).selectMap('tn')

        printTitle 'Count'
        new QueryMan(dbInfo, 'SELECT * from tab').selectCount()

        printTitle 'SelectColumnNameList'
        new QueryMan(dbInfo, 'SELECT * from tab').selectColumnNameList()

    }

    @Test
    void joinQuery(){
        printTitle 'SelectString'
        new QueryMan(dbInfo(), ManTestBean)
                .setJoin(ManColTestBean)
                .setTopRank(['tableName', 'classId', 'propertyId', 'columnName'],'createDt')
                .setOrder(['groupName','classId','className','DECODE(PROPERTY_ID, null, 0, PROPERTY_ID)', 'columnId'])
                .selectList()
    }

    @Test
    void insert(){
        new QueryMan(dbInfo2, selectQuery).selectList()
        new QueryMan(dbInfo2, insertQuery).insert()
    }

    @Test
    void update(){
        new QueryMan(dbInfo2, selectQuery).selectList()
        new QueryMan(dbInfo2, updateQuery).update()
    }

    @Test
    void delete(){
        new QueryMan(dbInfo2, selectQuery).selectList()
        new QueryMan(dbInfo2, deleteQuery).delete()
    }



    /*************************
     * BATCH
     *************************/
    @Test
    void insertBatch(){
        new QueryMan(dbInfo2, selectQuery).selectList()
        new QueryMan(dbInfo2, insertQuery).insertBatch( [ new ManTestBean(), new ManTestBean(), new ManTestBean() ] )
        new QueryMan(dbInfo2, selectQuery).selectList()
    }



    /*************************
     * TRANSACTION
     *************************/
    @Test
    void transaction(){
        //Same Connection => Same Pipe
        //No Connection => Recent Connection
        new QueryMan().transaction{ QueryMan qman ->
            printTitle 'SELECT'
            qman.init(conn, selectQuery).selectList()

            printTitle 'INSERT'
            qman.init(conn, insertQuery).insert()
            qman.init(true, selectQuery).selectList()

            printTitle 'UPDATE'
            qman.init(true, updateQuery).update()
            qman.init(conn, selectQuery).selectList()

            printTitle 'DELETE'
            qman.init(conn, deleteQuery).delete()
            qman.init(conn, selectQuery).selectList()
        }
    }

    @Test
    void transactionDefaultConn(){
        printTitle 'Transaction Type1'
        new QueryMan(dbInfo).transaction{ QueryMan qman ->
            println "\n\n///// Default"
            qman.init(true, selectQuery).selectList()

            qman.init(true, insertQuery).insert()
            qman.init(true, insertQuery).insert()
            qman.init(true, selectQuery).selectList()

            qman.init(true, updateQuery).update()
            println "\n\n///// Other"
            qman.init(dbInfo2, selectQuery).selectList()

            println "\n\n///// Default"
            qman.init(true, selectQuery).selectList()
        }

        printTitle 'Transaction Type2'
        QueryMan.transaction(dbInfo){ QueryMan qman ->
            println "\n\n///// Default"
            qman.init(true, selectQuery).selectList()

            qman.init(true, insertQuery).insert()
            qman.init(true, insertQuery).insert()
            qman.init(true, selectQuery).selectList()

            qman.init(true, updateQuery).update()
            println "\n\n///// Other"
            qman.init(dbInfo2, selectQuery).selectList()

            println "\n\n///// Default"
            qman.init(true, selectQuery).selectList()
        }
    }

    @Test
    void transactionConnPool(){
        printTitle 'Transaction Type1'
        new QueryMan().setConnPool([db1:conn, db2:conn2]).transaction{ QueryMan qman ->
            printTitle "\n\n///// db1"
            qman.init('db1', selectQuery).selectList()

            qman.init('db1', insertQuery).insert()
            qman.init('db1', insertQuery).insert()
            qman.init('db1', selectQuery).selectList()

            qman.init('db1', updateQuery).update()
            println "\n\n///// db2"
            qman.init('db2', selectQuery).selectList()

            println "\n\n///// db1"
            qman.init('db1', selectQuery).selectList()
        }

        printTitle 'Transaction Type2'
        QueryMan.transaction([db1:conn, db2:conn2]){ QueryMan qman ->
            printTitle "db1"
            qman.init('db1', selectQuery).selectList()

            qman.init('db1', insertQuery).insert()
            qman.init('db1', insertQuery).insert()
            qman.init('db1', selectQuery).selectList()

            qman.init('db1', updateQuery).update()
            printTitle "db2"
            qman.init('db2', selectQuery).selectList()

            printTitle "db1"
            qman.init('db1', selectQuery).selectList()
        }
    }

    @Test
    void transactionBatch(){

//        new QueryMan().init(conn).createTable( new ManTestBean() )

        printTitle 'Transaction Type1'
        new QueryMan().setConnPool([db1:conn, db2:conn2]).transaction{ QueryMan qman ->
            printTitle "db1"
            qman.init('db1', selectQuery).selectList()

            List paramList = []
            (1..10000).each{
                paramList << new ManTestBean(columnName:"hi${it}")
            }
            qman.init('db1').insertBatch( paramList )
            qman.init('db1', ManTestBean).selectList()

            qman.init('db1', updateQuery).update()

            printTitle "db2"
            qman.init('db2', selectQuery).selectList()

            printTitle "db1"
            qman.init('db1', selectQuery).selectList()
        }
    }


}