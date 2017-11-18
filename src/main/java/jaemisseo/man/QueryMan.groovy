package jaemisseo.man

import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import jaemisseo.man.annotation.*
import oracle.sql.TIMESTAMP
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Array
import java.lang.reflect.Field
import java.sql.Clob
import java.sql.Connection

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 16. 6. 12
 * Time: 오후 1:40
 * To change this template use File | Settings | File Templates.
 *
 */
class QueryMan {

    //Logger
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //Package
    private String packagePath = getClass().getName()

    static final String DESC = "DESC"
    static final String ASC = "ASC"

    class ConnectionGenerator{

        ConnectionGenerator(){}

        ConnectionGenerator(Map<String, String> map){
            setDatasource(map)
        }

        static final String ORACLE = "ORACLE"
        static final String TIBERO = "TIBERO"
//        static final String SYBASE_IQ = "SYBASE_IQ"
//        static final String SYBASE_ASE = "SYBASE_ASE"
//        static final String MYSQL = "MYSQL"
//        static final String DB2 = "DB2"


        String vendor, id, pw, ip, port, db, url, driver

        ConnectionGenerator init(){
            vendor=null; id=null; pw=null; ip=null; port=null; db=null; url=null; driver=null;
            return this
        }

        ConnectionGenerator setDatasource(Map map){
            vendor  = map['vendor']
            id      = map['id']
            pw      = map['pw']
            ip      = map['ip']
            port    = map['port']
            db      = map['db']
            url     = map['url']
            driver  = map['driver']
            return this
        }

        Map<String, String> generateDataBaseInfoMap(){
            Map o = [
                vendor  : vendor ?: ORACLE,
                id      : id,
                pw      : pw,
                ip      : ip ?: "127.0.0.1",
                port    : port ?: "1521",
                db      : db ?: "orcl",
            ]
            o['url']    = url ?: "${getURLProtocol(o.vendor)}@${o.ip}:${o.port}:${o.db}"
            o['driver'] = driver ?: getDriverName(o.vendor)
            return o
        }

        Connection generate(Map map){
            setDatasource(map)
            return generate()
        }

        Connection generate(){
            Sql sql
            Map<String, String> m = generateDataBaseInfoMap()
            sql = Sql.newInstance(m.url, m.id, m.pw, m.driver)
            return sql.getConnection()
        }

        String getDriverName(String vendor){
            vendor = (vendor) ?: ORACLE
            vendor = vendor?.toUpperCase()
            String driver = ''
            //Get By Vendor
            if (vendor.equals(ORACLE)) driver = 'oracle.jdbc.driver.OracleDriver'
            else if (vendor.equals(TIBERO)) driver = 'com.tmax.tibero.jdbc.TbDriver'
            return driver
        }

        String getURLProtocol(String vendor){
            vendor = (vendor) ?: ORACLE
            vendor = vendor?.toUpperCase()
            String URLProtocol = ''
            //Get By Vendor
            if (vendor.equals(ORACLE)) URLProtocol = 'jdbc:oracle:thin:'
            else if (vendor.equals(TIBERO)) URLProtocol = 'jdbc:tibero:thin:'
            return URLProtocol
        }

    }


    //No Clearable
    Connection recentConn
    Connection defaultConn
    Map<String, Sql> sqlMap = [:]
    Map<String, Sql> sqlMapForCloseAfter = [:]
    Map<String, Connection> connMap = [:]
    Map<String, Connection> connPoolMap
    boolean modeTransaction
    boolean modeAutoClose = true
    boolean modeJUnitTest
    Closure autoMetaValueClosure
    Closure batchClosureByOneStep


    //Clearable
    Sql sql
    Connection conn
    String connectionName
    String vendor
    int batchSize

    Map resultMap = [:]
    String tableName
    List attribute = []
    List<Class> clazzListToJoin = []
    def where
    Map<String, String> orderAttributeMap = [:]

    Map columnSetupMap = [:]
    Map tableSetupMap = [:]
    Map typeMap = [:]
    Map replaceMap = [:]
    String command
    String query
    Class resultType
    String resultId

    Integer pageNum
    Integer pageSize

    boolean modeCloseAfter
    boolean modeMeta
    boolean modeCount
    boolean modeToJavaType
    boolean modeTopRank
    boolean modeAutoCreateTable
    boolean modeFlattenQueryLog = true
    List rankPkAttributes
    String rankOrderAttribute

    int createTableCount = 0




    /*************************
     * Constructors
     *************************/
    QueryMan(){
        init()
        checkTest()
    }
    QueryMan(String connectionName){
        init(connectionName)
        checkTest()
    }
    QueryMan(Connection conn){
        init(conn)
        checkTest()
    }
    QueryMan(Map dataBaseInfoMap){
        init(dataBaseInfoMap)
        checkTest()
    }

    QueryMan(String connectionName, Closure autoValueClosure){
        init(connectionName)
        setAutoMetaValue(autoValueClosure)
        checkTest()
    }
    QueryMan(Connection conn, Closure autoValueClosure){
        init(conn)
        setAutoMetaValue(autoValueClosure)
        checkTest()
    }
    QueryMan(Map dataBaseInfoMap, Closure autoValueClosure){
        init(dataBaseInfoMap)
        setAutoMetaValue(autoValueClosure)
        checkTest()
    }

    QueryMan(String connectionName, Class resultType){
        init(connectionName, resultType)
        checkTest()
    }
    QueryMan(Connection conn, Class resultType){
        init(conn, resultType)
        checkTest()
    }
    QueryMan(Map dataBaseInfoMap, Class resultType){
        init(dataBaseInfoMap, resultType)
        checkTest()
    }

