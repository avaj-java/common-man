package jaemisseo.man

import jaemisseo.man.code.ChangeStatusCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/29/16
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */
class CompareMan {

    static final Logger logger = LoggerFactory.getLogger(this.getClass());

    List compareField
    def statusNone
    def statusNew
    def statusModified
    def statusRemoved
    String statusFieldName
    def standardObject
    def targetObject

    Closure closureNoneObject
    Closure closureModifiedObject
    Closure closureRemovedObject
    Closure closureNewObject



    CompareMan setCompareField(compareField){
        this.compareField = compareField
        return this
    }
    CompareMan setStatusNone(def statusNone){
        this.statusNone = statusNone
        return this
    }
    CompareMan setStatusNew(def statusNew){
        this.statusNew = statusNew
        return this
    }
    CompareMan setStatusModified(def statusModified){
        this.statusModified = statusModified
        return this
    }
    CompareMan setStatusRemoved(def statusRemoved){
        this.statusRemoved = statusRemoved
        return this
    }
    CompareMan setStatusFieldName(String statusFieldName){
        this.statusFieldName = statusFieldName
        return this
    }
    CompareMan setStandardObject(def standardObject){
        this.standardObject = standardObject
        return this
    }
    CompareMan setTargetObject(def targetObject){
        this.targetObject = targetObject
        return this
    }


    CompareMan eachNoneObject(Closure closureNoneObject){
        this.closureNoneObject = closureNoneObject
        return this
    }
    CompareMan eachModifiedObject(Closure closureModifiedObject){
        this.closureModifiedObject = closureModifiedObject
        return this
    }
    CompareMan eachNewObject(Closure closureNewObject){
        this.closureNewObject = closureNewObject
        return this
    }
    CompareMan eachRemovedObject(Closure closureDeletedObject){
        this.closureRemovedObject = closureDeletedObject
        return this
    }

    /**************************************************
     * INPUT STATUS
     **************************************************/
    CompareMan inputStatus(){
        // INPUT Status(New, Modified, None)
        targetObject.each{ String key, def target ->
            def standard = standardObject[key]
            // Compare
            if(standard){
                if(isEquals(target, standard)){
                    /** NONE **/
                    target[statusFieldName] = statusNone
                    standard[statusFieldName] =  statusNone
                    if (closureNoneObject)
                        closureNoneObject(standard, target)
                }else{
                    /** MODIFIED **/
                    target[statusFieldName] =  statusModified
                    standard[statusFieldName] = statusModified
                    if (closureModifiedObject)
                        closureModifiedObject(standard, target)
                }
            }else{
                /** NEW **/
                target[statusFieldName] = statusNew
                if (closureNewObject)
                    closureNewObject(standard, target)

            }
        }

        // INPUT Status(Removed)
        standardObject.findAll{ !it.value[statusFieldName] }.each{ String key, def standard ->
            /** REMOVED **/
            standard[statusFieldName] = statusRemoved
            if (closureRemovedObject)
                closureRemovedObject(standard)
        }
        return this
    }

    /**************************************************
     * EACH
     **************************************************/
    CompareMan each(Closure eachClosure){
        targetObject.each{ String key, def target ->
            def standard = standardObject[key]
            // Compare
            if(standard){
                if(isEquals(target, standard)){
                    /** NONE **/
                    eachClosure(standard, target, statusNone)
                    if (closureNoneObject)
                        closureNoneObject(standard, target)
                }else{
                    /** MODIFIED **/
                    eachClosure(standard, target, statusModified)
                    if (closureModifiedObject)
                        closureModifiedObject(standard, target)
                }
            }else{
                /** NEW **/
                eachClosure(standard, target, statusNew)
                if (closureNewObject)
                    closureNewObject(standard, target)

            }
        }

        standardObject.findAll{ !targetObject[it.key] }.each{ String key, def standard ->
            /** REMOVED **/
            eachClosure(standard, null, statusRemoved)
            if (closureRemovedObject)
                closureRemovedObject(standard)
        }
        return this
    }

    class AnalisysResult{
        List newList
        List modifiedList
        List noneList
        List removedList
    }





    Map getChangedMap(){
        Map removedMap = standardObject.findAll{ it.value[statusFieldName] == statusRemoved }
        targetObject.putAll(removedMap)
        Map changedUserMap = targetObject.findAll{ it.value[statusFieldName] != statusNone }
        return changedUserMap
    }

    List getChangedList(){
        List result = getChangedMap().collect{ it.value }
        return result
    }

    boolean isEquals(def standard, def target) {
        def standardValue
        def targetValue
        if (compareField){
            return compareField.every{ String fieldName ->
                if (fieldName.indexOf('.') != -1){
                    standardValue = getFieldValue(standard, fieldName)
                    targetValue = getFieldValue(target, fieldName)
                }else{
                    standardValue = standard[fieldName]
                    targetValue = target[fieldName]
                }
                return (standardValue == target)
            }
        }else{
            return (standard == target)
        }
    }

    static def getFieldValue(def object, String fieldName){
        String[] propertyParts = fieldName.split('[.]')
        propertyParts.each{
            object = object[it]
        }
        return object
    }



