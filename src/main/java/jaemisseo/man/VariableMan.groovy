package jaemisseo.man

import jaemisseo.man.util.SimpleDataUtil
import jaemisseo.man.util.SimpleSplitUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.CodeSource
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 9/30/16
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
class VariableMan {

    static final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*************************
     * CONSTRUCTORS
     *************************/
    VariableMan(){
        init()
    }

    VariableMan(Map<String, Object> variableStringMapToPut){
        putVariables(variableStringMapToPut)
        init()
    }

    VariableMan(Map<String, Object> variableStringMapToPut, Map<String, Closure> funcMapToPut){
        putVariables(variableStringMapToPut)
        putFuncs(funcMapToPut)
        init()
    }

    VariableMan(String charset){
        this.charset = charset
        init()
    }

    VariableMan(String charset, Map<String, Object> variableStringMapToPut){
        this.charset = charset
        putVariables(variableStringMapToPut)
        init()
    }

    void init(){
        logger.trace("......................... [INIT] VariableMan")
        putVariables(getBasicVariableMap())
        putVariableClosures(getBasicVariableClosureMap())
        putFuncs(getBasicFuncMap())
        putConditionFuncs(getBasicConditionFuncMap())
        putStreamFuncs(getBasicStreamFuncMap())
        putConditionComputerFuncs(getBasicConditionComputerFuncMap())
    }

    /**
     * Error Message
     */
    static final String VAR1 = "Doesn't Exist Code Rule."
    static final String VAR2 = "Code Number Exceeds Limit."
    static final String VAR3 = "It Needs Number Type Value To Set Length."
    static final String VAR4 = "It is Unknown Code Rule's Function."
    static final String VAR5 = "It Has Bad Syntax."
    static final String VAR6 = "It Has Bad Syntax - brace does not match."
    static final String VAR7 = "It Has Bad Syntax - bracket does not match."
    static final String VAR8 = "It Has Bad Syntax - nothing beetween braces."

//    enum ErrorMessage{
//        VAR1(1, "Doesn't Exist Code Rule."),
//        VAR2(2, "Code Number Exceeds Limit."),
//        VAR3(3, "It Needs Number Type Value To Set Length."),
//        VAR4(4, "It is Unknown Code Rule's Function."),
//        VAR5(5, "It Has Bad Syntax."),
//        VAR6(6, "It Has Bad Syntax - brace does not match."),
//        VAR7(7, "It Has Bad Syntax - bracket does not match."),
//        VAR8(8, "It Has Bad Syntax - nothing beetween braces.")
//
//        ErrorMessage(int code, String msg){ this.code = code; this.msg = msg }
//        final int code
//        final String msg
//        String prefix = "VAR-"
//        String getMsg(){ return "${prefix}${code}: ${msg}" }
//    }

    /**
     * ConditionNotMatchedFinishException
     */
    class ConditionNotMatchedFinishException extends Exception{

    }

    /**
     * You Can Create Function's Properties With This Object
     */
    class OnePartObject {
        String partValue = ''
        String partContent = ''
        String originalCode = ''
        String valueCode = ''
        String substitutes = ''
        Object originalValue = null
        String parsedValue = ''
        byte[] bytes
        int startIndex = 0
        int endIndex = 0
        int length = 0
        int userSetLength = 0
        int byteLength = 0
        int charLength = 0
        boolean isCode = false
        boolean isExistCode = false
        boolean isOver = false


        //Condition Method - Stream
        boolean modeStream = false
        List<String> reducedCollection = null
        StringBuilder virtualRuleBuilderInStream = null;

        Integer okStreamLevel = 0
        Integer nowStreamLevel = 0

        boolean checkStream(){
            return this.okStreamLevel == this.nowStreamLevel
        }


        //Condition Method - If
        boolean modeIf = false
        Integer okIfSeq = 0
        Integer nowIfSeq = 0

        //Check Condition
        boolean checkConditionNowSeq(){
            return this.okIfSeq == this.nowIfSeq
        }


        String overValue = ''
        String funcNm = ''
        String[] members = []
        Map<String, String[]> funcNameMemberListMap = [:]

        boolean hasFunc(String functionName){
            return funcNameMemberListMap.containsKey(functionName.toUpperCase())
        }
        String[] getMember(String functionName){
            return funcNameMemberListMap[functionName.toUpperCase()]
        }
        String getMember(String functionName, int memberIndex){
            String[] members = getMember(functionName)
            return members ? members[memberIndex] : null
        }


    }

    Map<String, Object> variableStringMap = [:]
    Map<String, Closure> variableClosureMap = [:]
    Map<String, Closure> funcMap = [:]
    Map<String, Closure> conditionFuncMap = [:]
    Map<String, Closure> streamFuncMap = [:]
    Map<String, Closure> conditionComputerFuncMap = [:]

    boolean modeDebug = false
    boolean modeMustExistCodeRule = true
    boolean modeExistCodeOnly = false
    boolean modeExistFunctionOnly = true
    String charset

    /* Pattern - Variable */
    private String variableSign = '$'
    private String patternBodyToGetVariable = '[{][^{}]*\\w+[^{}]*[}]'
    private String patternBodyStartToGetVariable = '[{][^{}]*'
    private String patternBodyContentsToGetVariable = '\\w+'
    private String patternBodyEndToGetVariable = '[^{}]*[}]'
    private String patternToGetVariable = "[" +variableSign+ "]" + patternBodyStartToGetVariable + patternBodyContentsToGetVariable + patternBodyEndToGetVariable     // If variable contains some word in ${} then convert to User Set Value or...

    /* Pattern - Member */
    private String patternToSeperateMemebers = ',(?=(?:[^"]*"[^"]*")*[^"]*$)(?=(?:[^\']*\'[^\']*\')*[^\']*$)'
    private String patternToGetMembers = '[(][^(]*[^)]*[)]'   //TODO: addVariable()에 들어가는 파라미터르 인식하기 위해서는 매치패턴의 업데이트 필요
    static private String charDoubleQuote = '"'
    static private String charSingleQuote = "'"

    /* Pattern - Func */
    private String patternToSeperateFuncs = '\\s*[.](?![^(]*[)])\\s*'



    /**
     * You Can Set Debug Mode To Watch Detail
     * @param modeDebug
     * @return
     */
    VariableMan setModeDebug(boolean modeDebug){
        this.modeDebug = modeDebug
        return this
    }

    VariableMan setModeMustExistCodeRule(boolean modeMustExistCodeRule){
        this.modeMustExistCodeRule = modeMustExistCodeRule
        return this
    }

    VariableMan setModeExistCodeOnly(boolean modeExistCodeOnly){
        this.modeExistCodeOnly = modeExistCodeOnly
        return this
    }

    VariableMan setModeExistFunctionOnly(boolean modeExistFunctionOnly){
        this.modeExistFunctionOnly = modeExistFunctionOnly
        return this
    }

    VariableMan setVariableSign(String variableSign){
        this.variableSign = variableSign
        if (variableSign){
            this.patternToGetVariable = "[" +variableSign+ "]" + patternBodyStartToGetVariable + patternBodyContentsToGetVariable + patternBodyEndToGetVariable
        }else{
            this.patternToGetVariable = patternBodyStartToGetVariable + patternBodyContentsToGetVariable + patternBodyEndToGetVariable

        }
        return this
    }



    /**
     * You Can Put Variable
     *  [ VariableName(String) : VariableValue(String) ]
     *  Please Do Not Set These Names => 'date', 'random'
     * @param variableStringMapToAdd
     * @return
     */
    VariableMan putVariables(Map<String, Object> variableStringMapToPut){
        if (variableStringMapToPut){

            //- Apply to Variable-Map
            variableStringMapToPut = convertToPropertiesMap(variableStringMapToPut)

            if (logger.isTraceEnabled()){
                variableStringMapToPut.each{
                    logger.trace("[${it.key}] ${it.value}")
                }
            }
            //Put
            this.variableStringMap.putAll(variableStringMapToPut)
        }
        return this
    }