    QueryMan(String connectionName, String query){
        init(connectionName, query)
        checkTest()
    }
    QueryMan(Connection conn, String query){
        init(conn, query)
        checkTest()
    }
    QueryMan(Map dataBaseInfoMap, String query){
        init(dataBaseInfoMap, query)
        checkTest()
    }



    /*************************
     * init
     *************************/
    QueryMan init(String connectionName){
        init()
        this.connectionName = connectionName
        return this
    }

    QueryMan init(boolean modeDefaultConnection, String query){
        init()
        setQuery(query)
        return this
    }

    QueryMan init(Connection conn){
        init()
        setConnection(conn)
        return this
    }

    QueryMan init(Class resultType){
        init()
        setResultType(resultType)
        return this
    }

    QueryMan init(Map dataBaseInfoMap){
        init()
        setConnection(dataBaseInfoMap)
        return this
    }

    QueryMan init(String connectionName, Class resultType){
        init()
        this.connectionName = connectionName
        setResultType(resultType)
        return this
    }

    QueryMan init(Connection conn, Class resultType){
        init()
        setConnection(conn)
        setResultType(resultType)
        return this
    }

    QueryMan init(Map dataBaseInfoMap, Class resultType){
        init()
        setConnection(dataBaseInfoMap)
        setResultType(resultType)
        return this
    }

    QueryMan init(String connectionName, String query){
        init()
        this.connectionName = connectionName
        setQuery(query)
        return this
    }

    QueryMan init(Connection conn, String query){
        init()
        setConnection(conn)
        setQuery(query)
        return this
    }

    QueryMan init(Map dataBaseInfoMap, String query){
        init()
        setConnection(dataBaseInfoMap)
        setQuery(query)
        return this
    }

    QueryMan init(){
        sql = null
        conn = null
        connectionName = ''
        batchSize = 100

        tableName = ''
        attribute = []
        clazzListToJoin = []
        where = null
        orderAttributeMap = [:]

        command = ''
        query = ''
//        typeMap = [:]
//        columnSetupMap = [:]
//        tableSetupMap = [:]
        replaceMap = [:]
        resultMap = [:]
        resultType
        resultId = ''
        //Pagination
        pageNum = null
        pageSize = null

        modeCloseAfter = false
        modeMeta = false
        modeCount = false
        modeToJavaType = false
        modeTopRank = false
        modeAutoCreateTable = false
        createTableCount = 0
        return this
    }



    /*************************
     * JUnit Check => if now is on JUnit, Auto Rollback
     *************************/
    QueryMan checkTest(){
        //JUnit Check
        if (isJUnitTest())
            modeJUnitTest = true
        return this
    }



    QueryMan setSql(Sql sql){
        this.sql = sql
        return this
    }

    QueryMan setConnection(Connection conn){
        this.conn = conn
        return this
    }
    QueryMan setConnection(ConnectionGenerator connGen){
        this.conn = connGen.generate()
        return this
    }
    QueryMan setConnection(String vendor, String ip, String port, String db, String id, String pw){
        this.conn = new ConnectionGenerator(vendor:vendor, ip:ip, port:port, db:db, id:id, pw:pw).generate()
        return this
    }
    QueryMan setConnection(Map<String, String> dbInfoMap){
        this.conn = new ConnectionGenerator(dbInfoMap).generate()
        return this
    }



    /*************************
     * Connection Pool for Transaction
     *************************/
    QueryMan setConnectionName(String connectionName){
        this.connectionName = connectionName
        return this
    }

    QueryMan setConnPool(Map<String, Object> connPoolMap){
        this.connPoolMap = [:]
        return addConnPool(connPoolMap)
    }

    QueryMan addConnPool(String connectionName, Connection conn){
        this.connPoolMap[connectionName] = conn
        return this
    }

    QueryMan addConnPool(String connectionName, Map<String, String> dbInfoMap){
        this.connPoolMap[connectionName] = new ConnectionGenerator(dbInfoMap).generate()
        return this
    }

    QueryMan addConnPool(Map<String, Object> connPoolMap){
        //No Pool => New
        if (!this.connPoolMap)
            this.connPoolMap = [:]
        //Input New Connection into Pool
        connPoolMap.each{ connectionName, conn ->
            if (conn instanceof Connection)
                addConnPool(connectionName, conn as Connection)
            else if (conn instanceof Map)
                addConnPool(connectionName, conn as Map)

        }
        return this
    }

    QueryMan setDefaultConn(Connection conn){
        this.defaultConn = conn
        return this
    }

    QueryMan setDefaultConn(Map<String, String> dbInfoMap){
        this.defaultConn = new ConnectionGenerator(dbInfoMap).generate()
        return this
    }



    QueryMan setVendor(String vendor){
        this.vendor = vendor
        return this
    }

    QueryMan setCommand(String command){
        this.command = command
        return this
    }

    QueryMan setTableName(String tableName){
        this.tableName = tableName
        return this
    }

    QueryMan setAttribute(List attribute){
        this.attribute = attribute.unique()    // Unique Attribute
        return this
    }

    QueryMan setWhere(def where){
        this.where = where
        return this
    }

    /**
     * ex)
     * setOrder([
     *      'group': QueryMan.DESC,
     *      'name': QueryMan.ASC,
     *      'email': QueryMan.DESC
     * ])
     */
    QueryMan setOrder(Map orderAttributeMap){
        this.orderAttributeMap = orderAttributeMap
        return this
    }

    QueryMan setOrder(String orderAttributeName){
        orderAttributeMap[orderAttributeName] = ASC
        return this
    }

    QueryMan setOrder(List orderAttributeList){
        orderAttributeList.each{ orderAttributeMap[it] = ASC }
        return this
    }

    QueryMan setOrderDesc(String orderAttributeName){
        orderAttributeMap[orderAttributeName] = DESC
        return this
    }

