package jaemisseo.man

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

    final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        // status REMOVED
        standardObject.findAll{ !it.value.status }.each{ String key, def standard ->
            standard[statusFieldName] = statusRemoved
            if (closureRemovedObject)
                closureRemovedObject(standard)
        }
        return this
    }

    Map getChangedMap(){
        Map removedMap = standardObject.findAll{ it.value.status == statusRemoved }
        targetObject.putAll(removedMap)
        Map changedUserMap = targetObject.findAll{ it.value.status != statusNone }
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


}
