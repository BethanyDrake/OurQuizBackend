package com.sycorax.ourquiz.controllers

class ListParticipantsService {


    fun listDoneParticipants(players: List<Player>, quiz:Quiz): List<Player>{
        val pendingPlayers = listPendingParticipants(players, quiz)
        return players.filter { !pendingPlayers.contains(it) }
    }

    fun listPendingParticipants(players: List<Player>, quiz:Quiz): List<Player>{
        if (!quiz.hasStarted) {
            return players.filter { !it.hasSubmittedQuestion }
        }
        return players.filter{it.lastAnsweredQuestion < quiz.currentQuestion}

    }


}