    QueryMan setOrderDesc(List orderAttributeList){
        orderAttributeList.each{ orderAttributeMap[it] = DESC }
        return this
    }



    /**
     * You can input QUERY
     */
    QueryMan setQuery(def query){
        this.query = query
        return this
    }

    QueryMan setReplaceMap(Map paramReplaceMap){
        paramReplaceMap.each{
            replaceMap[it.key] = it.value
        }
        return this
    }

    /**
     * [ ATTRIBUTENAME : COLUMNNAME ]
     */
    QueryMan setResultMap(Map matchingMap){
        this.resultMap = matchingMap
        return this
    }

    QueryMan setResultType(Class dtoClazz){
        this.resultType = dtoClazz
        return this
    }

    QueryMan setResultId(String resultId){
        this.resultId = resultId
        return this
    }




    QueryMan setAutoMetaValue(Closure autoMetaValueClosure){
        this.autoMetaValueClosure = autoMetaValueClosure
        return this
    }

    QueryMan setModeAutoClose(boolean modeAutoClose){
        this.modeAutoClose = modeAutoClose
        return this
    }

    QueryMan setModeCloseAfter(boolean modeCloseAfter){
        this.modeCloseAfter = modeCloseAfter
        return this
    }

    QueryMan closeAfter(){
        return setModeCloseAfter(true)
    }

    QueryMan setModeMeta(boolean modeMeta){
        this.modeMeta = modeMeta
        return this
    }

    QueryMan setModeCount(boolean modeCount){
        this.modeCount = modeCount
        return this
    }

    QueryMan setModeToJavaType(boolean modeToJavaType){
        this.modeToJavaType = modeToJavaType
        return this
    }

    QueryMan setTopRank(List rankPkAttributes, String rankOrderAttribute){
        this.rankPkAttributes = rankPkAttributes
        this.rankOrderAttribute = rankOrderAttribute
        if (rankPkAttributes && rankOrderAttribute)
            this.modeTopRank = true
        return this
    }



    QueryMan setPage(Integer pageNum){
        this.pageNum = pageNum
        return this
    }

    QueryMan setPage(Integer pageNum, Integer pageSize){
        this.pageNum = pageNum
        this.pageSize = pageSize
        return this
    }

    QueryMan setPageSize(Integer pageSize){
        this.pageSize = pageSize
        return this
    }

    QueryMan setJoin(Class clazzToJoin){
        this.clazzListToJoin = [clazzToJoin]
        return this
    }
    QueryMan setJoin(List<Class> clazzListToJoin){
        this.clazzListToJoin = clazzListToJoin
        return this
    }
    QueryMan addJoin(Class clazzToJoin){
        this.clazzListToJoin << clazzToJoin
        return this
    }
    QueryMan addJoin(List<Class> clazzListToJoin ){
        this.clazzListToJoin.addAll( clazzListToJoin )
        return this
    }






    QueryMan addAttributeByColumnName(String columnName){
        resultMap.findAll{ it.value.equals(columnName) }.each{
            String attributeName = it.key
            setAttribute(attribute + [attributeName])
        }
        return this
    }

    QueryMan addAttributeByColumnNames(List<String> columnNameList){
        columnNameList.each{ String columnName ->
            addAttributeByColumnName(columnName)
        }
        return this
    }

    QueryMan setValueByColumnName(String columnName, String value){
        resultMap.findAll{ it.value.equals(columnName) }.each{
            setReplaceMap([ "${it.key}":value ])
        }
        return this
    }

    QueryMan setBatchClosureByOneStep(Closure batchClosureByOneStep){
        this.batchClosureByOneStep = batchClosureByOneStep
        return this
    }




    /** SELECT LIST **/
    List selectList(){
        return selectList([])
    }
    List selectList(def param){
        return selectList(param, getSelectDtoClosure())
    }
    List selectList(Closure closure){
        return selectList([], closure)
    }
    List selectList(def param, Closure closure){
        if (!resultType && !param && query)
            setResultType(HashMap)
        return setCommand('select').rows(param, closure, [])
    }

    /** SELECT MAP **/
    Map selectMap(){
        return selectMap([])
    }
    Map selectMap(String resultId){
        setResultId(resultId)
        return selectMap()
    }
    Map selectMap(def param){
        return selectMap(param, getSelectDtoClosure())
    }
    Map selectMap(def param, String resultId){
        setResultId(resultId)
        return selectMap(param, getSelectDtoClosure())
    }
    Map selectMap(Closure closure){
        return selectMap([], closure)
    }
    Map selectMap(def param, Closure closure){
        if (!resultType && !param && query)
            setResultType(HashMap)
        return setCommand('select').rows(param, closure, [:])
    }

    /** SELECT COUNT INTEGER **/
    Integer selectCount(){
        return selectCount([])
    }
    Integer selectCount(def param){
        return setModeCount(true).setCommand('select').rows(param, null, null)
    }

    /** SELECT STRING **/
    String selectString(){
        return selectString([])
    }
    String selectString(String resultId){
        setResultId(resultId)
        return selectString()
    }
    String selectString(def param, String resultId){
        setResultId(resultId)
        return selectString(param)
    }
    String selectString(def param){
        if (!resultType && !param && query)
            setResultType(String)
        return setCommand('select').rows(param, null, new String())
    }

    /** SELECT INTEGER **/
    Integer selectInteger(){
        return selectInteger([])
    }
    Integer selectInteger(String resultId){
        setResultId(resultId)
        return selectInteger()
    }
    Integer selectInteger(def param, String resultId){
        setResultId(resultId)
        return selectInteger(param)
    }
    Integer selectInteger(def param){
        if (!resultType && !param && query)
            setResultType(Integer)
        return setCommand('select').rows(param, null, new Integer(0))
    }

