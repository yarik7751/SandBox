package com.joy.sandbox.core.ui.snackbar.flow

import com.joy.sandbox.core.ui.snackbar.model.SnackBarData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

/**
 * Контракт глобального снекбара приложения. Живёт в :core:ui, чтобы фичи
 * могли инжектить [SnackBarManager] в свои ViewModel; оверлей, рисующий
 * сообщения, живёт в :app и подписывается на [SnackBarManagerFlow].
 */
interface SnackBarManager {
    fun showMessage(data: SnackBarData)
}

interface SnackBarManagerFlow {

    fun observe(): Flow<SnackBarData>
}

class SnackBarManagerImpl @Inject constructor() : SnackBarManager, SnackBarManagerFlow {

    private val snackBarFlow = MutableSharedFlow<SnackBarData>(
        replay = 1,
        extraBufferCapacity = 2,
    )

    override fun showMessage(data: SnackBarData) {
        snackBarFlow.tryEmit(data)
    }

    override fun observe(): Flow<SnackBarData> {
        return snackBarFlow
    }
}
