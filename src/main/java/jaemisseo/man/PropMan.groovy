package jaemisseo.man

import jaemisseo.man.bean.FileSetup
import jaemisseo.man.util.CommitObject
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujung on 2016-09-25.
 */
class PropMan {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Properties properties = new Properties()
    Properties lastCommitProperties  = new Properties()
    List<CommitObject> commitStackList = []
    int headIndex = -1

    String nowPath
    String filePath
    Closure beforeGetClosure

    boolean modeIgnoreBeforeGetClosure




    PropMan(){
        init()
    }

    PropMan(Map propMap){
        init()
        merge(propMap)
    }

    PropMan(String filePath){
        init()
        readFile(filePath)
    }

    PropMan(File file){
        init()
        readFile(file)
    }

    PropMan(Map propMap, List<String> propertyNameFilterTargetList){
        init()
        if (propertyNameFilterTargetList)
            merge(propMap, propertyNameFilterTargetList)
        else
            merge(propMap)
    }

    PropMan(String filePath, List<String> propertyNameFilterTargetList){
        Map propMap = new PropMan(filePath).properties
//        PropMan(propMap, propertyNameFilterTargetList)
        init()
        if (propertyNameFilterTargetList)
            merge(propMap, propertyNameFilterTargetList)
        else
            merge(propMap)
    }

    PropMan(File file, List<String> propertyNameFilterTargetList){
        Map propMap = new PropMan(file).properties
//        PropMan(propMap, propertyNameFilterTargetList)
        init()
        if (propertyNameFilterTargetList)
            merge(propMap, propertyNameFilterTargetList)
        else
            merge(propMap)
    }


    PropMan init(){
        nowPath = System.getProperty('user.dir')
        return this
    }

    PropMan clear(){
        properties.clear()
        return this
    }

    PropMan setBeforeGetProp(Closure beforeGetClosure){
        this.beforeGetClosure = beforeGetClosure
        return this
    }

    /**********
     * HAS DATA
     **********/
    boolean has(String key){
        return properties.containsKey(key)
    }

    /**********
     * SET DATA
     **********/
    void set(String key, def data){
        properties[key] = data
    }

    /**********
     * GET DATA
     **********/
    def get(String key){
        return get(key, null)
    }

    def get(String key, Closure beforeClosure){
        def val = properties[key]
        if (val) {
            if (!modeIgnoreBeforeGetClosure && (beforeClosure || beforeGetClosure)){
                if (beforeClosure)
                    beforeClosure(key, val)
                else if (beforeGetClosure)
                    beforeGetClosure(key, val)
                val = properties[key]
            }
        }
        return val
    }

    def getRaw(String key){
        return get(key, { k, v -> })
    }

    String getString(String key){
        return (get(key) as String)
    }

    Integer getInteger(String key){
        String value = get(key)
        return (!value) ? null : Integer.parseInt(value)
    }

    Boolean getBoolean(String key){
        String value = get(key)
        return (!value) ? null : (value != '0' && value != 'false') ? true : false
    }

    Boolean getBoolean(String key, boolean defaultValue){
        Boolean value = getBoolean(key)
        return (value == null) ? defaultValue : value
    }

    Boolean getBoolean(List<String> keyList){
        return keyList.findAll{ key -> getBoolean(key) }.size() > 0
    }



    /*************************
     *
     * GET PARSED JSON DATA
     *
     *************************/
    def parse(String key){
        parse(key, null)
    }

    def parse(String key, Closure beforeClosure){
        def val = properties[key]
        if (val && val instanceof String){
            if (!modeIgnoreBeforeGetClosure && (beforeClosure || beforeGetClosure)){
                if (beforeClosure)
                    beforeClosure(key, val)
                else if (beforeGetClosure)
                    beforeGetClosure(key, val)
                val = properties[key]
            }
            String valToCompare = val.toString().trim()
            int lastIdx = valToCompare.length() -1
            if ( (valToCompare.indexOf('[') == 0 && valToCompare.lastIndexOf(']') == lastIdx) || (valToCompare.indexOf('{') == 0 && valToCompare.lastIndexOf('}') == lastIdx) ){
//                val = val.toString().replace('\\', '\\\\')
                def obj = new JsonSlurper().parseText(val)
                if (obj)
                    return obj
            }
        }
        return val
    }



