
package it.fitlifepro.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.fitlifepro.app.data.db.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "fitlifepro.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProgramDao(db: AppDatabase) = db.programDao()
    @Provides fun provideTrainingDayDao(db: AppDatabase) = db.trainingDayDao()
    @Provides fun provideExerciseDao(db: AppDatabase) = db.exerciseDao()
    @Provides fun provideWorkoutSessionDao(db: AppDatabase) = db.workoutSessionDao()
    @Provides fun providePerformedSetDao(db: AppDatabase) = db.performedSetDao()
    @Provides fun provideMealPlanDao(db: AppDatabase) = db.mealPlanDao()
    @Provides fun provideMealLogDao(db: AppDatabase) = db.mealLogDao()
    @Provides fun provideSupplementDao(db: AppDatabase) = db.supplementDao()
    @Provides fun provideSupplementLogDao(db: AppDatabase) = db.supplementLogDao()
    @Provides fun provideHydrationConfigDao(db: AppDatabase) = db.hydrationConfigDao()
    @Provides fun provideHydrationReminderDao(db: AppDatabase) = db.hydrationReminderDao()
    @Provides fun provideHydrationLogDao(db: AppDatabase) = db.hydrationLogDao()
    @Provides fun provideHealthDataDao(db: AppDatabase) = db.healthDataDao()
}