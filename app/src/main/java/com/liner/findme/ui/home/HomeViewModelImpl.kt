package com.liner.findme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.repositories.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    private val photoCardRepository: PhotoCardRepository
) : ViewModel(), HomeViewModel {

    private val _photoCards: MutableStateFlow<List<PhotoCard>> = MutableStateFlow(emptyList())
    override val photoCards: StateFlow<List<PhotoCard>> = _photoCards

    override val loadingState: MutableStateFlow<Int> = MutableStateFlow(HomeViewModel.STATE_START)

    override fun getPhotoCards(filter: String) {
        viewModelScope.launch {
            loadingState.value = HomeViewModel.STATE_START

            val photoCards = photoCardRepository.getPhotoCards(filter)
            _photoCards.value = photoCards

            loadingState.value = HomeViewModel.STATE_END
        }
    }

}