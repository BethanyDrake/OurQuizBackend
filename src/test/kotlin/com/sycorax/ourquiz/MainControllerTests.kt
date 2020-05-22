package com.sycorax.ourquiz

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
        var result = controller.join("non-existing-quiz-id")
        Assertions.assertEquals("NO", result)
	}

    @Test
	fun joinReturnsOkIfQuizDoesExist() {
        var controller = MainController()
        var result = controller.join("existing-quiz-id")
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
        var result = controller.join(newQuizId)
        Assertions.assertEquals("OK", result)
	}

}