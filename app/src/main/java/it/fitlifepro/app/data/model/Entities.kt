
package it.fitlifepro.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Programma di allenamento principale */
@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val goal: String,          // massa | definizione | mantenimento | forza
    val startDate: String,     // YYYY-MM-DD
    val endDate: String,
    val athleteName: String,
    val weightKg: Float,
    val heightCm: Int,
    val level: String,         // principiante | intermedio | avanzato
    val daysPerWeek: Int,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/** Giorno di allenamento */
@Entity(
    tableName = "training_days",
    foreignKeys = [ForeignKey(Program::class, ["id"], ["programId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("programId")]
)
data class TrainingDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dayOfWeek: String,     // Lunedi..Domenica
    val sessionType: String,   // SALA_PESI | TOTAL_BODY
    val timeHHMM: String,
    val durationMin: Int,
    val activeWeeks: String,   // "1-4" o "1,3"
    val notes: String = ""
)

/** Esercizio singolo in un giorno */
@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(TrainingDay::class, ["id"], ["trainingDayId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("trainingDayId")]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingDayId: Long,
    val order: Int,
    val name: String,
    val muscleGroup: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Float = 0f,
    val restSec: Int = 60,
    val videoUrl: String = "",
    val notes: String = ""
)

/** Sessione di allenamento registrata */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(TrainingDay::class, ["id"], ["trainingDayId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("trainingDayId")]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingDayId: Long?,
    val date: String,           // YYYY-MM-DD
    val startTime: Long,
    val endTime: Long? = null,
    val avgHeartRate: Int? = null,
    val caloriesBurned: Int? = null,
    val notes: String = "",
    val completed: Boolean = false
)

/** Singola serie eseguita durante una sessione */
@Entity(
    tableName = "performed_sets",
    foreignKeys = [ForeignKey(WorkoutSession::class, ["id"], ["sessionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("sessionId")]
)
data class PerformedSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val repsActual: Int,
    val weightActual: Float,
    val restSec: Int,
    val heartRate: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/** Pasto del menu settimanale */
@Entity(
    tableName = "meal_plan",
    foreignKeys = [ForeignKey(Program::class, ["id"], ["programId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("programId")]
)
data class MealPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dayOfWeek: String,
    val mealType: String,   // Colazione|Spuntino_mattina|Pranzo|Spuntino_pomeriggio|Cena|Post_workout
    val timeHHMM: String,
    val food1: String,
    val qty1g: Int,
    val food2: String = "",
    val qty2g: Int = 0,
    val kcalEstimated: Int = 0,
    val notes: String = ""
)

/** Assunzione giornaliera registrata per pasto */
@Entity(tableName = "meal_log")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealPlanId: Long,
    val date: String,           // YYYY-MM-DD
    val consumed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/** Integratore */
@Entity(
    tableName = "supplements",
    foreignKeys = [ForeignKey(Program::class, ["id"], ["programId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("programId")]
)
data class Supplement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val name: String,
    val brand: String = "",
    val category: String,   // Proteico|Creatina|BCAA|Omega3|Vitamina|Minerale|Pre-workout|Altro
    val dosage: String,
    val timing: String,     // mattina_digiuno|pre_workout|post_workout|con_pasto|sera|prima_sonno
    val timeHHMM: String,
    val days: String,       // Tutti|Solo_allenamento|Solo_riposo
    val takeWith: String = "",
    val notes: String = "",
    val active: Boolean = true
)

/** Log assunzione integratore */
@Entity(tableName = "supplement_log")
data class SupplementLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplementId: Long,
    val date: String,
    val taken: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/** Configurazione idratazione */
@Entity(tableName = "hydration_config")
data class HydrationConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dailyGoalMl: Int = 3000,
    val extraTrainingMl: Int = 500,
    val wakeUpDoseMl: Int = 250,
    val wakeUpTime: String = "07:00",
    val reminderIntervalMin: Int = 90,
    val glassStandardMl: Int = 250,
    val notificationsActive: Boolean = true,
    val stopNotificationsAt: String = "22:30"
)

/** Reminder idratazione personalizzato */
@Entity(tableName = "hydration_reminders")
data class HydrationReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val configId: Long,
    val label: String,
    val timeHHMM: String,
    val amountMl: Int,
    val days: String = "Tutti",
    val active: Boolean = true,
    val notes: String = ""
)

/** Log idratazione giornaliero */
@Entity(tableName = "hydration_log")
data class HydrationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/** Dati FC/attività da Health Connect */
@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val steps: Int? = null,
    val activeCalories: Int? = null,
    val sleepMinutes: Int? = null,
    val spo2: Float? = null,
    val syncedAt: Long = System.currentTimeMillis()
)
