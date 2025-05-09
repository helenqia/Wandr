package hu.ait.wandr.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.ait.wandr.data.TravelPin
import hu.ait.wandr.data.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val travelRepository: TravelRepository
) : ViewModel() {

    private val _rankedPlaces = MutableStateFlow<List<TravelPin>>(emptyList())
    val rankedPlaces: StateFlow<List<TravelPin>> = _rankedPlaces.asStateFlow()

    private val _pairForComparison = MutableStateFlow<List<TravelPin>>(emptyList())
    val pairForComparison: StateFlow<List<TravelPin>> = _pairForComparison.asStateFlow()

    init {
        viewModelScope.launch {
            travelRepository.getAllPinsRankedAsFlow().collect { pins ->
                _rankedPlaces.value = pins
            }
        }
        loadRandomPairForComparison()
    }

    fun loadRandomPairForComparison() {
        viewModelScope.launch {
            val pair = travelRepository.getRandomPairForComparison()
            if (pair.size == 2) {
                _pairForComparison.value = pair
            }
        }
    }

    fun selectBetterPlace(winnerId: Int, loserId: Int) {
        viewModelScope.launch {
            travelRepository.compareAndUpdateRatings(winnerId, loserId)
            // Load a new pair after rating
            loadRandomPairForComparison()
        }
    }
}