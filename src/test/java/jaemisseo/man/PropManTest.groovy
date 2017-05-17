package jaemisseo.man

import org.junit.Test

/**
 * Created by sujkim on 2017-05-06.
 */
class PropManTest {


    @Test
    void "as Boolean"(){
        assert "1" as Boolean
        assert "0" as Boolean
        assert "true" as Boolean
        assert "false" as Boolean
        assert null as Boolean == null
        assert "" as Boolean == false
        assert "tue" as Boolean == true
        assert "flse" as Boolean == true

        PropMan propman = new PropMan([aaa:'', bbb:'false', ccc:'true', ddd:'ddd', eee:false, fff:true])
        assert propman.getBoolean('aaa') == null
        assert propman.getBoolean('bbb') == false
        assert propman.getBoolean('ccc') == true
        assert propman.getBoolean('ddd') == true
        assert propman.getBoolean('eee') == false
        assert propman.getBoolean('fff') == true
    }

}
