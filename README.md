
# FitLife Pro — Android App

App Android per la gestione di programmi di fitness, nutrizione, integratori e idratazione.
Importa il piano dal template Excel e gestisci ogni aspetto del tuo allenamento.

## Stack Tecnologico

| Layer | Tecnologia |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architettura | MVVM + StateFlow |
| DI | Hilt |
| Database | Room (SQLite) |
| Navigazione | Navigation Compose |
| Background | WorkManager |
| Excel Import | Apache POI |
| Notifiche | Android NotificationManager + WorkManager |
| Async | Kotlin Coroutines + Flow |

## Requisiti

- Android Studio Hedgehog o superiore
- JDK 17
- Android SDK API 34
- Gradle 8.x

## Build

```bash
# Clone e apri in Android Studio
git clone <repo>
cd fitlifepro

# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Struttura Progetto

```
app/src/main/java/it/fitlifepro/app/
├── data/
│   ├── model/          # Entità Room (Program, Exercise, MealPlan, ...)
│   ├── db/             # DAO + AppDatabase
│   └── repository/     # FitLifeRepository
├── di/                 # Hilt AppModule
├── excel/              # ExcelImporter (Apache POI)
├── health/             # HealthConnectManager
├── worker/             # NotificationWorker, BootReceiver
├── viewmodel/          # Dashboard, Workout, Hydration, Import VM
├── ui/
│   ├── theme/          # FitLifeTheme, colori, tipografia
│   ├── components/     # SectionCard, StatChip, LinearProgressRow ...
│   ├── screens/
│   │   ├── dashboard/  # DashboardScreen
│   │   ├── workout/    # WorkoutScreen
│   │   ├── nutrition/  # NutritionScreen
│   │   ├── supplements/# SupplementsScreen
│   │   ├── hydration/  # HydrationScreen
│   │   ├── history/    # HistoryScreen
│   │   └── import/     # ImportScreen
│   └── Navigation.kt
├── MainActivity.kt
└── FitLifeApplication.kt
```

## Import Programma Excel

1. Compila il template `FitLife_Pro_Programma_Template.xlsx`
2. Apri FitLife Pro → tab **Importa**
3. Seleziona il file Excel
4. Verifica l'anteprima e conferma



## File allegati

- `FitLife_Pro_Programma_Template.xlsx` — template Excel da compilare
- `FitLife_Pro_Documento_Completo.pdf` — analisi tecnica e mockup completi
