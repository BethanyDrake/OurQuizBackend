package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import org.springframework.web.bind.annotation.*

class Player(val name:String, var hasSubmittedQuestion: Boolean = false)
class Quiz(val id: String, var hasStarted: Boolean = false, val questions:MutableList<Question> = mutableListOf<Question>())
data class Question(val questionText:String, val submittedBy: String)

@RestController
class MainController {

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
        existingQuizes.first { it.id == quizId }.hasStarted = true
        return "OK"
    }

    @GetMapping("/hasStarted")
    fun hasStarted(@RequestParam(value = "quizId") quizId: String): String {
        return existingQuizes.first { it.id == quizId }.hasStarted.toString()
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
        return Klaxon().toJsonString(question)
    }

    @GetMapping("/listParticipantsWho")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String, @RequestParam(value = "hasSubmittedQuestion") hasSubmittedQuestion: Boolean): String {
        return participants[quizId]?.filter { it.hasSubmittedQuestion == hasSubmittedQuestion }?.map { it.name }.toString()
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

    private fun quizExists(quizId: String): Boolean {
       return existingQuizes.any {
           it.id == quizId
       }
    }
}