    static Map<String, Object> convertToPropertiesMap(Map<String, Object> variableStringMap){
        boolean subObjectExists = variableStringMap.values().any{ it instanceof Map || it instanceof List }
        if (subObjectExists){
            logger.trace('Add Variable (from MultiDepth Map)')
            variableStringMap = toPropertiesMap(variableStringMap)
        }else{
            logger.trace('Add Variable (from SingleDepth Map)')
        }
        return variableStringMap
    }

    static Map<String, Object> toPropertiesMap(Map parameters){
        Map<String, Object> concatKeyStringValueMap = [:]
        return toPropertiesMap(parameters, '', concatKeyStringValueMap)
    }

    static Map<String, Object> toPropertiesMap(Map parameters, String prevKey, Map<String, Object> concatKeyStringValueMap){
        parameters.keySet().each{ String key ->
            String concatKey = (prevKey) ? prevKey + '.' + key : key
            def value = parameters[key]
            if (value instanceof Map)
                toPropertiesMap(value, concatKey, concatKeyStringValueMap)
            else{
                concatKeyStringValueMap[concatKey] = value
            }
        }
        return concatKeyStringValueMap
    }

    /**
     * You Can Delete Variable
     *  [ VariableName(String), VariableName(String) ]
     *  Please Do Not Set These Names => 'date', 'random'
     * @param variableStringMapToAdd
     * @return
     */
    VariableMan delVariables(List<String> variableStringList){
        if (variableStringList){
            variableStringList.each{
                this.variableStringMap.remove(it)
            }
        }
        return this
    }

    /**
     * You Can Put Variable
     *  [ VariableName(String) : Variable Function(Closure) ]
     *  Please Refer To getBasicVariableClosureMap()
     * @param variableFuncMapToPut
     * @return
     */
    VariableMan putVariableClosures(Map<String, Closure> variableFuncMapToPut){
        if (variableFuncMapToPut)
            this.variableClosureMap.putAll(variableFuncMapToPut)
        return this
    }

    /**
     *  You Can Create Custom Function
     *  [ FunctionName(String) : Function(Closure) ]
     *  Please Refer To getBasicFuncMap()
     * @param funcMapToPut
     */
    VariableMan putFuncs(Map<String, Closure> funcMapToPut){
        this.funcMap.putAll(funcMapToPut)
        return this
    }

    VariableMan putConditionFuncs(Map<String, Closure> funcMapToPut){
        this.conditionFuncMap.putAll(funcMapToPut)
        return this
    }

    VariableMan putStreamFuncs(Map<String, Closure> funcMapToPut){
        this.streamFuncMap.putAll(funcMapToPut)
        return this
    }

    VariableMan putConditionComputerFuncs(Map<Object, Closure> funcMapToPut){
        funcMapToPut.each{ key, value ->
            if (key instanceof String){
                this.conditionComputerFuncMap.put(key, value)
            }else if(key instanceof List<String>){
                key.each{k ->
                    this.conditionComputerFuncMap.put(k, value)
                }
            }
        }
        return this
    }



    /**
     * You Can Set CharacterSet
     * ex1) 'euc-kr'
     * ex2) 'utf-8'
     * @param charset
     * @return
     */
    VariableMan setCharset(String charset){
        this.charset = charset
        return this
    }



    /**
     * Parse !!!
     * @param codeRule
     * @return
     * @throws java.lang.Exception
     */
    Object parse(Object codeRule){
        parse(codeRule, this.variableStringMap)
    }

    String parseString(String codeRule) {
        return parseString(codeRule, this.variableStringMap)
    }

    Object parseObject(Object codeRule) {
        return parseObject(codeRule, this.variableStringMap)
    }

    private String parseVirtualRuleContent(String codeRuleContent, Object itItem, Map<String, Object> vsMap, Map<String, Closure> vcMap){
        OnePartObject partObj = new VariableMan(vsMap, vcMap)
                .setModeMustExistCodeRule(this.modeMustExistCodeRule)
                .setModeExistCodeOnly(this.modeExistCodeOnly)
                .setModeExistFunctionOnly(this.modeExistFunctionOnly)
                .setCharset(this.getCharset())
                .setVariableSign(this.variableSign)
                .setModeExistCodeOnly(false)
                .putVariables([it:itItem])
                .parseCodeContent(codeRuleContent)
        makePartResult(partObj)
        return partObj.parsedValue
    }



    /**
     * Parse !!!
     * @param codeRule
     * @param variableStringMap
     * @return
     * @throws java.lang.Exception
     */
    Object parse(Object codeRule, Map<String, Object> variableStringMap) {
        switch(codeRule){
            case {codeRule instanceof String}:
                return parseString(codeRule, variableStringMap)
                break
            default:
                return parseObject(codeRule, variableStringMap)
                break
        }
    }

    String parseString(String codeRule, Map<String, Object> variableStringMap) {
        logger.trace("......................... Start parse string: ${codeRule}")

        //- Apply to Variable-Map
        variableStringMap = convertToPropertiesMap(variableStringMap)

        //- Validation
        validateCodeRule(codeRule)

        //- Parse data
        List<OnePartObject> partObjectList = parsedDataList(codeRule, variableStringMap)

        logger.trace("..... Creating String Data from Parsed Data")
        int allCharLength = 0
        int allByteLength = 0
        int allUserSetLength = 0
        List parsedPartStringList = partObjectList.collect { OnePartObject partObj ->
            // CASE - DEBUG MODE
            if (modeDebug && partObj.substitutes){
                allUserSetLength += partObj.userSetLength
                allByteLength += partObj.byteLength
                allCharLength += partObj.charLength
                logger.debug "////////// ${partObj.partValue}"
                logger.debug "[${partObj.substitutes}]"
                logger.debug "Length: ${partObj.charLength} / Byte: ${partObj.byteLength} / Your Set: ${partObj.userSetLength}"
                if (partObj.userSetLength !=0 && partObj.userSetLength != partObj.byteLength){
                    logger.debug "!!! !!! !!! !!! !!!"
                    logger.debug "!!! Not Matched !!!"
                    logger.debug "!!! !!! !!! !!! !!!"
                }
            }
            return partObj.parsedValue
        }
        logger.trace('......................... Finish parse string')
        if (modeDebug){
            println ""
            println "//////////////////////////////////////////////////"
            println "////// ALL LENGTH ////////////////////////////////"
            println "//////////////////////////////////////////////////"
            println "SETUP LENGTH : ${allUserSetLength}"
            println " BYTE LENGTH : ${allByteLength}"
            println " CHAR LENGTH : ${allCharLength}"
            println ""
        }
        return parsedPartStringList.join('')
    }

    Object parseObject(Object codeRule, Map<String, Object> variableStringMap) {
        switch (codeRule){
            case {codeRule instanceof Map}:
                codeRule.each{ key, item ->
                    if (item instanceof String)
                        codeRule[key] = parseString(item, variableStringMap)
                    else
                        parseObject(item, variableStringMap)
                }
                break

            case {codeRule instanceof List}:
                codeRule.eachWithIndex{ item, i ->
                    if (item instanceof String)
                        codeRule[i] = parseString(item, variableStringMap)
                    else
                        parseObject(item, variableStringMap)
                }
                break

            case {codeRule instanceof String}:
                return parseString(codeRule, variableStringMap)
                break
        }
    }


    Map<String, String> matchVariableMap(String codeRule, String targetString) {
        Map<String, String> variableStringMap = [:];

        //- Validation
        validateCodeRule(codeRule, true)

        //- Get String In ${ } step by step
        List<OnePartObject> partObjects = parsedDataList(codeRule)
        String regex = codeRuleToPattern(partObjects)
        Pattern pattern = Pattern.compile(regex)
        Matcher matcher = pattern.matcher( targetString )

        //- Put captured values
        boolean matched = matcher.matches()
        Integer groupCount = matcher.groupCount()
        boolean thisIsCapturePattern = matched && groupCount > 0
        if (thisIsCapturePattern){
            partObjects.each{
                if (it.isCode){
                    String capturedValue = matcher.group(it.valueCode)
                    variableStringMap.put(it.valueCode, capturedValue)
                }
            }

        }

        return variableStringMap
    }

