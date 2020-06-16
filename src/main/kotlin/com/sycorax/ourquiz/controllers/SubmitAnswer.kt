package com.sycorax.ourquiz.controllers

import com.sycorax.ourquiz.SubmitAnswerBody

class SubmitAnswerService() {

    fun submitAnswer(quiz:Quiz, questionNumber: Int, answerId: Int, playerName: String): String {
        if (quiz.currentQuestion != questionNumber) return "NO - wrong question"

        println(playerName + " attempting to submit answers. players: " + quiz.players.map{it.name})

        val player = quiz.players.firstOrNull{it.name == playerName } ?: return "NO - player has not joined quiz"

        if (player.answers.size == questionNumber) {
            println("adding answer")
            player.answers.add(answerId)
        }


        player.lastAnsweredQuestion = questionNumber


        println("done submitting answer")
        return "OK"
    }

}