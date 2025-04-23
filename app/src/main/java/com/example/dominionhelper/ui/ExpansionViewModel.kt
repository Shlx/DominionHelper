package com.example.dominionhelper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dominionhelper.data.Expansion
import com.example.dominionhelper.data.ExpansionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpansionViewModel @Inject constructor(
    private val expansionDao: ExpansionDao
) : ViewModel() {

    private val _expansions = MutableStateFlow<List<Expansion>>(emptyList())
    val expansions: StateFlow<List<Expansion>> = _expansions.asStateFlow()

    init {
        loadExpansions()
    }

    private fun loadExpansions() {
        viewModelScope.launch {
            _expansions.value = expansionDao.getAll()
        }
    }

    fun updateIsOwned(expansionId: Int, newIsOwned: Boolean) {
        viewModelScope.launch {
            expansionDao.updateIsOwned(expansionId, newIsOwned)
            // Optionally, reload the expansions to update the UI
            loadExpansions()
        }
    }

}