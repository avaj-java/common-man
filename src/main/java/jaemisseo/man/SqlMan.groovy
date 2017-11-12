package jaemisseo.man

import groovy.sql.Sql
import jaemisseo.man.bean.SqlSetup
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.SQLException
import java.util.regex.Matcher

/**
 * Created by sujung on 2016-09-24.
 */
class SqlMan extends SqlAnalMan{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Sql sql

    SqlSetup gOpt = new SqlSetup()
    SqlSetup connectedOpt = new SqlSetup()

    String sqlContent
    String patternToGetQuery
    List<SqlObject> analysisResultList = []
    Map resultReportMap


    public static final String ORACLE = "ORACLE"
    public static final String TIBERO = "TIBERO"

    public static final int ALL = 0
    public static final int ALL_WITHOUT_PLSQL = 1

    public static final int CREATE = 1000
    public static final int CREATE_TABLE = 1001
    public static final int CREATE_INDEX = 1002
    public static final int CREATE_VIEW = 1003
    public static final int CREATE_SEQUENCE = 1004
    public static final int CREATE_TABLESPACE = 1005
    public static final int CREATE_USER = 1006

    public static final int CREATE_PACKAGE = 1007
    public static final int CREATE_PROCEDURE = 1008
    public static final int CREATE_FUNCTION = 1009
    public static final int CREATE_TRIGGER = 1010
    public static final int CREATE_JAVA = 1011

    public static final int CREATE_ETC = 1090
    public static final int CREATE_ETC2 = 1091

    public static final int ALTER = 20
    public static final int ALTER_TABLE = 21
    public static final int ALTER_USER = 22
    public static final int INSERT = 30
    public static final int UPDATE = 40
    public static final int COMMENT = 50
    public static final int GRANT = 60
    public static final int PLSQL = 70
    public static final int SELECT = 80
    public static final int DELETE = 90
    public static final int DROP = 100



    public static final int IGNORE_CHECK = 1
    public static final int CHECK_BEFORE_AND_STOP = 2
    public static final int CHECK_RUNTIME_AND_STOP = 3



    List<SqlObject> getAnalysisResultList(){
        return analysisResultList
    }

    Map getResultReportMap(){
        return resultReportMap
    }

    List<String> getReplacedQueryList(){
        return analysisResultList.collect{ it.query }
    }



    List<String> getAnalysisStringResultList(){
        return getAnalysisStringResultList(analysisResultList)
    }

    List<String> getAnalysisStringResultList(List<SqlObject> analysisList){
        List<String> warningList = getWarningList(analysisList)
        List<String> analysisStringList = analysisList.collect{
            """
            [${it.sqlFileName}] ${it.seq} ${it.warnningMessage?:''}         
            ${it.query}
            """
        }
        return (warningList + analysisStringList)
    }

    List<String> getResultList(){
        return getResultList([resultReportMap])
    }

    List<String> getResultList(List<Map> resultReportMapList){
        List<String> resultList = resultReportMapList.collect{
            """
            ${it}
            """
        }
        return resultList
    }



    SqlMan set(SqlSetup opt){
        gOpt.merge(opt)
//        gOpt.url         = (opt.url) ? opt.url : getUrl(opt.vendor, opt.ip, opt.port, opt.db)
//        gOpt.driver      = (opt.driver) ? opt.driver : getDriver(opt.vendor)
        gOpt.setup()
        return this
    }



    SqlMan connect(){
        close()
        return connect(gOpt)
    }

    SqlMan connect(SqlSetup localOpt){
        SqlSetup opt = mergeOption(localOpt)
//        opt.url    = (opt.url) ? opt.url : getUrl(localOpt.vendor, localOpt.ip, localOpt.port, localOpt.db)
//        opt.driver = (opt.driver) ? opt.driver : getDriver(localOpt.vendor)
//        this.sql = Sql.newInstance(opt.url, opt.user, opt.password, opt.driver)
        this.sql = opt.setup().generateSqlInstance()
        connectedOpt = opt
        return this
    }



