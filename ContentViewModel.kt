package com.example.madhavmaheshwari.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madhavmaheshwari.R
import com.example.madhavmaheshwari.home.repository.ContentRepository
import com.example.madhavmaheshwari.utils.DispatcherProvider
import com.example.madhavmaheshwari.utils.Status
import com.example.madhavmaheshwari.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ContentViewModel
    @Inject
    constructor(
        private val contentRepository: ContentRepository,
        private val dispatcherProvider: DispatcherProvider,
    ) : ViewModel() {
        private val _uiState = MutableLiveData<UIState<String>>(UIState.None)
        val uiState: LiveData<UIState<String>> = _uiState

        private val _result15thChar = MutableLiveData<UIState<String>>(UIState.Loading)
        val result15thChar: LiveData<UIState<String>> = _result15thChar

        private val _resultEvery15thChar = MutableLiveData<UIState<List<Char>>>(UIState.Loading)
        val resultEvery15thChar: LiveData<UIState<List<Char>>> = _resultEvery15thChar

        private val _wordCountMap = MutableLiveData<UIState<Map<String, Int>>>(UIState.Loading)
        val wordCountMap: LiveData<UIState<Map<String, Int>>> = _wordCountMap

        fun fetchContent() {
            viewModelScope.launch(dispatcherProvider.io) {
                try {
                    contentRepository.fetchWebContent().collectLatest {
                        when (it) {
                            Status.Loading -> {
                                _uiState.postValue(UIState.Loading)
                            }
                            is Status.OnFailed -> {
                                _uiState.postValue(UIState.Error(R.string.text_unknown_error))
                            }
                            is Status.OnSuccess -> {
                                _uiState.postValue(UIState.Success(it.response))
                                if (it.response.isNotEmpty()) {
                                    _result15thChar.postValue(UIState.Success(find15thCharacter(it.response)))
                                    _resultEvery15thChar.postValue(UIState.Success(findEvery15thCharacter(it.response)))
                                    _wordCountMap.postValue(UIState.Success(countWords(it.response)))
                                } else {
                                    _result15thChar.postValue(UIState.Error(R.string.text_not_enough_character))
                                    _resultEvery15thChar.postValue(UIState.Error(R.string.text_not_enough_character))
                                    _wordCountMap.postValue(UIState.Error(R.string.text_not_enough_character))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.postValue(UIState.Error(R.string.text_unknown_error))
                }
            }
        }

        fun find15thCharacter(content: String): String =
            if (content.length >=
                15
            ) {
                content[14].toString()
            } else {
                "Not Enough Characters"
            }

        fun findEvery15thCharacter(content: String): List<Char> {
            // Added just to showcase Loading text
            runBlocking {
                delay(1500)
            }
            val result = mutableListOf<Char>()
            for (i in 14 until content.length step 15) {
                result.add(content[i])
            }
            return result
        }

        fun countWords(content: String): Map<String, Int> {
            // Added just to showcase Loading text
            runBlocking {
                delay(1500)
            }
            return content
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .map { it.lowercase(Locale.ROOT) }
                .groupingBy { it }
                .eachCount()
        }
    }
