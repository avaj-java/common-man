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



    CompareMan inputStatus(){
        // INPUT Status(New, Modified, None)
        targetObject.each{ String key, def target ->
            def standard = standardObject[key]
            // Compare
            if(standard){
                //status NONE
                if(isEquals(target, standard)){
                    target[statusFieldName] = statusNone
                    standard[statusFieldName] =  statusNone
                    if (closureNoneObject)
                        closureNoneObject(standard, target)
                //status MODIFIED
                }else{
                    target[statusFieldName] =  statusModified
                    standard[statusFieldName] = statusModified
                    if (closureModifiedObject)
                        closureModifiedObject(standard, target)
                }
            //status NEW
            }else{
                target[statusFieldName] = statusNew
                if (closureNewObject)
                    closureNewObject(standard, target)

            }
        }

        // INPUT Status(Removed)
        standardObject.findAll{ !it.value[statusFieldName] }.each{ String key, def standard ->
            standard[statusFieldName] = statusRemoved
            if (closureRemovedObject)
                closureRemovedObject(standard)
        }
        return this
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
        boolean isOk = true
        compareField.each{ String fieldName ->
            if (standard[fieldName] != target[fieldName])
                isOk = false
        }
        return isOk
    }



    /*************************
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
                    logger.debug(" - NEW")
                }
                .eachModifiedObject{ Object standardData, Object targetData ->
                    /** [수정]시 **/
                    logger.debug(" - MOD")
                }
                .eachRemovedObject{ Object standardData ->
                    /** [삭제]시 **/
                    logger.debug(" - DEL")
                }

        //4. 동기화 설정 적용
        compareMan.inputStatus()
        return compareMan.getChangedList()
    }

    static Map<String, Object> toIdDataMap(Object object, List<String> keyFieldNameList){
        Map<String, Object> resultMap = [:]
        switch (object){
            case {object instanceof List}:
                object.each{ Object item ->
                    String key = keyFieldNameList.collect{ item[it] }.join('+')
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

}
