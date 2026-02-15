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

    public double getTotalIncome() {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                            " WHERE " + COL_TYPE + " = 'INCOME'", null);

            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }

            Log.d(TAG, "Total Income: " + total);

        } catch (Exception e) {
            Log.e(TAG, "Error calculating income: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return total;
    }

    public double getTotalExpense() {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                            " WHERE " + COL_TYPE + " = 'EXPENSE'", null);

            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }

            Log.d(TAG, "Total Expense: " + total);

        } catch (Exception e) {
            Log.e(TAG, "Error calculating expense: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return total;
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_TRANSACTIONS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "Deleted transaction ID " + id + ", rows affected: " + rowsDeleted);
        return rowsDeleted > 0;
    }

    // ==================== SALARY METHODS ====================

    public double getSalary() {
        double salary = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT " + COL_SALARY_AMOUNT + " FROM " + TABLE_SALARY + " LIMIT 1", null);

            if (cursor != null && cursor.moveToFirst()) {
                salary = cursor.getDouble(0);
            }

            Log.d(TAG, "Current Salary: " + salary);

        } catch (Exception e) {
            Log.e(TAG, "Error getting salary: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return salary;
    }

    public boolean setSalary(double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_SALARY_AMOUNT, amount);
        values.put(COL_SALARY_UPDATED, System.currentTimeMillis());

        try {
            // Update the first (and only) row
            int rowsUpdated = db.update(TABLE_SALARY, values, COL_SALARY_ID + " = 1", null);

            if (rowsUpdated > 0) {
                Log.d(TAG, "Salary updated to: " + amount);
                return true;
            } else {
                // If no row exists, insert one
                long result = db.insert(TABLE_SALARY, null, values);
                Log.d(TAG, "Salary inserted: " + amount);
                return result != -1;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting salary: " + e.getMessage(), e);
            return false;
        }
    }
}