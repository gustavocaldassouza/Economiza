package com.example.economiza;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@Database(entities = {
        Transaction.class,
        Category.class,
        Budget.class,
        RecurringPayment.class
}, version = 1)
@TypeConverters({com.example.economiza.TypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract RecurringPaymentDao recurringPaymentDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            SQLiteDatabase.loadLibs(context);

            byte[] passphrase = getOrGenerateDatabaseKey(context);
            SupportFactory factory = new SupportFactory(passphrase);

            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "economiza_database")
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    private static byte[] getOrGenerateDatabaseKey(Context context) {
        try {
            androidx.security.crypto.MasterKey masterKey = new androidx.security.crypto.MasterKey.Builder(context)
                    .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                    .build();

            android.content.SharedPreferences sharedPreferences = androidx.security.crypto.EncryptedSharedPreferences.create(
                    context,
                    "secure_db_prefs",
                    masterKey,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String keyString = sharedPreferences.getString("db_key", null);
            if (keyString == null) {
                // Generate a random 256-bit key
                java.security.SecureRandom secureRandom = new java.security.SecureRandom();
                byte[] newKey = new byte[32]; // 256 bits
                secureRandom.nextBytes(newKey);
                
                keyString = android.util.Base64.encodeToString(newKey, android.util.Base64.DEFAULT);
                sharedPreferences.edit().putString("db_key", keyString).apply();
            }

            return android.util.Base64.decode(keyString, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate or retrieve secure database key", e);
        }
    }
}