
package it.fitlifepro.app.data.db

import androidx.room.*
import it.fitlifepro.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY createdAt DESC") fun getAll(): Flow<List<Program>>
    @Query("SELECT * FROM programs WHERE isActive=1 LIMIT 1") fun getActive(): Flow<Program?>
    @Query("SELECT * FROM programs WHERE id=:id") suspend fun getById(id: Long): Program?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(p: Program): Long
    @Update suspend fun update(p: Program)
    @Delete suspend fun delete(p: Program)
    @Query("UPDATE programs SET isActive=0") suspend fun deactivateAll()
    @Query("UPDATE programs SET isActive=1 WHERE id=:id") suspend fun activate(id: Long)
}

@Dao interface TrainingDayDao {
    @Query("SELECT * FROM training_days WHERE programId=:pid ORDER BY CASE dayOfWeek "
         + "WHEN 'Lunedi' THEN 1 WHEN 'Martedi' THEN 2 WHEN 'Mercoledi' THEN 3 "
         + "WHEN 'Giovedi' THEN 4 WHEN 'Venerdi' THEN 5 WHEN 'Sabato' THEN 6 ELSE 7 END")
    fun getByProgram(pid: Long): Flow<List<TrainingDay>>
    @Query("SELECT * FROM training_days WHERE id=:id") suspend fun getById(id: Long): TrainingDay?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(d: TrainingDay): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(days: List<TrainingDay>)
    @Update suspend fun update(d: TrainingDay)
    @Delete suspend fun delete(d: TrainingDay)
    @Query("DELETE FROM training_days WHERE programId=:pid") suspend fun deleteByProgram(pid: Long)
}

@Dao interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE trainingDayId=:dayId ORDER BY `order`")
    fun getByDay(dayId: Long): Flow<List<Exercise>>
    @Query("SELECT * FROM exercises WHERE trainingDayId=:dayId ORDER BY `order`")
    suspend fun getByDaySync(dayId: Long): List<Exercise>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: Exercise): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(exs: List<Exercise>)
    @Update suspend fun update(e: Exercise)
    @Delete suspend fun delete(e: Exercise)
    @Query("DELETE FROM exercises WHERE trainingDayId=:dayId") suspend fun deleteByDay(dayId: Long)
}

@Dao interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 20): Flow<List<WorkoutSession>>
    @Query("SELECT * FROM workout_sessions WHERE date=:date") fun getByDate(date: String): Flow<List<WorkoutSession>>
    @Query("SELECT * FROM workout_sessions WHERE id=:id") suspend fun getById(id: Long): WorkoutSession?
    @Insert suspend fun insert(s: WorkoutSession): Long
    @Update suspend fun update(s: WorkoutSession)
    @Delete suspend fun delete(s: WorkoutSession)
}

@Dao interface PerformedSetDao {
    @Query("SELECT * FROM performed_sets WHERE sessionId=:sid ORDER BY id")
    fun getBySession(sid: Long): Flow<List<PerformedSet>>
    @Insert suspend fun insert(s: PerformedSet): Long
    @Insert suspend fun insertAll(sets: List<PerformedSet>)
    @Delete suspend fun delete(s: PerformedSet)
    @Query("DELETE FROM performed_sets WHERE sessionId=:sid") suspend fun deleteBySession(sid: Long)
}

@Dao interface MealPlanDao {
    @Query("SELECT * FROM meal_plan WHERE programId=:pid ORDER BY CASE dayOfWeek "
         + "WHEN 'Lunedi' THEN 1 WHEN 'Martedi' THEN 2 WHEN 'Mercoledi' THEN 3 "
         + "WHEN 'Giovedi' THEN 4 WHEN 'Venerdi' THEN 5 WHEN 'Sabato' THEN 6 ELSE 7 END, id")
    fun getByProgram(pid: Long): Flow<List<MealPlan>>
    @Query("SELECT * FROM meal_plan WHERE programId=:pid AND dayOfWeek=:day ORDER BY id")
    fun getByDay(pid: Long, day: String): Flow<List<MealPlan>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: MealPlan): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(meals: List<MealPlan>)
    @Delete suspend fun delete(m: MealPlan)
    @Query("DELETE FROM meal_plan WHERE programId=:pid") suspend fun deleteByProgram(pid: Long)
}

@Dao interface MealLogDao {
    @Query("SELECT * FROM meal_log WHERE date=:date") fun getByDate(date: String): Flow<List<MealLog>>
    @Insert suspend fun insert(l: MealLog): Long
    @Query("DELETE FROM meal_log WHERE date=:date AND mealPlanId=:mid") suspend fun delete(date: String, mid: Long)
}

@Dao interface SupplementDao {
    @Query("SELECT * FROM supplements WHERE programId=:pid AND active=1 ORDER BY timeHHMM")
    fun getByProgram(pid: Long): Flow<List<Supplement>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: Supplement): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(sups: List<Supplement>)
    @Update suspend fun update(s: Supplement)
    @Delete suspend fun delete(s: Supplement)
    @Query("DELETE FROM supplements WHERE programId=:pid") suspend fun deleteByProgram(pid: Long)
}

@Dao interface SupplementLogDao {
    @Query("SELECT * FROM supplement_log WHERE date=:date") fun getByDate(date: String): Flow<List<SupplementLog>>
    @Insert suspend fun insert(l: SupplementLog): Long
    @Query("DELETE FROM supplement_log WHERE date=:date AND supplementId=:sid") suspend fun delete(date: String, sid: Long)
}

@Dao interface HydrationConfigDao {
    @Query("SELECT * FROM hydration_config WHERE programId=:pid LIMIT 1") fun getByProgram(pid: Long): Flow<HydrationConfig?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(c: HydrationConfig): Long
    @Update suspend fun update(c: HydrationConfig)
}

@Dao interface HydrationReminderDao {
    @Query("SELECT * FROM hydration_reminders WHERE configId=:cid ORDER BY timeHHMM")
    fun getByConfig(cid: Long): Flow<List<HydrationReminder>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(rs: List<HydrationReminder>)
    @Update suspend fun update(r: HydrationReminder)
    @Delete suspend fun delete(r: HydrationReminder)
}

@Dao interface HydrationLogDao {
    @Query("SELECT * FROM hydration_log WHERE date=:date ORDER BY timestamp")
    fun getByDate(date: String): Flow<List<HydrationLog>>
    @Query("SELECT COALESCE(SUM(amountMl),0) FROM hydration_log WHERE date=:date")
    fun getTotalMl(date: String): Flow<Int>
    @Insert suspend fun insert(l: HydrationLog): Long
    @Query("DELETE FROM hydration_log WHERE id=:id") suspend fun delete(id: Long)
}

@Dao interface HealthDataDao {
    @Query("SELECT * FROM health_data ORDER BY date DESC LIMIT :limit") fun getRecent(limit: Int=30): Flow<List<HealthData>>
    @Query("SELECT * FROM health_data WHERE date=:date LIMIT 1") suspend fun getByDate(date: String): HealthData?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(h: HealthData): Long
    @Query("DELETE FROM health_data WHERE date < :cutoff") suspend fun deleteOlderThan(cutoff: String)
}
