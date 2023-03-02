package jaemisseo.man

import jaemisseo.man.bean.SqlSetup
import jaemisseo.man.util.ConnectionGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/2/16
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
class SqlAnalMan {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    class SqlObject{

        String sqlFileName
        Integer seq
        String query
        String executor
        boolean isExistOnDB
        Boolean isOk
        String warnningMessage
        Exception error

        def arrayToCompare

        String commandType
        String objectType
        String objectName

        int objectNameIdx
        int objectTypeIdx

        String schemaName
        String schemaNameForObject
        String ownerName
        String password
        String datafileName
        String tablespaceName
        String tempTablespaceName
        List<String> quotaNames = []
        String userName
        List<String> tableNames = []
        String indexName
        String viewName
        String functionName
        String sequenceName

        int passwordIdx
        int datafileNameIdx
        int tablespaceNameIdx
        int tempTablespaceNameIdx
        List<Integer> quotaNameIdxs = []
        int databaseNameIdx
        int schemaNameIdx
        int userNameIdx
        int ownerNameIdx
        List<Integer> tableNameIdxs = []
        int indexNameIdx
        int viewNameIdx
        int functionNameIdx
        int sequenceNameIdx
    }

    public static final String WARN_MSG_1 = '=> There is no object!'
    public static final String WARN_MSG_2 = '=> Object already exist!'

    String sqlFileName











    // Analysis Sql Query
    SqlObject getAnalyzedObject(String query){
        getAnalyzedObject(query, new SqlSetup())
    }

    SqlObject getAnalyzedObject(String query, SqlSetup opt){
        SqlObject sqlObj = new SqlObject()
//        String queryToCompare = query.replace(")", " ) ").replace("(", " ( ").replaceAll("[,]", " , ").replaceAll("[;]", " ;").replaceAll(/\s{2,}/, " ")
        String sp = "{#-%}"
        String queryToCompare = getReplaceNotInOracleQuote(query, [
            "(" : "${sp}(${sp}",
            ")" : "${sp})${sp}",
            "," : "${sp},${sp}",
            ";" : "${sp};",
            "\n" : sp,
            "\r" : sp,
            "\t" : sp,
            " " : sp
        ])
        sqlObj.with{
            setQuery(query)
            arrayToCompare = queryToCompare.split("([{][#][-][%][}])+")
            commandType = arrayToCompare[0].toUpperCase()
            if (['DECLARE','BEGIN'].contains(commandType)){
                setCommandType('PLSQL')
            }
        }

        switch (sqlObj.commandType){
            case "CREATE":
                sqlObj = analCreate(sqlObj, opt)
                break
            case "ALTER":
                sqlObj = analAlter(sqlObj, opt)
                break
            case "INSERT":
                sqlObj = analInsert(sqlObj, opt)
                break
            case "UPDATE":
                sqlObj = analUpdate(sqlObj, opt)
                break
            case "COMMENT":
                sqlObj = analComment(sqlObj, opt)
                break
            case "GRANT":
                sqlObj = analGrant(sqlObj, opt)
                break
            case "PLSQL":
                sqlObj = analPlsql(sqlObj, opt)
                break
            case "SELECT":
                sqlObj = analSelect(sqlObj, opt)
                break
            case "DELETE":
                sqlObj = analDelete(sqlObj, opt)
                break
            case "DROP":
                sqlObj = analDrop(sqlObj, opt)
                break
            default:
                break
        }
        return sqlObj
    }

