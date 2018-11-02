package jaemisseo.man

import org.junit.Test

class AnsiManTest {

    @Test
    void mainTest(){
        String text = 'AnsiMan TEST !~ ? %&*$#(^%()[].nz/\\ TEST~~~'
        println AnsiMan.test(text)
        println AnsiMan.testCyan(text)
        println AnsiMan.testYellow(text)
        println AnsiMan.testBlue(text)
        println AnsiMan.testGreen(text)
        println AnsiMan.testPupple(text)
        println AnsiMan.testRed(text)

        println AnsiMan.testCyanBg(text)
        println AnsiMan.testYellowBg(text)
        println AnsiMan.testBlueBg(text)
        println AnsiMan.testGreenBg(text)
        println AnsiMan.testPuppleBg(text)
        println AnsiMan.testRedBg(text)

        println AnsiMan.testRainbowBg(text)
    }

}
