package com.sycorax.ourquiz.controllers

import com.sycorax.ourquiz.SubmitAnswerBody

class SubmitAnswerService() {

    fun submitAnswer(quiz:Quiz, participants: List<Player>, questionNumber: Int, answerId: Int, playerName: String): String {
        if (quiz.stage != questionNumber) return "NO"
        if (participants.none { it.name == playerName }) return "NO"

        participants.first { it.name ==playerName }.lastAnsweredQuestion = questionNumber

        return "OK"
    }

}