package com.sycorax.ourquiz.controllers

class ListParticipantsService {


    fun listParticipantsWho(waiting: Boolean, participants: Map<String, List<Player>>, quizId: String): String{
        val hasSubmittedQuestion = !waiting
        return participants[quizId]?.filter { it.hasSubmittedQuestion == hasSubmittedQuestion }?.map { it.name }.toString()
    }
}