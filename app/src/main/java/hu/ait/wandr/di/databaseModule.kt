package hu.ait.wandr.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.ait.wandr.data.AppDatabase
import hu.ait.wandr.data.TravelPinDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "appDatabase"
        ).build()
    }

    @Provides
    fun provideTravelPinDao(appDatabase: AppDatabase): TravelPinDao {
        return appDatabase.travelPinDao()
    }
}