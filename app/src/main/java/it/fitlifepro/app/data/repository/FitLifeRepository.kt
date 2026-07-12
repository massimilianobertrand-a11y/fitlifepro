
package it.fitlifepro.app.data.repository

import it.fitlifepro.app.data.db.*
import it.fitlifepro.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitLifeRepository @Inject constructor(
    private val programDao: ProgramDao,
    private val trainingDayDao: TrainingDayDao,
    private val exerciseDao: ExerciseDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val performedSetDao: PerformedSetDao,
    private val mealPlanDao: MealPlanDao,
    private val mealLogDao: MealLogDao,
    private val supplementDao: SupplementDao,
    private val supplementLogDao: SupplementLogDao,
    private val hydrationConfigDao: HydrationConfigDao,
    private val hydrationReminderDao: HydrationReminderDao,
    private val hydrationLogDao: HydrationLogDao,
    private val healthDataDao: HealthDataDao
) {
    // Programs
    val allPrograms: Flow<List<Program>> = programDao.getAll()
    val activeProgram: Flow<Program?> = programDao.getActive()
    suspend fun activateProgram(id: Long) { programDao.deactivateAll(); programDao.activate(id) }
    suspend fun saveProgram(p: Program): Long = programDao.insert(p)
    suspend fun deleteProgram(p: Program) = programDao.delete(p)

    // Training days
    fun getTrainingDays(programId: Long) = trainingDayDao.getByProgram(programId)
    suspend fun saveTrainingDay(d: TrainingDay) = trainingDayDao.insert(d)
    suspend fun saveTrainingDays(days: List<TrainingDay>) = trainingDayDao.insertAll(days)
    suspend fun deleteTrainingDay(d: TrainingDay) = trainingDayDao.delete(d)

    // Exercises
    fun getExercises(dayId: Long) = exerciseDao.getByDay(dayId)
    suspend fun getExercisesSync(dayId: Long) = exerciseDao.getByDaySync(dayId)
    suspend fun saveExercises(exs: List<Exercise>) = exerciseDao.insertAll(exs)
    suspend fun saveExercise(e: Exercise) = exerciseDao.insert(e)
    suspend fun updateExercise(e: Exercise) = exerciseDao.update(e)
    suspend fun deleteExercise(e: Exercise) = exerciseDao.delete(e)

    // Workout sessions
    fun getRecentSessions(limit: Int = 20) = workoutSessionDao.getRecent(limit)
    fun getSessionsByDate(date: String) = workoutSessionDao.getByDate(date)
    suspend fun startSession(dayId: Long?): Long {
        val s = WorkoutSession(
            trainingDayId = dayId,
            date = LocalDate.now().toString(),
            startTime = System.currentTimeMillis()
        )
        return workoutSessionDao.insert(s)
    }
    suspend fun completeSession(session: WorkoutSession) =
        workoutSessionDao.update(session.copy(endTime = System.currentTimeMillis(), completed = true))
    suspend fun updateSession(s: WorkoutSession) = workoutSessionDao.update(s)

    // Performed sets
    fun getSetsForSession(sessionId: Long) = performedSetDao.getBySession(sessionId)
    suspend fun logSet(s: PerformedSet) = performedSetDao.insert(s)
    suspend fun logSets(sets: List<PerformedSet>) = performedSetDao.insertAll(sets)

    // Meal plan
    fun getMealPlan(programId: Long) = mealPlanDao.getByProgram(programId)
    fun getMealsByDay(programId: Long, day: String) = mealPlanDao.getByDay(programId, day)
    suspend fun saveMeals(meals: List<MealPlan>) = mealPlanDao.insertAll(meals)
    suspend fun deleteMeal(m: MealPlan) = mealPlanDao.delete(m)

    // Meal log
    fun getMealLog(date: String) = mealLogDao.getByDate(date)
    suspend fun logMeal(mealPlanId: Long, date: String) =
        mealLogDao.insert(MealLog(mealPlanId = mealPlanId, date = date))
    suspend fun unlogMeal(date: String, mealPlanId: Long) =
        mealLogDao.delete(date, mealPlanId)

    // Supplements
    fun getSupplements(programId: Long) = supplementDao.getByProgram(programId)
    suspend fun saveSupplements(sups: List<Supplement>) = supplementDao.insertAll(sups)
    suspend fun saveSupplements(s: Supplement) = supplementDao.insert(s)
    suspend fun updateSupplement(s: Supplement) = supplementDao.update(s)
    suspend fun deleteSupplement(s: Supplement) = supplementDao.delete(s)

    // Supplement log
    fun getSupplementLog(date: String) = supplementLogDao.getByDate(date)
    suspend fun logSupplement(suppId: Long, date: String) =
        supplementLogDao.insert(SupplementLog(supplementId = suppId, date = date))
    suspend fun unlogSupplement(date: String, suppId: Long) =
        supplementLogDao.delete(date, suppId)

    // Hydration config
    fun getHydrationConfig(programId: Long) = hydrationConfigDao.getByProgram(programId)
    suspend fun saveHydrationConfig(c: HydrationConfig) = hydrationConfigDao.insert(c)
    suspend fun updateHydrationConfig(c: HydrationConfig) = hydrationConfigDao.update(c)

    // Hydration reminders
    fun getHydrationReminders(configId: Long) = hydrationReminderDao.getByConfig(configId)
    suspend fun saveHydrationReminders(rs: List<HydrationReminder>) = hydrationReminderDao.insertAll(rs)
    suspend fun updateHydrationReminder(r: HydrationReminder) = hydrationReminderDao.update(r)
    suspend fun deleteHydrationReminder(r: HydrationReminder) = hydrationReminderDao.delete(r)

    // Hydration log
    fun getHydrationLog(date: String) = hydrationLogDao.getByDate(date)
    fun getHydrationTotal(date: String): Flow<Int> = hydrationLogDao.getTotalMl(date)
    suspend fun logWater(amountMl: Int) =
        hydrationLogDao.insert(HydrationLog(date = LocalDate.now().toString(), amountMl = amountMl))
    suspend fun deleteHydrationEntry(id: Long) = hydrationLogDao.delete(id)

    // Health data
    fun getRecentHealthData(limit: Int = 30) = healthDataDao.getRecent(limit)
    suspend fun saveHealthData(h: HealthData) = healthDataDao.insert(h)
}
