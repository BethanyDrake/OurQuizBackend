package com.sycorax.ourquiz.controllers


data class Player(
        val name:String,
        var hasSubmittedQuestion: Boolean = false,
        var lastAnsweredQuestion: Int = -1)

data class Quiz(
        val id: String,
        var stage: Int = -1,
        val questions:MutableList<Question> = mutableListOf<Question>())

data class Question(
        val questionText:String,
        val submittedBy: String,
        val answers: List<String> = listOf(),
        val correctQuestionId: Int = 0)
