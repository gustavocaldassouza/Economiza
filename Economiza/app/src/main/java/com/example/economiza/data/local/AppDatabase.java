package com.example.economiza.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.model.Transaction;

import net.zetetic.database.sqlcipher.SupportOpenHelperFactory;

@Database(entities = {
        Transaction.class,
        Category.class,
        Budget.class,
        RecurringPayment.class
}, version = 3, exportSchema = false)
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
            System.loadLibrary("sqlcipher");
            SupportOpenHelperFactory factory = new SupportOpenHelperFactory(keyBytes);

            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "economiza_vault.db")
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        private void seedIfEmpty(@NonNull SupportSQLiteDatabase db) {
                            android.database.Cursor cursor = db.query(
                                    "SELECT COUNT(*) FROM categories", null);
                            boolean empty = cursor.moveToFirst() && cursor.getInt(0) == 0;
                            cursor.close();
                            if (!empty) return;

                            String[][] cats = {
                                    {"Food & Dining", "#FF8A65", "ic_food"},
                                    {"Transport",     "#3D8BFF", "ic_transport"},
                                    {"Utilities",     "#FFD54F", "ic_utilities"},
                                    {"Healthcare",    "#81C784", "ic_health"},
                                    {"Entertainment", "#CE93D8", "ic_entertainment"},
                                    {"Shopping",      "#F48FB1", "ic_shopping"},
                                    {"Savings",       "#00D084", "ic_savings"},
                                    {"Other",         "#8E97A8", "ic_other"},
                            };
                            for (String[] c : cats) {
                                db.execSQL(
                                    "INSERT INTO categories (name, color_hex, icon_name) VALUES (?, ?, ?)",
                                    new Object[]{c[0], c[1], c[2]});
                            }
                        }

                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            seedIfEmpty(db);
                        }

                        @Override
                        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                            seedIfEmpty(db);
                        }

                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            seedIfEmpty(db);
                        }
                    })
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
