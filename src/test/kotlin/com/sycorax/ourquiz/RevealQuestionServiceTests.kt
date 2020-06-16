package com.sycorax.ourquiz

import com.sycorax.ourquiz.controllers.Question
import com.sycorax.ourquiz.controllers.Quiz
import com.sycorax.ourquiz.controllers.RevealQuestionService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RevealQuestionServiceTests {
    @Test
    fun `mutates revealed to true`() {
        val quiz = Quiz(
                "a-quiz",
                questions = mutableListOf(
                        Question("q1", "p1", revealed = false)
                ),
                currentQuestion = 0,
                hasStarted = true
        );


        val service = RevealQuestionService();
        service.revealQuestion(quiz, 0)
        assertTrue(quiz.questions[0].revealed)

    }


    @Test
    fun `throws error if it's not the current question`() {

        val quiz = Quiz(
                "a-quiz",
                questions = mutableListOf(
                        Question("q1", "p1"),
                        Question("q2", "p1")
                ),
                currentQuestion = 0,
                hasStarted = true
        );


        val service = RevealQuestionService();
        try{
            service.revealQuestion(quiz, 1)
            fail<Any>("should have thrown exception")
        } catch (e: Exception){
            assertNotNull(e)
        }



    }

}