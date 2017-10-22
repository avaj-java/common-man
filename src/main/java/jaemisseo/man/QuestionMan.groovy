package jaemisseo.man

import jaemisseo.man.bean.QuestionSetup
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-03-18.
 */
class QuestionMan {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int QUESTION_TYPE_FREE = 1
    public static final int QUESTION_TYPE_YN = 2
    public static final int QUESTION_TYPE_CHOICE = 3

    public static final String Y = 'Y'
    public static final String N = 'N'

    QuestionSetup gOpt = new QuestionSetup()
    QuestionSetup nowOpt
    String yourAnswer
    List<String> validAnswerList = []
    List<String> invalidAnswerList = []

    Closure beforeQuestionClosure
    Closure afterQuestionClosure

    QuestionMan(){
    }

    QuestionMan(QuestionSetup opt){
        set(opt)
    }

    QuestionMan(List<String> validAnswerList){
        setValidAnswer(validAnswerList)
    }

    QuestionMan(List<String> validAnswerList, List<String> invalidAnswerList){
        setValidAnswer(validAnswerList)
        setInvalidAnswer(invalidAnswerList)
    }

    QuestionMan(QuestionSetup opt, List<String> validAnswerList, List<String> invalidAnswerList){
        set(opt)
        setValidAnswer(validAnswerList)
        setInvalidAnswer(invalidAnswerList)
    }

    /**
     * SET
     */
    QuestionMan set(QuestionSetup opt){
        gOpt.merge(opt)
        nowOpt = gOpt
        return this
    }

    /**
     * VALIDLIST
     */
    QuestionMan setValidAnswer(String validAnswer){
        this.validAnswerList = [validAnswer]
        return this
    }

    QuestionMan setValidAnswer(List<String> validAnswerList){
        this.validAnswerList = validAnswerList
        return this
    }

    QuestionMan addValidAnswer(String validAnswer){
        this.validAnswerList << validAnswer
        return this
    }

    QuestionMan addValidAnswer(List<String> validAnswerList){
        this.validAnswerList.addAll(validAnswerList)
        return this
    }

    /**
     * INVALIDLIST
     */
    QuestionMan setInvalidAnswer(String invalidAnswer){
        this.invalidAnswerList = [invalidAnswer]
        return this
    }

    QuestionMan setInvalidAnswer(List<String> invalidAnswerList){
        this.invalidAnswerList = invalidAnswerList
        return this
    }

    QuestionMan addInvalidAnswer(String invalidAnswer){
        this.invalidAnswerList << invalidAnswer
        return this
    }

    QuestionMan addInvalidAnswer(List<String> invalidAnswerList){
        this.invalidAnswerList.addAll(invalidAnswerList)
        return this
    }

    /**
     * Closure
     */
    QuestionMan setBeforeQuestionClosure(Closure closure){
        beforeQuestionClosure = closure
        return this
    }

    QuestionMan setAfterQuestionClosure(Closure closure){
        afterQuestionClosure = closure
        return this
    }

    /**
     * QUESTION
     */
    String question(){
        return question(gOpt, null)
    }

    String question(QuestionSetup lOpt){
        return question(lOpt, null)
    }

    String question(Closure validClosure){
        return question(gOpt, validClosure)
    }

    String question(int questionType){
        return question(gOpt, questionType)
    }

    String question(QuestionSetup lOpt, int questionType){
        return question(lOpt, getValidAnswerClosure(questionType))
    }

    String question(QuestionSetup lOpt, Closure validAnswerClosure){
        nowOpt = gOpt.clone().merge(lOpt)
        String yourAnswer = nowOpt.answer
        String recommandAnswer = nowOpt.recommandAnswer ?: ''

        //Ask Question
        int repeatLimit = nowOpt.repeatLimit
        int repeatCount = 0
        boolean isOk = false

        while (!isOk){
            if (repeatCount > repeatLimit)
                throw new Exception('So Many Not Good Answer. Please Correct Answer :) ')

            //Print Question
            if (beforeQuestionClosure)
                beforeQuestionClosure()
            logger.info( genQuestion(nowOpt) )
            if (afterQuestionClosure)
                afterQuestionClosure()

            //Print Selection
            genSelection(nowOpt).each{ logger.info it }

            //Wait Your Input
            print "> "
            if ( yourAnswer || (nowOpt.modeLoadResponseFile && nowOpt.modeOnlyInteractive) ){
            }else{
                yourAnswer = new Scanner(System.in).nextLine()
            }

            //If You Just Enter, Input Recommand Answer
            if (!yourAnswer)
                yourAnswer = recommandAnswer

            //Valid Answer ?
            isOk = (validAnswerClosure && validAnswerClosure(yourAnswer, nowOpt)) || (!validAnswerClosure)
            isOk = (validAnswerList.contains(yourAnswer) || isOk) && !invalidAnswerList.contains(yourAnswer)

            //Check Answer
            logger.info "=> ${yourAnswer}\n"
            if (!isOk){
                logger.error "Not Good Answer. Please Answer Angain"
                yourAnswer = ""
            }

            repeatCount++
        }

        this.yourAnswer = yourAnswer
        return yourAnswer
    }

    List<String> genQuestionAndSelection(QuestionSetup nowOpt){
        List<String> lineList = []
        //Gen Question
        lineList << genQuestion(nowOpt)
        //Gen Selection
        lineList.addAll(genSelection(nowOpt))
        return lineList
    }

    String genQuestion(QuestionSetup nowOpt){
        String question = nowOpt.question
        String recommandAnswer = nowOpt.recommandAnswer ?: ''
        String questionAndRecommandAnswer = "${question} [${recommandAnswer}]? "
        return questionAndRecommandAnswer
    }

    List<String> genSelection(QuestionSetup nowOpt){
        List<String> lineList = []
        Map descriptionMap = nowOpt.descriptionMap
        if (descriptionMap){
            descriptionMap.sort{ a,b -> a.key <=> b.key }.each{
                lineList << "  ${it.key}) ${it.value}"
            }
        }
        return lineList
    }

    String getValue(){
        return getValue(yourAnswer)
    }

    String getValue(String answer){
        Map valMap = nowOpt.valueMap
        return (valMap && valMap[answer]) ? valMap[answer] : answer
    }

    Closure getValidAnswerClosure(int questionType){
        Closure resultClosure
        switch (questionType){
            case QUESTION_TYPE_FREE:
                resultClosure = null
                break
            case QUESTION_TYPE_YN:
                resultClosure = { String answer, QuestionSetup opt ->
                    answer = answer ? answer.toUpperCase() : ''
                    return (answer.equals(Y) || answer.equals(N))
                }
                break
            case QUESTION_TYPE_CHOICE:
                resultClosure = { String answer, QuestionSetup opt ->
                    return answer && ( (opt.valueMap && opt.valueMap.containsKey(answer)) || (opt.descriptionMap && opt.descriptionMap.containsKey(answer)) )
                }
                break
            default:
                break
        }
        return resultClosure
    }


}
