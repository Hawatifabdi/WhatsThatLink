package com.whatsThatLink.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whatsThatLink.app.data.PhishingRepository
import com.whatsThatLink.app.data.RecentScan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PhishingRepository(application)
    val history = repository.getAllScans()

    private val _scanResult = MutableStateFlow<Result<RecentScan>?>(null)
    val scanResult: StateFlow<Result<RecentScan>?> = _scanResult.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun scanUrl(url: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = repository.scanUrl(url)
            _isScanning.value = false
        }
    }

    suspend fun getScanById(id: Long): RecentScan? {
        return repository.getScanById(id)
    }

    fun clearScanResult() {
        _scanResult.value = null
    }
}