    SqlSetup mergeOption(){
        return mergeOption(gOpt)
    }

    SqlSetup mergeOption(SqlSetup localOpt){
        return gOpt.clone().merge(localOpt)
    }



    SqlMan close(){
        if (sql)
            sql.close()
        return this
    }


    SqlMan init(){
        this.sqlFileName = ''
        this.sqlContent = ''
        this.patternToGetQuery = ''
        this.analysisResultList = []
        this.resultReportMap = [:]
        return this
    }

    SqlMan query(String query){
        sqlContent = query
        return this
    }

    SqlMan queryFromFile(String url){
        File file = new File(url)
        this.sqlFileName = file.getName()
        return query(file.text)
    }

    SqlMan command(def targetList){
        this.patternToGetQuery = getSqlPattern(targetList)
        return this
    }

    List<String> getMatchedQueryList(){
        Matcher m = getMatchedList(this.sqlContent, this.patternToGetQuery)
        return m.findAll() as List
    }

    SqlMan replace(SqlSetup localOpt){
        connectedOpt = mergeOption(localOpt)
        // analysis
        analysisResultList = getAnalyzedObjectList(getMatchedQueryList(), connectedOpt)
        return this
    }

    /*************************
     * CHECK BEFORE
     *************************/
    SqlMan checkBefore(SqlSetup localOpt){
        try {
            def existObjectList
            def existTablespaceList
            def existUserList
            connect(localOpt)

            //Collecting
            logger.debug '- Collecting OBJECT from DB...'
            Map data = Util.startPrinter(3, 20, localOpt.modeSqlProgressBar)
            // - OBJECT
            existObjectList = sql.rows("SELECT OBJECT_NAME, OBJECT_TYPE, OWNER AS SCHEME FROM ALL_OBJECTS")
            // - TABLESPACE
            def resultsForTablespace = analysisResultList.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("TABLESPACE") }
            if (resultsForTablespace){
                data.stringList << '- Collecting TABLESPACE from DB...'
                existTablespaceList = sql.rows("SELECT TABLESPACE_NAME AS OBJECT_NAME, 'TABLESPACE' AS OBJECT_TYPE FROM USER_TABLESPACES")
            }
            // - USER
            def resultsForUser = analysisResultList.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("USER") }
            if (resultsForUser){
                data.stringList << '- Collecting USER from DB...'
                existUserList = sql.rows("SELECT USERNAME AS OBJECT_NAME, 'USER' AS OBJECT_TYPE FROM ALL_USERS")
            }
            Util.endWorker(data)

