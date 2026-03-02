package com.example.economiza.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.model.Transaction;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@Database(entities = {
        Transaction.class,
        Category.class,
        Budget.class,
        RecurringPayment.class
}, version = 1, exportSchema = false)
@TypeConverters({ AppTypeConverters.class })
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract TransactionDao transactionDao();

    public abstract CategoryDao categoryDao();

    public abstract BudgetDao budgetDao();

    public abstract RecurringPaymentDao recurringPaymentDao();

    /**
     * Opens (or creates) the encrypted Room database using the raw key bytes
     * derived by {@link VaultManager} from the user's password.
     *
     * This method MUST be called only after the user has authenticated.
     * The caller is responsible for providing a valid key.
     *
     * @param context  application context
     * @param keyBytes 32-byte AES-256 key from PBKDF2
     */
    public static synchronized AppDatabase getInstance(Context context, byte[] keyBytes) {
        if (instance == null) {
            // Load native SQLCipher libraries
            SQLiteDatabase.loadLibs(context);

            SupportFactory factory = new SupportFactory(keyBytes);

            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "economiza_vault.db")
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    /** Closes and destroys the current instance (e.g. on lock/sign-out). */
    public static synchronized void destroyInstance() {
        if (instance != null && instance.isOpen()) {
            instance.close();
        }
        instance = null;
    }
}