    //- Variable(${}) to REGEX(.*)
    String codeRuleToPattern(List<OnePartObject> partObjectList){
        StringBuilder sb = new StringBuilder();
        partObjectList.eachWithIndex{ part, i ->
            if (part.isCode){
                sb.append("(?<").append(part.valueCode).append(">.*)")
            }else{
                String convertedString = toRegexExpression(part.partValue);
//                String convertedString = part.partValue;
                sb.append(convertedString);
            }
        }.join("")
        String regex = sb.toString()
        return regex
    }


    static String toRegexExpression(String string){
        String regexpStr = toSlash(string)
                .replace('(', '\\(').replace(')', '\\)')
                .replace('[', '\\[').replace(']', '\\]')
                .replace('.', '\\.').replace('$', '\\$')
                .replace('*',"[^\\/\\\\]*")
                .replace('[^\\/\\\\]*[^\\/\\\\]*/','(\\S*[\\/\\\\]|)')
                .replace('[^\\/\\\\]*[^\\/\\\\]*',"\\S*")
//        return regexpStr.replace("\\", "\\\\")
        return regexpStr
    }

    static toSlash(String path){
        return path?.replaceAll(/[\/\\]+/, '/')
    }



    List<OnePartObject> parsedDataList(String codeRule){
        return parsedDataList(codeRule, this.variableStringMap)
    }

    List<OnePartObject> parsedDataList(String codeRule, Map<String, Object> variableStringMap) {
        //- Validation
        validateCodeRule(codeRule)
        //- Parse data
        return parsedDataList(codeRule, variableStringMap, this.variableClosureMap, this.patternToGetVariable)
    }

    List<OnePartObject> parsedDataList(String codeRule, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap, String patternToGetVariable){
        logger.trace("..... Analysis code rule")
        //- Get String In ${ } step by step
        Matcher matchedList = Pattern.compile(patternToGetVariable).matcher(codeRule)
        List<String> splitWithCodeRuleList = codeRule?.split(patternToGetVariable)?.toList()
        List<String> codeList = matchedList ? matchedList?.collect{ it } : []

        logger.trace("..... Creating Part-Object from parsed datas with code rule: (variable: ${codeList.size()})")
        List<OnePartObject> partObjectList = []
        int cnt = -1
        codeList.each{ String code ->
            cnt++
            if (splitWithCodeRuleList && splitWithCodeRuleList[cnt]){
                partObjectList << generateOnePartObjectForString(splitWithCodeRuleList[cnt])
            }
            try{
                partObjectList << generateOnePartObjectForVariable(code, variableStringMap, variableClosureMap)
            }catch(ConditionNotMatchedFinishException cnmfe){
                //Exit
            }
        }
        cnt++
        if (splitWithCodeRuleList && splitWithCodeRuleList[cnt]){
            partObjectList << generateOnePartObjectForString(splitWithCodeRuleList[cnt])
        }
        logger.trace("..... Remaking Part-Object")
        int index = 0
        partObjectList.each{ OnePartObject partObj ->
            makePartResult(partObj)
            partObj.startIndex = index
            partObj.endIndex = index + partObj.charLength + (partObj.charLength == 0 ? 0 : -1)
            index += partObj.parsedValue.length()
        }
        return partObjectList
    }

    void makePartResult(OnePartObject partObj){
        if (partObj.isCode){
            if (!partObj.isExistCode && !partObj.substitutes){
                partObj.parsedValue = (modeExistCodeOnly && partObj.valueCode) ? partObj.partValue : ''
            }else{
                partObj.parsedValue = partObj.substitutes
            }
        }else{
            partObj.parsedValue = partObj.partValue
        }
        byte[] bytes = (charset) ? partObj.parsedValue.getBytes(charset) : partObj.parsedValue.getBytes()
        partObj.userSetLength = partObj.length
        partObj.byteLength = bytes.length
        partObj.charLength = partObj.parsedValue.length()
    }

    OnePartObject generateOnePartObjectForString(String partValue){
        return new OnePartObject(
                partValue: partValue,
                isCode: false,
        )
    }

    OnePartObject generateOnePartObjectForVariable(String partValue, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        // 1. get String in ${ }
        String content
        if (variableSign){
            content = partValue.replaceFirst("[${variableSign}]", '').replaceFirst('\\{', '').replaceFirst('\\}', '')
        }else{
            content = partValue.replaceFirst('\\{', '').replaceFirst('\\}', '')
        }
        validateFunc(content)

        // 2. Analysis And Run Function
        OnePartObject partObj = new OnePartObject()
        partObj.partValue = partValue
        partObj.partContent = content
        partObj.isCode = true

        //- Parse
        OnePartObject parsedPartObj = parseCodeContent(partObj, variableStringMap, variableClosureMap)
        return parsedPartObj
    }

    OnePartObject parseCodeContent(String content){
        OnePartObject partObj = new OnePartObject()
        partObj.partValue = new StringBuilder().append(variableSign).append("{").append(content).append("}").toString()
        partObj.partContent = content
        partObj.isCode = true

        //- Parse
        OnePartObject parsedPartObj = parseCodeContent(partObj, this.variableStringMap, this.variableClosureMap)
        return parsedPartObj
    }

    OnePartObject parseCodeContent(OnePartObject partObj, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        String content = partObj.partContent
        List<String> funcs = content?.split(patternToSeperateFuncs)?.toList()
        int variableEndIndex = funcs.findIndexOf{ it.indexOf('(') != -1 }
        int endIndex = funcs.size() -1
        if (variableEndIndex != -1){
            String variable = funcs[0..variableEndIndex].join('.')
            if (variableEndIndex < endIndex)
                funcs = [variable] + funcs[variableEndIndex+1..endIndex]
            else
                funcs = [content]
        }else{
            funcs = [content]
        }

        // If There are no LEFT or RIGHT FUNCTION => Set RIGHT FUNCTION
        if ( containsIgnoreCase(funcs, 'LEFT(') || containsIgnoreCase(funcs, 'RIGHT(') ){
        }else{
            funcs << 'RIGHT()'
        }

        funcs.eachWithIndex{ String oneFunc, int procIdx ->
            // run variable or func
            String funcNm = getFuncNameFromOneFunc(oneFunc)
            if (funcNm){
                String originalCode = funcNm
                funcNm = funcNm.toUpperCase()
                partObj.funcNm = funcNm
                partObj.members = getMemebersFromOneFunc(oneFunc)

                //- Check ConditionFunc
                if ( getIgnoreCase(conditionFuncMap, funcNm) ) {
                    if (partObj.modeStream) {
                        collectStreamContents(partObj, oneFunc)
                    }else{
                        try{
                            runConditionFunc(partObj, variableStringMap, variableClosureMap)
                        }catch(e){
                            throw e
                        }
                    }

                    //- Check StreamFunc
                }else if ( getIgnoreCase(streamFuncMap, funcNm) ) {
                    try{
                        runStreamFunc(partObj, variableStringMap, variableClosureMap)
                    }catch(e){
                        throw e
                    }

                    // 1) Get Variable's Value
                }else if (procIdx == 0){
                    partObj.originalCode = originalCode
                    partObj.valueCode = funcNm
                    getVariableValue(partObj, variableStringMap, variableClosureMap)

                    // 2) Run Fucntions To Adjust Value
                }else if (procIdx > 0){
                    if (!modeExistFunctionOnly || getIgnoreCase(funcMap, funcNm)){
                        try{
                            partObj.funcNameMemberListMap = partObj.funcNameMemberListMap ?: [:]
                            partObj.funcNameMemberListMap[funcNm] = partObj.members
                            if (partObj.modeStream){
                                collectStreamContents(partObj, oneFunc)

                            }else if (partObj.modeIf){
                                if (partObj.checkConditionNowSeq())
                                    runFunc(partObj, variableStringMap, variableClosureMap)

                            }else{
                                runFunc(partObj, variableStringMap, variableClosureMap)
                            }

                        }catch(e){
                            throw e
                        }
                    }else{
                        throw new Exception(VAR4, new Throwable("[${funcNm}]") )
                    }

                }else{
                    throw new Exception(VAR4, new Throwable("[${funcNm}]") )
                }

            }else{
//                maybe nothing is not bad
//                    throw new Exception( ErrorMessage.VAR5.msg, new Throwable("[${partValue}]") )
            }
        }
        return partObj
    }

