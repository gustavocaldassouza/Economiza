# Economiza 💰

A privacy-first personal finance app for Android. All data lives **on your device, encrypted**, with no cloud sync and no accounts.

---

## Features

| Feature | Description |
|---|---|
| **Encrypted Vault** | All financial data is stored in an AES-256 encrypted SQLite database, unlocked only by your master password. |
| **Transactions** | Log income and expenses, edit them by tap, delete by swipe. |
| **Categories** | Colour-coded, icon categories assignable to each transaction. |
| **Budgets** | Set monthly spending limits per category with a live progress bar. |
| **Recurring Payments** | Scheduled payments auto-post when the app is unlocked. |
| **Dashboard Charts** | Pie chart, bar chart & summary cards built with MPAndroidChart. |
| **Export** | Download your full transaction history as a **PDF** or **CSV** directly to your Downloads folder. |

---

## Architecture

### Overview: Clean Architecture + MVVM

The project is structured around three strict layers:

```
┌─────────────────────────────────────────────────┐
│  UI  (Activities · Fragments · Adapters)         │
│  Observes LiveData. Never touches the DB.        │
├─────────────────────────────────────────────────┤
│  Domain  (Use Cases · Models · Repository Interfaces) │
│  Pure Java. No Android imports. Business logic.  │
├─────────────────────────────────────────────────┤
│  Data  (Room DAOs · Repository Impls · VaultManager) │
│  Knows about Android & SQLite. Nothing else.     │
└─────────────────────────────────────────────────┘
```

**Why this layering?**
The Domain layer has zero Android dependencies, making business rules easy to understand and test in isolation. The UI only observes `LiveData` — it cannot mutate state directly — which eliminates an entire class of race-condition bugs.

---

### Dependency Flow

```
EconomizaApp (Application)
  └─ initDependencies(keyBytes)          ← called after vault unlock
       ├─ AppDatabase (Room + SQLCipher)
       ├─ Repository implementations
       ├─ Use Cases (one class per action)
       └─ ViewModelFactory
            └─ Injects use cases into ViewModels
```

`EconomizaApp` owns the entire dependency graph and is the single source of truth for all live objects. `lockVault()` sets every reference to `null` and closes the DB — no data leaks into memory when the app backgrounds.

---

### Security: Vault System

| Component | Mechanism |
|---|---|
| **Key derivation** | PBKDF2-HMAC-SHA256, 310 000 iterations (OWASP 2023 recommendation) |
| **Salt** | 16 random bytes generated with `SecureRandom`, stored in `SharedPreferences` |
| **Verification** | A separate PBKDF2 hash (domain-separated salt) is stored to verify the password without exposing the DB key |
| **Database** | Room backed by **SQLCipher** (`net.zetetic:android-database-sqlcipher`) — the entire `.db` file is AES-256 encrypted at rest |
| **Key lifetime** | The raw key bytes live only in memory and are discarded on app background — never written to disk |
| **Timing safety** | Password comparison uses constant-time equality to prevent timing attacks |

The password is **never stored**. On every unlock, the key is re-derived from `password + salt` and used to open the database. If the password is wrong, the derived key won't decrypt the DB.

---

### ViewModel & LiveData Pattern

Every screen has a dedicated `ViewModel` injected via a shared `ViewModelFactory`:

```
ViewModelFactory.create(Class)
  ├─ TransactionViewModel  → wraps CRUD use cases, exposes LiveData<List<Transaction>>
  ├─ DashboardViewModel    → aggregates totals + category data for charts
  ├─ BudgetViewModel       → budget CRUD + computes spending vs. limit
  ├─ RecurringPaymentViewModel → manages scheduled payments
  └─ ExportViewModel       → runs export on a background thread, posts ExportResult state
```

**Why a manual factory instead of Hilt/Dagger?**
All use cases are instantiated in `EconomizaApp.initDependencies()` — after the vault is unlocked and the DB key is available. Dependency injection frameworks initialise at app start and can't gate object creation on runtime authentication. The manual factory gives us explicit control over the moment dependencies are constructed.

**Why `LiveData` and not `Flow`?**
The codebase is pure Java. `LiveData` integrates cleanly with the Android lifecycle without requiring Kotlin coroutines or a reactive framework, which would add significant complexity for a Java project.

---

### Data Export

The export system follows a clean pipeline:

