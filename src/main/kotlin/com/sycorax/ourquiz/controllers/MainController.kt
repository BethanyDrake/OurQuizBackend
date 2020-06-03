package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import org.springframework.web.bind.annotation.*

class Player(val name:String, var hasSubmittedQuestion: Boolean = false)
class Quiz(val id: String, var hasStarted: Boolean = false)

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

    data class SubmissionBody(val quizId: String, val playerName: String)

    @PutMapping("/submit")
    fun submit(@RequestBody body: String): String {
        //println("submittedQuestion: "+ body)

        val parsedBody = Klaxon()
                .parse<SubmissionBody>(body)

        val quizParticipants = participants[parsedBody?.quizId];
        val player:Player? = quizParticipants?.filter {it.name ==(parsedBody?.playerName)}?.firstOrNull()

        if (player == null) {
           return "NO"
        }
        player?.hasSubmittedQuestion = true

        return "OK"
    }



    @GetMapping("/listParticipants")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String): String {
        return participants[quizId]?.map { it.name }.toString()
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