    /*************************
     *
     * LOAD PROPERTIES
     *
     *************************/
    PropMan readFile(List paths, String filename){
        def results = []
        paths.each { String path ->
            def result = [:]
            try{
                readFile("${path}/${filename}")
                result.isOk = true

            }catch(Exception){
                result.error = Exception
            }
            results << result
        }
        // Check Error
        if (!results.findAll{ return it.isOk }){
            results.each{
                logger.error it.error
            }
            throw new Exception("Couldn't Find Properties File")
        }
        return this
    }

    /*************************
     * read From file
     *************************/
    PropMan readFile(String filePath){
        String absolutePath = getFullPath(filePath)
        File file = new File(absolutePath)
        return readFile(file)
    }

    PropMan readFile(File file){
        try{
            load(file)

        }catch(Exception e){
            throw e
        }
        return this
    }

    /*************************
     * read From file(PROPERTIES)
     *************************/
    PropMan readPropertiesFile(String filePath){
        // Load Properties File
        try{
            String absolutePath = getFullPath(filePath)
            File file = new File(absolutePath)
            loadPropertiesFile(file)

        }catch(Exception e){
            throw e
        }
        return this
    }

    /*************************
     * read From resource file(PROPERTIES)
     *************************/
    PropMan readResource(String resourcePath){
        String name = "/${resourcePath}";
//        File resourceFile = getFileFromResource(resourcePath)

        //Works in IDE
//        URL url = getClass().getResource(resourcePath);
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath)
        int fileNameLastDotIndex = resourcePath.lastIndexOf('.')
        String fileNameExtension = (fileNameLastDotIndex != -1) ? resourcePath.substring(fileNameLastDotIndex +1)?.toLowerCase() : ''
        File file
        if (!url){
            file = null
        }else if (url.toString().startsWith("jar:")){
            //Works in JAR
            try {
                InputStream input = getClass().getResourceAsStream(name) ;
                input = input != null ? input : Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                input = input != null ? input : getClass().getResourceAsStream(resourcePath);
                if (getClass().getClassLoader()){
                    input = input != null ? input : getClass().getClassLoader().getResourceAsStream(resourcePath);
                }

                if (input == null)
                    throw new Exception("Does not exists file from resource [${resourcePath}]");

                file = File.createTempFile("tempfile", ".${fileNameExtension}")
                OutputStream out = new FileOutputStream(file)
                int len
                byte[] bytes = new byte[1024]
                while ((len = input.read(bytes)) != -1) {
                    out.write(bytes, 0, len)
                }
                file.deleteOnExit()
            } catch (IOException ex) {
                ex.printStackTrace()
            }
        }else{
            //Works in your IDE, but not from a JAR
            file = new File(url.getFile())
        }
        if (file != null && !file.exists())
            throw new FileNotFoundException("Error: File " + file + " not found!")

        File resourceFile = file

//        if (resourceFile == null)
//            throw new Exception("Does not exists file from resource. [${resourcePath}]")

        load(file)