    /*************************
     *
     * 비교하여 상태값 저장하기
     *
     *  - standardData = 특정 Bean 또는 List
     *  - targetData = 특정 Bean 또는 List
     *  - keyAttributenName = 아이디값을 갖고 있는 필드명
     *  - statusAttributeName = 상태값을 넣을 필드명
     *  - compareAttributeList = 비교할 필드명 List
     *************************/
    static List<Object> compare(Object standardData, Object targetData, String keyAttributeName, String statusAttributeName, List<String> compareAttributeList){
        return compare(standardData, targetData, [keyAttributeName], statusAttributeName, compareAttributeList)
    }

    static List<Object> compare(Object standardData, Object targetData, List<String> keyAttributeNameList, String statusAttributeName, List<String> compareAttributeList){
        Map<String, Object> standardDataMap = toIdDataMap(standardData, keyAttributeNameList)
        Map<String, Object> targetDataMap = toIdDataMap(targetData, keyAttributeNameList)
        List<Object> changedList = compare(standardDataMap, targetDataMap, statusAttributeName, compareAttributeList)
        return changedList
    }

    static List<Object> compare(Map<String, Object> standardDataMap, Map<String, Object> targetDataMap, String statusAttributeName, List<String> compareAttributeList){
        //3. 동기화 설정 (Meta:Target)
        CompareMan compareMan = new CompareMan()
                .setStandardObject(standardDataMap)
                .setTargetObject(targetDataMap)
                .setCompareField(compareAttributeList)
                .setStatusFieldName(statusAttributeName)
                .setStatusNone(ChangeStatusCode.NONE)
                .setStatusNew(ChangeStatusCode.NEW)
                .setStatusModified(ChangeStatusCode.MODIFIED)
                .setStatusRemoved(ChangeStatusCode.REMOVED)
                .eachNewObject{ Object standardData, Object targetData ->
                    /** [신규]시 **/
                    logger.trace("compare - NEW")
                }
                .eachModifiedObject{ Object standardData, Object targetData ->
                    /** [수정]시 **/
                    logger.trace("compare - MOD")
                }
                .eachRemovedObject{ Object standardData ->
                    /** [삭제]시 **/
                    logger.trace("compare - DEL")
                }

        //4. 동기화 설정 적용
        compareMan.inputStatus()
        return compareMan.getChangedList()
    }

    static Map<String, Object> toIdDataMap(Object object, List<String> keyFieldNameList){
        Map<String, Object> resultMap = [:]
        switch (object){
            case null:
                break

            case {object instanceof List}:
                object.each{ Object item ->
                    String key = keyFieldNameList.collect{ getFieldValue(item, it) }.join('+')
                    resultMap[key] = item
                }
                break

            default:
                String key = keyFieldNameList.collect{ object[it] }.join('+')
                resultMap[key] = object
                break
        }
        return resultMap
    }

    /*************************
     *
     * 비교 루프 돌기
     *
     *  - standardData = 특정 Bean 또는 List
     *  - targetData = 특정 Bean 또는 List
     *  - keyAttributenName = 아이디값을 갖고 있는 필드명
     *  - statusAttributeName = 상태값을 넣을 필드명
     *  - compareAttributeList = 비교할 필드명 List
     *************************/
    static void each(Object standardData, Object targetData, String keyAttributeName, List<String> compareAttributeList, Closure eachClosure){
        each(standardData, targetData, [keyAttributeName], compareAttributeList, eachClosure)
    }

    static void each(Object standardData, Object targetData, List<String> keyAttributeNameList, List<String> compareAttributeList, Closure eachClosure){
        Map<String, Object> standardDataMap = toIdDataMap(standardData, keyAttributeNameList)
        Map<String, Object> targetDataMap = toIdDataMap(targetData, keyAttributeNameList)
        each(standardDataMap, targetDataMap, compareAttributeList, eachClosure)
    }

    static void each(Map<String, Object> standardDataMap, Map<String, Object> targetDataMap, List<String> compareAttributeList, Closure eachClosure){
        //3. 동기화 설정 (Meta:Target)
        CompareMan compareMan = new CompareMan()
                .setStandardObject(standardDataMap)
                .setTargetObject(targetDataMap)
                .setCompareField(compareAttributeList)
//                .setStatusFieldName(statusAttributeName)
                .setStatusNone(ChangeStatusCode.NONE)
                .setStatusNew(ChangeStatusCode.NEW)
                .setStatusModified(ChangeStatusCode.MODIFIED)
                .setStatusRemoved(ChangeStatusCode.REMOVED)
                .eachNewObject{ Object standardData, Object targetData ->
                    /** [신규]시 **/
                    logger.trace("each - NEW")
                }
                .eachModifiedObject{ Object standardData, Object targetData ->
                    /** [수정]시 **/
                    logger.trace("each - MOD")
                }
                .eachRemovedObject{ Object standardData ->
                    /** [삭제]시 **/
                    logger.trace("each - DEL")
                }

        //4. 동기화 설정 적용
        compareMan.each(eachClosure)
    }


}
