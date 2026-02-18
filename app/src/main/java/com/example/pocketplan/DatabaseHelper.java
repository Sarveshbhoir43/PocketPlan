package com.example.pocketplan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pocketplan.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "pocketplan.db";
    private static final int DATABASE_VERSION = 2; // Increased for salary table

    // Transactions Table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_CATEGORY = "category";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_NOTE = "note";
    public static final String COL_TYPE = "type"; // INCOME or EXPENSE
    public static final String COL_TIMESTAMP = "timestamp";

    // Salary Table
    private static final String TABLE_SALARY = "salary";
    private static final String COL_SALARY_ID = "id";
    private static final String COL_SALARY_AMOUNT = "amount";
    private static final String COL_SALARY_UPDATED = "updated_at";
    private static final String TABLE_SETTINGS = "settings";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Transactions Table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_NOTE + " TEXT, " +
                COL_TYPE + " TEXT NOT NULL, " +
                COL_TIMESTAMP + " INTEGER NOT NULL)";
        db.execSQL(createTransactionsTable);

        // Create Salary Table
        String createSalaryTable = "CREATE TABLE " + TABLE_SALARY + " (" +
                COL_SALARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SALARY_AMOUNT + " REAL DEFAULT 0, " +
                COL_SALARY_UPDATED + " INTEGER)";
        db.execSQL(createSalaryTable);

        // Insert default salary row
        db.execSQL("INSERT INTO " + TABLE_SALARY + " (amount, updated_at) VALUES (0, " + System.currentTimeMillis() + ")");

        Log.d(TAG, "Database created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add salary table if upgrading from version 1
            String createSalaryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SALARY + " (" +
                    COL_SALARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SALARY_AMOUNT + " REAL DEFAULT 0, " +
                    COL_SALARY_UPDATED + " INTEGER)";
            db.execSQL(createSalaryTable);
            db.execSQL("INSERT INTO " + TABLE_SALARY + " (amount, updated_at) VALUES (0, " + System.currentTimeMillis() + ")");
        }
    }

    // ==================== TRANSACTION METHODS ====================

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_TRANSACTIONS, null, null, null, null, null, COL_TIMESTAMP + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                    );
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Retrieved " + transactions.size() + " transactions");

        } catch (Exception e) {
            Log.e(TAG, "Error getting transactions: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return transactions;
    }



    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_TRANSACTIONS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "Deleted transaction ID " + id + ", rows affected: " + rowsDeleted);
        return rowsDeleted > 0;
    }

    // ==================== SALARY METHODS ====================
    
    public long addTransaction(String title, String category, double amount,
                               String note, String type, long timestamp) {
        SQLiteDatabase db = null;
        long result = -1;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("category", category);
            values.put("amount", amount);
            values.put("note", note);
            values.put("type", type);
            values.put("timestamp", timestamp);

            result = db.insert(TABLE_TRANSACTIONS, null, values);

            Log.d("DatabaseHelper", "Transaction added with ID: " + result);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding transaction: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return result;
    }

    /**
     * Get total income from all INCOME transactions
     * @return Total income amount
     */
    public double getTotalIncome() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalIncome = 0;

        try {
            db = this.getReadableDatabase();

            String query = "SELECT SUM(amount) as total FROM " + TABLE_TRANSACTIONS +
                    " WHERE type = 'INCOME'";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalIncome = cursor.getDouble(0);
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting total income: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return totalIncome;
    }

    /**
     * Get total expense from all EXPENSE transactions
     * @return Total expense amount
     */
    public double getTotalExpense() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalExpense = 0;

        try {
            db = this.getReadableDatabase();

            String query = "SELECT SUM(amount) as total FROM " + TABLE_TRANSACTIONS +
                    " WHERE type = 'EXPENSE'";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalExpense = cursor.getDouble(0);
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting total expense: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return totalExpense;
    }

    /**
     * Clear all transactions from the database
     * @return true if successful, false otherwise
     */
    public boolean clearAllTransactions() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // Delete all rows from transactions table
            int rowsDeleted = db.delete(TABLE_TRANSACTIONS, null, null);

            Log.d("DatabaseHelper", "Cleared " + rowsDeleted + " transactions");
            return true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing transactions: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public double getSalary() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double salary = 0;

        try {
            db = this.getReadableDatabase();

            String query = "SELECT value FROM " + TABLE_SETTINGS +
                    " WHERE key = 'salary'";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                salary = cursor.getDouble(0);
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting salary: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return salary;
    }

    /**
     * Set/Update salary in settings table
     * @param salary Salary amount to set
     * @return true if successful, false otherwise
     */
    public boolean setSalary(double salary) {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("key", "salary");
            values.put("value", salary);

            // Try to update first
            int rowsUpdated = db.update(TABLE_SETTINGS, values, "key = ?",
                    new String[]{"salary"});

            // If no rows updated, insert new
            if (rowsUpdated == 0) {
                db.insert(TABLE_SETTINGS, null, values);
            }

            Log.d("DatabaseHelper", "Salary set to: " + salary);
            return true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error setting salary: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }


}