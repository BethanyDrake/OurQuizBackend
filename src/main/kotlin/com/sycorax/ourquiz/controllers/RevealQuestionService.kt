package com.sycorax.ourquiz.controllers

class RevealQuestionService {
    fun revealQuestion(quiz: Quiz, questionNumber: Int) {
        if (quiz.currentQuestion != questionNumber) throw Exception()
        quiz.questions[questionNumber].revealed = true
    }
}