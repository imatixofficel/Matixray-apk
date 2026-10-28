package com.v2ray.ang.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.UpdateManager
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface UpdateState {
        data object Idle : UpdateState
        data class Available(val info: UpdateManager.UpdateInfo) : UpdateState
        data class Downloading(val info: UpdateManager.UpdateInfo, val progress: Float) : UpdateState
        data class Ready(val info: UpdateManager.UpdateInfo, val file: File) : UpdateState
        data class Failed(val info: UpdateManager.UpdateInfo) : UpdateState
    }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var job: Job? = null

    /** Looks for a newer release; called every time the app is opened. */
    fun checkForUpdate() {
        if (_state.value != UpdateState.Idle || job?.isActive == true) return
        val now = System.currentTimeMillis()
        if (now - lastCheckAt < MIN_CHECK_GAP_MS) return
        lastCheckAt = now
        job = viewModelScope.launch {
            try {
                val info = UpdateManager.checkForUpdate()
                if (info != null && _state.value == UpdateState.Idle) {
                    _state.value = UpdateState.Available(info)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Update check failed", e)
            }
        }
    }

    fun startDownload() {
        val info = when (val current = _state.value) {
            is UpdateState.Available -> current.info
            is UpdateState.Failed -> current.info
            else -> return
        }
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = UpdateState.Downloading(info, 0f)
            try {
                val file = UpdateManager.download(getApplication(), info) { progress ->
                    _state.value = UpdateState.Downloading(info, progress)
                }
                _state.value = UpdateState.Ready(info, file)
                UpdateManager.install(getApplication(), file)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Update download failed", e)
                _state.value = UpdateState.Failed(info)
            }
        }
    }

    fun install() {
        val ready = _state.value as? UpdateState.Ready ?: return
        UpdateManager.install(getApplication(), ready.file)
    }

    fun dismiss() {
        job?.cancel()
        _state.value = UpdateState.Idle
    }

    private companion object {
        const val MIN_CHECK_GAP_MS = 10 * 60 * 1000L

        @Volatile
        var lastCheckAt = 0L
    }
}
