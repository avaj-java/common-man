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

        PropMan propman = new PropMan([
                aaa:'',
                bbb:'false',
                ccc:'true',
                ddd:'ddd',
                eee:false,
                fff:true
        ])

        assert propman.getBoolean('aaa') == null
        assert propman.getBoolean('bbb') == false
        assert propman.getBoolean('ccc') == true
        assert propman.getBoolean('ddd') == true
        assert propman.getBoolean('eee') == false
        assert propman.getBoolean('fff') == true
    }



    @Test
    void isMatchingProperty(){
        Map<String, String> testMap = [
            'fruit.apple.taste' : 'val',
            'fruit.apple.value' : 'val',
            'fruit.apple.color' : 'val',
            'fruit.banana.taste' : 'val',
            'fruit.banana.value' : 'val',
            'fruit.banana.color' : 'val',
            'fruit.blueberry.taste' : 'val',
            'fruit.blueberry.value' : 'val',
            'fruit.blueberry.color' : 'val',
            'fruit.melon.taste' : 'val',
            'fruit.melon.value' : 'val',
            'fruit.melon.color' : 'val',
        ]
        List<String> propertyNameList = testMap.keySet().toList()

        assert isMachingData(propertyNameList, 'fruit.*.taste', [
                'fruit.apple.taste', 'fruit.banana.taste', 'fruit.blueberry.taste', 'fruit.melon.taste'
        ])

        assert isMachingData(propertyNameList, 'fruit.b*.taste', [
                'fruit.banana.taste', 'fruit.blueberry.taste'
        ])

        assert isMachingData(propertyNameList, '*.b*.taste', [
                'fruit.banana.taste', 'fruit.blueberry.taste'
        ])

        assert isMachingData(propertyNameList, '*.*l*.*', [
                'fruit.apple.taste', 'fruit.apple.value', 'fruit.apple.color',
                'fruit.melon.taste', 'fruit.melon.value', 'fruit.melon.color',
                'fruit.blueberry.taste', 'fruit.blueberry.value', 'fruit.blueberry.color'
        ])

        assert isMachingData(propertyNameList, '**', propertyNameList)

        assert isMachingData(propertyNameList, '**.value', [
                'fruit.apple.value', 'fruit.banana.value', 'fruit.melon.value', 'fruit.blueberry.value'
        ])

        assert isMachingData(propertyNameList, 'f**Hi', [])
    }

    private boolean isMachingData(List<String> dataList, String range, List<String> assertDataList){
        boolean isSameSize = true
        boolean containsAll = true
        List<String> filteredDataList = dataList.findAll{ PropMan.isMatchingProperty(it, range) }
        //Log
        println "${range} "
        println "  => ${filteredDataList}"
        //Check
        if (filteredDataList){
            isSameSize = (assertDataList.size() == filteredDataList.size())
            containsAll = assertDataList.every{ filteredDataList.contains(it) }
        }else{
            isSameSize = (assertDataList.size() == 0)
        }
        return isSameSize && containsAll
    }


    @Test
    void compareTest(){
        PropMan beforeProp_1 = new PropMan([a:1])
        PropMan afterProp_1 = new PropMan([a:1])
        assert !beforeProp_1.compareAndMakeInsertedProperties(afterProp_1)
        assert !beforeProp_1.compareAndMakeModifiedProperties(afterProp_1)
        assert !beforeProp_1.compareAndMakeDeletedProperties(afterProp_1)

        PropMan beforeProp_2 = new PropMan([b:'b', c:'c', d:'d'])
        PropMan afterProp_2 = new PropMan([a:'a', b:'bbb', c:'c'])
        assert beforeProp_2.compareAndMakeInsertedProperties(afterProp_2) == [a:'a']
        assert beforeProp_2.compareAndMakeModifiedProperties(afterProp_2) == [b:'bbb']
        assert beforeProp_2.compareAndMakeDeletedProperties(afterProp_2) == [d:'']

        PropMan beforeProp_3 = new PropMan([b:'b', c:'c', d:'d'])
        PropMan afterProp_3 = new PropMan([a:'a', b:'bbb', c:'c', e:''])
        assert beforeProp_3.compareAndMakeInsertedProperties(afterProp_3) == [a:'a', e:'']
        assert beforeProp_3.compareAndMakeModifiedProperties(afterProp_3) == [b:'bbb']
        assert beforeProp_3.compareAndMakeDeletedProperties(afterProp_3) == [d:'']
    }


}
