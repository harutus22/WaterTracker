package com.clifertam.watertracker.di

import android.content.Context
import androidx.room.Room
import com.clifertam.watertracker.db.WaterDao
import com.clifertam.watertracker.db.WaterDatabase
import com.clifertam.watertracker.db.repository.WaterRepository
import com.clifertam.watertracker.repository.WaterRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WaterModule {

    @Provides
    @Singleton
    fun provideWaterRepository(
        dao: WaterDao
    ): WaterRepository {
        return WaterRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun providesDao(waterDatabase: WaterDatabase): WaterDao{
        return waterDatabase.waterDao()
    }

    @Provides
    @Singleton
    fun waterDatabase(@ApplicationContext context: Context): WaterDatabase{
        return Room.databaseBuilder(context, WaterDatabase::class.java, "water_db").build()
    }
}