package com.liner.findme.ui.home.global_scores

import com.liner.findme.network.models.User
import kotlinx.coroutines.flow.Flow

interface GlobalScoresViewModel {
    fun getGlobalScores(): Flow<List<User>>
}