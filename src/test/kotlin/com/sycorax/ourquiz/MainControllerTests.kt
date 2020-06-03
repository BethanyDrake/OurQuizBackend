package com.sycorax.ourquiz

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
        var controller = MainController()
        val quizId = "id123"
        val playerName = "player1"
        createQuizWithPlayers(controller, quizId, listOf(playerName) )
        val result = submitQuestion(controller, quizId, playerName);

        Assertions.assertEquals("OK", result)
    }

    @Test
    fun submitQuestionToQuizThatDoesNotEquist() {
        var controller = MainController()

        val result = submitQuestion(controller, "id123", "");

        Assertions.assertEquals("NO", result)
    }

    @Test
    fun submitQuestionToQuizThatDoesNotHaveThatPlayer() {
        var controller = MainController()
        createQuizWithPlayers(controller, "quiz1", listOf("player1"))
        createQuizWithPlayers(controller, "quiz2", listOf("player2"))

        val result = submitQuestion(controller, "quiz1", "player2");

        Assertions.assertEquals("NO", result)
    }

    fun submitQuestion(controller: MainController, quizId:String, playerName:String): String{
        val body = "{" +
                "\"quizId\":\"${quizId}\"" +
                "\"playerName\":\"${playerName}\"" +
                "}"

        return controller.submit(body);
    }

    fun createQuizWithPlayers(controller: MainController, quizId: String, players: List<String>) {
        controller.create(quizId)
        players.forEach {controller.join(quizId, it)  }
    }

    @Test
    fun listUsersWhoHaveSubmittedAQuestion() {
        var controller = MainController()
        var quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf("person1", "person2", "person3", "person4"))


        submitQuestion(controller, quizId, "person1");
        submitQuestion(controller, quizId, "person3");


        var expectedPlayersWithQuestion = listOf("person1", "person3")
        var playersWithQuestionResult = controller.listParticipants(quizId, true);

        Assertions.assertEquals(expectedPlayersWithQuestion.toString(), playersWithQuestionResult)
    }


    @Test
    fun listUsersWhoHaveNotSubmittedAQuestion() {
        var controller = MainController()
        var quizId = "a-quiz"
        createQuizWithPlayers(controller, quizId, listOf("person1", "person2", "person3", "person4"))


        submitQuestion(controller, quizId, "person1");
        submitQuestion(controller, quizId, "person3");

        var expectedPlayersWithoutQuestion = listOf("person2", "person4")
        var playersWithoutQuestionResult = controller.listParticipants(quizId, false);

        Assertions.assertEquals(expectedPlayersWithoutQuestion.toString(), playersWithoutQuestionResult)
    }


    @Test
    fun beforeTheQuizHasBeenStarted() {
        var controller = MainController()
        var quizId = "a-quiz"
        controller.create(quizId)

        Assertions.assertEquals("false", controller.hasStarted(quizId))
    }

    @Test
    fun afterTheQuizHasBeenStarted() {
        var controller = MainController()
        var quizId = "a-quiz"
        controller.create(quizId)

        controller.start(quizId)


        Assertions.assertEquals("true", controller.hasStarted(quizId))
    }

}