    /** SELECT META LIST **/
    List selectColumnNameList(){
        return selectColumnNameList([])
    }
    List selectColumnNameList(def param){
        if (!vendor)
            setVendor('ORACLE')
        return setModeMeta(true).selectList(param, {})
    }



    Closure getSelectDtoClosure(){
        Closure closure
        //CLOB To STRING
        if (modeToJavaType){
            closure = { row ->
                def rowDto = resultType.newInstance()
                attribute.each{ String propNm ->
                    def value = row[resultMap[propNm]]
                    if (value instanceof java.sql.Clob) {
                        rowDto[propNm] = getString(value as Clob)
                    }else if (value instanceof TIMESTAMP){
                        rowDto[propNm] = (value as TIMESTAMP).dateValue()
                    }else{
                        rowDto[propNm] = value
                    }
                }
                return rowDto
            }
        //NORMAL
        }else{
            closure = { row ->
                def rowDto = resultType.newInstance()
                attribute.each{ String propNm ->
                    rowDto[propNm] = row[resultMap[propNm]]
                }
                return rowDto
            }
        }
        return closure
    }

    Closure getMetaClosure(){
        Closure closure = { meta ->
            (1..meta.columnCount).each{
                String columnName = meta.getColumnName(it);
                resultMap[columnName] = columnName
            }
            attribute = (attribute) ?: resultMap.keySet().toList()
        }
        return closure
    }



    /** INSERT **/
    boolean insert(){
        insert([])
    }

    boolean insert(def param){
        return setCommand("insert").execute(param)
    }

    /** update **/
    boolean update(){
        update([])
    }

    boolean update(def param){
        return setCommand("update").execute(param)
    }

    /** DELETE **/
    boolean delete(){
        delete([])
    }

    boolean delete(def param){
        return setCommand("delete").execute(param)
    }

    /** INSERT (BATCH) **/
    int[] insertBatch(def param){
        return insertBatch(param, {})
    }
    int[] insertBatch(def param, Closure closure){
        return setCommand("insert").batch(param, closure)
    }

    /** UPDATE (BATCH) **/
    int[] updateBatch(def param){
        return updateBatch(param, {})
    }
    int[] updateBatch(def param, Closure closure){
        return setCommand("update").batch(param, closure)
    }

    /** INSERT (BATCH) **/
    int[] deleteBatch(def param){
        return deleteBatch(param, {})
    }
    int[] deleteBatch(def param, Closure closure){
        return setCommand("delete").batch(param, closure)
    }

    /** CREATE TABLE **/
    boolean createTable(def param){
        return setCommand("create").execute(param)
    }



    /**
     * TRANSACTION
     */
    static void transaction(Connection conn, Closure closure){
        transaction(conn, {}, closure)
    }

    static void transaction(Connection conn, Closure autoValueClosure, Closure closure){
        new QueryMan().setDefaultConn(conn).transaction{ QueryMan qman ->
            qman.setAutoMetaValue(autoValueClosure)
            closure(qman)
        }
    }

    static void transaction(Map<String, Object> poolOrDefaultMap, Closure closure){
        transaction(poolOrDefaultMap, {}, closure)
    }

    static void transaction(Map<String, Object> poolOrDefaultMap, Closure autoValueClosure, Closure closure){
        Map connPoolMap = poolOrDefaultMap.findAll{ it.value instanceof Map || it.value instanceof Connection }
        if (connPoolMap){
            new QueryMan().setConnPool(poolOrDefaultMap).transaction{ QueryMan qman ->
                qman.setAutoMetaValue(autoValueClosure)
                closure(qman)
            }
        }else{
            new QueryMan().setDefaultConn(poolOrDefaultMap).transaction{ QueryMan qman ->
                qman.setAutoMetaValue(autoValueClosure)
                closure(qman)
            }
        }
    }


    void transaction(Closure closure){
        if (!defaultConn && conn)
            setDefaultConn(conn)

        modeTransaction = true
        try{
//            sql.withTransaction{
                closure(this)
//            }
            if (!modeJUnitTest)
                commitAll()

        }catch(Exception e){
            e.printStackTrace()
            rollBackAll()
        }finally{
            disconnectAll()
            modeTransaction = false
        }
    }