```
ExportViewModel.exportToPdf(tempFile)
  └─ Background thread → ExportDataUseCase.executeToPdf(file)
       └─ repository.getAllTransactionsSync()   ← synchronous DAO query
       └─ iText 7 document builder
            ├─ Teal-branded header (app name, transaction count, date)
            ├─ Summary cards (Income · Expenses · Net Balance)
            ├─ Alternating-row table with colour-coded amounts
            └─ Footer with generation timestamp + page numbers
  └─ ExportResult(SUCCESS, file) posted to LiveData
TransactionsFragment observes →
  └─ saveToDownloads(file, format)
       ├─ API 29+: MediaStore.Downloads (no storage permission required)
       └─ API 24–28: Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
  └─ Snackbar "Saved to Downloads ✓" with "Share" action
```

**Why MediaStore for saving?**
`ACTION_SEND` (share intent) only displays share targets — it does not offer a "save to device" option reliably across OEMs and emulators. `MediaStore.Downloads` on API 29+ writes directly to the public Downloads folder without requiring the `WRITE_EXTERNAL_STORAGE` permission, which was deprecated in API 29.

**Why iText 7 and not Android Canvas?**
iText 7 provides a high-level, paginated document model with proper text flow, table layout, and colour management. Replicating that with `android.graphics.Canvas` would require significant manual geometry, clipping and page-break logic with far greater fragility.

---

## Project Structure

```
app/src/main/java/com/example/economiza/
│
├── EconomizaApp.java              Application — owns the DI graph
├── MainActivity.java              Host activity for Navigation Component
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.java       Room + SQLCipher database singleton
│   │   ├── VaultManager.java      PBKDF2 key derivation & vault lifecycle
│   │   ├── TransactionDao.java
│   │   ├── CategoryDao.java
│   │   ├── BudgetDao.java
│   │   └── RecurringPaymentDao.java
│   └── repository/
│       ├── TransactionRepositoryImpl.java
│       ├── CategoryRepositoryImpl.java
│       ├── BudgetRepositoryImpl.java
│       └── RecurringPaymentRepositoryImpl.java
│
├── domain/
│   ├── model/                     Plain Java entities (also Room @Entity)
│   │   ├── Transaction.java
│   │   ├── Category.java
│   │   ├── Budget.java
│   │   └── RecurringPayment.java
│   ├── repository/                Interfaces only — no Android imports
│   │   └── TransactionRepository.java  (+ others)
│   └── usecase/                   One class per user action
│       ├── AddTransactionUseCase.java
│       ├── ExportDataUseCase.java       ← PDF + CSV generation
│       ├── ProcessRecurringPaymentsUseCase.java
│       └── ...
│
└── ui/
    ├── activities/
    │   ├── OnboardingActivity.java
    │   ├── CreateVaultActivity.java
    │   ├── UnlockVaultActivity.java
    │   ├── AddTransactionActivity.java
    │   └── ManageCategoriesActivity.java
    ├── fragments/
    │   ├── DashboardFragment.java
    │   ├── TransactionsFragment.java    ← export button + save-to-downloads
    │   ├── BudgetFragment.java
    │   ├── RecurringFragment.java
    │   └── SettingsFragment.java
    ├── adapter/
    │   └── TransactionAdapter.java
    └── viewmodel/
        ├── ViewModelFactory.java
        ├── TransactionViewModel.java
        ├── DashboardViewModel.java
        ├── BudgetViewModel.java
        ├── RecurringPaymentViewModel.java
        └── ExportViewModel.java
```

---

## Key Dependencies

| Library | Version | Purpose |
|---|---|---|
| `androidx.room` | 2.6.1 | ORM and compile-time SQL validation |
| `net.zetetic:android-database-sqlcipher` | 4.5.4 | AES-256 database encryption |
| `com.itextpdf:itext7-core` | 7.2.5 | Paginated PDF generation with layouts and fonts |
| `com.opencsv:opencsv` | 5.9 | RFC-4180 compliant CSV writing |
| `com.github.PhilJay:MPAndroidChart` | v3.1.0 | Pie and bar charts on the dashboard |
| `androidx.navigation` | 2.7.7 | Single-Activity navigation graph |
| `androidx.lifecycle` | 2.8.4 | ViewModel + LiveData |

---

## Build Requirements

- **Android Studio** Ladybug or later (uses embedded JDK 21)
- **minSdk** 24 (Android 7.0)
- **targetSdk** 36 (Android 16)
- **Java** 11 source / target compatibility

> **Note:** Building from the terminal requires the Android Studio embedded JDK.  
> Use **Build → Make Project** inside Android Studio for reliable builds.
