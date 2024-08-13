package com.liner.findme.ui.home

import com.liner.findme.network.models.PhotoCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface HomeViewModel {

    companion object {
        const val STATE_START: Int = 0
        const val STATE_END: Int = 1
    }
    val loadingState: MutableStateFlow<Int>

    val photoCards: StateFlow<List<PhotoCard>>

    fun getPhotoCards(filter: String = "explore")

}