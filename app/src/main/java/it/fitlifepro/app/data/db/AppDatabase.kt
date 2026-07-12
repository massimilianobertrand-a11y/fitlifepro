
package it.fitlifepro.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import it.fitlifepro.app.data.model.*

@Database(
    entities = [
        Program::class, TrainingDay::class, Exercise::class,
        WorkoutSession::class, PerformedSet::class,
        MealPlan::class, MealLog::class,
        Supplement::class, SupplementLog::class,
        HydrationConfig::class, HydrationReminder::class, HydrationLog::class,
        HealthData::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun programDao(): ProgramDao
    abstract fun trainingDayDao(): TrainingDayDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun performedSetDao(): PerformedSetDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun supplementDao(): SupplementDao
    abstract fun supplementLogDao(): SupplementLogDao
    abstract fun hydrationConfigDao(): HydrationConfigDao
    abstract fun hydrationReminderDao(): HydrationReminderDao
    abstract fun hydrationLogDao(): HydrationLogDao
    abstract fun healthDataDao(): HealthDataDao
}
