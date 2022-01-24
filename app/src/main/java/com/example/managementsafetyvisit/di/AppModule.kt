package com.example.managementsafetyvisit.di

import android.content.Context
import com.example.managementsafetyvisit.BaseApplication
import com.example.managementsafetyvisit.retrofit.RetrofitFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }

    @Singleton
    @Provides
    fun getRetro() : RetrofitFunctions{
        return RetrofitFunctions()
    }

}