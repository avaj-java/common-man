package jaemisseo.man.bean

import jaemisseo.man.annotation.QueryColumn
import jaemisseo.man.annotation.QueryTable

/**
 * Created by sujkim on 2017-02-02.
 */
@QueryTable("USER_TAB_COLUMNS")
class ManColTestBean {

    @QueryColumn("TABLE_NAME")
    Integer tableName

    @QueryColumn("COLUMN_NAME")
    Integer columnName

    @QueryColumn("COLUMN_ID")
    Integer columnId

}
