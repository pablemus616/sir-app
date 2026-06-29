package com.xnihilfx.sirmobile.di

import android.content.Context
import com.xnihilfx.sirmobile.data.local.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun sessionStore(@ApplicationContext c: Context): SessionStore = SessionStore.create(c)
}
