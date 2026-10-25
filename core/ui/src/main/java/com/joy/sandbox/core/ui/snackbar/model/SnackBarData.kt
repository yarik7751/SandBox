package com.joy.sandbox.core.ui.snackbar.model

import com.joy.sandbox.utils.textorresource.TextOrResource

data class SnackBarData(
    val type: SnackBarType,
    val duration: SnackBarDuration = SnackBarDuration.Short,
    val message: TextOrResource,
)
