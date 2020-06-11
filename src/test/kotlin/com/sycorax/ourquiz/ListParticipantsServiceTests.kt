package com.sycorax.ourquiz

import com.sycorax.ourquiz.controllers.ListParticipantsService
import com.sycorax.ourquiz.controllers.Player
import com.sycorax.ourquiz.controllers.Quiz
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ListParticipantsServiceTests {

    @Test
    fun `when the quiz hasn't started -- list pending participants -- returns the participants who have not yet submitted a question`(){
        val service = ListParticipantsService()

        val players = listOf(
                Player("a"),
                Player("b", hasSubmittedQuestion = true),
                Player("c"),
                Player("d", hasSubmittedQuestion = true))
        val quiz = Quiz("id", hasStarted = false)
        val result = service.listPendingParticipants(players, quiz)

        assertEquals(listOf("a", "c"), result.map{it.name})
    }

    @Test
    fun `when the quiz hasn't started -- list done participants -- returns the participants who have submitted a question`(){

        val service = ListParticipantsService()

        val players = listOf(
                Player("a"),
                Player("b", hasSubmittedQuestion = true),
                Player("c"),
                Player("d", hasSubmittedQuestion = true))
        val quiz = Quiz("id", hasStarted = false)
        val result = service.listDoneParticipants(players, quiz)

        assertEquals(listOf("b", "d"), result.map{it.name})
    }

    @Test
    fun `when the quiz has started -- list pending participants -- returns the participants who have not yet answered the current question`(){

        val service = ListParticipantsService()

        val players = listOf(
                Player("a", hasSubmittedQuestion = true, lastAnsweredQuestion = -1),
                Player("b", hasSubmittedQuestion = true, lastAnsweredQuestion = 5),
                Player("c", hasSubmittedQuestion = true, lastAnsweredQuestion = 3),
                Player("d", hasSubmittedQuestion = true, lastAnsweredQuestion = 7))
        val quiz = Quiz("id", hasStarted = true, currentQuestion = 5)
        val result = service.listPendingParticipants(players, quiz)

        assertEquals(listOf("a", "c"), result.map{it.name})
    }


}