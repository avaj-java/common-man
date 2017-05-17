package jaemisseo.man.bean

import jaemisseo.man.annotation.QueryAutoValue
import jaemisseo.man.annotation.QueryColumn
import jaemisseo.man.annotation.QueryPageNumber
import jaemisseo.man.annotation.QueryPageSize
import jaemisseo.man.annotation.QueryTable

/**
 * Created by sujkim on 2017-02-02.
 */
@QueryAutoValue
@QueryTable("META_DEVELOPER")
class ManTestBean {

    @QueryColumn("OBJECT_ID")
    Integer objectId

    @QueryColumn("GROUP_NAME")
    String groupName

    @QueryColumn("CLASS_NAME")
    String className


    @QueryColumn("ATTRIBUTE_NAME")
    String attributeName

    @QueryColumn("DESCRIPTION")
    String description

    @QueryColumn("TABLE_NAME")
    String tableName

    @QueryColumn("CLASS_ID")
    Integer classId

    @QueryColumn("PROPERTY_ID")
    Integer propertyId

    @QueryColumn("COLUMN_NAME")
    String columnName

    @QueryColumn("CREATE_DT")
    Date createDt

    @QueryColumn("CUSR")
    String cusr

    @QueryPageNumber
    Integer page

    @QueryPageSize
    Integer pageSize

    void setTableName(String tableName){ this.tableName = tableName?.toUpperCase() }
    void setColumnName(String columnName){ this.columnName = columnName?.toUpperCase() }
}
