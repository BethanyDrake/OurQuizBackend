package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.controllers.*
import org.springframework.web.bind.annotation.*

data class SubmitAnswerBody(val quizId: String, val playerName: String, val questionNumber: Int, val answerId: Int)
data class RevealAnswerResponse(val answerText: String, val yourAnswer: String)

@RestController
class MainController(
        val submitAnswerService: SubmitAnswerService = SubmitAnswerService(),
        val listParticipantsService: ListParticipantsService = ListParticipantsService()
) {

    val existingQuizes = mutableListOf(Quiz("existing-quiz-id"));


    fun logErrorAndReturn(request: String, message: String) : String {
        println("failed request: " + request)
        println("result: " + message)
        return message
    }



    @GetMapping("/hello")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        return "Hello " + name;
    }

    @GetMapping("/create")
    fun create(@RequestParam(value = "quizId") quizId: String): String {
        if (quizExists(quizId)) {
            return logErrorAndReturn("create", "NO")
        }
        existingQuizes.add(Quiz(quizId))
        return "OK"
    }

    @PutMapping("/start")
    fun start(@RequestParam(value = "quizId") quizId: String): String {
        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("start", "NO")
        quiz.currentQuestion = 0
        quiz.hasStarted = true
        return "OK"
    }

    @PutMapping("/nextQuestion")
    fun nextQuestion(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "currentQuestion") currentQuestion: String): String {
        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("nextQuestion", "NO - quiz does not exist")
        val parsedCurrentQuestion = currentQuestion.toIntOrNull()
        if (quiz.currentQuestion != parsedCurrentQuestion) return logErrorAndReturn("nextQuestion","NO - current question does not match")

        if (!quiz.questions[quiz.currentQuestion].revealed) return logErrorAndReturn("nextQuestion", "NO - current question not yet revealed")
        quiz.currentQuestion +=1
        return "OK"
    }


    @PutMapping("/revealQuestion")
    fun revealQuestion(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "questionNumber") questionNumber: String): String {
        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("revealQuestion", "NO - quiz does not exist")
        val parsedQuestionNumber = questionNumber.toIntOrNull() ?: return logErrorAndReturn("revealQuestion", "NO - invalid question number: " + questionNumber)

        try {
            RevealQuestionService().revealQuestion(quiz, parsedQuestionNumber)
        } catch (e: Exception) {
            return logErrorAndReturn("revealQuestion","NO - "+ e.message)
        }

        return "OK"
    }

    @GetMapping("/stage")
    fun stage(@RequestParam(value = "quizId") quizId: String): String {
        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("stage", "NO")

        val response = StatusService().getStatus(quiz)


        //return existingQuizes.first { it.id == quizId }.currentQuestion.toString()
        return Klaxon().toJsonString(response)
    }

    data class SubmissionBody(val quizId: String, val question: Question)

    @PutMapping("/submit")
    fun submit(@RequestBody body: String): String {
        //println("submittedQuestion: "+ body)

        val parsedBody = Klaxon()
                .parse<SubmissionBody>(body) ?: return logErrorAndReturn("submit", "NO")

        println("submitted question for " + parsedBody.quizId)

        val question = parsedBody.question

        if (question.correctAnswerId < 0 || question.correctAnswerId > 4) return logErrorAndReturn("submit", "NO - invalid question id: " + question.correctAnswerId)
        if (question.answers.size != 4) return logErrorAndReturn("submit", "NO - invalid number of possible answers: " + question.answers.size)


        val quiz = existingQuizes.firstOrNull { it.id == parsedBody.quizId } ?: return logErrorAndReturn("submit", "NO")
        val player: Player = quiz.players.firstOrNull { it.name == question.submittedBy } ?: return logErrorAndReturn("submit", "NO")

        player.hasSubmittedQuestion = true
        quiz.questions.add(question)

        return "OK"
    }

    @GetMapping("/listParticipants")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String): String {

        val players = getQuizById(quizId)?.players ?: listOf<Player>()
        return players.map { it.name }.toString()
    }

    @GetMapping("/correctAnswer")
    fun revealAnswer(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "questionNumber") questionNumber: String, @RequestParam(value = "playerName") playerName: String, @RequestParam(value = "isHost") isHost: String = "false"): String {
        println("getting correct answer: quizId: " + quizId + " questionNumber: " + questionNumber + " playerName: " + playerName)

        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("correctAnswer", "NO")
        val parsedQuestionNumber = questionNumber.toIntOrNull() ?: return logErrorAndReturn("correctAnswer", "NO")
        val parsedIsHost: Boolean = isHost.toBoolean()

        val question = quiz.questions[parsedQuestionNumber]

        println("getting correct answer id: " + question.correctAnswerId)

        val correctAnswerText = question.answers[question.correctAnswerId]

        val yourAnswer = if (parsedIsHost) {
            ""
        } else {
            val player: Player = quiz.players.firstOrNull { it.name == playerName } ?: return logErrorAndReturn("correctAnswer", "NO - player has not joined this quiz")
            if (player.answers.count() <= parsedQuestionNumber) return logErrorAndReturn("correctAnswer", "NO - question not yet answered by player")
            val playerAnswerId = player.answers[parsedQuestionNumber]
            question.answers[playerAnswerId]
        }

        val response = RevealAnswerResponse(correctAnswerText, yourAnswer)

        return Klaxon().toJsonString(response)
    }

    @GetMapping("/currentQuestion")
    fun currentQuestion(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "expectedQuestionNumber") expectedQuestionNumber: String?): String {
        println("getting currentQuestion for " + quizId)
        val quiz = existingQuizes.firstOrNull { it.id == quizId } ?: return logErrorAndReturn("currentQuestion", "NO")
        println("current question: " + quiz.currentQuestion);
        if(quiz.questions.size == 0) return logErrorAndReturn("currentQuestion", "NO - quiz has no questions")
        val parsedExpectedQuestionNumber = expectedQuestionNumber?.toIntOrNull()
        if (parsedExpectedQuestionNumber != null && parsedExpectedQuestionNumber != quiz.currentQuestion ) {
            return logErrorAndReturn("currentQuestion", "NO - expected question number is "+ expectedQuestionNumber + ", but current question is " + quiz.currentQuestion)
        }
        if (quiz.currentQuestion >= quiz.questions.size) return logErrorAndReturn("currentQuestion", "NO")
        if (quiz.currentQuestion < 0) return logErrorAndReturn("currentQuestion", "NO")

        val question = quiz.questions[quiz.currentQuestion]?: return logErrorAndReturn("currentQuestion", "NO")

        //val question = Question("sample", "someone", listOf("option 1", "option 2", "option 3", "option 4") ,0 )
        println("current question: " + question)

        return Klaxon().toJsonString(question)
    }

    @GetMapping("/listParticipantsWho")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "waiting") waiting: Boolean): String {

        //val playersForQuiz = participants[quizId]?.toList() ?: listOf()
        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("listParticipantsWho", "NO")
        var players: List<Player> = listOf()

        if (!waiting) {
            players = listParticipantsService.listDoneParticipants(quiz.players, quiz)
        } else {
            players = listParticipantsService.listPendingParticipants(quiz.players, quiz)
        }

        return players.map { it.name }.toString()


    }

    //var participants = hashMapOf<String, MutableList<Player>>()

    @GetMapping("/join")
    fun join(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "name") name: String): String {
        val quiz = getQuizById(quizId) ?: return logErrorAndReturn("join", "NO")

        quiz.players.add(Player(name))
        return "OK"

    }

    fun getQuizById(id: String): Quiz? {
        return existingQuizes.firstOrNull { it.id == id }
    }

    @PutMapping("/submitAnswer")
    fun submitAnswer(@RequestBody body: String): String {
        println("submitAnswer: " + body)
        val parsedBody = Klaxon().parse<SubmitAnswerBody>(body) ?: return logErrorAndReturn("submitAnswer", "NO")
        val quiz = getQuizById(parsedBody.quizId) ?: return logErrorAndReturn("submitAnswer", "NO")
        return submitAnswerService.submitAnswer(quiz, parsedBody.questionNumber, parsedBody.answerId, parsedBody.playerName)
    }

    private fun quizExists(quizId: String): Boolean {
        return existingQuizes.any {
            it.id == quizId
        }
    }
}