            //Checking
            // - Check Exist Object
            logger.debug '- Check OBJECT...'
            Util.eachWithTimeProgressBar(analysisResultList, 20, connectedOpt.modeSqlProgressBar){
                SqlObject obj = it.item
                //PLSQL and DELETE are ignore BeforeCheck
                //Others do BeforeCheck
                if (!['PLSQL', 'DELETE', 'DROP'].contains(obj.commandType)){
                    int count = it.count
                    obj.isExistOnDB = isExistOnSchemeOnDB(obj, existObjectList)
                    if (obj.isExistOnDB) {
                        //Already exist object!
                        if (containsIgnoreCase(localOpt.commnadListThatObjectMustNotExist, obj.commandType))
                            obj.warnningMessage = WARN_MSG_2
                    } else {
                        //Does not exist!
                        if (containsIgnoreCase(localOpt.commnadListThatObjectMustExist, obj.commandType))
                            obj.warnningMessage = WARN_MSG_1
                    }
                }
            }
            // - Check Exist TableSpace
            if (resultsForTablespace){
                logger.debug '- Check TABLESPACE...'
                resultsForTablespace.each { SqlObject obj ->
                    obj.isExistOnDB = isExistOnDB(obj, existTablespaceList)
                    if (obj.isExistOnDB)
                        obj.warnningMessage = WARN_MSG_2
                }
                logger.debug ' DONE'
            }
            // - Check Exist User
            if (resultsForUser){
                logger.debug '- Check USER...'
                resultsForUser.each { SqlObject obj ->
                    obj.isExistOnDB = isExistOnDB(obj, existUserList)
                    if (obj.isExistOnDB)
                        obj.warnningMessage = WARN_MSG_2
                }
                logger.debug ' DONE'
            }

        }catch(Exception e){
            throw e
        }finally{
            close()
        }

        //Error Check
        List<SqlAnalMan.SqlObject> warnList = analysisResultList.findAll{ it.warnningMessage }
        if (!localOpt.modeSqlIgnoreErrorCheckBefore && warnList){
            //Mode Ignore Error (Already Exsist)
            if (localOpt.modeSqlIgnoreErrorAlreadyExist){
                if (warnList.findAll{ it.warnningMessage != WARN_MSG_2 })
                    throw new SQLException(warnList[0].warnningMessage)
            }else{
                throw new SQLException(warnList[0].warnningMessage)
            }
        }

        return this
    }



    SqlMan run() {
        return run(new SqlSetup())
    }
    SqlMan run(SqlSetup localOpt) {
        // SQL
        runSql(localOpt, analysisResultList)

        // create report
        createReport(analysisResultList)
        return this
    }





    List<SqlObject> getAnalyzedObjectList(List m, SqlSetup opt){
        def resultList = []
        logger.debug "- Replace Object Name..."
        Util.eachWithTimeProgressBar(m, 20, opt.modeSqlProgressBar) { data ->
            String query = data.item
            int count = data.count
            SqlObject sqlObj = getAnalyzedObject(query)
            sqlObj.sqlFileName = sqlFileName
            sqlObj.seq = count
            if (sqlObj.commandType == 'PLSQL'){
                String plsqlQuery = sqlObj.query
                SqlMan plsqlman = this.class.newInstance().init().query(plsqlQuery).command([SqlMan.ALL_WITHOUT_PLSQL, SqlMan.SELECT])
                List<String> matchedQueryList = plsqlman.getMatchedQueryList()
                List<SqlObject> InPlsqlReplacedQueryList = plsqlman.replace(opt.clone().put([modeSqlProgressBar:false])).getAnalysisResultList()
                InPlsqlReplacedQueryList.eachWithIndex{ SqlObject plsqlobj, int i ->
                    sqlObj.query = sqlObj.query.replace(matchedQueryList[i], plsqlobj.query)
                }
            }else{
                sqlObj = getReplacedObject(sqlObj, opt, count)
            }
            resultList << sqlObj
        }
        return resultList
    }


    void runSql(SqlSetup localOpt, List<SqlObject> analysisResultList){
        connect(localOpt)
        sql.withTransaction{
            logger.debug "- Executing Sqls..."
            Util.eachWithTimeProgressBar(analysisResultList, 20, connectedOpt.modeSqlProgressBar){ data ->
                SqlObject sqlObj = data.item
                int count = data.count
                try{
                    String query = removeLastSlash(removeLastSemicoln(sqlObj.query))
                    //- CREATE JAVA
                    if (sqlObj.objectType == 'JAVA'){
                        query = """
                        DECLARE
                            V_SQL_TEXT CLOB := '${query}';
                        BEGIN
                          EXECUTE IMMEDIATE V_SQL_TEXT;
                        END;
                        """
                    }
                    //- RUN
                    sql.execute(query)
                    sqlObj.isOk = true
                }catch(Exception e){
                    sqlObj.isOk = false
                    sqlObj.error = e
                    if (!localOpt.modeSqlIgnoreErrorExecute){
                        //Mode Ignore Error (Already Exsists)
                        if (localOpt.modeSqlIgnoreErrorAlreadyExist && e.message.indexOf('ORA-00955') != -1){
                        }else{
                            throw e
                        }
                    }
                }
            }
        }
        close()
    }




    void createReport(List<SqlObject> results){
        //Option
        List optionList = []
        connectedOpt.eachFieldName {
            if (connectedOpt[it])
                optionList << "${it}=${connectedOpt[it]}"
        }
        //Collect Rpoert
        this.resultReportMap = [
//                option      :optionList.join(' | '),
//                pattern     :patternToGetQuery,
                matchedCount    :results.size(),
                succeededCount  :results.findAll{ it.isOk }.size(),
                failedCount     :results.findAll{ !it.isOk }.size(),
                summary         :generateSummary(results).findAll{ it.value.all > 0 },
//                analysisResultList    :analysisResultList
        ]
    }



    /**
     * Report 'Before Check' With Console
     */
    SqlMan reportAnalysis(){
        List<String> warningList = getWarningList()
        warningList.each{
            logger.debug it
        }
        reportGeneratedQuerys()
        return this
    }

    SqlMan reportGeneratedQuerys(){
        logger.debug ""
        logger.debug ""
        logger.debug "<QUERY>"
        analysisResultList.each{
            logger.debug "\n[${it.sqlFileName}] ${it.seq} ${it.warnningMessage}"
            logger.debug "${it.query}"
        }
        return this
    }

    /**
     * Report 'SQL Result' With Console
     */
    SqlMan reportResult(){
        List<String> reportLineList = generateResultReportSummaryLineList()
        if (reportLineList){
            reportLineList.each{
                logger.debug it
            }
            logger.debug ""
            logger.debug ""
            logger.debug ""
        }
        return this
    }

    List<String> generateResultReportSummaryLineList(){
        List<String> resultReportLineList = []
        if (resultReportMap) {
            resultReportLineList << ""
            resultReportLineList << ""
            resultReportLineList << "<REPORT>"
            Map mainReportMap = resultReportMap.findAll { it.key != 'summary' }
            Map summaryReportMap = resultReportMap.summary
            if (mainReportMap) {
                resultReportLineList << "---------------"
                int longestStringLength = Util.getLongestLength(mainReportMap.keySet().toList())
                mainReportMap.each {
                    String item = it.key.toString().toUpperCase()
                    String spacesToLineUp = Util.getSpacesToLineUp(item, longestStringLength)
                    resultReportLineList << "${item}:${spacesToLineUp} ${it.value}"
                }
            }

            if (summaryReportMap) {
                resultReportLineList << "---------------"
                int longestStringLength = Util.getLongestLength(summaryReportMap.keySet().toList())
                summaryReportMap.each {
                    String item = it.key.toString().toUpperCase()
                    String spacesToLineUp = Util.getSpacesToLineUp(item, longestStringLength)
                    resultReportLineList << "${item}:${spacesToLineUp} ${it.value}"
                }
            }
        }

        return resultReportLineList
    }





    Map generateSummary(List<SqlObject> resultList){
        def createTableList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("TABLE") }
        def createIndexList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("INDEX") }
        def createViewList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("VIEW") }
        def createSequenceList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("SEQUENCE") }
        def createTablespaceList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("TABLESPACE") }
        def createUserList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("USER") }
        def createPackageList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("PACKAGE") }
        def createPocedureList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("PROCEDURE") }
        def createTriggerList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("TRIGGER") }
        def createFunctionList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("FUNCTION") }
        def createJavaList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("JAVA") }
        def createEtcList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase("ETC") }
        def alterList = resultList.findAll{ it.commandType.equalsIgnoreCase('ALTER') }
        def commentList = resultList.findAll{ it.commandType.equalsIgnoreCase('COMMENT') }
        def grantList = resultList.findAll{ it.commandType.equalsIgnoreCase('GRANT') }
        def insertList = resultList.findAll{ it.commandType.equalsIgnoreCase('INSERT') }
        def updateList = resultList.findAll{ it.commandType.equalsIgnoreCase('UPDATE') }
        def plsqlList = resultList.findAll{ it.commandType.equalsIgnoreCase('PLSQL') }
        def deleteList = resultList.findAll{ it.commandType.equalsIgnoreCase('DELETE') }
        def dropList = resultList.findAll{ it.commandType.equalsIgnoreCase('DROP') }
        def summary = [
                'create table':[
                        all: createTableList.size(),
                        o: createTableList.findAll{ it.isOk }.size(),
                        x: createTableList.findAll{ !it.isOk }.size()
                ],
                'create index':[
                        all: createIndexList.size(),
                        o: createIndexList.findAll{ it.isOk }.size(),
                        x: createIndexList.findAll{ !it.isOk }.size()

                ],
                'create view':[
                        all: createViewList.size(),
                        o: createViewList.findAll{ it.isOk }.size(),
                        x: createViewList.findAll{ !it.isOk }.size()

                ],
                'create sequence':[
                        all: createSequenceList.size(),
                        o: createSequenceList.findAll{ it.isOk }.size(),
                        x: createSequenceList.findAll{ !it.isOk }.size()
                ],
                'create tablespace':[
                        all: createTablespaceList.size(),
                        o: createTablespaceList.findAll{ it.isOk }.size(),
                        x: createTablespaceList.findAll{ !it.isOk }.size()
                ],
                'create user':[
                        all: createUserList.size(),
                        o: createUserList.findAll{ it.isOk }.size(),
                        x: createUserList.findAll{ !it.isOk }.size()
                ],
                'create function':[
                        all: createFunctionList.size(),
                        o: createFunctionList.findAll{ it.isOk }.size(),
                        x: createFunctionList.findAll{ !it.isOk }.size()
                ],
                'create package':[
                        all: createPackageList.size(),
                        o: createPackageList.findAll{ it.isOk }.size(),
                        x: createPackageList.findAll{ !it.isOk }.size()
                ],
                'create procedure':[
                        all: createPocedureList.size(),
                        o: createPocedureList.findAll{ it.isOk }.size(),
                        x: createPocedureList.findAll{ !it.isOk }.size()
                ],
                'create trigger':[
                        all: createTriggerList.size(),
                        o: createTriggerList.findAll{ it.isOk }.size(),
                        x: createTriggerList.findAll{ !it.isOk }.size()
                ],
                'create java':[
                        all: createJavaList.size(),
                        o: createJavaList.findAll{ it.isOk }.size(),
                        x: createJavaList.findAll{ !it.isOk }.size()
                ],
                'create etc':[
                        all: createEtcList.size(),
                        o: createEtcList.findAll{ it.isOk }.size(),
                        x: createEtcList.findAll{ !it.isOk }.size()
                ],
                'alter':[
                        all: alterList.size(),
                        o: alterList.findAll{ it.isOk }.size(),
                        x: alterList.findAll{ !it.isOk }.size()
                ],
                'comment':[
                        all: commentList.size(),
                        o: commentList.findAll{ it.isOk }.size(),
                        x: commentList.findAll{ !it.isOk }.size()
                ],
                grant:[
                        all: grantList.size(),
                        o: grantList.findAll{ it.isOk }.size(),
                        x: grantList.findAll{ !it.isOk }.size()
                ],
                insert:[
                        all: insertList.size(),
                        o: insertList.findAll{ it.isOk }.size(),
                        x: insertList.findAll{ !it.isOk }.size()
                ],
                update:[
                        all: updateList.size(),
                        o: updateList.findAll{ it.isOk }.size(),
                        x: updateList.findAll{ !it.isOk }.size()
                ],
                plsql:[
                        all: plsqlList.size(),
                        o: plsqlList.findAll{ it.isOk }.size(),
                        x: plsqlList.findAll{ !it.isOk }.size()
                ],
                delete:[
                        all: deleteList.size(),
                        o: deleteList.findAll{ it.isOk }.size(),
                        x: deleteList.findAll{ !it.isOk }.size()
                ],
                drop:[
                        all: dropList.size(),
                        o: dropList.findAll{ it.isOk }.size(),
                        x: dropList.findAll{ !it.isOk }.size()
                ]
        ]
        return summary
    }





    boolean isExistOnDB(def result, def objectList){
        def equalList = objectList.findAll{ Map<String, String> row ->
            return row["OBJECT_NAME"].equalsIgnoreCase(result.objectName) && row["OBJECT_TYPE"].equalsIgnoreCase(result.objectType)
        }
        return (equalList) ? true : false
    }

    boolean isExistOnSchemeOnDB(SqlObject sqlObj, def catalogList){
        boolean isExist
        def equalList
        String objectName = sqlObj.objectName
        if (objectName){
            int idx = objectName.indexOf(".")
            objectName = (idx == -1) ? objectName : objectName.substring(idx+1)
            equalList = catalogList.findAll{ Map<String, String> row ->
                return row["OBJECT_NAME"].equalsIgnoreCase(objectName) && row["OBJECT_TYPE"].equalsIgnoreCase(sqlObj.objectType) && row["SCHEME"].equalsIgnoreCase(sqlObj.schemeName)
            }
        }
        isExist = (equalList) ? true : false
        return isExist
    }

    List<String> getWarningAlreadyExist(String msg, List<SqlObject> list){
        List<String> result = []
        int existCnt = list.findAll{ it.isExistOnDB }.size()
        if (existCnt)
            result << "[${msg}] Already Exists Object :   ${existCnt} / ${list.size()}"
        return result
    }
    List<String> getWarningNotExist(String msg, List<SqlObject> list){
        List<String> result = []
        int notExistCnt = list.findAll{ !it.isExistOnDB }.size()
        if (notExistCnt)
            result << "[${msg}] Not Exsist Object :   ${notExistCnt} / ${list.size()}"
        return result
    }

    List<String> getWarningList(){
        return getWarningList(analysisResultList)
    }

    List<String> getWarningList(List<SqlObject> analysisResultList){
        List<String> warningList = []
        def createTablespaceList    = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TABLESPACE') }
        def createUserList          = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('USER') }
        def createTableList         = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TABLE') }
        def createIndexList         = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('INDEX') }
        def createViewList          = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('VIEW') }
        def createSequenceList      = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('SEQUENCE') }
        def createProcedureList     = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('PROCEDURE') }
        def createPackageList       = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('PACKAGE') }
        def createFunctionList      = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('FUNCTION') }
        def createTriggerList       = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TRIGGER') }
        def createJavaList          = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('JAVA') }
        def createEtcList           = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('ETC') }
        def insertList              = analysisResultList.findAll { it.commandType.equalsIgnoreCase('INSERT') }
        def updateList              = analysisResultList.findAll { it.commandType.equalsIgnoreCase('UPDATE') }
        warningList += getWarningAlreadyExist("create tablespace warning:", createTablespaceList)
        warningList += getWarningAlreadyExist("create user warning:", createUserList)
        warningList += getWarningAlreadyExist("create table warning:", createTableList)
        warningList += getWarningAlreadyExist("create index warning:", createIndexList)
        warningList += getWarningAlreadyExist("create view warning:", createViewList)
        warningList += getWarningAlreadyExist("create sequence warning:", createSequenceList)
        warningList += getWarningAlreadyExist("create procedure warning:", createProcedureList)
        warningList += getWarningAlreadyExist("create package warning:", createPackageList)
        warningList += getWarningAlreadyExist("create function warning:", createFunctionList)
        warningList += getWarningAlreadyExist("create tirgger warning:", createTriggerList)
        warningList += getWarningAlreadyExist("create java warning:", createJavaList)
        warningList += getWarningAlreadyExist("create etc warning:", createEtcList)
        warningList += getWarningNotExist("insert warning:", insertList)
        warningList += getWarningNotExist("update warning:", updateList)
        return warningList
        // check before run SQL
//        if (checkType == SqlMan.CHECK_BEFORE_AND_STOP
//        && (createTableWarningCnt || createIndexWarningCnt || createViewWarningCnt || createSequenceWarningCnt || createFunctionWarningCnt || insertWarningCnt || updateWarningCnt)){
    }




    boolean containsIgnoreCase(List<String> targetList, String compareItem){
        List foundedList = targetList.findAll{ it.equalsIgnoreCase(compareItem) }
        return !!foundedList
    }

}

