package jaemisseo.man

class AnsiMan {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static final String HIGH_INTENSITY		= "\u001B[1m";
    public static final String LOW_INTENSITY		= "\u001B[2m";



    static String test(String text){
        return ANSI_WHITE_BACKGROUND + ANSI_BLACK + text + ANSI_RESET
    }



    static String reset(String text){
        return text + ANSI_RESET
    }

    static String white(String text){
        return ANSI_BLACK + text + ANSI_RESET
    }

    static String bold(String text){
        return HIGH_INTENSITY + text + ANSI_RESET
    }



    static String testCyan(String text){
        return ANSI_CYAN + text + ANSI_RESET
    }

    static String testYellow(String text){
        return ANSI_YELLOW + text + ANSI_RESET
    }

    static String testGreen(String text){
        return ANSI_GREEN + text + ANSI_RESET
    }

    static String testBlue(String text){
        return ANSI_BLUE + text + ANSI_RESET
    }

    static String testPupple(String text){
        return ANSI_PURPLE + text + ANSI_RESET
    }

    static String testRed(String text){
        return ANSI_RED + text + ANSI_RESET
    }




    static String testCyanBg(String text){
        return ANSI_CYAN_BACKGROUND + ANSI_BLACK + HIGH_INTENSITY + text + ANSI_RESET
    }

    static String testYellowBg(String text){
        return ANSI_YELLOW_BACKGROUND + ANSI_BLACK + HIGH_INTENSITY + text + ANSI_RESET
    }

    static String testGreenBg(String text){
        return ANSI_GREEN_BACKGROUND + ANSI_BLACK + HIGH_INTENSITY + text + ANSI_RESET
    }

    static String testBlueBg(String text){
        return ANSI_BLUE_BACKGROUND + ANSI_BLACK + HIGH_INTENSITY + text + ANSI_RESET
    }

    static String testPuppleBg(String text){
        return ANSI_PURPLE_BACKGROUND + ANSI_BLACK + HIGH_INTENSITY + text + ANSI_RESET
    }

    static String testRedBg(String text){
        return ANSI_RED_BACKGROUND + ANSI_BLACK + HIGH_INTENSITY + text + ANSI_RESET
    }



    static String testRainbowBg(String text){
        String newText = ''
        text.eachWithIndex{ String c, int i ->
            newText += doRainbow(c, i)
        }
        return newText
    }

    static String doRainbow(String c, int i){
        int type = i % 5
        switch (type){
            case 0: return testRedBg(c); break;
            case 1: return testYellowBg(c); break;
            case 2: return testGreenBg(c); break;
            case 3: return testBlueBg(c); break;
            case 4: return testPuppleBg(c); break;
            default: return testCyanBg(c); break;
        }
    }

}