        return this
    }

    private void load(File file){
        loadPropertiesFile(file)
    }

    private void loadPropertiesFile(File file){
        try{
            String text = getTextFromFile(file)
//            text = text?.replace('\\','\\\\')
            properties.load( new StringReader(text) )
            filePath = file.path

        }catch(Exception e){
            throw e
        }
    }

    String getTextFromFile(File file){
        return getTextFromFile(file, new FileSetup())
    }

    String getTextFromFile(File file, FileSetup fileSetup){
        List<String> lineList = loadFileContent(file, fileSetup)
        return lineList.join(System.getProperty("line.separator"))
    }

    int size(){
        return properties.size()
    }

    Map getMatchingMap(PropMan bPropman){
        return getMatchingMap(bPropman.properties)
    }

    Map getMatchingMap(Map bPropMap){
        Map aPropMap = this.properties
        return getMatchingMap(aPropMap, bPropMap)
    }

    Map getMatchingMap(Map aPropMap, Map bPropMap){
        return aPropMap.findAll{ bPropMap.containsKey(it.key) }
    }

    Map getNotMatchingMap(PropMan bPropman){
        return getNotMatchingMap(bPropman.properties)
    }

    Map getNotMatchingMap(Map bPropMap){
        Map aPropMap = this.properties
        return getNotMatchingMap(aPropMap, bPropMap)
    }

    Map getNotMatchingMap(Map aPropMap, Map bPropMap){
        return aPropMap.findAll{ !bPropMap.containsKey(it.key) }
    }


    //Check Condition
    boolean match(def condition){
        return !!find(properties, condition)
    }







    private List<String> loadFileContent(File f, FileSetup opt){
        String encoding = opt.encoding
        List<String> lineList = new ArrayList<String>()
        String line
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), encoding)
            BufferedReader br = new BufferedReader(isr)
            while ((line = br.readLine()) != null){
                lineList.add(line)
            }
            isr.close()
            br.close()

        } catch (Exception ex) {
            throw ex
        }

        return lineList
    }



    /*************************
     *
     * MERGE
     *
     *************************/
    PropMan merge(String filePath){
        return mergeFile(filePath)
    }

    PropMan merge(File file){
        return mergeFile(file)
    }

    PropMan merge(List<String> filePathList){
        filePathList.each{ mergeFile(it) }
        return this
    }

    PropMan mergeFile(String filePath){
        Properties properties = new PropMan(filePath).properties
        return merge(properties)
    }

    PropMan mergeFile(File file){
        Properties properties = new PropMan(file).properties
        return merge(properties)
    }

    PropMan mergeFile(List<File> filePathList){
        filePathList.each{ mergeFile(it) }
        return this
    }

    PropMan mergeResource(String resourcePath){
        Properties properties = new PropMan().readResource(resourcePath).properties
        return merge(properties)
    }

    PropMan merge(PropMan otherPropman){
        return merge( otherPropman?.properties )
    }

    PropMan merge(Properties otherProperties){
        Map propMap = otherProperties
        return merge( (Map) propMap )
    }

    PropMan merge(Map propMap){
        propMap.each{
            if (it.key != null)
                properties[it.key] = it.value
        }
        return this
    }

    PropMan merge(Map propMap, List<String> propertyNameFilterTargetList){
        propMap.each{ String key, value ->
            if (key != null && isMatchingProperty(key, propertyNameFilterTargetList))
                properties[key] = value
        }
        return this
    }

    static boolean isMatchingProperty(String propertyName, List<String> propertyNameFilterTargetList){
        if (propertyNameFilterTargetList)
            return propertyNameFilterTargetList.any{ String propertyRange -> isMatchingProperty(propertyName, propertyRange) }
        else
            return true
    }

    static boolean isMatchingProperty(String propertyName, String propertyRange){
        String regexpStr = toSlash(propertyRange)
                .replace('(', '\\(').replace(')', '\\)')
                .replace('[', '\\[').replace(']', '\\]')
                .replace('.', '\\.').replace('$', '\\$')
                .replace('*',"[^.]*")
                .replace('[^.]*[^.]*',"\\S*")
        return propertyName.replace('\\', '/').matches(regexpStr)
    }



    /*************************
     *
     * MERGE NEW
     * Merge Only New Property
     *
     *************************/
    PropMan mergeOnlyNew(String filePath){
        return mergeFileNew(filePath)
    }

    PropMan mergeFileNew(String filePath){
        String absolutePath = getFullPath(filePath)
        Properties properties = new PropMan(absolutePath).properties
        return mergeOnlyNew(properties)
    }

    PropMan mergeResourceNew(String resourcePath){
        Properties properties = new PropMan().readResource(resourcePath).properties
        return mergeOnlyNew(properties)
    }

    PropMan mergeOnlyNew(PropMan otherPropman){
        return mergeOnlyNew( otherPropman.properties )
    }

    PropMan mergeOnlyNew(Properties otherProperties){
        Map propMap = otherProperties
        return mergeOnlyNew( (Map) propMap )
    }

    PropMan mergeOnlyNew(Map propMap){
        propMap.each{
            if (properties[it.key] == null)
                properties[it.key] = it.value
        }
        return this
    }



    /*************************
     *
     * DIFF
     *
     *************************/
    Map diffMap(PropMan with){
        return diffMap(with.properties)
    }

    Map diffMap(Map with){
        return getMatchingMap(with).findAll{ !it.value.equals(with[it.key]) }
    }



    /*************************
     *
     * Compare and Make properties
     *
     *************************/
    Properties compareAndMakeInsertedProperties(PropMan propManAfter){
        return compareAndMakeInsertedProperties(propManAfter.properties)
    }

    Properties compareAndMakeInsertedProperties(Map<String, Object> propertiesMapAfter){
        Properties resultProperties = new Properties()
        String beforeValue
        String afterValue
        for (Object key : propertiesMapAfter.keySet()){
            beforeValue = (String) this.get(key)
            afterValue = (String) propertiesMapAfter.get(key)
            //- New
            if (beforeValue == null && afterValue != null){
                resultProperties.put(key, afterValue)
            }
        }
        return resultProperties
    }


    Properties compareAndMakeModifiedProperties(PropMan propManAfter){
        return compareAndMakeModifiedProperties(propManAfter.properties)
    }

    Properties compareAndMakeModifiedProperties(Map<String, Object> propertiesMapAfter){
        Properties resultProperties = new Properties()
        String beforeValue
        String afterValue
        for (Object key : propertiesMapAfter.keySet()){
            beforeValue = (String) this.get(key)
            afterValue = (String) propertiesMapAfter.get(key)
            //- Modifed
            if (beforeValue != null && afterValue != null && !beforeValue.equals(afterValue)){
                resultProperties.put(key, afterValue)
            }
        }
        return resultProperties
    }


    Properties compareAndMakeDeletedProperties(PropMan propManAfter){
        return compareAndMakeDeletedProperties(propManAfter.properties)
    }

    Properties compareAndMakeDeletedProperties(Map<String, Object> propertiesMapAfter){
        Properties resultProperties = new Properties()
        String beforeValue
        String afterValue
        for (Object key : this.properties.keySet()){
            beforeValue = (String) this.get(key)
            afterValue = (String) propertiesMapAfter.get(key)
            //- Deleted
            if (beforeValue != null && afterValue == null){
                resultProperties.put(key, "")
            }
        }
        return resultProperties
    }





    /*************************
     *
     * Utils
     *
     *************************/
    void validate(def checkList){
        checkList.each { propKey ->
            if (!properties[propKey])
                throw new Exception("Doesn't exist ${propKey} on install.properties!!!  Please Check install.properties")
        }
    }


    String getFullPath(String path){
        return getFullPath(nowPath, path)
    }

    String getFullPath(String standardPath, String relativePath){
        if (!relativePath)
            return null
        if (isItStartsWithRootPath(relativePath))
            return relativePath
        if (!standardPath)
            return ''
        relativePath.split(/[\/\\]/).each{ String next ->
            if (next.equals('..')){
                standardPath = new File(standardPath).getParent()
            }else if (next.equals('.')){
                // ignore
            }else if (next.equals('~')){
                standardPath = System.getProperty("user.home")
            }else{
                standardPath = "${standardPath}/${next}"
            }
        }
        return new File(standardPath).path
    }

    boolean isItStartsWithRootPath(String path){
        boolean isRootPath = false
        path = new File(path).path
        if (path.startsWith('/') || path.startsWith('\\'))
            return true
        File.listRoots().each{
            String rootPath = new File(it.path).path
//            logger.debug "Root Path:" + rootPath
            if (path.startsWith(rootPath) || path.startsWith(rootPath.toLowerCase()))
                isRootPath = true
        }
        return isRootPath
    }



    List<String> getPropertyList(){
        return properties.keySet().toList()
    }



    /*************************
     *
     * FIND OBJECT
     *
     *************************/
    def find(def object, def condition){
        return find(object, condition, null)
    }

    def find(def object, def condition, Closure closure){
        if (object instanceof Map){
            def matchedObj = getMatchedObject(object, condition, closure)
            return matchedObj
        }else if (object instanceof List){
            List results = []
            object.each{
                def matchedObj = getMatchedObject(it, condition, closure)
                if (matchedObj)
                    results << matchedObj
            }
            return results
        }
        return [:]
    }

    def getMatchedObject(def object, def condition){
        return getMatchedObject(object, condition, null)
    }

    def getMatchedObject(def object, def condition, Closure closure){
        if (condition instanceof String){
            condition = [id:condition]
        }
        if (condition instanceof List){
            for (int i=0; i<condition.size(); i++){
                if (find(object, condition[i]))
                    return object
            }
            return null //No Matching
        }
        if (condition instanceof Map){
            if (closure){
                return closure(object, condition)
            }else{
                for (String key : (condition as Map).keySet()) {
                    def attributeValue = object[key]
                    def conditionValue = condition[key]
                    if (attributeValue) {
                        /**{"attribute":["value1","value2"]} **/
                        if (attributeValue instanceof List) {
                            //{"conditionAttribute":["value1","value2"]}
                            if (conditionValue instanceof List) {
                                if (!attributeValue.findAll { (conditionValue as List).contains(it) })
                                    return //No Matching

                                //{"conditionAttribute": ... }
                            } else {
                                if (!attributeValue.findAll { it == conditionValue })
                                    return //No Matching
                            }

                            /**{"attribute": ... } **/
                        } else {
                            //{"conditionAttribute":["value1","value2"]}
                            if (conditionValue instanceof List) {
                                if (!conditionValue.contains(attributeValue))
                                    return //No Matching

                                //{"conditionAttribute": ... }
                            } else {
                                if (attributeValue != conditionValue)
                                    return //No Matching
                            }
                        }

                    } else {
                        if (attributeValue != conditionValue)
                            return //No Matching
                    }
                }
                return object
            }
        }
        if (condition == null)
            return object
    }



    /*************************
     *
     * UNDO & REDO Commit MANAGER
     *
     *************************/
    PropMan undo(){
        checkout(headIndex - 1)
        return this
    }

    PropMan redo(){
        checkout(headIndex + 1)
        return this
    }

    PropMan resetHard(){
        commitStackList = commitStackList[0..headIndex]
        return this
    }

    PropMan reset(){
        checkout(headIndex + 1)
        return this
    }

    PropMan checkout(int checkIndex){
        if (checkIndex < 0){
            headIndex = -1
            properties = new Properties()
            lastCommitProperties = properties.clone()

        }else if (checkIndex == headIndex){
            properties = lastCommitProperties.clone()

        }else if (commitStackList && -1 < checkIndex && checkIndex < commitStackList.size()){
            properties = new Properties()
            properties.putAll(gen(checkIndex))
            lastCommitProperties = properties.clone()
            headIndex = checkIndex
        }
        return this
    }

    PropMan commit(){
        return commit('none_id')
    }

    PropMan commit(String commitId){
        //CASE headIndex is Not Top Index
        //Delete Greater than headIndex
        if (isNotHeadLast())
            commitStackList = (headIndex != -1) ? commitStackList[0..headIndex] : []

        //CASE headIndex is Top Index
        if (isHeadLast()){
            commitStackList << new CommitObject(commitId).gap(lastCommitProperties, properties)
            headIndex = commitStackList.size() - 1
            lastCommitProperties = properties.clone()
        }

        //Top Commit's Relation
        CommitObject topCommit = commitStackList[headIndex]
        topCommit.child = null

        //Second Commit's Relation
        if (commitStackList && commitStackList.size() > 1){
            CommitObject secondCommit = commitStackList[headIndex-1]
            topCommit.parent = secondCommit
            secondCommit.child = topCommit
        }
        return this
    }

    PropMan rollback(){
        checkout(headIndex)
        return this
    }

    Map gen(int commitIndex){
        Map resultMap = [:]
        List<CommitObject> tempStackList
        if (commitStackList && commitIndex < commitStackList.size()){
            tempStackList = commitStackList[0..commitIndex]
            tempStackList.each{
                it.insertedMap.each{
                    resultMap[it.key] = it.value
                }
                it.deletedMap.each{
                    resultMap.remove(it.key)
                }
                it.updatedAfterMap.each{
                    resultMap[it.key] = it.value
                }
            }
        }
        return resultMap
    }

    CommitObject getCommit(){
        return getCommit(this.headIndex)
    }

    CommitObject getCommit(int headIndex){
        return commitStackList[headIndex]
    }

    String getCommitId(){
        return getCommitId(this.headIndex)
    }

    String getCommitId(int headIndex){
        return commitStackList[headIndex].id
    }

    CommitObject getBeforeCommit(){
        return getCommit(this.headIndex -1)
    }

    CommitObject getNextCommit(){
        return getCommit(this.headIndex +1)
    }

    boolean isHeadFirst(){
        return (headIndex == 0)
    }

    boolean isNotHeadLast(){
        return (headIndex < commitStackList.size() - 1)
    }

    boolean isHeadLast(){
        return (headIndex == commitStackList.size() - 1)
    }

    static toSlash(String path){
        return path?.replaceAll(/[\/\\]+/, '/')
    }

}
