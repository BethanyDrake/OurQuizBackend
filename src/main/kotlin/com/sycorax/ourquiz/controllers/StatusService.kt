package com.sycorax.ourquiz.controllers


data class StatusResponse(val questionNumber: Int, val revealed: Boolean)

class StatusService {

    val NOT_STARTED: StatusResponse = StatusResponse(-1, false)
    val FINISHED: StatusResponse = StatusResponse(-1, true)
    fun getStatus(quiz: Quiz): StatusResponse{
        if (!quiz.hasStarted) return NOT_STARTED
        if( quiz.currentQuestion == quiz.questions.size) return FINISHED
        val revealed = quiz.questions[quiz.currentQuestion].revealed
        return  StatusResponse(quiz.currentQuestion, revealed)
    }
}