package com.sycorax.ourquiz

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/join")
    fun join(@RequestParam(value = "quizId") quizId: String): String {
        if (quizExists(quizId)) {
            return "OK"
        }
        return "NO"
    }

    private fun quizExists(@RequestParam(value = "quizId") quizId: String): Boolean {
       return existingQuizes.contains(quizId)
    }
}