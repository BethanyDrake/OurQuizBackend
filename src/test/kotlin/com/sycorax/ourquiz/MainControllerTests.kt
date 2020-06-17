package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.controllers.*
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions;
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class MainControllerTests {

	@Test
	fun helloReturnsSomething() {
        var controller = MainController()
        var result = controller.greeting("a name")
        Assertions.assertNotNull(result)
	}

    @Test
	fun joinReturnsNoIfQuizDoesNotExist() {
        var controller = MainController()
        var result = controller.join("non-existing-quiz-id", "")
        Assertions.assertEquals("NO", result)
	}

    @Test
	fun joinReturnsOkIfQuizDoesExist() {
        var controller = MainController()
        var result = controller.join("existing-quiz-id", "")
        Assertions.assertEquals("OK", result)
	}

    @Test
    fun createReturnsOkIfQuizDoesNotExist() {
        var controller = MainController()
        var result = controller.create("non-existing-quiz-id")
        Assertions.assertEquals("OK", result)
	}

    @Test
    fun createReturnsNoIfQuizDoesExist() {
        var controller = MainController()
        var result = controller.create("existing-quiz-id")
        Assertions.assertEquals("NO", result)
	}

     @Test
    fun canJoinAQuizAfterCreatingId() {
        var controller = MainController()
        var newQuizId = "new-quiz-id"
        controller.create(newQuizId)
        var result = controller.join(newQuizId, "")
        Assertions.assertEquals("OK", result)
	}

    @Test
    fun listAllJoinedParticipants() {
        var controller = MainController()
        var newQuizId = "a-quiz"
        controller.create(newQuizId)
        controller.join(newQuizId, "person1")
        controller.join(newQuizId, "person2")
        var resultingList = controller.listParticipants(newQuizId)
        var expectedResult = "[person1, person2]"
        Assertions.assertEquals(expectedResult, resultingList)
	}

    @Test
    fun submitAValidQuestion() {
        val controller = MainController()
        val quizId = "id123"
        val playerName = "player1"
        createQuizWithPlayers(controller, quizId, listOf(playerName) )
        val result = submitQuestion(controller, quizId, playerName);

        Assertions.assertEquals("OK", result)
    }

    @Test
    fun submitQuestionToQuizThatDoesNotEquist() {
        val controller = MainController()

        val result = submitQuestion(controller, "id123", "");

        Assertions.assertEquals("NO", result)
    }

    @Test
    fun submitQuestionToQuizThatDoesNotHaveThatPlayer() {
        val controller = MainController()
        createQuizWithPlayers(controller, "quiz1", listOf("player1"))
        createQuizWithPlayers(controller, "quiz2", listOf("player2"))

        val result = submitQuestion(controller, "quiz1", "player2");

        Assertions.assertEquals("NO", result)
    }

    fun submitQuestion(controller: MainController, quizId:String, playerName:String): String{
        return submitQuestion(controller, quizId, Question("", playerName))
    }

    fun submitQuestion(controller: MainController, quizId:String, question:Question): String{
        val body = "{" +
                "\"quizId\":\"${quizId}\"" +
                "\"question\":" + Klaxon().toJsonString(question) +
                "}"

        return controller.submit(body);
    }

    fun createQuizWithPlayers(controller: MainController, quizId: String, players: List<String>) {
        controller.create(quizId)
        players.forEach {controller.join(quizId, it)  }
    }

    @Test
    fun `listParticipantsWho --when waiting=true -- delegates to service`() {
        val mService = mockk<ListParticipantsService>(relaxed = true)
        val controller = MainController(listParticipantsService = mService)
        val quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf("a"))

        controller.listParticipants(quizId, true)

        verify { mService.listPendingParticipants(any(), any())  }

    }

    @Test
    fun `listParticipantsWho --when waiting=false -- delegates to service`() {
        val mService = mockk<ListParticipantsService>(relaxed = true)
        val controller = MainController(listParticipantsService = mService)
        val quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf("a"))

        controller.listParticipants(quizId, false)
        verify { mService.listDoneParticipants(any(), any())  }

    }

    @Test
    fun beforeTheQuizHasBeenStarted() {
        val controller = MainController()
        val quizId = "a-quiz"
        controller.create(quizId)

        val expectedStatus = StatusResponse(-1, false)
        val response = controller.stage(quizId)
        val parsedResponse = Klaxon().parse<StatusResponse>(response)

        Assertions.assertEquals(expectedStatus, parsedResponse)
    }

    @Test
    fun `next question -- progresses the quiz to the next question`() {
        val controller = MainController()
        val quizId = "a-quiz"
        val quiz = Quiz(quizId,
                hasStarted = true,
                currentQuestion = 0,
                questions = mutableListOf(Question("q1", "p1"), Question("q2", "p2")))
        controller.existingQuizes.add(quiz)

        controller.nextQuestion(quizId, "0")

        Assertions.assertEquals(1, quiz.currentQuestion)
    }

    @Test
    fun `next question -- if current question is incorrect -- says no and does not update `() {
        val controller = MainController()
        val quizId = "a-quiz"
        val quiz = Quiz(quizId,
                hasStarted = true,
                currentQuestion = 0,
                questions = mutableListOf(Question("q1", "p1"), Question("q2", "p2")))
        controller.existingQuizes.add(quiz)

        val response = controller.nextQuestion(quizId, "7")
        Assertions.assertEquals("NO - current question does not match", response)
        Assertions.assertEquals(0, quiz.currentQuestion)
    }

    @Test
    fun `stage - afterTheQuizHasBeenStarted`() {
        val controller = MainController()
        val quizId = "a-quiz"

        createQuizWithPlayers(controller, quizId, listOf("p1"))
        submitQuestion(controller, quizId, "p1")

        controller.start(quizId)

        val expectedStatus = StatusResponse(0, false)
        val response = controller.stage(quizId)
        val parsedResponse = Klaxon().parse<StatusResponse>(response)

        Assertions.assertEquals(expectedStatus, parsedResponse)
    }

    @Test
    fun `stage - after the first question has been revealed`() {
        val controller = MainController()
        val quizId = "a-quiz"

        createQuizWithPlayers(controller, quizId, listOf("p1"))
        submitQuestion(controller, quizId, "p1")

        controller.start(quizId)
        controller.revealQuestion(quizId, "0")

        val expectedStatus = StatusResponse(0, true)
        val response = controller.stage(quizId)
        val parsedResponse = Klaxon().parse<StatusResponse>(response)

        Assertions.assertEquals(expectedStatus, parsedResponse)
    }

    @Test
    fun currentQuestionReturnsNoIfQuizHasNoQuestions() {
        val controller = MainController()
        val quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf())

        Assertions.assertEquals("NO", controller.currentQuestion(quizId) )
    }

    @Test
    fun firstQuestionReturnsFirstSubmittedQuestionWithPossibleAnswers() {
        val controller = MainController()
        val quizId = "a-quiz"
        val player = "player1"
        createQuizWithPlayers(controller, quizId, listOf(player))
        val questionText = "Is this question 1?"
        val answers = listOf("Option 1", "Option 2", "Option 3", "Option 4")
        val correctQuestionId = 2
        val question = Question(questionText, player, answers, correctQuestionId)
        submitQuestion(controller, quizId, question)
        val result = Klaxon().parse<Question>(controller.currentQuestion(quizId))
        Assertions.assertEquals(question, result)
    }

    @Test
    fun questionEqualsComparesAnswers() {
        val q1 = Question("blah", "blah", listOf("a", "b", "c"), 0)
        val q2 = Question("blah", "blah", listOf("a", "c", "b"), 0)
        Assertions.assertNotEquals(q1,q2)
    }

    @Test
    fun `submitAnswer -- delegated service mutates player`() {
        val controller = MainController()
        controller.existingQuizes.add(Quiz(
                "a-quiz",
                true,
                0,
                players = mutableListOf(Player("p1", true, -1)) ,
                questions = mutableListOf(Question("q1", "p2")))
        )


        val submitAnswerBody = SubmitAnswerBody("a-quiz", "p1", 0, 2)

        val result = controller.submitAnswer(Klaxon().toJsonString(submitAnswerBody))
        assertEquals("OK", result)
        val quiz = controller.existingQuizes.last()
        val player = quiz.players[0]
        assertEquals(0, player.lastAnsweredQuestion)



    }

    @Test
    fun `reveal answer -- gets correct answer text for that question -- and the answer the player provided`(){
        val controller = MainController()
        val quizId = "a-quiz"

        createQuizWithPlayers(controller, quizId, listOf("p1"))
        submitQuestion(controller, quizId, Question("Question text?", "p1",listOf("MY PICK", "no", "CORRECT ANSWER", "no"), 2 ))
        controller.start(quizId)

        val submitAnswerBody = SubmitAnswerBody(quizId, "p1", 0, 0)
        controller.submitAnswer(Klaxon().toJsonString(submitAnswerBody))

        val result = controller.revealAnswer(quizId, "0", "p1")

        try {
            val parsedResult = Klaxon().parse<RevealAnswerResponse>(result);
            assertEquals("CORRECT ANSWER", parsedResult?.answerText)
            assertEquals("MY PICK", parsedResult?.yourAnswer)
        } catch(e:Exception) {
            kotlin.test.fail(result)
        }

    }

    @Test
    fun `submitAnswer -- delegates`() {
        //delegates to SubmitAnswer with the quiz, its participants, and the parsed submission body

        val mSubmitAnswerService = mockk<SubmitAnswerService>()
        val quizCaptureSlot = CapturingSlot<Quiz>()
        //val playersCaptureSlot = CapturingSlot<List<Player>>()
        every { mSubmitAnswerService.submitAnswer(capture(quizCaptureSlot), any(), any(), any()) } returns "mocked result"



        val controller = MainController(mSubmitAnswerService)
        val quizId = "a-quiz"
        val player = "player1"


        createQuizWithPlayers(controller, quizId, listOf(player))


        val submitAnswerBody = SubmitAnswerBody(quizId, player, 7, 2)

        val result = controller.submitAnswer(Klaxon().toJsonString(submitAnswerBody))

        assertEquals("mocked result", result)


        assertTrue { quizCaptureSlot.isCaptured }
        assertEquals(quizId, quizCaptureSlot.captured.id)

        //assertTrue { playersCaptureSlot.isCaptured }
        assertEquals(1, quizCaptureSlot.captured.players.size)
        assertEquals(player, quizCaptureSlot.captured.players[0].name)

        verify { mSubmitAnswerService.submitAnswer(any(), 7, 2, player) }
    }

}