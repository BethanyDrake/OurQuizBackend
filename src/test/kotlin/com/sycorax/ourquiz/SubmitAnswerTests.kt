package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.controllers.Player
import com.sycorax.ourquiz.controllers.Question
import com.sycorax.ourquiz.controllers.Quiz
import com.sycorax.ourquiz.controllers.SubmitAnswerService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubmitAnswerTests {

    @Test
    fun `when I submit an answer-- to the current question -- it says ok`() {

        val service = SubmitAnswerService()
        val questions = mutableListOf(Question("id", "name"))
        val quiz = Quiz("id", true, 0, questions)


        val players = listOf(Player("player1"))

        val result = service.submitAnswer(quiz,players, 0, 0, "player1" )

        assertEquals("OK", result)
    }

    @Test
    fun `when I submit a valid answer -- it sets my last answerd question -- to that question`() {

        val service = SubmitAnswerService()
        val questions = mutableListOf(Question("id", "name"))
        val quiz = Quiz("id",true, 1, questions)


        val player = Player("player1", lastAnsweredQuestion = 0)
        val players = listOf(player)

        service.submitAnswer(quiz,players, 1, 0, "player1" )

        assertEquals(1, player.lastAnsweredQuestion)
    }

    @Test
    fun `when I submit an answer-- but it's not for the current question -- it says NO`() {

        val service = SubmitAnswerService()
        val questions = mutableListOf(Question("id", "name"))
        val quiz = Quiz("id", true, 0, questions)

        val players = listOf(Player("player1"))

        val result = service.submitAnswer(quiz,players, 6, 0, "player1" )

        assertEquals("NO", result)
    }

    @Test
    fun `when I submit an answer -- to a quiz I haven't joined -- it says NO`() {

        val service = SubmitAnswerService()
        val questions = mutableListOf(Question("id", "name"))
        val quiz = Quiz("id", true, 0, questions)

        val players = listOf(Player("player1"))

        val result = service.submitAnswer(quiz,players, 0, 0, "other player")

        assertEquals("NO", result)
    }


}