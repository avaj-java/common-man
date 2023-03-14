package jaemisseo.man

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

class VariableManTest_basic_variable {

    File testDir
    File testFile

    VariableMan varman
    Map<String, Object> variableMap

    @Before
    void beforeTest(){
        // VariableMan 생성
        varman = new VariableMan('EUC-KR')
//        .setModeDebug(true)

        //TODO: 조금 위험할 수도?
        varman.putVariableClosures([
                "EXEC": { VariableMan.OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String command = VariableMan.parseMember(it.members[0], vsMap, vcMap)
                        String result
                        File dir = new File(".")
                        try{
                            result = command.execute([], dir).text.trim()
                        }catch(e){
//                            logger.error("Error ocurred during executing command - [$command]", e)
                        }

                        it.substitutes = result
//                        it.length = it.substitutes.length()
                        it.length = it.substitutes.getBytes().length
                    }
                }
        ])

    }

    @After
    void after(){
//        testFile.delete()
    }

    boolean checkTestFile(){
        return testDir.exists() && testFile.exists()
    }





    @Test
    void simpleVariable() {
        // Test empty string
        String result = varman.parse('hello ${exec(git describe --tags)} 2233^^')
        assert result != null
    }



}