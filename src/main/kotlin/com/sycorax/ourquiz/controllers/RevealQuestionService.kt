package com.sycorax.ourquiz.controllers

class RevealQuestionService {
    fun revealQuestion(quiz: Quiz, questionNumber: Int) {
        if (quiz.currentQuestion != questionNumber) throw Exception("current question is " + quiz.currentQuestion +", but requested question is " + questionNumber)
        quiz.questions[questionNumber].revealed = true
        quiz.players.forEach {
            if(it.answers.size <= questionNumber ){
                it.answers.add(-1)
            }

        }
    }
}