    /**
     * SELECT
     * @param param
     * @return
     */
    def rows(def param, Closure closure, def result){
        String query
        List values
        connect()
        if (!resultType && param){
            resultType = param.getClass()
        }
        if (resultType){
            resultMap = resultMap ?: generateMatchingMap(resultType)
            tableName = tableName ?: getTableName(resultType)
            tableSetupMap = tableSetupMap ?: generateTableSetupMap(resultType)
            columnSetupMap = columnSetupMap ?: generateColumnSetupMap(resultType)
            doAutoMetaValue(resultType) //AutoSetting
        }
        if (param){
            setPageValue(param)
        }

        // Attribute
        if (!attribute || attribute.size() == 0)
            setAttribute(resultMap.keySet().toList())

        query = this.query ?: generateQuery()
        if (modeTopRank)
            query = generateTopRankQuery(query)
        if (orderAttributeMap)
            query = generateOrderQuery(query)
        if (pageNum && pageSize)
            query = generatePageinateQuery(query)
        if (modeCount)
            query = generateCountQuery(query)

        values = (param) ? generateValues(param) : []

        try{

            /** SELECT META LIST **/
            if (modeMeta) {
                query = getQueryForOneRow(query)
                sql.rows(query, values, { meta ->
                    int colCnt = meta.columnCount
                    (1..colCnt).each{
                        result << meta.getColumnName(it)
                    }
                })

            /** SELECT COUNT INTEGER **/
            }else if (modeCount){
                sql.rows(query, values).each{
                    result = it['CNT'] as Integer
                }
            }else{

                /** SELECT MAP **/
                if (result instanceof Map){
                    Closure rowClosure
                    if (!resultId && attribute && attribute[0])
                        setResultId(attribute[0])
                    if (resultId){
                        String mapIdColumnNm = resultMap[resultId] ?: resultId
                        rowClosure = { row -> result[row[mapIdColumnNm]] = closure(row) }
                    }else{
                        rowClosure = { row -> result[row[0]] = closure(row) }
                    }
                    if (resultMap){
                        sql.eachRow(query, values, rowClosure)
                    }else{
                        sql.eachRow(query, values, getMetaClosure(), rowClosure)
                    }

                /** SELECT LIST **/
                }else if (result instanceof List){
                    if (!resultMap){
                        sql.eachRow(query, values, getMetaClosure()){ row -> result << closure(row) }
                    }else{
                        sql.eachRow(query, values){ row -> result << closure(row) }
                    }

                /** SELECT STRING **/
                }else if (result instanceof String){
                    GroovyRowResult row = sql.firstRow(query, values)
                    String idColumnName = resultMap[resultId] ?: resultId
                    if (row)
                        result = (idColumnName) ? (String)row[idColumnName] : (String)row[0]
                    else
                        result = ""

                /** SELECT INTEGER **/
                }else if (result instanceof Integer){
                    GroovyRowResult row = sql.firstRow(query, values)
                    String idColumnName = resultMap[resultId] ?: resultId
                    if (row)
                        result = (idColumnName) ? (Integer)row[idColumnName] : (Integer)row[0]
                }
            }

        }catch(Exception e){
            //If Table is not Exist Then Try to Create Table,
            if (modeAutoCreateTable && isNotExistTable(e)){
                if (createTableCount == 0){
                    createTableCount++
                    // - Try to Create Table
                    QueryMan tableCreator = getClass().newInstance(conn, resultType)
                    tableCreator.setModeAutoClose(false).createTable()
                    logger.debug 'Table was Created'
                    // - Retry
                    return rows(param, closure, result)
                }
            }
            throw e
        }finally{
            // Log Result
            logResult(query, values, result)
            disconnect()
        }

        return result
    }

    /**
     * INSERT, UPDATE, DELETE, CREATETABLE
     * @param dtoList
     * @param closure
     * @return
     */
    boolean execute(def param){
        boolean result
        String query
        List values
        connect()

        if (!resultType && param){
            resultType = param.getClass()
        }
        if (resultType){
            resultMap = resultMap ?: generateMatchingMap(resultType)
            typeMap = typeMap ?: generateTypeMap(resultType)
            tableSetupMap = tableSetupMap ?: generateTableSetupMap(resultType)
            columnSetupMap = columnSetupMap ?: generateColumnSetupMap(resultType)
            tableName = getTableName(resultType)
            //AutoSetting
            doAutoMetaValue(resultType)
        }

        //Attribute
        if (!attribute || attribute.size() == 0)
            setAttribute(resultMap.keySet().toList())


        //Query
        query = this.query ?: generateQuery()
        if (batchClosureByOneStep)
            batchClosureByOneStep(param)
        //Value
        values = (param) ? generateValues(param) : []

        try{
            if (values && !command.equals('create'))
                result = sql.execute(query, values)
            else
                result = sql.execute(query)

        }catch(Exception e){
            //If Table is not Exist Then Try to Create Table,
            if (modeAutoCreateTable && isNotExistTable(e)){
                if (createTableCount == 0){
                    createTableCount++
                    // - Try to Create Table
                    QueryMan tableCreator = getClass().newInstance(conn, resultType)
                    tableCreator.setModeAutoClose(false).createTable()
                    logger.debug 'Table was Created'
                    // - Retry
                    return execute(param)
                }
            }
            sql.rollback()
            throw e
        }finally{
            //Log Result
            logResultForExecute(query, values, result)
            disconnect()
        }

        return result
    }

    /**
     * BATCH
     * @param dtoList
     * @param closure
     * @return
     */
    int[] batch(List paramList, Closure closure){
        int[] result
        String query
        List values
        connect()

        if (paramList && paramList.size() > 0){
            Class mainClass = paramList[0].getClass()
            resultMap = resultMap ?: generateMatchingMap(mainClass)
            tableName = getTableName(mainClass)
            doAutoMetaValue(mainClass)

            if (!attribute || attribute.size() == 0)
                setAttribute(resultMap.keySet().toList())
            query = this.query ?: generateQuery()

            try{
                result = sql.withBatch(batchSize, query){ BatchingPreparedStatementWrapper ps ->
                    paramList.each { def param ->
                        if (batchClosureByOneStep)
                            batchClosureByOneStep(param)
                        closure(param)
                        values = generateValues(param)
                        ps.addBatch(values)
                    }
                }
            }catch(Exception e){
                sql.rollback()
                throw e
            }finally{
                //Log Result
                logResultForExecute(query, values, result)
                disconnect()
            }

        }

        return result
    }

    boolean isNotExistTable(Exception e){
        if (e.message.indexOf('ORA-00942') != -1){
            logger.error " - Table or View not exist."
            return true
        }else if (e.message.indexOf('ORA-00955') != -1){
            logger.error " - Already Exist Table."
        }else
            e.printStackTrace()
        return false
    }





    /**********
     * Generate Values
     * @param param
     * @return
     */
    List generateValues(def param){
        List values = []

        if (!command.equals('select')){
            attribute.each{ String propNm ->
                if (!replaceMap[propNm])
                    values << param[propNm]
            }
        }

        if (!command.equals('insert')){
            if (where){
                List list = where.collect{ def oneCon ->
                    if (oneCon instanceof String){
                        return (replaceMap[oneCon] ?: param[oneCon]) as String
                    }else if (oneCon instanceof List){
                        def tempList = []
                        oneCon.each{
                            tempList << (replaceMap[it] ?: param[it]) as String
                        }
                        return tempList
                    }else if (oneCon instanceof Map){
                    }
                }.flatten() as List
                list.each{
                    if (it)
                        values.add(it)
                }
            }
        }
        return values
    }







