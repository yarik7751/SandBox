package com.joy.sandbox.di.module

import com.joy.sandbox.core.ui.snackbar.flow.SnackBarManager
import com.joy.sandbox.core.ui.snackbar.flow.SnackBarManagerFlow
import com.joy.sandbox.core.ui.snackbar.flow.SnackBarManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SnackBarModule {

    @Provides
    @Singleton
    fun provideSnackBarManagerImpl() = SnackBarManagerImpl()

    @Provides
    fun provideSnackBarManager(impl: SnackBarManagerImpl) = impl as SnackBarManager

    @Provides
    fun provideSnackBarManagerFlow(impl: SnackBarManagerImpl) = impl as SnackBarManagerFlow
}
