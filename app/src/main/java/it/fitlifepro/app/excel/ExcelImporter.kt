
package it.fitlifepro.app.excel

import android.content.Context
import android.net.Uri
import it.fitlifepro.app.data.model.*
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import javax.inject.Inject

data class ImportResult(
    val program: Program,
    val trainingDays: List<TrainingDay>,
    val exercises: List<Exercise>,
    val meals: List<MealPlan>,
    val supplements: List<Supplement>,
    val hydrationConfig: HydrationConfig?,
    val hydrationReminders: List<HydrationReminder>,
    val errors: List<String> = emptyList()
)

class ExcelImporter @Inject constructor() {

    fun import(context: Context, uri: Uri): ImportResult {
        val errors = mutableListOf<String>()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val wb = WorkbookFactory.create(stream)

            val program = parseProgram(wb.getSheet("PROGRAMMA"), errors)
                ?: return ImportResult(Program(name="?",goal="",startDate="",endDate="",
                    athleteName="",weightKg=0f,heightCm=0,level="",daysPerWeek=0),
                    emptyList(), emptyList(), emptyList(), emptyList(), null, emptyList(),
                    listOf("Foglio PROGRAMMA non trovato o vuoto"))

            val trainingDays = parseTrainingDays(wb.getSheet("ALLENAMENTO"), errors)
            val exercises    = parseExercises(wb.getSheet("ESERCIZI"), trainingDays, errors)
            val meals        = parseMeals(wb.getSheet("MENU_SETTIMANALE"), errors)
            val supplements  = parseSupplements(wb.getSheet("INTEGRATORI"), errors)
            val (hydCfg, hydRem) = parseHydration(wb.getSheet("IDRATAZIONE"), errors)

            wb.close()
            return ImportResult(program, trainingDays, exercises, meals, supplements, hydCfg, hydRem, errors)
        }
        return ImportResult(Program(name="?",goal="",startDate="",endDate="",
            athleteName="",weightKg=0f,heightCm=0,level="",daysPerWeek=0),
            emptyList(), emptyList(), emptyList(), emptyList(), null, emptyList(),
            listOf("Impossibile aprire il file"))
    }

    private fun parseProgram(sheet: org.apache.poi.ss.usermodel.Sheet?, errors: MutableList<String>): Program? {
        if (sheet == null) return null
        val data = mutableMapOf<String,String>()
        for (i in 3 until sheet.lastRowNum + 1) {
            val row = sheet.getRow(i) ?: continue
            val key = row.getCell(0)?.toString()?.trim() ?: continue
            val value = row.getCell(1)?.toString()?.trim() ?: ""
            data[key] = value
        }
        return Program(
            name        = data["nome_programma"] ?: "Programma",
            goal        = data["obiettivo"] ?: "massa",
            startDate   = data["data_inizio"] ?: "",
            endDate     = data["data_fine"] ?: "",
            athleteName = data["atleta"] ?: "",
            weightKg    = data["peso_kg"]?.toFloatOrNull() ?: 0f,
            heightCm    = data["altezza_cm"]?.toDoubleOrNull()?.toInt() ?: 0,
            level       = data["livello"] ?: "intermedio",
            daysPerWeek = data["giorni_settimana"]?.toDoubleOrNull()?.toInt() ?: 4,
            notes       = data["note"] ?: ""
        )
    }

    private fun parseTrainingDays(sheet: org.apache.poi.ss.usermodel.Sheet?, errors: MutableList<String>): List<TrainingDay> {
        if (sheet == null) return emptyList()
        val days = mutableListOf<TrainingDay>()
        for (i in 3 until sheet.lastRowNum + 1) {
            val row = sheet.getRow(i) ?: continue
            val day = row.str(0).ifEmpty { continue }
            days.add(TrainingDay(
                programId    = 0L, // set after program insert
                dayOfWeek    = day,
                sessionType  = row.str(1).ifEmpty { "SALA_PESI" },
                timeHHMM     = row.str(2),
                durationMin  = row.intVal(3),
                activeWeeks  = row.str(4).ifEmpty { "1-4" },
                notes        = row.str(5)
            ))
        }
        return days
    }

    private fun parseExercises(sheet: org.apache.poi.ss.usermodel.Sheet?, days: List<TrainingDay>, errors: MutableList<String>): List<Exercise> {
        if (sheet == null) return emptyList()
        val exs = mutableListOf<Exercise>()
        for (i in 3 until sheet.lastRowNum + 1) {
            val row = sheet.getRow(i) ?: continue
            val day = row.str(0).ifEmpty { continue }
            exs.add(Exercise(
                trainingDayId = 0L, // set after day insert
                order         = row.intVal(1),
                name          = row.str(2),
                muscleGroup   = row.str(3),
                sets          = row.intVal(4),
                reps          = row.intVal(5),
                weightKg      = row.floatVal(6),
                restSec       = row.intVal(7),
                videoUrl      = row.str(8),
                notes         = "day:$day" // temp marker
            ))
        }
        return exs
    }

    private fun parseMeals(sheet: org.apache.poi.ss.usermodel.Sheet?, errors: MutableList<String>): List<MealPlan> {
        if (sheet == null) return emptyList()
        val meals = mutableListOf<MealPlan>()
        for (i in 3 until sheet.lastRowNum + 1) {
            val row = sheet.getRow(i) ?: continue
            val day = row.str(0).ifEmpty { continue }
            meals.add(MealPlan(
                programId    = 0L,
                dayOfWeek    = day,
                mealType     = row.str(1),
                timeHHMM     = row.str(2),
                food1        = row.str(3),
                qty1g        = row.intVal(4),
                food2        = row.str(5),
                qty2g        = row.intVal(6),
                kcalEstimated= row.intVal(7),
                notes        = row.str(8)
            ))
        }
        return meals
    }

    private fun parseSupplements(sheet: org.apache.poi.ss.usermodel.Sheet?, errors: MutableList<String>): List<Supplement> {
        if (sheet == null) return emptyList()
        val sups = mutableListOf<Supplement>()
        for (i in 3 until sheet.lastRowNum + 1) {
            val row = sheet.getRow(i) ?: continue
            val name = row.str(0).ifEmpty { continue }
            sups.add(Supplement(
                programId = 0L,
                name      = name,
                brand     = row.str(1),
                category  = row.str(2),
                dosage    = row.str(3),
                timing    = row.str(4),
                timeHHMM  = row.str(5),
                days      = row.str(6).ifEmpty { "Tutti" },
                takeWith  = row.str(7),
                notes     = row.str(8)
            ))
        }
        return sups
    }

    private fun parseHydration(sheet: org.apache.poi.ss.usermodel.Sheet?, errors: MutableList<String>): Pair<HydrationConfig?, List<HydrationReminder>> {
        if (sheet == null) return Pair(null, emptyList())
        val cfg = mutableMapOf<String,String>()
        for (i in 4 until 14) {
            val row = sheet.getRow(i) ?: continue
            val k = row.str(0).ifEmpty { continue }
            cfg[k] = row.str(1)
        }
        val config = HydrationConfig(
            programId          = 0L,
            dailyGoalMl        = cfg["obiettivo_acqua_ml_giorno"]?.toDoubleOrNull()?.toInt() ?: 3000,
            extraTrainingMl    = cfg["extra_allenamento_ml"]?.toDoubleOrNull()?.toInt() ?: 500,
            wakeUpDoseMl       = cfg["dose_sveglia_ml"]?.toDoubleOrNull()?.toInt() ?: 250,
            wakeUpTime         = cfg["orario_sveglia"] ?: "07:00",
            reminderIntervalMin= cfg["intervallo_reminder_min"]?.toDoubleOrNull()?.toInt() ?: 90,
            glassStandardMl    = cfg["bicchiere_standard_ml"]?.toDoubleOrNull()?.toInt() ?: 250,
            notificationsActive= (cfg["notifiche_attive"] ?: "SI").equals("SI", true),
            stopNotificationsAt= cfg["orario_stop_notifiche"] ?: "22:30"
        )
        val reminders = mutableListOf<HydrationReminder>()
        for (i in 16 until sheet.lastRowNum + 1) {
            val row = sheet.getRow(i) ?: continue
            val label = row.str(0).ifEmpty { continue }
            reminders.add(HydrationReminder(
                configId  = 0L,
                label     = label,
                timeHHMM  = row.str(1),
                amountMl  = row.intVal(2),
                days      = row.str(3).ifEmpty { "Tutti" },
                active    = row.str(4).equals("SI", true),
                notes     = row.str(5)
            ))
        }
        return Pair(config, reminders)
    }

    private fun Row.str(col: Int) = getCell(col)?.toString()?.trim() ?: ""
    private fun Row.intVal(col: Int) = getCell(col)?.toString()?.toDoubleOrNull()?.toInt() ?: 0
    private fun Row.floatVal(col: Int) = getCell(col)?.toString()?.toFloatOrNull() ?: 0f
}
