package jaemisseo.man.bean

import jaemisseo.man.annotation.QueryColumn
import jaemisseo.man.annotation.QueryTable

@QueryTable('TEST_QUERY_MAN')
class QueryManTestBean {

    @QueryColumn('COL1')
    String col1

    @QueryColumn('COL2')
    String col2

    @QueryColumn('COL3')
    boolean col3

    @QueryColumn('COL_NUM1')
    Integer colNum1

    @QueryColumn('COL_NUM2')
    Long colNum2

    @QueryColumn('COL_DATE1')
    Date colDate1

}
