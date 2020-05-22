package com.sycorax.ourquiz

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
@RestController
class MainController {
    @GetMapping("/hello")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        return "Hello " + name;
    }

    @GetMapping("/join")
    fun join(@RequestParam(value = "quizId") quizId: String): String {
        if (quizExists(quizId)) {
            return "OK"
        }
        return "NO"
    }

    private fun quizExists(@RequestParam(value = "quizId") quizId: String): Boolean {
       return quizId == "existing-quiz-id"
    }
}