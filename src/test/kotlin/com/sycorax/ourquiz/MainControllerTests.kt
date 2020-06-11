package com.sycorax.ourquiz

import com.beust.klaxon.Klaxon
import com.sun.org.apache.xpath.internal.operations.Bool
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions;

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
    fun listUsersWhoHaveSubmittedAQuestion() {
        val controller = MainController()
        val quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf("person1", "person2", "person3", "person4"))

        submitQuestion(controller, quizId, "person1");
        submitQuestion(controller, quizId, "person3");

        val expectedPlayersWithQuestion = listOf("person1", "person3")
        val playersWithQuestionResult = controller.listParticipants(quizId, false);

        Assertions.assertEquals(expectedPlayersWithQuestion.toString(), playersWithQuestionResult)
    }


    @Test
    fun listUsersWhoHaveNotSubmittedAQuestion() {
        val controller = MainController()
        val quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf("person1", "person2", "person3", "person4"))


        submitQuestion(controller, quizId, "person1");
        submitQuestion(controller, quizId, "person3");

        val expectedPlayersWithoutQuestion = listOf("person2", "person4")
        val playersWithoutQuestionResult = controller.listParticipants(quizId, true);

        Assertions.assertEquals(expectedPlayersWithoutQuestion.toString(), playersWithoutQuestionResult)
    }


    @Test
    fun beforeTheQuizHasBeenStarted() {
        val controller = MainController()
        val quizId = "a-quiz"
        controller.create(quizId)

        Assertions.assertEquals("-1", controller.stage(quizId))
    }

    @Test
    fun afterTheQuizHasBeenStarted() {
        val controller = MainController()
        val quizId = "a-quiz"

        controller.create(quizId)
        controller.start(quizId)

        Assertions.assertEquals("0", controller.stage(quizId))
    }

    @Test
    fun currentQuestionReturnsNoIfQuizHasNoQuestions() {
        val controller = MainController()
        val quizId = "a-quiz"

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

}