    private boolean collectStreamContents(OnePartObject partObj, String oneFunc){
        boolean correctLevel = partObj.checkStream()
        //1) Stream 영역 내용 수집
        if (correctLevel){
            //2) Collecting
            partObj.virtualRuleBuilderInStream.append(".").append( oneFunc )
        }
        return correctLevel
    }

    private String getFuncNameFromOneFunc(String oneFunc){
        String funcName = ""
        def array = oneFunc.replaceFirst('\\(', ' ').split(' ')
        array.eachWithIndex{ String el, int memIdx ->
            if (memIdx==0)
                funcName = el
        }
        return funcName
    }

    private String[] getMemebersFromOneFunc(String oneFunc){
        String[] members = []
        // get members
        Matcher m = Pattern.compile(patternToGetMembers).matcher(oneFunc)
        if (m){
            String memberString = m[0]
            members = memberString.substring(1, memberString.length() -1).split(patternToSeperateMemebers).collect{ it.trim() }
        }
        return members
    }

//    String removeSomeVariableCode(String codeRule, String code){
//        String removedCode
//        return removedCode
//    }
//
//    String removeSomeVariableCode(String codeRule, List<String> codeList){
//        String removedCode
//        codeList.each{ String code ->
//            removedCode = removeSomeVariableCode(codeRule, code)
//        }
//        return removedCode
//    }

    String parseDefaultVariableOnly(String codeRule){
        VariableMan variableMan = new VariableMan().setModeExistCodeOnly(true).putVariableClosures(this.getVariableClosureMap())
        Object result = variableMan.parse(codeRule)
        return result
    }

    static String getRightReplacement(String replacement){
        // This Condition's Logic prevent disapearance \
        if (replacement.indexOf('\\') != -1)
            replacement = replacement.replaceAll('\\\\','\\\\\\\\')
        // This Condition's Logic prevent Error - java.lang.IllegalArgumentException: named capturing group has 0 length name
        if (replacement.indexOf('$') != -1)
            replacement = replacement.replaceAll('\\$','\\\\\\$')
        return replacement
    }

    static Object parseMember(String member, Map variableStringMap, Map variableClosureMap){
        try{
            if (member){
                if (member.length() > 1
                        && (
                        (member.startsWith(charDoubleQuote) && member.endsWith(charDoubleQuote))
                                ||
                                (member.startsWith(charSingleQuote) && member.endsWith(charSingleQuote))
                )
                ){
                    member = member.substring(1, member.length() -1)

                }else if (member.isNumber()){
                    if (member.indexOf(".") != -1){
                        return Double.parseDouble(member)
                    }else{
                        return Integer.parseInt(member)
                    }

                }else{
                    Closure variableClosure = getIgnoreCase(variableClosureMap, member)
                    String variableValue = getIgnoreCase(variableStringMap, member)
                    if (variableClosure != null || variableValue != null){
                        member = new VariableMan(variableStringMap).setModeExistCodeOnly(false).parse( '${' +member+ '}' )
                    }
                }
            }

        }catch(e){
            logger.error("[VariableMan] faild to parse member", e)
            throw e
        }
        return member
    }

    static Object parseMemberAsStrict(String member, Map variableStringMap, Map variableClosureMap){
        Object memberValue
        try{
            if (member){
                if (member.length() > 1
                        && (
                        (member.startsWith(charDoubleQuote) && member.endsWith(charDoubleQuote))
                                ||
                                (member.startsWith(charSingleQuote) && member.endsWith(charSingleQuote))
                )
                ){
                    memberValue = member.substring(1, member.length() - 1)

                }else if (member.isNumber()){
                    if (member.indexOf(".") != -1){
                        memberValue = Double.parseDouble(member)
                    }else{
                        memberValue = Integer.parseInt(member)
                    }

                }else{
                    Closure variableClosure = getIgnoreCase(variableClosureMap, member)
                    Object variableValue = getIgnoreCase(variableStringMap, member)
                    if (containsKeyIgnoreCase(variableStringMap, member)){
                        memberValue = variableValue
                    }else if (variableClosure != null || variableValue != null){
                        memberValue = new VariableMan(variableStringMap).setModeExistCodeOnly(false).parse( '${' +member+ '}' )
                    }else{
                        memberValue = null
                    }
                }
            }
        }catch(e){
            logger.error("[VariableMan] faild to parse member", e)
            throw e
        }
        return memberValue
    }



    /**
     * Validation
     * @param codeRule
     * @return
     */
    boolean validateCodeRule(String codeRule) throws Exception{
        return validateCodeRule(codeRule, modeMustExistCodeRule)
    }

    static boolean validateCodeRule(String codeRule, boolean modeMustExistCodeRule) throws Exception{
        /* DEVELOPER COMMENT: "It does not matter." */
        if (modeMustExistCodeRule && codeRule == null)
            throw new NullPointerException()
        //- BEFORE
//        if ( modeMustExistCodeRule && (!codeRule || !codeRule.trim()) )
//            throw new Exception( ErrorMessage.VAR1.msg + "[${codeRule}]" )
        if (Pattern.compile('[{]').matcher(codeRule).size() != Pattern.compile('[}]').matcher(codeRule).size())
            throw new Exception(VAR6 + "[${codeRule}]" )
        return true
    }

    boolean validateFunc(String func){
        /* DEVELOPER COMMENT: "Maybe ${} is not bad" */
//        if (!func.trim())
//            throw new Exception( ErrorMessage.VAR8.msg + "[${func}]" )
        /* DEVELOPER COMMENT: It is not good checking*/
//        if (func.matches('.*[)]{1}[^.]{1}.*'))
//            throw new Exception( ErrorMessage.VAR5.msg + "[${func}]" )
        if (Pattern.compile('[(]').matcher(func).size() != Pattern.compile('[)]').matcher(func).size())
            throw new Exception(VAR7 + "[${func}]" )
        return true
    }


    void getVariableValue(OnePartObject partObj, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        String funcNm = partObj.funcNm
        String[] members = partObj.members
        //Variable(default) - DATE, RANDOM ...
        Closure variableClosure = getIgnoreCase(variableClosureMap, funcNm)
        if (variableClosure){
            variableClosure(partObj, variableStringMap, variableClosureMap)
            partObj.isExistCode = true
            return
        }
        //Variable(custom) - ? (from variableStringMap)
        String variableValue = getIgnoreCase(variableStringMap, funcNm)
        if (variableValue != null){
            partObj.isExistCode = true
            partObj.substitutes = variableValue?:''
            partObj.bytes = (charset) ? partObj.substitutes.getBytes(charset) : partObj.substitutes.getBytes()
            String userSetedValueLengthString = (members) ? parseMember(members[0], variableStringMap, variableClosureMap) : null
            if (userSetedValueLengthString && userSetedValueLengthString.matches('[0-9]*') ){
                //- Set length
                partObj.length = Integer.parseInt(userSetedValueLengthString)
                //- Set meta datas
                if (partObj.length > 0){
                    int diff = partObj.length - partObj.bytes.length
                    if (diff < 0){
//                        partObj.substitutes = partObj.substitutes.substring(0, partObj.length)
                        partObj.isOver = true
                        partObj.overValue = partObj.substitutes
                        partObj.substitutes = (charset) ? new String(partObj.bytes, 0, partObj.length, charset) : new String(partObj.bytes, 0, partObj.length)
                    }
                }else{
                    partObj.length = 0
                }
            }else if (!userSetedValueLengthString){
            }else{
                throw new Exception(VAR3, new Throwable("[${partObj.partValue}]") )
            }
            //Variable(?) - (Not Matched)
        }else{
            partObj.isExistCode = false
            partObj.substitutes = ''
            partObj.length = (members && members[0]) ? Integer.parseInt(members[0]) : 0
        }
    }


