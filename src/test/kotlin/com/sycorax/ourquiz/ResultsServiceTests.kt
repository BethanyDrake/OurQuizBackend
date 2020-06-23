package com.sycorax.ourquiz

import com.sycorax.ourquiz.controllers.Player
import com.sycorax.ourquiz.controllers.Question
import com.sycorax.ourquiz.controllers.Quiz
import com.sycorax.ourquiz.controllers.ResultsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.util.Assert
import kotlin.test.assertEquals

class ResultsServiceTests {
    @Test
    fun `throws exception if quiz hasnt started`(){
        val resultService = ResultsService()
        val quiz = Quiz("quizId", hasStarted=false)
        try {
            resultService.getResults(quiz)
            fail("should throw exception")
        }catch(e: Exception) {

        }
    }

    @Test
    fun `returns empty results if quiz had no players or questions`(){
        val resultService = ResultsService()
        val quiz = Quiz("quizId", hasStarted=true)

        val expectedResult = ResultsService.QuizResult("quizId", 0, listOf())

       assertEquals(expectedResult,resultService.getResults(quiz))

    }

    @Test
    fun `returns score of zero if player has not answered any questions or got them all wrong`(){
        val resultService = ResultsService()
        val quiz = Quiz("quizId", hasStarted=true, questions = mutableListOf(
                Question("q1", "p1", correctAnswerId = 0),
                Question("q2", "p2", correctAnswerId = 1)
        ),
                players = mutableListOf(
                        Player("p1", answers = mutableListOf(-1, -1)),
                        Player("p2", answers = mutableListOf(2, 3))
                )
        )

        val expectedResult = ResultsService.QuizResult("quizId", 2, listOf(
                ResultsService.PlayerScore("p1", 0),
                ResultsService.PlayerScore("p2", 0)))

        assertEquals(expectedResult,resultService.getResults(quiz))
    }

    @Test
    fun `returns number of correct answers`(){
        val resultService = ResultsService()
        val quiz = Quiz("quizId", hasStarted=true, questions = mutableListOf(
                Question("q1", "p1", correctAnswerId = 0),
                Question("q2", "p2", correctAnswerId = 1)
        ),
                players = mutableListOf(
                        Player("p1", answers = mutableListOf(0, 1)),
                        Player("p2", answers = mutableListOf(2, 1))
                )
        )

        val expectedResult = ResultsService.QuizResult("quizId", 2, listOf(
                ResultsService.PlayerScore("p1", 2),
                ResultsService.PlayerScore("p2", 1)))

        assertEquals(expectedResult,resultService.getResults(quiz))
    }
}