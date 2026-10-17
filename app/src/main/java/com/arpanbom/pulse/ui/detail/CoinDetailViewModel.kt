package com.arpanbom.pulse.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpanbom.pulse.domain.usecase.ObserveCoinUseCase
import com.arpanbom.pulse.domain.usecase.ObserveLiveQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCoin: ObserveCoinUseCase,
    observeLiveQuotes: ObserveLiveQuotesUseCase,
) : ViewModel() {
    private val coinId: String = checkNotNull(savedStateHandle.get<String>(COIN_ID))

    val uiState: StateFlow<CoinDetailUiState> = observeCoin(coinId)
        .flatMapLatest { coin ->
            if (coin == null) {
                flowOf(CoinDetailUiState(isLoading = false, coin = null))
            } else {
                val asset = coin.symbol.uppercase()
                observeLiveQuotes(setOf(asset)).map { quotes ->
                    val live = quotes[asset]
                    CoinDetailUiState(
                        isLoading = false,
                        coin = coin,
                        displayPrice = live?.price ?: coin.currentPrice,
                        isLive = live != null,
                    )
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CoinDetailUiState(isLoading = true),
        )

    companion object {
        const val COIN_ID = "coinId"
    }
}
