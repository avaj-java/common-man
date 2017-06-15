package jaemisseo.man

import groovy.sql.Sql
import jaemisseo.man.util.SqlSetup
import jaemisseo.man.util.Util

import java.sql.SQLException
import java.util.regex.Matcher

/**
 * Created by sujung on 2016-09-24.
 */
class SqlMan extends SqlAnalMan{

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
    public static final int CREATE = 10
    public static final int CREATE_TABLE = 11
    public static final int CREATE_INDEX = 12
    public static final int CREATE_VIEW = 13
    public static final int CREATE_SEQUENCE = 14
    public static final int CREATE_FUNCTION = 15
    public static final int CREATE_TABLESPACE = 16
    public static final int CREATE_USER = 17
    public static final int ALTER = 20
    public static final int ALTER_TABLE = 21
    public static final int ALTER_USER = 22
    public static final int INSERT = 30
    public static final int UPDATE = 40
    public static final int COMMENT = 50
    public static final int GRANT = 60

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
        gOpt.url         = (opt.url) ? opt.url : getUrl(opt.vendor, opt.ip, opt.port, opt.db)
        gOpt.driver      = (opt.driver) ? opt.driver : getDriver(opt.vendor)
        return this
    }



    SqlMan connect(){
        close()
        return connect(gOpt)
    }

    SqlMan connect(SqlSetup localOpt){
        SqlSetup opt = gOpt.clone().merge(localOpt)
        opt.url    = (opt.url) ? opt.url : getUrl(localOpt.vendor, localOpt.ip, localOpt.port, localOpt.db)
        opt.driver = (opt.driver) ? opt.driver : getDriver(localOpt.vendor)
        this.sql = Sql.newInstance(opt.url, opt.user, opt.password, opt.driver)
        connectedOpt = opt
        return this
    }



    SqlMan doOption(){
        return doOption(gOpt)
    }

    SqlMan doOption(SqlSetup localOpt){
        connectedOpt = gOpt.clone().merge(localOpt)
        return this
    }



    SqlMan close(){
        if (sql) sql.close()
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

    SqlMan replace(SqlSetup localOpt){
        doOption(localOpt)
        // analysis
        Matcher m = getMatchedList(this.sqlContent, this.patternToGetQuery)
        analysisResultList = getAnalysisResultList(m)
        return this
    }

    SqlMan checkBefore(SqlSetup localOpt){
        try {
            def existObjectList
            def existTablespaceList
            def existUserList
            connect(localOpt)

            //Analysis -
            existObjectList = sql.rows("SELECT OBJECT_NAME, OBJECT_TYPE, OWNER AS SCHEME FROM ALL_OBJECTS")

            //Check Exist
            println 'Check OBJECT...'
            Util.eachWithProgressBar(analysisResultList, 20){ SqlObject obj ->
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

            //Check Exist TableSpace
            def resultsForTablespace = analysisResultList.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("TABLESPACE") }
            if (resultsForTablespace) {
                print ' - Check TABLESPACE...'
                existTablespaceList = sql.rows("SELECT TABLESPACE_NAME AS OBJECT_NAME, 'TABLESPACE' AS OBJECT_TYPE FROM USER_TABLESPACES")
                resultsForTablespace.each { SqlObject obj ->
                    obj.isExistOnDB = isExistOnDB(obj, existTablespaceList)
                    if (obj.isExistOnDB)
                        obj.warnningMessage = WARN_MSG_2
                }
                println ' DONE'
            }

            //Check Exist User
            def resultsForUser = analysisResultList.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("USER") }
            if (resultsForUser){
                print ' - Check USER...'
                existUserList = sql.rows("SELECT USERNAME AS OBJECT_NAME, 'USER' AS OBJECT_TYPE FROM ALL_USERS")
                resultsForUser.each { SqlObject obj ->
                    obj.isExistOnDB = isExistOnDB(obj, existUserList)
                    if (obj.isExistOnDB)
                        obj.warnningMessage = WARN_MSG_2
                }
                println ' DONE'
            }

        }catch(Exception e){
            e.printStackTrace()
            throw e
        }finally{
            close()
        }

        //Error Check
        List warnList = analysisResultList.findAll{ it.warnningMessage }
        if ( !localOpt.modeSqlIgnoreErrorCheckBefore && warnList )
            throw new SQLException(warnList[0].warnningMessage)

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





    List<SqlObject> getAnalysisResultList(Matcher m){
        def resultList = []
        println "Replace Object Name..."
        Util.eachWithCountAndProgressBar( (m.findAll() as List), 20) { String query, int count ->
            resultList << getReplacedObject(getAnalysisObject(query), connectedOpt, count)
        }
        return resultList
    }


    void runSql(SqlSetup localOpt, List<SqlObject> analysisResultList){
        connect(localOpt)
        sql.withTransaction{
            println "Executing Sqls..."
            Util.eachWithProgressBar(analysisResultList, 20){ SqlObject result ->
                try{
                    String query = result.query
                    sql.execute(removeLastSemicoln(removeLastSlash(query)))
                    result.isOk = true
                }catch(Exception e){
                    result.isOk = false
                    result.error = e
                    if (!localOpt.modeSqlIgnoreErrorExecute)
                        throw e
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
                option      :optionList.join(' | '),
                pattern     :patternToGetQuery,
                matchedCount  :results.size(),
                succeededCount:results.findAll{ it.isOk }.size(),
                failedCount   :results.findAll{ !it.isOk }.size(),
                summary     :getSummary(results).findAll{ it.value.all > 0 },
//                analysisResultList:analysisResultList
        ]
    }



    /**
     * Report 'Before Check' With Console
     */
    SqlMan reportAnalysis(){
        List<String> warningList = getWarningList()
        warningList.each{
            println it
        }
        reportGeneratedQuerys()
        return this
    }

    SqlMan reportGeneratedQuerys(){
        println ""
        println ""
        println "<QUERY>"
        analysisResultList.each{
            println "\n[${it.sqlFileName}] ${it.seq} ${it.warnningMessage}"
            println "${it.query}"
        }
        return this
    }

    /**
     * Report 'SQL Result' With Console
     */
    SqlMan reportResult(){
        if (resultReportMap){
            println ""
            println ""
            println "<REPORT>"
            Map mainReportMap = resultReportMap.findAll{ it.key != 'summary' }
            Map summaryReportMap = resultReportMap.summary
            if (mainReportMap){
                println "---------------"
                int longestStringLength = Util.getLongestLength(mainReportMap.keySet().toList())
                mainReportMap.each{
                    String item = it.key.toString().toUpperCase()
                    String spacesToLineUp = Util.getSpacesToLineUp(item, longestStringLength)
                    println "${item}:${spacesToLineUp} ${it.value}"
                }
            }

            if (summaryReportMap){
                println "---------------"
                int longestStringLength = Util.getLongestLength(summaryReportMap.keySet().toList())
                summaryReportMap.each{
                    String item = it.key.toString().toUpperCase()
                    String spacesToLineUp = Util.getSpacesToLineUp(item, longestStringLength)
                    println "${item}:${spacesToLineUp} ${it.value}"
                }
            }

            println ""
            println ""
            println ""
        }
        return this
    }






    String getUrl(String vendor, String ip, String port, String db){
        String url
        switch (vendor.toUpperCase()) {
            case SqlMan.ORACLE:
                url = "jdbc:oracle:thin:@${ip}:${port}:${db}"
                break
            default:
                url = "jdbc:oracle:thin:@${ip}:${port}:${db}"
                break
        }
        return url
    }

    String getDriver(String vendor){
        String driver
        switch (vendor.toUpperCase()) {
            case SqlMan.ORACLE:
                driver = "oracle.jdbc.driver.OracleDriver"
                break
            default:
                driver = "oracle.jdbc.driver.OracleDriver"
                break
        }
        return driver
    }





    Map getSummary(List<SqlObject> resultList){
        def createTableList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("TABLE") }
        def createIndexList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("INDEX") }
        def createViewList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("VIEW") }
        def createSequenceList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("SEQUENCE") }
        def createFunctionList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("FUNCTION") }
        def createTablespaceList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("TABLESPACE") }
        def createUserList = resultList.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("USER") }
        def alterList = resultList.findAll{ it.commandType.equalsIgnoreCase('ALTER') }
        def commentList = resultList.findAll{ it.commandType.equalsIgnoreCase('COMMENT') }
        def grantList = resultList.findAll{ it.commandType.equalsIgnoreCase('GRANT') }
        def insertList = resultList.findAll{ it.commandType.equalsIgnoreCase('INSERT') }
        def updateList = resultList.findAll{ it.commandType.equalsIgnoreCase('UPDATE') }
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
                'create function':[
                        all: createFunctionList.size(),
                        o: createFunctionList.findAll{ it.isOk }.size(),
                        x: createFunctionList.findAll{ !it.isOk }.size()
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
        String objectName = sqlObj.objectName
        int idx = objectName.indexOf(".")
        objectName = (idx == -1) ? objectName : objectName.substring(idx+1)
        def equalList = catalogList.findAll{ Map<String, String> row ->
            return row["OBJECT_NAME"].equalsIgnoreCase(objectName) && row["OBJECT_TYPE"].equalsIgnoreCase(sqlObj.objectType) && row["SCHEME"].equalsIgnoreCase(sqlObj.schemeName)
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
        def createFunctionList      = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('FUNCTION') }
        def insertList              = analysisResultList.findAll { it.commandType.equalsIgnoreCase('INSERT') }
        def updateList              = analysisResultList.findAll { it.commandType.equalsIgnoreCase('UPDATE') }
        warningList += getWarningAlreadyExist("create tablespace warning:", createTablespaceList)
        warningList += getWarningAlreadyExist("create user warning:", createUserList)
        warningList += getWarningAlreadyExist("create table warning:", createTableList)
        warningList += getWarningAlreadyExist("create index warning:", createIndexList)
        warningList += getWarningAlreadyExist("create view warning:", createViewList)
        warningList += getWarningAlreadyExist("create sequence warning:", createSequenceList)
        warningList += getWarningAlreadyExist("create function warning:", createFunctionList)
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