    boolean close(){
        return disconnectAll()
    }

    boolean testClose(){
        rollBackAll()
        return disconnectAll()
    }

    boolean commitAll(){
        sqlMap.each{ String connectionId, Sql sql -> sql.commit() }
        logger.debug " - Completed Commit All"
        return true
    }

    boolean rollBackAll(){
        sqlMap.each{ String connectionId, Sql sql -> sql.rollback() }
        logger.debug " - Completed Rollback All"
        return true
    }

    private boolean connect(){
        //Transaction Check
        String connectionId
        if (connectionName && connPoolMap)
            conn = connPoolMap[connectionName]
        else if (!conn && !connectionName && defaultConn)
            conn = defaultConn
        else if (!conn && !connectionName && !defaultConn && recentConn)
            conn = recentConn

        if (conn)
            connectionId = conn.toString()

        if (modeTransaction){
            if (connMap[connectionId] && sqlMap[connectionId]){
                sql = sqlMap[connectionId]
            }else{
                sql = new Sql(conn)
                connMap[connectionId] = conn
                sqlMap[connectionId] = sql
                sql.connection.autoCommit = false
            }
        }else{
            sql = new Sql(conn)
            connMap[connectionId] = conn
            sqlMap[connectionId] = sql
        }

        if (modeCloseAfter)
            sqlMapForCloseAfter[connectionId] = sql


        //MODE - JUnit
        if (modeJUnitTest)
            sql.connection.autoCommit = false
        recentConn = conn
        return true
    }

    private boolean disconnect(){
        if (modeTransaction){
            //Transaction Mode => No Disconnect
        }else{
            if (modeJUnitTest){
                logger.debug " (Auto Rollback on JUnit TEST)"
                sql.rollback()
            }
            if (modeAutoClose){
                sql.close()
                logger.debug " - Completed Disconnected"
            }
            if (modeCloseAfter){
                sql.close()
                logger.debug " - Completed Disconnected"
            }
        }
        return true
    }

    private boolean disconnectAll() {
        //When JUnit Test => Rollback All
        if (modeJUnitTest) {
            logger.debug " (Auto Rollback on JUnit TEST)"
            rollBackAll()
        }
        //Disconnect All
        if (modeAutoClose){
            sqlMap.each { String connectionId, Sql sql ->
                sql.close()
            }
            logger.debug " - Completed Disconnected All"
        }else{
            sqlMapForCloseAfter.each{ String connectionId, Sql sql ->
                sql.close()
                logger.debug " - Completed Disconnected"
            }
        }
        modeTransaction = false
        return true
    }

