package com.vugaenterprises.androidtv.di

import android.content.Context
import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.utils.ErrorLogger
import com.vugaenterprises.androidtv.utils.ErrorReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ErrorModule {
    
    @Provides
    @Singleton
    fun provideErrorLogger(
        @ApplicationContext context: Context,
        apiService: ApiService
    ): ErrorLogger {
        return ErrorLogger(context, apiService)
    }
    
    @Provides
    @Singleton
    fun provideErrorReporter(
        @ApplicationContext context: Context,
        errorLogger: ErrorLogger
    ): ErrorReporter {
        return ErrorReporter(context, errorLogger)
    }
}