    void runFunc(OnePartObject partObj, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        Closure closure = getIgnoreCase(this.funcMap, partObj.funcNm)
        if (closure)
            closure(partObj, variableStringMap, variableClosureMap)
    }

    void runConditionFunc(OnePartObject partObj, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        Closure closure = getIgnoreCase(this.conditionFuncMap, partObj.funcNm)
        if (closure)
            closure(partObj, variableStringMap, variableClosureMap)
    }

    void runStreamFunc(OnePartObject partObj, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        Closure closure = getIgnoreCase(this.streamFuncMap, partObj.funcNm)
        if (closure)
            closure(partObj, variableStringMap, variableClosureMap)
    }




    /**
     *  These are Basic Variable
     *  Watch Below To Add Variable.
     *
     *  Method 1 => New VariableMan(Map)
     *  Method 2 => New VariableMan().addVariableClosures(Map)
     *
     * @return
     */
    Map<String, Object> getBasicVariableMap(){
        InetAddress ip = InetAddress.getLocalHost()
        return [
                '_os.name': System.getProperty('os.name'),
                '_os.version': System.getProperty('os.version'),
                '_user.name': System.getProperty('user.name'),
                '_java.version': System.getProperty('java.version'),
                '_java.home': replacePathWinToLin( System.getProperty('java.home') ),
                '_user.dir': replacePathWinToLin( System.getProperty('user.dir') ),
                '_user.home': replacePathWinToLin( System.getProperty('user.home') ),
                '_hostname': ip.getHostName(),
                '_ip': ip.getHostAddress(),
                '_lib.dir': replacePathWinToLin( getThisAppFile()?.getParentFile()?.getPath() ) ?: '',
                '_lib.path': replacePathWinToLin( getThisAppFile()?.getPath() ) ?: '',
        ]
    }

    String replacePathWinToLin(String path){
        return path?.replace("\\", "/")
    }

    File getThisAppFile(){
        File thisAppFile
        CodeSource src = this.getClass().getProtectionDomain().getCodeSource()
        if (src)
            thisAppFile = new File( src.getLocation().toURI().getPath() )
        return thisAppFile
    }

    /**
     *  These are Basic Functions
     *  Watch Below To Add Function.
     *
     *  Method 1 => New VariableMan(Map)
     *  Method 2 => New VariableMan().addVariableClosures(Map)
     *
     * @return
     */
    Map<String, Closure> getBasicVariableClosureMap(){
        return [
                'EMPTY': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    it.substitutes = ""
                    it.length = 0
                },
                'DATE': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    String[] members = it.members
                    String format = (members && members[0]) ? members[0] : 'yyyyMMdd'
                    if (format == 'long'){
                        it.substitutes = String.valueOf(new Date().getTime())
                        it.length = it.substitutes.length()
                    }else{
                        it.substitutes = new SimpleDateFormat(format).format(new Date())
                        it.length = format.length()
                    }
                },
                'RANDOM': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    String[] members = it.members
                    int length = (members && members[0]) ? Integer.parseInt(members[0]) : 0
                    String numStr = "1"
                    String plusNumStr = "1"
                    for (int i = 0; i < length; i++){
                        numStr += "0"
                        if (i != length - 1)
                            plusNumStr += "0"
                    }
                    int result = new Random().nextInt(Integer.parseInt(numStr)) + Integer.parseInt(plusNumStr)
                    if (result > Integer.parseInt(numStr))
                        result -= Integer.parseInt(plusNumStr)
                    it.substitutes = result
                    it.length = length
                },
                'ENTER': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    String[] members = it.members
                    int count = (members && members[0]) ? Integer.parseInt(members[0]) : 1
                    it.substitutes = (1..count).collect{ '\n' }.join('')
                    it.length = it.substitutes.length()
                },
                'PICK': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members.size() > 1) {
                        String arg1 = parseMember(it.members[0], vsMap, vcMap)
                        Integer lastIndex = it.members.length -1
                        Integer seq = (arg1.isNumber()) ? Integer.parseInt(arg1) : 0
                        //- Making Data List
                        List rangeList = it.members[1..lastIndex].collect{ range ->
                            if (range.contains("..")){
                                List<String> fromAndTo = range.split("\\.\\.").toList()
                                def a = fromAndTo[0]
                                def b = fromAndTo[1]
                                if (a.isNumber() && b.isNumber()){
                                    return ((a as Integer)..(b as Integer))
                                }else{
                                    return ((a as String)..(b as String))
                                }
                            }else{
                                return range as String
                            }
                        }.flatten()
                        //- Pick One
                        String pickedData = rangeList[seq-1]
                        it.substitutes = pickedData
                        it.length = it.substitutes.length()
                    }
                },
                'PICKNEXT': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members.size() > 1) {
                        String arg1 = parseMember(it.members[0], vsMap, vcMap)
                        Integer lastIndex = it.members.length -1
                        Integer seq = (arg1.isNumber()) ? Integer.parseInt(arg1) : 0
                        //- Making Data List
                        List rangeList = it.members[1..lastIndex].collect{ range ->
                            if (range.contains("..")){
                                List<String> fromAndTo = range.split("\\.\\.").toList()
                                def a = fromAndTo[0]
                                def b = fromAndTo[1]
                                if (a.isNumber() && b.isNumber()){
                                    return ((a as Integer)..(b as Integer))
                                }else{
                                    return ((a as String)..(b as String))
                                }
                            }else{
                                return range as String
                            }
                        }.flatten()
                        //- Pick One
                        String pickedData = rangeList[seq-1]
                        it.substitutes = pickedData
                        it.length = it.substitutes.length()
                    }
                },
                'PICKNEXTOF': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members.size() > 1) {
                        String arg1 = parseMember(it.members[0], vsMap, vcMap)
                        Integer lastIndex = it.members.length -1
                        String indexString = arg1
                        //- Making Data List
                        List rangeList = it.members[1..lastIndex].collect{ range ->
                            if (range.contains("..")){
                                List<String> fromAndTo = range.split("\\.\\.").toList()
                                def a = fromAndTo[0]
                                def b = fromAndTo[1]
                                if (a.isNumber() && b.isNumber()){
                                    return ((a as Integer)..(b as Integer))
                                }else{
                                    return ((a as String)..(b as String))
                                }
                            }else{
                                return range as String
                            }
                        }.flatten()
                        //- Pick One
                        Integer nowIndex = rangeList.indexOf(indexString)
                        String pickedData = rangeList[nowIndex+1]
                        it.substitutes = pickedData
                        it.length = it.substitutes.length()
                    }
                },

                'PROP': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String propFilePath = parseMember(it.members[0], vsMap, vcMap)
                        String checkPropKey = parseMember(it.members[1], vsMap, vcMap)
                        Properties prop
                        String value = ""
                        try{
                            prop = new Properties()
                            prop.load(new File(propFilePath).newInputStream())
                            value = prop.get(checkPropKey)
                        }catch(e){
                            logger.error("Error ocurred during loading file - [$propFilePath]", e)
                        }
                        it.substitutes = value
//                        it.length = it.substitutes.length()
                        it.length = it.substitutes.getBytes().length
                    }
                },

