package com.sycorax.ourquiz.controllers


data class Player(
        val name:String,
        var hasSubmittedQuestion: Boolean = false,
        var lastAnsweredQuestion: Int = -1)

data class Quiz(
        val id: String,
        var hasStarted:Boolean = false,
        var currentQuestion: Int = -1,
        val players: MutableList<Player> = mutableListOf(),
        val questions:MutableList<Question> = mutableListOf<Question>())

data class Question(
        val questionText:String,
        val submittedBy: String,
        val answers: List<String> = listOf(),
        val correctQuestionId: Int = 0,
        var revealed: Boolean = false)



