package com.sycorax.ourquiz.controllers

class ResultsService {


    data class PlayerScore(
            val name: String,
            val correctAnswers: Int
    )
    data class QuizResult(
            val quizId:String,
            val totalQuestions: Int,
            val playerScores: List<PlayerScore>
    )
    fun getResults(quiz: Quiz):QuizResult {
        if (!quiz.hasStarted) throw Exception("quiz has not started")
       return QuizResult(quiz.id, quiz.questions.size, listPlayerScores(quiz))
    }


    private fun listPlayerScores(quiz: Quiz):List<PlayerScore>{
        return quiz.players.map{PlayerScore(it.name, countCorrectAnswers(it.answers, quiz.questions))}
    }

    private fun countCorrectAnswers(answers: List<Int>, questions: List<Question>): Int {
        if (answers.size != questions.size) throw Exception("Error calculating result: answers list is not the same size as question list.")

        return answers.filterIndexed{ index, answer ->  answer == questions[index].correctAnswerId}.count()

    }
}