//                //TODO: not good for security?
//                "EXEC": { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
//                    if (it.members){
//                        String command = parseMember(it.members[0], vsMap, vcMap)
//                        String result
//                        File dir = new File(".")
//                        try{
//                            result = command.execute([], dir).text.trim()
//                        }catch(e){
//                            logger.error("Error ocurred during executing command - [$command]", e)
//                        }
//
//                        it.substitutes = result
////                        it.length = it.substitutes.length()
//                        it.length = it.substitutes.getBytes().length
//                    }
//                }
        ]
    }



    /**
     *  These are Basic Functions
     *  Watch Below To Add Function.
     *
     *  Method 1 => New VariableMan().addFuncs(Map)
     *
     * @return
     */
    Map<String, Closure> getBasicFuncMap(){
        return [
                'START': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.length && it.members ){
                        long nextSeq = Long.parseLong(it.substitutes)
                        long standardStartNum = Long.parseLong(it.members[0])
                        if (nextSeq < standardStartNum)
                            it.substitutes = it.members[0]
                    }
                },
                'LEFT': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.length && it.members ){
                        int diff = -1
                        int tryRemoveIdx = 0
                        if (!it.members || it.members[0].isEmpty() )
                            it.members[0] = ' '
                        while (diff != 0){
                            byte[] bytes = (charset) ? it.substitutes.getBytes(charset) : it.substitutes.getBytes()
                            diff = it.length - bytes.length
                            if (diff < 0){
//                                it.substitutes = it.substitutes.substring(0, it.length)
                                it.substitutes = (charset) ? new String(bytes, 0, it.length -tryRemoveIdx, charset) : new String(bytes, 0, it.length -tryRemoveIdx)
                                tryRemoveIdx++
                            }else if (diff == 0){
                            }else if (diff > 0){
                                it.substitutes = "${it.members[0]}${it.substitutes}"
                            }
                        }
                    }
                },
                'RIGHT': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.length && it.members ){
                        int diff = -1
                        int tryRemoveIdx = 0
                        if (!it.members || it.members[0].isEmpty() )
                            it.members[0] = ' '
                        while (diff != 0){
                            byte[] bytes = (charset) ? it.substitutes.getBytes(charset) : it.substitutes.getBytes()
                            diff = it.length - bytes.length
                            if (diff < 0){
//                                it.substitutes = it.substitutes.substring(0, it.length-1)
                                it.substitutes = (charset) ? new String(bytes, 0, it.length -tryRemoveIdx, charset) : new String(bytes, 0, it.length -tryRemoveIdx)
                                tryRemoveIdx++
                            }else if (diff == 0){
                            }else if (diff > 0){
                                it.substitutes = "${it.substitutes}${it.members[0]}"
                            }
                        }
                    }
                },

                /*************************
                 * Condition
                 *************************/
                'ERROR': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.length && it.members ){
                        String errorNm = it.members[0].toUpperCase()
                        if (errorNm.equals('OVER') && it.isOver)
                            throw new Exception( "${VAR2} - Rule is '${it.partValue}', But ${it.valueCode}'s value is ${it.overValue}", new Throwable(" Seted Rule is '${it.partValue}', But ${it.valueCode}'s value is ${it.overValue}") )
                    }
                },
                'EXIST': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (!it.substitutes)
                        throw new ConditionNotMatchedFinishException()
                },

                /*************************
                 * Convert
                 *************************/
                'LOWER': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    it.substitutes = (it.substitutes) ? it.substitutes.toLowerCase() : ""
                },
                'UPPER': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    it.substitutes = (it.substitutes) ? it.substitutes.toUpperCase() : ""
                },
                'NUMBERONLY': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    it.substitutes = (it.substitutes) ? it.substitutes.replaceAll("[^0-9.]", "") : ""
                },
                'NVL': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (!it.substitutes && it.members){
                        String nvlValue = it.members[0]
                        if (nvlValue)
                            it.substitutes = nvlValue
                    }
                },
                'DATEFORMAT': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (!it.substitutes)
                        return
                    String beforeDateFormat
                    String afterDateFormat
                    if (it.members.size() == 1){
                        //ex) var().dateformat(yyyymmdd)
                        afterDateFormat = parseMember(it.members[0], vsMap, vcMap)
                        Date dateValue = new SimpleDateFormat(afterDateFormat).parse(it.substitutes)
                        it.substitutes = new SimpleDateFormat(afterDateFormat).format(dateValue)
                    }else if (it.members.size() == 2){
                        beforeDateFormat = parseMember(it.members[0], vsMap, vcMap)
                        afterDateFormat = parseMember(it.members[1], vsMap, vcMap)
                        if (afterDateFormat.equals("long")){
                            //ex) var().dateformat(yyyymmdd, long)
                            Date dateValue = new SimpleDateFormat(beforeDateFormat).parse(it.substitutes)
                            it.substitutes = dateValue.getTime()

                        }else if (beforeDateFormat.equals("long")){
                            //ex) var().dateformat(long, yyyymmdd)
                            if (!it.substitutes)
                                return
                            Long time = (it.substitutes instanceof Long) ? it.substitutes : Long.parseLong(it.substitutes)
                            Date beforeDate = new Date(time)
                            it.substitutes = new SimpleDateFormat(afterDateFormat).format(beforeDate)

                        }else{
                            //ex) var().dateformat(yyyymmdd, yyyy/mm/dd)
                            Date dateValue = new SimpleDateFormat(beforeDateFormat).parse(it.substitutes)
                            it.substitutes = new SimpleDateFormat(afterDateFormat).format(dateValue)
                        }
                    }
                    it.length = it.substitutes.getBytes().length
                },

                /*************************
                 * Append
                 *************************/
                'ADD': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String addtion = parseMember(it.members[0], vsMap, vcMap)
                        it.substitutes = (it.substitutes ?: '') + addtion
                    }
                },
                'ADDBEFORE': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String addtion = parseMember(it.members[0], vsMap, vcMap)
                        it.substitutes = addtion + (it.substitutes ?: '')
                    }
                },
                'ADDVARIABLE': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String addtion = parseMember(it.members[0], vsMap, vcMap)
                        String parsedString = new VariableMan(vsMap).parse( '${' +addtion+ '}' )
                        it.substitutes = (it.substitutes ?: '') + parsedString
                    }
                },
                'ADDVARIABLEBEFORE': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String addtion = parseMember(it.members[0], vsMap, vcMap)
                        String parsedString = new VariableMan(vsMap).parse(addtion)
                        it.substitutes = parsedString + (it.substitutes ?: '')
                    }
                },

                /*************************
                 * Replace
                 *************************/
                'REPLACE': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members && it.members.size() == 2){
                        String target = parseMember(it.members[0], vsMap, vcMap)
                        String replacement = parseMember(it.members[1], vsMap, vcMap)
                        it.substitutes = (it.substitutes) ? it.substitutes.replace(target, replacement) : ""
                    }
                },
                'REPLACEALL': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members && it.members.size() == 2){
                        String target = parseMember(it.members[0], vsMap, vcMap)
                        String replacement = parseMember(it.members[1], vsMap, vcMap)
                        it.substitutes = (it.substitutes) ? it.substitutes.replaceAll(target, replacement) : ""
                    }
                },
                'REPLACEFIRST': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members && it.members.size() == 2){
                        String target = parseMember(it.members[0], vsMap, vcMap)
                        String replacement = parseMember(it.members[1], vsMap, vcMap)
                        it.substitutes = (it.substitutes) ? it.substitutes.replaceFirst(target, replacement) : ""
                    }
                },
                'JOIN': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if ( (it.substitutes || it.reducedCollection) && it.members ){
                        String seperator = parseMember(it.members[0], vsMap, vcMap)
                        Object rawObjectValue = (it.reducedCollection != null) ? it.reducedCollection : (it.originalValue != null) ? it.originalValue : vsMap[it.originalCode]
                        if (rawObjectValue instanceof List){
                            it.substitutes = rawObjectValue.join(seperator)
                        }
                    }
                },
        ]
    }

    /**
     *  These are Basic Functions
     *  Watch Below To Add Function.
     *
     *  Method 1 => New VariableMan().addFuncs(Map)
     *
     * @return
     */
    Map<String, Closure> getBasicConditionFuncMap(){
        return [
                /*************************
                 * Condition
                 *************************/
                'IF': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    it.modeIf = true
                    it.okIfSeq = 0
                    it.nowIfSeq = 1
                    if (it.modeIf && checkConditionFunction(it, vsMap, vcMap)){
                        it.okIfSeq = it.nowIfSeq
                    }
                },
                'ELSEIF': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    ++it.nowIfSeq
                    if (it.modeIf && it.okIfSeq == 0 && checkConditionFunction(it, vsMap, vcMap)){
                        it.okIfSeq = it.nowIfSeq
                    }
                },
                'ELSE': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    ++it.nowIfSeq
                    if (it.modeIf && it.okIfSeq == 0){
                        it.okIfSeq = it.nowIfSeq
                    }
                },
                'ENDIF': { OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    it.modeIf = false
                    it.okIfSeq = 0
                    it.nowIfSeq = 0
                },
        ]
    }

    Map<List<String>, Closure> getBasicStreamFuncMap(){
        return [
                /*************************
                 * Stream
                 *************************/
                'STREAM': { OnePartObject part, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    part.modeStream = true
                    part.okStreamLevel = 0
                    part.nowStreamLevel = 1
                    if (part.modeStream){
                        part.okStreamLevel = part.nowStreamLevel;
                        part.virtualRuleBuilderInStream = new StringBuilder().append("empty()");
                        part.originalCode = part.originalCode ?: part.members[0]
                        part.originalValue = getIgnoreCase(vsMap, part.originalCode);
                        //Copy Collection
                        if (part.originalValue instanceof List){
                            List<?> originList = part.originalValue
                            int size = originList.size();
                            List<?> reducedCollection = new ArrayList<>(originList);
                            Collections.copy(reducedCollection, originList);
                            part.reducedCollection = reducedCollection
                        }else if (part.originalValue instanceof Map){
                            Map originMap = part.originalValue
                            part.reducedCollection = new HashMap(originMap);
                        }
                    }
                },
                'FILTER': { OnePartObject part, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (part.modeStream){
                        if (part.reducedCollection instanceof List){
                            List list = part.reducedCollection
                            List newList = new ArrayList<>();
                            Object some = null;
                            Object itBefore = vsMap.get("it");
                            for (int i=0; i<list.size(); i++){
                                some = list.get(i);
                                vsMap.put("it", some);
                                if (computeAsMemberArrayInIf(part.members, some, vsMap, vcMap))
                                    newList.add(some);
                            }
                            vsMap.put("it", itBefore);
                            part.reducedCollection = newList;

                        }else (part.reducedCollection instanceof Map){
                            Map map = it.reducedCollection
                            Map newMap = new HashMap<>();
                            Iterator iter = map.iterator()
                            Object some = null;
                            Object itBefore = vsMap.get("it");
                            while (iter.hasNext()){
                                some = iter.next();
                                vsMap.put("it", some);
                                if (computeAsMemberArrayInIf(part.members, some, vsMap, vcMap))
                                    newMap.put(some);
                            }
                            vsMap.put("it", itBefore);
                            part.reducedCollection = newMap;
                        }

                    }
                },
                'ENDSTREAM': { OnePartObject part, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    part.modeStream = false
                    part.okStreamLevel = 0
                    part.nowStreamLevel = 0
                    if (!part.modeStream){
                        //3) End 일때 새롭게 파싱
                        String virtualRuleContent = part.virtualRuleBuilderInStream;
                        Object beforeReducedCollection = part.reducedCollection
                        if (beforeReducedCollection instanceof List){
                            List<?> afterReducedCollection = new ArrayList<>();
                            for (Object beforeReducedItem : beforeReducedCollection){
                                String reducedParsedResult = parseVirtualRuleContent(virtualRuleContent, beforeReducedItem, vsMap, vcMap)
                                afterReducedCollection.add( reducedParsedResult )
                            }
                            part.reducedCollection = afterReducedCollection
                        }else if (beforeReducedCollection instanceof Map){
                            Map<?> afterReducedCollection = new HashMap<>();
                            for (Object item : beforeReducedCollection){
                                String reducedParsedResult = parseVirtualRuleContent(virtualRuleContent, item, vsMap, vcMap)
                                afterReducedCollection.put( item, reducedParsedResult )
                            }
                            part.reducedCollection = afterReducedCollection
                        }
                    }
                },
        ]
    }

    Map<List<String>, Closure> getBasicConditionComputerFuncMap(){
        return [
                /*************************
                 * Condition Computer
                 *************************/
                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A == B)
                 ***************/
                ['EQUALS', '==']: { valA, valB ->
                    return (valB == null) ? valA == null : (valA != null) ? valA.equals(valB) : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A != B)
                 ***************/
                ['NOTEQUALS', '!=']: { valA, valB ->
                    return (valB == null) ? valA != null : (valA != null) ? !valA.equals(valB) : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A < B)
                 ***************/
                ['LT', '<']: { valA, valB ->
                    Number a = (Number) valA
                    Number b = (Number) valB
                    return (valA != null) ? a < b : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A > B)
                 ***************/
                ['GT', '>']: { valA, valB ->
                    Number a = (Number) valA
                    Number b = (Number) valB
                    return (valA != null) ? a > b : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A <= B)
                 ***************/
                ['LTE', '<=']: { valA, valB ->
                    Number a = (Number) valA
                    Number b = (Number) valB
                    return (valA != null) ? a <= b : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A >= B)
                 ***************/
                ['GTE', '>=']: { valA, valB ->
                    Number a = (Number) valA
                    Number b = (Number) valB
                    return (valA != null) ? a >= b : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A ^ B)
                 ***************/
                ['STARTS', '^']: { valA, valB ->
                    return (valA != null) ? valA.startsWith(valB) : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A $ B)
                 ***************/
                ['ENDS', '$']: { valA, valB ->
                    return (valA != null) ? valA.endsWith(valB) : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A ~ B)
                 ***************/
                ['CONTAINS', '~']: { valA, valB ->
                    return (valA != null) ? valA.contains(valB) : false;
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX)
                 *      - if(A, computer, B)
                 *      - if(A ==~ B)
                 ***************/
                ['MATCHES', '==~']: { valA, valB ->
                    return (valA != null) ? valA.matches(valB) : false;
                },

                /***************
                 * - A: value
                 * - EX)
                 *      - if(A, computer)
                 *      - if(A EXISTS)
                 ***************/
                ['EXISTS', '!!']: { valA, valB ->
                    return (!!valA);
                },

                /***************
                 * - A: value
                 * - EX)
                 *      - if(A, computer)
                 *      - if(A EMPTY)
                 ***************/
                ['EMPTY']: { valA, valB ->
                    return (valA != null) ? (valA == '') : false;
                },

                /***************
                 * - A: value
                 * - EX) if(computer, A)
                 ***************/
                ['NULL']: { valA, valB ->
                    return (valA == null);
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX) if(A, computer, B)
                 ***************/
                ["FILE-EXISTS", "FILE"]: {  valA, valB ->
                    return new File(valB).exists()
                },

                /***************
                 * - A: value
                 * - B: condition value
                 * - EX) if(A, computer, B)
                 ***************/
                ["FILE-CONTAINS", "FILE~"]: { valA, valB ->
                    if (valA == null)
                        return false
                    String text = new File(valA).getText()
                    return (valB != null) ? text.contains(valB) : (text == valB)
                },

                /***************
                 * - A: ! 으로 split하여 앞은 파일경로 뒤는 property key
                 * - B: condition value
                 * - EX) if(A, computer, B)
                 ***************/
                ["PROP-EQUALS", "PROP="]: { valA, valB ->
                    String[] pathAndPropKey = valA.split("!")
                    String filePath = pathAndPropKey[0]
                    String checkPropKey = pathAndPropKey[1]
                    Properties prop = new Properties()
                    prop.load(new File(filePath).newInputStream())
                    String value = prop.get(checkPropKey)
                    return valB.equals(value)
                },
        ]
    }


    private boolean checkConditionFunction(OnePartObject part, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        String value = part.substitutes ?: getIgnoreCase(variableStringMap, part.valueCode)
        return computeAsMemberArrayInIf(part.members, value, variableStringMap, variableClosureMap)
    }

    private boolean computeAsMemberArrayInIf(String[] members, Object alreadyDefinedValue, Map<String, Object> variableStringMap, Map<String, Object> variableClosureMap){
        boolean result = false
        int memberSize = members.size()
        String value, computer, targetValue

        if (memberSize == 1){
            String notYetConfirmedOperater = members[0]
            String maybeComputerString = notYetConfirmedOperater.toUpperCase()
            if (hasComputer(maybeComputerString) || hasComputeWithNegativeMark(maybeComputerString)){
                value = alreadyDefinedValue //변수를 먼저 입력 후 Pipeline 방식 IF가 입력될때 value variable로서 인정한다.
                computer = notYetConfirmedOperater
                result = computeOperator(value, computer, targetValue)
            }else{
                result = computeAsNaturalOperatingStringInIf(notYetConfirmedOperater, alreadyDefinedValue, variableStringMap, variableClosureMap)
            }

        }else if (memberSize == 2){
            value = alreadyDefinedValue //변수를 먼저 입력 후 Pipeline 방식 IF가 입력될때 value variable로서 인정한다.
            computer = members[0]
            targetValue = parseMemberAsStrict(members[1], variableStringMap, variableClosureMap)
            result = computeOperator(value, computer, targetValue)

        }else if (memberSize == 3){
            value = parseMemberAsStrict(members[0], variableStringMap, variableClosureMap)
            computer = members[1]
            targetValue = parseMemberAsStrict(members[2], variableStringMap, variableClosureMap)
            result = computeOperator(value, computer, targetValue)

        }else{
            throw new Exception("(Error - member-size:$memberSize - ${members?.toArrayString()})")
        }

        return result
    }

    private boolean hasComputer(String computerKey){
        return this.conditionComputerFuncMap.containsKey(computerKey)
    }

    private boolean hasComputeWithNegativeMark(String computerKey){
        computerKey = removeNegativeMark(computerKey)
        return hasComputer(computerKey)
    }

    private String removeNegativeMark(String computerKey){
        while (true){
            if (computerKey.startsWith("NOT")){
                computerKey = computerKey.replaceFirst("NOT", "")
            }else if (computerKey.startsWith("!")){
                computerKey = computerKey.replaceFirst("!", "")
            }else{
                break;
            }
        }
        return computerKey
    }

    private Closure getComputer(String computerKey){
        return this.conditionComputerFuncMap.get(computerKey)
    }

    private boolean computeAsNaturalOperatingStringInIf(String notYetConfirmed){
        return computeAsNaturalOperatingStringInIf(notYetConfirmed, null, this.variableStringMap, this.variableClosureMap)
    }

    private boolean computeAsNaturalOperatingStringInIf(String notYetConfirmed, Object alreadyDefinedValue, Map<String, Object> variableStringMap, Map<String, Closure> variableClosureMap){
        boolean result = false
        Object value, targetValue
        String computer

        //Quote 인 것들 {} 로 대체하며(다른 Collection에 담아놓기), 문자들 Concat하고 다시 기존 특수문자 Split 진행 후, 담아놓은 대체된 문자들 value로 활용
        List<String> groups = SimpleSplitUtil.separateGroupByQuotes(notYetConfirmed)
        List<String> stringValues = groups.findAll{ SimpleDataUtil.checkStatusQuotedString(it) }
        String alternativeString = '{}'
        String convertedNotYetConfirmed = groups.collect{(SimpleDataUtil.checkStatusQuotedString(it)) ? alternativeString : it }.join("")

        //Extract Value
        String[] splitedOperatingLogicalArray = convertedNotYetConfirmed.split('\\s*[\\^$><=!~]+\\s*')
        int injectIndex = -1;
        splitedOperatingLogicalArray = splitedOperatingLogicalArray.collect{
            while (it.contains(alternativeString)){
                String replacement = stringValues[++injectIndex]
                if (replacement.indexOf('\\') != -1)
                    replacement = replacement.replaceAll('\\\\','\\\\\\\\')
                // This Condition's Logic prevent Error - java.lang.IllegalArgumentException: named capturing group has 0 length name
                if (replacement.indexOf('$') != -1)
                    replacement = replacement.replaceAll('\\$','\\\\\\$')
                it = it.replaceFirst("[{][}]", replacement)
            }
            it
        }.toArray() as String[]
        int memberSize = splitedOperatingLogicalArray.size()

        //Extract Operator
        List<String> matchedString = Pattern.compile('[\\^$><=!~]+').matcher(convertedNotYetConfirmed)?.findAll()
        computer = (matchedString.size() > 0) ? matchedString.get(0) : null

        if (computer == null){
            switch (memberSize){
                case 1: //- if(value)
                    value = parseMemberAsStrict(splitedOperatingLogicalArray[0], variableStringMap, variableClosureMap)
                    result = computeBoolean(value)
                    break;

                default: //- if(value value..???)
                    throw new Exception("(Error - member-size:$memberSize - ${splitedOperatingLogicalArray?.toArrayString()})")
                    break;
            }

        }else{
            switch (memberSize){
                case 1: //- if(value)
                    value = parseMemberAsStrict(splitedOperatingLogicalArray[0], variableStringMap, variableClosureMap)
                    result = computeBoolean(value)
                    break;

                case 2:
                    if (splitedOperatingLogicalArray[0].isEmpty()){
                        if (alreadyDefinedValue != null){
                            //- someValue().if(computer value)
                            value = alreadyDefinedValue
                            targetValue = parseMemberAsStrict(splitedOperatingLogicalArray[1], variableStringMap, variableClosureMap)
                            result = computeOperator(value, computer, targetValue)
                        }else if (computer.startsWith("!") && computer.endsWith("!")){
                            //- if(!value)
                            value = parseMemberAsStrict(splitedOperatingLogicalArray[1], variableStringMap, variableClosureMap)
                            result = computeBoolean(value)
                            result = computeNegativeMark(result, computer)
                        }
                    }else{
                        //- if (value computer targetValue)
                        value = parseMemberAsStrict(splitedOperatingLogicalArray[0], variableStringMap, variableClosureMap)
                        targetValue = parseMemberAsStrict(splitedOperatingLogicalArray[1], variableStringMap, variableClosureMap)
                        result = computeOperator(value, computer, targetValue)
                    }
                    break;

                default: //- if (???)
                    throw new Exception("(Error - member-size:$memberSize - ${splitedOperatingLogicalArray?.toArrayString()})")
                    break;
            }
        }

        return result
    }

    private boolean computeBoolean(Object variable){
        //List,Map 등은 Size !0 //String !EmptyString, Integer,Long은 !0
        return !!variable
    }

    private boolean computeNegativeMark(boolean result, String computer){
        return computeNegativeMark(result, computer, Arrays.asList("!"))
    }

    private boolean computeNegativeMark(boolean result, String computer, List<String> negativeChars){
        String negativeChar
        while (true){
            negativeChar = negativeChars.find{ computer.startsWith(it) }
            if (negativeChar != null){
                int lastIndex = negativeChar.length()
                computer = computer.substring(lastIndex)
                result = !result
            }else{
                break;
            }
        }
        return result
    }

    private boolean computeOperator(Object a, String computer, Object b){
        String computerUpper = computer.toUpperCase()
        boolean computerResult = false
        boolean modeNegative = false
        Closure closure

        //- cacluate negative
        if (!hasComputer(computerUpper) && hasComputeWithNegativeMark(computerUpper)){
            //- parse '!' or 'not'
            modeNegative = computeNegativeMark(modeNegative, computerUpper, ["!", "NOT"])
            computerUpper = removeNegativeMark(computerUpper)
        }

        //- compute
        closure = getComputer(computerUpper)
        if (closure){
            computerResult = (modeNegative) ? !closure(a, b) : closure(a, b)
        }else{
            throw new Exception(VAR4, new Throwable("[${computerUpper}]") )
        }

        return computerResult
    }

    private boolean containsIgnoreCase(List list, String value){
        value = value.toUpperCase()
        List resultItems = list.findAll{ String item ->
            item.toUpperCase().contains(value)
        }
        if (resultItems && resultItems.size() > 0)
            return true
        else
            return false
    }

    private static boolean containsKeyIgnoreCase(Map map, String key){
        key = key.toUpperCase()
        List resultKeys = map.keySet().findAll{ String itKey ->
            itKey.toUpperCase().contains(key)
        }.toList()
        if (resultKeys && resultKeys.size() > 0)
            return true
        else
            return false
    }

    private static Object getIgnoreCase(Map map, String key){
        key = key.toUpperCase()
        List resultKeys = map.keySet().findAll{ String itKey ->
            itKey.toUpperCase().equals(key)
        }.toList()
        if (resultKeys && resultKeys.size() > 0){
            key = resultKeys[0]
            return map.get(key);
        }else{
            return null
        }
    }

}