    public static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }




    /**
     * Generate Query
     * @param command
     * @param matchingBean
     * @param props
     * @param replaceMap
     * @return
     */
    String generateQuery(){
        String query
        if (command.equals('select')){
            query = generateSelectQuery(resultMap, attribute)
        }else if (command.equals('insert')){
            query = generateInsertQuery(resultMap, attribute, replaceMap)
        }else if (command.equals('update')){
            query = generateUpdateQuery(resultMap, attribute, replaceMap)
        }else if (command.equals('delete')){
            query = generateDeleteQuery()
        }else if (command.equals('create')){
            query = generateCreateTableQuery(resultMap, attribute, typeMap, columnSetupMap)
        }
        return query
    }

    String generateSelectQuery(Map matchingMap, List props){
        String query
        //SELECT
        List listSelect = []
        if (clazzListToJoin)
            props.each{ String propNm -> listSelect << "T0.${matchingMap[propNm]}" }
        else
            props.each{ String propNm -> listSelect << "${matchingMap[propNm]}" }
        //TABLE (FROM)
        String tableName = tableName
        String tableAlias = ""
        //WHERE
        List listWhere = generateListWhere()
        String whereExpression = listWhere.join(' OR ')
        String whereQuery = (listWhere) ? "WHERE ${whereExpression}" : ""
        //JOIN
        String joinQuery = ""
        if (clazzListToJoin){
            tableAlias = "T0"
            clazzListToJoin.eachWithIndex{ Class join, int i ->
                String joinTableName = getTableName(join)
                String joinAlias = "T${i+1}"
                Map joinResultMap = generateMatchingMap(join)
                List joinKeyList = []
                joinResultMap.each{ att, col ->
                    if (matchingMap.containsKey(att)){
                        joinKeyList << att
                    }else{
                        matchingMap[att] = col
                        listSelect << "${joinAlias}.${col}"
                    }
                }
                //
                if (joinKeyList){
                    String firstJoinCol = joinKeyList[0]
                    matchingMap[firstJoinCol]
                    joinQuery += "LEFT OUTER JOIN ${joinTableName} ${joinAlias} ON ${tableAlias}.${matchingMap[firstJoinCol]} = ${joinAlias}.${joinResultMap[firstJoinCol]} "
                    if (joinKeyList.size() > 1)
                        joinQuery += joinKeyList[1..(joinKeyList.size()-1)].collect{ "AND ${tableAlias}.${matchingMap[it]} = ${joinAlias}.${joinResultMap[it]} " }.join('')
                }
            }
        }
        //QUERY COMPLETE
        query = """
        SELECT ${listSelect.join(', ')}
          FROM ${tableName} ${tableAlias}
        ${joinQuery}            
        ${whereQuery} """
        return query
    }

    String generateInsertQuery(Map matchingMap, List props, Map replaceMap){
        String tableName = tableName
        List listColumn = []
        List listValue = []

        props.each{ String propNm ->
            listColumn << matchingMap[propNm]
            listValue << (replaceMap[propNm] ?: "?")
        }

        String query ="""
            INSERT INTO ${tableName}(
               ${listColumn.join(', ')}
            )VALUES(
               ${listValue.join(', ')}
            )"""
        return query
    }

    String generateUpdateQuery(Map matchingMap, List props, Map replaceMap){
        String tableName = tableName
        List listSet = []
        List listWhere = generateListWhere()

        props.each{ String propNm ->
            listSet << "${matchingMap[propNm]} = ${replaceMap[propNm] ?: '?'}"
        }

        String query ="""
            UPDATE ${tableName}
               SET ${listSet.join(', ')}
             WHERE ${listWhere.join(' OR ')} """
        return query
    }

    String generateDeleteQuery(){
        String tableName = tableName
        List listWhere = generateListWhere()

        String query ="""
            DELETE FROM ${tableName}
             WHERE ${listWhere.join(' OR ')} """
        return query
    }

    String generateCreateTableQuery(Map matchingMap, List props, Map typeMap, Map columnSetupMap){
        String tableName = tableName
        List listColumnInfo = []

        props.each{ String propNm ->
            String columnName = matchingMap[propNm]
            String columnType = getColumnType(typeMap[propNm], columnSetupMap[propNm])
            listColumnInfo << "${columnName} ${columnType}"
        }

        String query = """
            CREATE TABLE ${tableName}(
                ${listColumnInfo.join(',')}
            ) """
        return query
    }





    List generateListWhere(){
        List listWhere = []
        if (where){
            if (where instanceof String || where instanceof Map){
                where = [where]
            }
            if (where instanceof List){
                where.each{ def con ->
                    if (con)
                        listWhere << parseWhere(con)
                }
            }
        }
        if (listWhere.size() == 0)
            listWhere << "1 = 1"
        return listWhere
    }

    String parseWhere(def con){
        String strCon = ''
        if (con instanceof Map){
            def conAnd = []
            con.each{
                conAnd << "${it.key} = ${it.value}"
            }
            strCon = "${conAnd.join(' AND ')}"
        }else if (con instanceof String){
            strCon = "${resultMap[con]} = ?"
        }else if (con instanceof List){
            def listWhere = []
            con.each{
                listWhere << parseWhere(it)
            }
            strCon = listWhere.join(' AND ')
        }
        return strCon
    }




    String getTableName(Class resultType) throws IllegalAccessException {
        if (resultType.getAnnotation(QueryTable.class)){
            return resultType.getAnnotation(QueryTable.class).value()
        }
        return null
    }

    Map generateMatchingMap(Class resultType) throws IllegalAccessException {
        Map machingMap = [:]
        resultType.getDeclaredFields().each{ Field field ->
            QueryColumn annotation = field.getAnnotation(QueryColumn.class);
            if( annotation ){
                if (field.type == String.class || field.type == Date.class  || field.type == Integer.class){
                    field.accessible = true
                    machingMap[field.name] = annotation.value()
                }
            }
        }
        return machingMap
    }

    Map generateTableSetupMap(Class resultType) throws IllegalAccessException {
        Map tableSetupMap = [:]
        if (resultType.getAnnotation(QueryTableSetup.class)){
            QueryTableSetup annotation = resultType.getAnnotation(QueryTableSetup.class)
        }
        if (resultType.getAnnotation(QueryTableAutoCreate.class)){
            QueryTableAutoCreate annotation = resultType.getAnnotation(QueryTableAutoCreate.class)
            modeAutoCreateTable = true
        }
        return tableSetupMap
    }

    Map generateColumnSetupMap(Class resultType) throws IllegalAccessException {
        Map columnSetupMap = [:]
        resultType.getDeclaredFields().each{ Field field ->
            QueryColumnSetup annotation = field.getAnnotation(QueryColumnSetup.class);
            if( annotation ){
                if (field.type == String.class || field.type == Date.class  || field.type == Integer.class){
                    field.accessible = true
                    columnSetupMap[field.name] = annotation
                }
            }
        }
        return columnSetupMap
    }


    void setPageValue(def instance) throws IllegalAccessException {
        instance.getClass().getDeclaredFields().each{ Field field ->
            if( field.getAnnotation(QueryPageSize.class) ){
                if (field.type == String.class || field.type == Date.class  || field.type == Integer.class){
                    field.accessible = true
                    pageSize = (instance[field.name] as Integer)
                }
            }
            if( field.getAnnotation(QueryPageNumber.class) ){
                if (field.type == String.class || field.type == Date.class  || field.type == Integer.class){
                    field.accessible = true
                    pageNum = (instance[field.name] as Integer)
                }
            }
        }
    }

    Map generateTypeMap(def instance){
        Map typeMap = [:]
        if (instance){
            instance.metaClass.properties.each{
                typeMap[it.name] = it.type
            }
        }
        return typeMap
    }

    String generateTopRankQuery(String query){
        List rankPkColumns = rankPkAttributes.collect{ resultMap[it] }
        String rankOrderColumn = resultMap[rankOrderAttribute]
        //ORDER
        return """
            SELECT * 
            FROM(              
                SELECT TEMP_TABLE_A.*, 
                       ROW_NUMBER() OVER(PARTITION BY ${rankPkColumns.join(', ')} ORDER BY ${rankOrderColumn} DESC) RN 
                FROM (
                       ${query}                    
                ) TEMP_TABLE_A
            )
            WHERE RN = 1 """
    }

    String generateOrderQuery(String query){
        String orderExpression = orderAttributeMap.collect{ att, method -> "${resultMap[att]?:att} ${method!=ASC?method:''}" }.join(', ')
        String orderQuery = (orderAttributeMap) ? "ORDER BY ${orderExpression}" : ""
        //ORDER
        return "${query}\n${orderQuery}"
    }

    String generatePageinateQuery(String query){
        Integer pageStartNum = (pageNum -1) * pageSize
        Integer pageEndNum = pageNum * pageSize
        return """
            SELECT * 
            FROM ( 
                SELECT A.*, ROWNUM as ROWSEQ 
                FROM (
                    ${query}                    
                ) A 
                WHERE ROWNUM <= ${pageEndNum}
            ) 
            WHERE ROWSEQ > ${pageStartNum} """
    }

    String generateCountQuery(query){
        return """
             SELECT COUNT(*) AS CNT
             FROM (
                ${query}
             ) """
    }





    /**
     *  특정 컬럼에 특정 값 입력 자동화 어노테이션
     *  @AutoMetaValue 적용시
     */
    void doAutoMetaValue(Class resultType){

        if (resultType.getAnnotation(QueryAutoValue.class)){
            // Attribute를 지정하지 않은 경우, 자동으로 모든 어트리뷰트 선택
            if (!attribute)
                resultMap.each{ attribute << it.key }

            // autoMetaValueClosure를 등록한 경우 실행
            if (autoMetaValueClosure)
                autoMetaValueClosure(this)
        }

    }


    String getQueryForOneRow(query){
        switch(vendor.toUpperCase()){
            case 'ORACLE':
                query = """
                    SELECT * FROM( ${query} ) WHERE ROWNUM <= 1
                """
                break;
            case 'MYSQL':
                query = """
                    ${query} LIMIT 1
                """
                break;
            case { it == 'SQLServer' || it == 'MSAccess' }:
                query = """
                    SELECT TOP 1 FROM( ${query} )
                """
                break;
            default:
                query = """
                    SELECT * FROM( ${query} ) WHERE ROWNUM <= 1
                """
                break;
        }
        return query
    }

    String getColumnType(Class clazz, QueryColumnSetup columnSetupAnnotation){
        if (clazz == Date.class){
            return "DATE"

        }else if (clazz == String.class){
            if (columnSetupAnnotation && columnSetupAnnotation.big())
                return "CLOB"
            else
                return "VARCHAR2(500)"

        }else if (clazz == Integer.class){
            return "NUMBER"
        }

        return "VARCHAR2"
    }


    /*************************
     * query 조합기
     * ? => params 순서대로 치환
     *************************/
    String getQuery(String query, String[] params){
        return replaceWithParams(query, params)
    }
    String replaceWithParams(String query, String[] params){
        params.each{
            query = query.replaceFirst("[\\?]{1}", it)
        }
        return query
    }

    /*************************
     * 로그 출력
     * 본 QueryManager를 호출한 Class명으로 로그(QueryName, Query, Parameters)출력
     *************************/
    void log(String logContent){
        log('', logContent)
    }

    void log(String logName, String logContent){
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace()
        for (int i=0; i<stacks.length; i++){
            if (i > 1
                    && !stacks[i].getClassName().startsWith(packagePath)
                    && !stacks[i].getClassName().startsWith('java.lang')
                    && !stacks[i].getClassName().startsWith('sun.reflect')
                    && !stacks[i].getClassName().startsWith('org.codehaus.groovy')){
                String className = stacks[i].getClassName()
                String methodName = stacks[i].getMethodName()
                int lineNumber = stacks[i].getLineNumber()
                String logString = "[${methodName}:${lineNumber}] - <${logName}> \n${logContent}"
                if (modeJUnitTest){
                    println logString
                }else{
                    logger = LoggerFactory.getLogger(className)
                    logger.debug logString
                }
                break
            }
        }
    }

    void logResult(String query, List params, def result){
        String queryLog = ''
        String paramLog = ''
        String resultLog = ''
        //Query Log
        if (modeFlattenQueryLog)
            query = query.replaceAll("\n+", " ").replaceAll("\r+", " ").replaceAll("\t+", " ").replaceAll("\\s+", " ")
        queryLog = " - Query: ${query}\n"
        //Parameter Log
        if (params)
            paramLog = " - Parameters: ${params.toString()}\n"
        //Result Log
        if (result){
            if (result instanceof List || result instanceof Map)
                resultLog = " - Size: ${result.size()}"
            else
                resultLog = " - Result: ${result}"
        }
        log('Query', "${queryLog}${paramLog}${resultLog}")
    }

    void logResultForExecute(String query, List params, def result){
        String queryLog = ''
        String paramLog = ''
        String resultLog = ''
        //Query Log
        if (modeFlattenQueryLog)
            query = query.replaceAll("\n+", " ").replaceAll("\r+", " ").replaceAll("\t+", " ").replaceAll("\\s+", " ")
        queryLog = " - Query: ${query}\n"
        //Parameter Log
        if (params)
            paramLog = " - Parameters: ${params.toString()}\n"
        //Result Log
        if (result){
            if (result instanceof List || result instanceof Array)
                resultLog = " - Success: ${result.findAll{ it == -2 }.size()} / ${result.size()}"
            else if (result instanceof Boolean)
                resultLog = " - Success: ${result}"
        }
        log('Query', "${queryLog}${paramLog}${resultLog}")
    }

    /**
     * Clob To String
     * @param clob
     * @return
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    String getString(Clob clob) throws IOException{
        String result = ""
        if (clob){
            StringBuffer strBuffer = new StringBuffer()
            BufferedReader reader = new BufferedReader(clob.getCharacterStream())
            while ((result = reader.readLine()) != null){
                strBuffer.append(result)
            }
            result = strBuffer.toString()
        }
        return result
    }

}
