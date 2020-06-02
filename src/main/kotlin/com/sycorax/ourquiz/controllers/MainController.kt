package com.sycorax.ourquiz

import org.springframework.web.bind.annotation.*
import java.io.Console


@RestController
class MainController {

    val existingQuizes = mutableListOf<String>("existing-quiz-id");


    @GetMapping("/hello")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        return "Hello " + name;
    }

    @GetMapping("/create")
    fun create(@RequestParam(value = "quizId") quizId: String): String {
        if (quizExists(quizId)) {
            return "NO"
        }
        existingQuizes.add(quizId)
        return "OK"
    }

    @PutMapping("/submit")
    fun submit(@RequestBody body: String): String {
        println("question submitted")
        println("details: " + body)
        return "OK"
    }



    @GetMapping("/listParticipants")
    fun listParticipants(@RequestParam(value = "quizId") quizId: String): String {
        return participants[quizId].toString()
    }

    var participants = hashMapOf<String, MutableList<String>>()

    @GetMapping("/join")
    fun join(@RequestParam(value = "quizId") quizId: String,  @RequestParam(value = "name") name: String): String {
        if (quizExists(quizId)) {
            if (!participants.containsKey(quizId)) {
                participants[quizId] = mutableListOf<String>()
            }
            participants[quizId]?.add(name)

            return "OK"
        }
        return "NO"
    }

    private fun quizExists(quizId: String): Boolean {
       return existingQuizes.contains(quizId)
    }
}