package com.sycorax.ourquiz.controllers

class ListParticipantsService {


    fun listDoneParticipants(players: List<Player>, quiz:Quiz): List<Player>{
        val pendingPlayers = listPendingParticipants(players, quiz)
        return players.filter { !pendingPlayers.contains(it) }
    }

    fun listPendingParticipants(players: List<Player>, quiz:Quiz): List<Player>{
//        println("listing pending participants: " + players.map{
//            it.name +" " + it.hasSubmittedQuestion +" " + it.lastAnsweredQuestion
//        })

        if (!quiz.hasStarted) {
            return players.filter { !it.hasSubmittedQuestion }
        }
        return players.filter{it.lastAnsweredQuestion < quiz.currentQuestion}

    }


}