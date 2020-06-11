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
        val quiz = getQuizById(quizId)
        quiz.currentQuestion = 0
        quiz.hasStarted = true
        return "OK"
    }

    @GetMapping("/stage")
    fun stage(@RequestParam(value = "quizId") quizId: String): String {
        // -1 means not yet started
        // 0 to questions.size means question number
        // questions.size means finished
        val quiz = getQuizById(quizId)
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
        val quizParticipants = participants[parsedBody.quizId] ?: return "NO";
        val quiz = existingQuizes.firstOrNull { it.id == parsedBody.quizId } ?: return "NO"
        val player: Player = quizParticipants.firstOrNull {it.name ==question.submittedBy} ?: return "NO"

        player.hasSubmittedQuestion = true
        quiz.questions.add(question)

        return "OK"
    }

    @GetMapping("/listParticipants")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String): String {
        return participants[quizId]?.map { it.name }.toString()
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

        val playersForQuiz = participants[quizId]?.toList() ?: listOf()
        val quiz = getQuizById(quizId)
        var players: List<Player> = listOf()

        if (!waiting) {
            players = listParticipantsService.listDoneParticipants(playersForQuiz, quiz)
        }else {
            players = listParticipantsService.listPendingParticipants(playersForQuiz, quiz)
        }

        return players.map{it.name}.toString()


    }

    var participants = hashMapOf<String, MutableList<Player>>()

    @GetMapping("/join")
    fun join(@RequestParam(value = "quizId") quizId: String,  @RequestParam(value = "name") name: String): String {
        if (quizExists(quizId)) {
            if (!participants.containsKey(quizId)) {
                participants[quizId] = mutableListOf<Player>()
            }
            participants[quizId]?.add(Player(name))

            return "OK"
        }
        return "NO"
    }

    fun getQuizById(id: String): Quiz{
        return existingQuizes.first { it.id == id }
    }

    @PutMapping("/submitAnswer")
    fun submitAnswer(@RequestBody body: String): String {
        val parsedBody = Klaxon().parse<SubmitAnswerBody>(body)
        if (parsedBody == null) return "NO"


        return submitAnswerService.submitAnswer(getQuizById(parsedBody.quizId), participants[parsedBody.quizId]!!.toList(), parsedBody.questionNumber, parsedBody.answerId, "")
    }

    private fun quizExists(quizId: String): Boolean {
       return existingQuizes.any {
           it.id == quizId
       }
    }
}