    String getObjectType(String query){
        String objectType = ""
        if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_TABLE)).size()){
            objectType = "TABLE"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_INDEX)).size()){
            objectType = "INDEX"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_VIEW)).size()){
            objectType = "VIEW"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_SEQUENCE)).size()){
            objectType = "SEQUENCE"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_TABLESPACE)).size()){
            objectType = "TABLESPACE"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_DATABASE)).size()){
            objectType = "DATABASE"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_SCHEMA)).size()){
            objectType = "SCHEMA"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_USER)).size()){
            objectType = "USER"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_PACKAGE)).size()){
            objectType = "PACKAGE"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_PROCEDURE)).size()){
            objectType = "PROCEDURE"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_FUNCTION)).size()){
            objectType = "FUNCTION"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_TRIGGER)).size()){
            objectType = "TRIGGER"
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_JAVA)).size()){
            objectType = "JAVA"
        }else if (getMatchedList(query, getSqlPattern([SqlMan.CREATE_ETC, SqlMan.CREATE_ETC2])).size()){
            objectType = "ETC"
        }
        return objectType
    }

    String addOR(String pattern, String patternToAdd){
        pattern = pattern ? (pattern+ "|" +patternToAdd) : patternToAdd
        return pattern
    }


    Matcher getMatchedList(String content, pattern){
        String querys = removeNewLine(removeAnnotation(content))
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        Matcher m = p.matcher(querys)
        return m
    }



    String getSqlPattern(int target){
        getSqlPattern([target])
    }
    String getSqlPattern(List targetList){
        String pattern = ''
        targetList.each{
            switch (it){
                case SqlMan.ALL:
                    pattern = getSqlPattern([SqlMan.CREATE, SqlMan.INSERT, SqlMan.UPDATE, SqlMan.ALTER, SqlMan.COMMENT, SqlMan.GRANT, SqlMan.DELETE, SqlMan.DROP, SqlMan.SET, SqlMan.PLSQL])
                    break
                case SqlMan.ALL_WITHOUT_PLSQL:
                    pattern = getSqlPattern([SqlMan.CREATE, SqlMan.INSERT, SqlMan.UPDATE, SqlMan.ALTER, SqlMan.COMMENT, SqlMan.GRANT, SqlMan.DELETE, SqlMan.DROP, SqlMan.SET])
                    break
                case SqlMan.CREATE:
                    pattern = getSqlPattern([SqlMan.CREATE_TABLE, SqlMan.CREATE_INDEX, SqlMan.CREATE_VIEW, SqlMan.CREATE_TABLESPACE, SqlMan.CREATE_DATABASE, SqlMan.CREATE_SCHEMA, SqlMan.CREATE_USER, SqlMan.CREATE_SEQUENCE, SqlMan.CREATE_PROCEDURE, SqlMan.CREATE_PACKAGE, SqlMan.CREATE_FUNCTION, SqlMan.CREATE_TRIGGER, SqlMan.CREATE_JAVA, SqlMan.CREATE_ETC, SqlMan.CREATE_ETC2])
                    break
                case SqlMan.CREATE_TABLE:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+TABLE\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_INDEX:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+INDEX\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_VIEW:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+VIEW\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_SEQUENCE:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+SEQUENCE\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_TABLESPACE:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+TABLESPACE\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_DATABASE:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+DATABASE\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_SCHEMA:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+SCHEMA\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.CREATE_USER:
                    pattern = addOR(pattern, "CREATE[^;]{0,40}\\s+USER\\s+(?:[^;'\"]|(?:'[^']*')|(?:\"[^\"]*\"))+[;]{1}")
                    break
                case SqlMan.CREATE_PACKAGE:
                    pattern = addOR(pattern, "CREATE[^/]{0,40}\\s+PACKAGE\\s+(?:[^/']|(?:'[^']*'))+[/]{1}")
                    break
                case SqlMan.CREATE_PROCEDURE:
                    pattern = addOR(pattern, "CREATE[^/]{0,40}\\s+PROCEDURE\\s+(?:[^/']|(?:'[^']*'))+[/]{1}")
                    break
                case SqlMan.CREATE_FUNCTION:
                    pattern = addOR(pattern, "CREATE[^/]{0,40}\\s+FUNCTION\\s+(?:[^/']|(?:'[^']*'))+RETURN(?:[^/'\"]|(?:'[^']*')|(?:\"[^\"]*\"))+[/]{1}")
                    break
                case SqlMan.CREATE_TRIGGER:
                    pattern = addOR(pattern, "CREATE[^/]{0,40}\\s+TRIGGER\\s+(?:[^/']|(?:'[^']*'))+[/]{1}")
                    break
                case SqlMan.CREATE_JAVA:
                    pattern = addOR(pattern, "CREATE[^/]{0,40}\\s+JAVA\\s+(?:SOURCE|RESOURCE|CLASS)(?:[^/\"]|(?:\"[^\"]*\"))+[/]{1}")
                    break
                case SqlMan.CREATE_ETC:
                    pattern = addOR(pattern, "CREATE[^/]{0,40}\\s+(?:FUNCTION|TRIGGER|PACKAGE|PROCEDURE|JAVA|LIBRARY|TYPE)\\s+(?:[^/']|(?:'[^']*'))+[/]{1}")
                    break
                case SqlMan.CREATE_ETC2:
                    pattern = addOR(pattern, "CREATE\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break

                case SqlMan.INSERT:
                    pattern = addOR(pattern, "INSERT\\s+[^;]{0,40}\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.UPDATE:
                    pattern = addOR(pattern, "UPDATE\\s+[^;]{0,40}\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.ALTER:
                    pattern = addOR(pattern, "ALTER\\s+[^;]{0,40}\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.COMMENT:
                    pattern = addOR(pattern, "COMMENT\\s+[^;]{0,40}\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.GRANT:
                    pattern = addOR(pattern, "GRANT\\s+[^;]{0,40}\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.PLSQL:
                    pattern = addOR(pattern, "(?:DECLARE|BEGIN)\\s+(?:[^/']|(?:'[^']*'))+\\s+END\\s*;\\s*[/]{1}")
                    break
                case SqlMan.SELECT:
                    pattern = addOR(pattern, "SELECT\\s+(?:[^;'()]|(?:'[^']*')|(?:[(][^()]*[)]))+")
                    break
                case SqlMan.DELETE:
                    pattern = addOR(pattern, "DELETE\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.DROP:
                    pattern = addOR(pattern, "DROP\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                case SqlMan.SET:
                    pattern = addOR(pattern, "SET\\s+(?:[^;']|(?:'[^']*'))+[;]{1}")
                    break
                default:
                    break
            }
        }
        return pattern
    }



    /*************************
     *  CREATE
     *************************/
    SqlObject analCreate(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        String objectType = getObjectType(obj.query)
        words.eachWithIndex{ String word, int idx ->
            word = word.toUpperCase()
            if (!obj.objectTypeIdx && word.equalsIgnoreCase(objectType)){
                obj.objectTypeIdx = idx
                if (objectType.equalsIgnoreCase("PACKAGE") && words[idx+1].equalsIgnoreCase("BODY")) {
                    objectType = "PACKAGE BODY"
                    obj.objectNameIdx = obj.objectTypeIdx + 2
                }else if (objectType.equalsIgnoreCase("TYPE") && words[idx+1].equalsIgnoreCase("BODY")){
                    objectType = "TYPE BODY"
                    obj.objectNameIdx = obj.objectTypeIdx + 2
                }else {
                    obj.objectNameIdx = obj.objectTypeIdx + 1
                }

            }else if (word.equalsIgnoreCase("TABLESPACE")){
                if (words[idx -1].equalsIgnoreCase("TEMPORARY"))
                    obj.tempTablespaceNameIdx = idx +1
                else
                    obj.tablespaceNameIdx = idx +1

            }else if (word.equalsIgnoreCase("QUOTA")){
                (0..3).each{
                    int tempIdx = idx + it
                    if (words[tempIdx].equalsIgnoreCase("ON"))
                        obj.quotaNameIdxs << tempIdx +1
                }
            }else if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
            switch (objectType){
                case 'TABLE':
                    if (word.equalsIgnoreCase("REFERENCES"))
                        obj.tableNameIdxs << idx + 1
                    break
                case 'INDEX':
                    if (word.equalsIgnoreCase("ON") && words[idx-1].equalsIgnoreCase(words[obj.objectNameIdx]))
                        obj.tableNameIdxs << idx + 1
                    break
                case 'VIEW':
                    break
                case 'SEQUENCE':
                    break
                case 'FUNCTION':
                    break
                case 'TABLESPACE':
                    if (word.equalsIgnoreCase("DATAFILE"))
                        obj.datafileNameIdx = idx + 1
                    break
                case 'DATABASE':
                    if (word.equalsIgnoreCase("DATABASE"))
                        obj.databaseNameIdx = idx + 1
                    break
                case 'SCHEMA':
                    if (word.equalsIgnoreCase("SCHEMA")){
                        obj.schemaNameIdx = idx + 1
                        obj.schemaName = words[obj.schemaNameIdx]
                    }else if (word.equalsIgnoreCase("OWNER")){
                        obj.ownerNameIdx = idx + 2
                        obj.ownerName = words[obj.ownerNameIdx]
                    }
                    break
                case 'USER':
                    if ( (opt.vendor?:'').equalsIgnoreCase(ConnectionGenerator.POSTGRESQL) ){
                        if (word.equalsIgnoreCase("PASSWORD"))
                            obj.passwordIdx = idx + 1
                    }else{
                        if (word.equalsIgnoreCase("IDENTIFIED"))
                            obj.passwordIdx = idx + 2
                    }
                    break
                default:
                    break
            }
        }
        switch (objectType){
            case 'TABLE':
                obj.tableNameIdxs << obj.objectNameIdx
                break
            case 'INDEX':
                obj.indexNameIdx = obj.objectNameIdx
                break
            case 'VIEW':
                obj.viewNameIdx = obj.objectNameIdx
                break
            case 'SEQUENCE':
                obj.sequenceNameIdx = obj.objectNameIdx
                break
            case 'FUNCTION':
                obj.functionNameIdx = obj.objectNameIdx
                break
            case 'TABLESPACE':
                obj.tablespaceNameIdx = obj.objectNameIdx
                break
            case 'DATABASE':
                obj.databaseNameIdx = obj.objectNameIdx
                break
            case 'SCHEMA':
                obj.schemaNameIdx = obj.objectNameIdx
                obj.schemaName = words[obj.schemaNameIdx]

                break
            case 'USER':
                obj.userNameIdx = obj.objectNameIdx
                obj.userName = words[obj.objectNameIdx]
                break
            default:
                break
        }
        obj.with{
            setObjectType(objectType)
        }
        return analObjectName(obj)
    }



    /*************************
     *  ALTER
     *************************/
    SqlObject analAlter(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectTypeIdx = 1
        obj.objectNameIdx = 2
        obj.objectType = words[obj.objectTypeIdx]

        words.eachWithIndex{ String word, int idx ->
            word = word.toUpperCase()
            if (word.equalsIgnoreCase(obj.objectType) && !obj.objectTypeIdx){
                obj.objectTypeIdx = idx
                obj.objectNameIdx = obj.objectTypeIdx + 1

            }else if (word.equalsIgnoreCase("TABLESPACE")){
                if (words[idx -1].equalsIgnoreCase("TEMPORARY"))
                    obj.tempTablespaceNameIdx = idx +1
                else
                    obj.tablespaceNameIdx = idx +1

            }else if (word.equalsIgnoreCase("QUOTA")){
                (0..2).each{
                    int tempIdx = idx + it
                    if (words[tempIdx].equalsIgnoreCase("ON"))
                        obj.quotaNameIdxs << tempIdx +1
                }
            }
            switch (obj.objectType){
                case 'TABLE':
                    if (word.equalsIgnoreCase("OWNER")){
                        obj.ownerNameIdx = idx + 2
                        obj.ownerName = words[obj.ownerNameIdx]
                    }
                    break
                case 'INDEX':
                    if (word.equalsIgnoreCase("ON") && words[idx-1].equalsIgnoreCase(words[obj.objectNameIdx]))
                        obj.tableNameIdxs << idx + 1
                    break
                case 'VIEW':
                    break
                case 'SEQUENCE':
                    break
                case 'FUNCTION':
                    break
                case 'TABLESPACE':
                    if (word.equalsIgnoreCase("DATAFILE"))
                        obj.datafileNameIdx = idx + 1
                    break
                case 'DATABASE':
                    break
                case 'SCHEMA':
                    if (word.equalsIgnoreCase("SCHEMA")){
                        obj.schemaNameIdx = idx + 1
                        obj.schemaName = words[obj.schemaNameIdx]
                    }else if (word.equalsIgnoreCase("OWNER")){
                        obj.ownerNameIdx = idx + 2
                        obj.ownerName = words[obj.ownerNameIdx]
                    }
                    break
                case 'USER':
                    if ( (opt.vendor?:'').equalsIgnoreCase(ConnectionGenerator.POSTGRESQL) ){
                        if (word.equalsIgnoreCase("PASSWORD"))
                            obj.passwordIdx = idx + 1
                    }else{
                        if (word.equalsIgnoreCase("IDENTIFIED"))
                            obj.passwordIdx = idx + 2
                    }
                    break
                default:
                    break
            }
        }

        switch (obj.objectType){
            case 'TABLE':
                obj.tableNameIdxs << obj.objectNameIdx
                break
            case 'INDEX':
                obj.indexNameIdx = obj.objectNameIdx
                break
            case 'VIEW':
                obj.viewNameIdx = obj.objectNameIdx
                break
            case 'SEQUENCE':
                obj.sequenceNameIdx = obj.objectNameIdx
                break
            case 'FUNCTION':
                obj.functionNameIdx = obj.objectNameIdx
                break
            case 'TABLESPACE':
                obj.tablespaceNameIdx = obj.objectNameIdx
                break
            case 'DATABASE':
                obj.databaseNameIdx = obj.objectNameIdx
                break
            case 'SCHEMA':
                obj.schemaNameIdx = obj.objectNameIdx
                obj.schemaName = words[obj.objectNameIdx]
                break
            case 'USER':
                obj.userNameIdx = obj.objectNameIdx
                obj.userName = words[obj.objectNameIdx]
                break
            default:
                break
        }
        return analObjectName(obj)
    }



    /*************************
     *  INSERT
     *************************/
    SqlObject analInsert(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'TABLE'
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase('INTO')){
                obj.objectNameIdx = idx + 1
                obj.tableNameIdxs << obj.objectNameIdx
            }else if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
        }
        return analObjectName(obj)
    }



    /*************************
     *  UPDATE
     *************************/
    SqlObject analUpdate(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'TABLE'
        obj.objectNameIdx = 1
        obj.tableNameIdxs << obj.objectNameIdx
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
        }
        return analObjectName(obj)
    }

    /*************************
     *  SELECT
     *************************/
    SqlObject analSelect(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectType = ''
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
        }
        return analObjectName(obj)
    }

    /*************************
     *  DELETE
     *************************/
    SqlObject analDelete(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'TABLE'
        words.eachWithIndex{ String word, int idx ->
            if (idx == 2){
                if (word == 'FROM')
                    obj.objectNameIdx = idx +1
                else
                    obj.objectNameIdx = idx
                obj.objectName = words[obj.objectNameIdx]
            }
            if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
        }
        return analObjectName(obj)
    }

    /*************************
     *  DROP
     *************************/
    SqlObject analDrop(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        words.eachWithIndex{ String word, int idx ->
            if (idx == 2){
                obj.objectTypeIdx = idx
                obj.objectType = word.toUpperCase()
            }else if (idx == 3){
                if (obj.objectType.equalsIgnoreCase("PACKAGE") && word.equalsIgnoreCase("BODY")) {
                    obj.objectNameIdx = idx + 1
                }else if (obj.objectType.equalsIgnoreCase("TYPE") && word.equalsIgnoreCase("BODY")){
                    obj.objectNameIdx = idx + 1
                }else{
                    obj.objectNameIdx = idx
                }
            }
        }
        return analObjectName(obj)
    }


    /*************************
     *  COMMENT
     *************************/
    SqlObject analComment(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase("ON")){
                obj.objectTypeIdx = idx + 1
                obj.objectNameIdx = idx + 2
            }
        }
        String objectName = words[obj.objectNameIdx]
        String objectType = words[obj.objectTypeIdx]
        obj.objectType = objectType
        def array = objectName.split('[.]')
        if (objectType.equalsIgnoreCase("COLUMN")){
            if (array.size() == 3){
                obj.schemaNameForObject = array[0]
                obj.objectName = "${array[1]}.${array[2]}"
            }else if (array.size() == 2){
                obj.objectName = "${array[0]}.${array[1]}"
            }
        }else if (objectType.equalsIgnoreCase("TABLE")){
            if (array.size() == 2){
                obj.schemaNameForObject = array[0]
                obj.objectName = array[1]
            }else if (array.size() == 1){
                obj.objectName = array[0]
            }
        }
        return obj
    }



    /*************************
     *  GRANT
     *************************/
    SqlObject analGrant(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'USER'
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase('TO')){
                obj.objectNameIdx = idx + 1
                obj.userName = words[obj.objectNameIdx]
            }
        }
        return analObjectName(obj)
    }

    /*************************
     *  PLSQL
     *************************/
    SqlObject analPlsql(SqlObject obj, SqlSetup opt){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'PLSQL'
        return obj
    }



    SqlObject analObjectName(SqlObject obj){
        if (obj.objectNameIdx){
            String objectName = obj.arrayToCompare[obj.objectNameIdx]
            if (objectName.indexOf('.') != -1){
                def array = objectName.split('[.]')
                obj.schemaNameForObject = array[0]
                obj.objectName = array[1]
            }else{
                obj.objectName = objectName
            }
        }
        return obj
    }





    SqlObject getReplacedObject(SqlObject obj, SqlSetup opt, Integer seq){
        //- Replace Some With Some On Query
        def words = obj.arrayToCompare
        String target
        if (opt){
            if (opt.replaceAll){
                opt.replaceAll.each{ String before, String after ->
                    words.collect{ it.replaceAll(before, after) }
                }
            }
            if (obj.userName){
                if (obj.objectType.equalsIgnoreCase("USER") || obj.commandType.equalsIgnoreCase("GRANT")){
                    target = obj.userName
                    ifReturn(opt.replaceUser, target).each{ String replaceStr ->
                        replaceName(words, obj.objectNameIdx, replaceStr)
                        obj.objectName = replaceStr
                        obj.userName = replaceStr
                    }
                }
            }
            if (obj.schemaName){
                if (obj.objectType.equalsIgnoreCase("SCHEMA")){
                    target = obj.schemaName
                    ifReturn(opt.replaceSchema, target).each{ String replaceStr ->
                        replaceName(words, obj.objectNameIdx, replaceStr)
                        obj.objectName = replaceStr
                        obj.schemaName = replaceStr
                    }
                }
            }
            if (opt.replaceForceSchemaForObject){
                boolean noTarget = (
                        obj.objectType.equalsIgnoreCase("USER")
                        || obj.commandType.equalsIgnoreCase("GRANT")
                        || obj.objectType.equalsIgnoreCase("INDEX")
                        || obj.objectType.equalsIgnoreCase("SCHEMA")
                        || obj.objectType.equalsIgnoreCase("TABLESPACE")
                        || obj.objectType.equalsIgnoreCase("DATABASE")
                )
                if (!noTarget){
                    String replaceStr = opt.replaceForceSchemaForObject
                    replaceName(words, obj.objectNameIdx, "${replaceStr}.${obj.objectName}" as String) //TODO: 이건 강제고 있으면 체크해서 해볼까?
                    obj.schemaNameForObject = replaceStr
                }
            }else if (obj.schemaNameForObject){
                target = obj.schemaNameForObject
                ifReturn(opt.replaceSchemaForObject, target).each{ String replaceStr ->
                    replaceName(words, obj.objectNameIdx, "${replaceStr}.${obj.objectName}" as String) //TODO: 이건 강제고 있으면 체크해서 해볼까?
                    obj.schemaNameForObject = replaceStr
                }
            }
            if (obj.ownerName){
                target = obj.ownerName
                logger.info "OWNER!!!! (${opt.replaceOwner}) ==> ${obj.ownerName} ${obj.ownerNameIdx}"
                ifReturn(opt.replaceOwner, target).each{ String replaceStr ->
                    replaceName(words, obj.ownerNameIdx, replaceStr)
                    obj.ownerName = replaceStr
                }
            }
            if (obj.tablespaceNameIdx){
                target = words[obj.tablespaceNameIdx]
                ifReturn(opt.replaceTablespace, target).each{ String replaceStr ->
                    replaceName(words, obj.tablespaceNameIdx, replaceStr)
                    if (obj.objectType.equalsIgnoreCase("TABLESPACE")){
                        obj.objectName = replaceStr
                        obj.tablespaceName = replaceStr
                    }else{
                        obj.tablespaceName = replaceStr
                    }
                }
            }
            if (obj.tempTablespaceNameIdx){
                target = words[obj.tempTablespaceNameIdx]
                ifReturn(opt.replaceTablespace, target).each{ String replaceStr ->
                    replaceName(words, obj.tempTablespaceNameIdx, replaceStr)
                    obj.tempTablespaceName = replaceStr
                }
            }
            if (obj.quotaNameIdxs){
                obj.quotaNameIdxs.each{
                    target = words[it]
                    ifReturn(opt.replaceTablespace, target).each{ String replaceStr ->
                        replaceName(words, it, replaceStr)
                        obj.quotaNames << replaceStr
                    }
                }
            }
            if (obj.datafileNameIdx){
                target = words[obj.datafileNameIdx]
                target = target.substring(1, target.length() -1)
                ifReturn(opt.replaceDatafile, target).each{ String replaceStr ->
                    String wordWithQuote = "'${replaceStr}'" as String
                    replaceName(words, obj.datafileNameIdx, wordWithQuote)
                    obj.datafileName = wordWithQuote
                }
            }
            if (obj.passwordIdx){
                target = words[obj.passwordIdx]
                target = target.substring(1, target.length() -1)
                ifReturn(opt.replacePassword, target).each{ String replaceStr ->
                    String wordWithQuote
                    if ( (opt.vendor?:'').equalsIgnoreCase(ConnectionGenerator.POSTGRESQL) ){
                        wordWithQuote = "'${replaceStr}'" as String
                    }else{
                        wordWithQuote = "\"${replaceStr}\"" as String
                    }
                    replaceName(words, obj.passwordIdx, wordWithQuote)
                    obj.password = wordWithQuote
                }
            }
            if (obj.commandType.equalsIgnoreCase("COMMENT")){
                def array = obj.objectName.split('[.]')
                String tab = array[0]
                String replaceTab = tab
                ifReturn(opt.replaceTable, tab).each{ String replaceStr ->
                    replaceTab = replaceStr
                }
                if (obj.objectType.equalsIgnoreCase("COLUMN") && array.size() == 2){
                    replaceTab = "${replaceTab}.${array[1]}" as String
                }else if (obj.objectType.equalsIgnoreCase("TABLE")){
                    obj.tableNames << replaceTab
                }
                obj.objectName = replaceTab
                String replaceStr = ((obj.schemaName) ? "${obj.schemaName}.${replaceTab}" : "${replaceTab}") as String
                replaceName(words, obj.objectNameIdx, replaceStr)
            }
            if (obj.tableNameIdxs && !obj.commandType.equalsIgnoreCase("COMMENT")){
                obj.tableNameIdxs.each{
                    target = words[it]
                    String replaceStr = getReplacedName(target, opt.replaceSchemaForObject, opt.replaceForceSchemaForObject, opt.replaceTable)
                    replaceName(words, it, replaceStr)
                    obj.tableNames << replaceStr
                }
                if (obj.objectType.equalsIgnoreCase("TABLE"))
                    obj.objectName = obj.tableNames[0]
            }
            if (obj.functionNameIdx){
                target = words[obj.functionNameIdx]
                String replaceStr = getReplacedName(target, opt.replaceSchemaForObject, opt.replaceForceSchemaForObject, opt.replaceFunction)
                replaceName(words, obj.functionNameIdx, replaceStr)
                obj.functionName = replaceStr
                if (obj.objectType.equalsIgnoreCase("FUNCTION"))
                    obj.objectName = obj.functionName
            }
            if (obj.viewNameIdx){
                target = words[obj.viewNameIdx]
                String replaceStr = getReplacedName(target, opt.replaceSchemaForObject, opt.replaceForceSchemaForObject, opt.replaceView)
                replaceName(words, obj.viewNameIdx, replaceStr)
                obj.viewName = replaceStr
                if (obj.objectType.equalsIgnoreCase("VIEW"))
                    obj.objectName = obj.viewName
            }
            if (obj.sequenceNameIdx){
                target = words[obj.sequenceNameIdx]
                String replaceStr = getReplacedName(target, opt.replaceSchemaForObject, opt.replaceForceSchemaForObject, opt.replaceSequence)
                replaceName(words, obj.sequenceNameIdx, replaceStr)
                obj.sequenceName = replaceStr
                if (obj.objectType.equalsIgnoreCase("SEQUENCE"))
                    obj.objectName = obj.sequenceName
            }
            if (obj.indexNameIdx){
                target = words[obj.indexNameIdx]
//                String replaceStr = getReplacedName(target, opt.replaceSchemaForObject, opt.replaceForceSchemaForObject, opt.replaceIndex)
                ifReturn(opt.replaceTable, target).each{ String replaceStr ->
                    replaceName(words, obj.indexNameIdx, replaceStr)
                    obj.indexName = replaceStr
                    if (obj.objectType.equalsIgnoreCase("INDEX"))
                        obj.objectName = obj.indexName
                }

            }
            analObjectName(obj)
        }

        //- Regenerate Query
        obj.query = words.join(" ")
        return obj
    }


    private static String getReplacedName(String target, def replaceSchemaForObject, def replaceForceSchemaForObject, def replaceObject){
        String sNm
        String oNm
        target.split('[.]').eachWithIndex{ String o, int i ->
            if (i==0){
                oNm = o
            }else if (i==1){
                sNm = oNm
                oNm = o
            }
        }
        String replaceSNm = sNm
        String replaceONm = oNm
        if (replaceForceSchemaForObject){
            replaceSNm = replaceForceSchemaForObject
        }else{
            ifReturn(replaceSchemaForObject, sNm).each{ String replaceStr ->
                replaceSNm = replaceStr
            }
        }
        ifReturn(replaceObject, oNm).each{ String replaceStr ->
            replaceONm = replaceStr
        }
        return (replaceSNm) ? ("${replaceSNm}.${replaceONm}" as String) : replaceONm
    }

    private static def ifReturn(def replaceObj, String target){
        List result = []
        if (!replaceObj || !target){

        }else if (replaceObj instanceof String){
            result << (replaceObj as String)

        }else{
            replaceObj.findAll{
                (it.key as String).equalsIgnoreCase(target)
            }.each{
                result << (it.value as String)
            }
        }
        return result
    }

    private static void replaceName(def words, int index, String wordToReplace){
        words[index] = wordToReplace;
    }
















    static String removeAnnotation(String query){
//        return query.replaceAll(/^\s*\-\-.*[\r\n]{1}/, " ")
        return query.replaceAll(/(?m)^\s*\-\-.*[\r\n]{1}/, " ")
    }
    static String removeNewLine(String query){
        return query.replaceAll(/[\r\n]/, " ")
    }
    static String removeLastSemicoln(String query){
        return query.replaceAll(/[;]\s*$/, '')
    }
    static String removeLastSlash(String query){
        return query.replaceAll(/[\/]\s*$/, '')
    }


    static String getReplaceNotInOracleQuote(String query, def replaceMap){
        Map<Long, String> map = [:]
        Map<Long, String> validReplaceMap
        // Get All Index
        Map<String, List> indexListMap = [:]
        replaceMap.each{ String replaceTarget, String replacement ->
            indexListMap[replaceTarget] = query.findIndexValues{ it == replaceTarget }.sort{ long a, long b -> b <=> a }
        }
        List singleQuoteIndexList = query.findIndexValues{ it == '\'' }
        // Get protectRangeList
        int i = -1
        int quoteCnt = 0
        int startQuoteIdx = -1
        int endQuoteIdx = -1
        List<Long> protectRangeList = []
        while(singleQuoteIndexList[++i] != null){
            quoteCnt++
            if (quoteCnt == 1){
                startQuoteIdx = singleQuoteIndexList[i]
            }else if (quoteCnt == 2){
                if (i+1 < singleQuoteIndexList.size() && Math.abs(singleQuoteIndexList[i] - singleQuoteIndexList[i+1]) == 1){
                }else{
                    endQuoteIdx = singleQuoteIndexList[i]
                    protectRangeList << startQuoteIdx
                    protectRangeList << endQuoteIdx
                    quoteCnt = 0
                    startQuoteIdx = -1
                    endQuoteIdx = -1
                }
            }else if (endQuoteIdx == -1 && quoteCnt == 3){
                quoteCnt = 1
            }
        }
        // Get validReplaceMap
        indexListMap.each{ String replaceTarget, List indexList ->
            indexList.each{ Long idx -> map[idx] = replaceTarget }
        }
        validReplaceMap = map.sort{ a, b -> b.key <=> a.key }.findAll{
            boolean isProtected = false
            for (int idx=0; idx<protectRangeList.size(); idx+=2){
                Long startRange = protectRangeList[idx]
                Long endRange = protectRangeList[idx+1]
                Long target = it.key as Long
                if (startRange <= target && target <= endRange){
                    isProtected = true
                    break
                }
            }
            return !isProtected
        }
        // Get Replaced String
        validReplaceMap.each{
            query = replaceIndexRange(query, it.key as int, replaceMap[it.value])
        }
        return query
    }

    static String replaceIndexRange(String target, int index, String replacement){
        return replaceIndexRange(target, index, 1, replacement)
    }

    static String replaceIndexRange(String target, int startIndex, int count, String replacement){
        return "${target.substring(0, startIndex)}${replacement}${target.substring(startIndex + count)}"
    }



}
