package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.controllers.*
import org.springframework.web.bind.annotation.*

data class SubmitAnswerBody(val quizId: String, val playerName: String, val questionNumber: Int, val answerId:Int)


@RestController
class MainController(
        val submitAnswerService: SubmitAnswerService = SubmitAnswerService(),
        val listParticipantsService: ListParticipantsService = ListParticipantsService()
) {

    val existingQuizes = mutableListOf(Quiz("existing-quiz-id"));



    @GetMapping("/hello")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        return "Hello " + name;
    }

    @GetMapping("/create")
    fun create(@RequestParam(value = "quizId") quizId: String): String {
        if (quizExists(quizId)) {
            return "NO"
        }
        existingQuizes.add(Quiz(quizId))
        return "OK"
    }

    @PutMapping("/start")
    fun start(@RequestParam(value = "quizId") quizId: String):String {
        val quiz = getQuizById(quizId)?: return "NO"
        quiz.currentQuestion = 0
        quiz.hasStarted = true
        return "OK"
    }

    @GetMapping("/stage")
    fun stage(@RequestParam(value = "quizId") quizId: String): String {
        // -1 means not yet started
        // 0 to questions.size means question number
        // questions.size means finished
        val quiz = getQuizById(quizId)?:return "NO"
        if (!quiz.hasStarted) return "-1"
        return existingQuizes.first { it.id == quizId }.currentQuestion.toString()
    }

    data class SubmissionBody(val quizId: String, val question: Question)

    @PutMapping("/submit")
    fun submit(@RequestBody body: String): String {
        //println("submittedQuestion: "+ body)

        val parsedBody = Klaxon()
                .parse<SubmissionBody>(body) ?: return "NO"

        println("submitted question for "+ parsedBody.quizId )

        val question = parsedBody.question

        val quiz = existingQuizes.firstOrNull { it.id == parsedBody.quizId } ?: return "NO"
        val player: Player = quiz.players.firstOrNull {it.name ==question.submittedBy} ?: return "NO"

        player.hasSubmittedQuestion = true
        quiz.questions.add(question)

        return "OK"
    }

    @GetMapping("/listParticipants")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String): String {

        val players = getQuizById(quizId)?.players ?: listOf<Player>()
        return players.map { it.name }.toString()
    }

    @GetMapping("/currentQuestion")
    fun currentQuestion(@RequestParam(value = "quizId") quizId: String): String {
        println("currentQuestion for "+ quizId )
        val quiz = existingQuizes.firstOrNull{ it.id == quizId } ?: return "NO"
        val question = quiz.questions.firstOrNull() ?: return "NO"

        //val question = Question("sample", "someone", listOf("option 1", "option 2", "option 3", "option 4") ,0 )

        return Klaxon().toJsonString(question)
    }

    @GetMapping("/listParticipantsWho")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "waiting") waiting: Boolean): String {

        //val playersForQuiz = participants[quizId]?.toList() ?: listOf()
        val quiz = getQuizById(quizId) ?: return "NO"
        var players: List<Player> = listOf()

        if (!waiting) {
            players = listParticipantsService.listDoneParticipants(quiz.players, quiz)
        }else {
            players = listParticipantsService.listPendingParticipants(quiz.players, quiz)
        }

        return players.map{it.name}.toString()


    }

    //var participants = hashMapOf<String, MutableList<Player>>()

    @GetMapping("/join")
    fun join(@RequestParam(value = "quizId") quizId: String,  @RequestParam(value = "name") name: String): String {
        val quiz = getQuizById(quizId) ?: return "NO"

        quiz.players.add(Player(name))
        return "OK"

    }

    fun getQuizById(id: String): Quiz?{
        return existingQuizes.firstOrNull { it.id == id }
    }

    @PutMapping("/submitAnswer")
    fun submitAnswer(@RequestBody body: String): String {
        println("submitAnswer: " + body)
        val parsedBody = Klaxon().parse<SubmitAnswerBody>(body) ?: return "NO"
        val quiz = getQuizById(parsedBody.quizId) ?: return "NO"
        return submitAnswerService.submitAnswer(quiz, parsedBody.questionNumber, parsedBody.answerId, parsedBody.playerName)
    }

    private fun quizExists(quizId: String): Boolean {
       return existingQuizes.any {
           it.id == quizId
       }
    }
}