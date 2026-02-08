package com.example.pocketplan;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pocketplan.models.Transaction;

import java.util.ArrayList;
import java.util.List;

// ✅ FIX: import R
import com.example.pocketplan.R;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PocketPlan.db";
    private static final int DATABASE_VERSION = 3;

    // ================= TRANSACTIONS TABLE =================
    public static final String TABLE_TRANSACTIONS = "transactions";

    // ================= SALARY TABLE =================
    public static final String TABLE_SALARY = "salary";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // -------- TRANSACTIONS TABLE --------
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTIONS + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "category TEXT," +
                        "amount REAL," +
                        "note TEXT," +
                        "type TEXT," +
                        "time LONG" +
                        ")"
        );

        // -------- SALARY TABLE --------
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_SALARY + "(" +
                        "id INTEGER PRIMARY KEY," +
                        "amount REAL" +
                        ")"
        );

        // ✅ Ensure salary row exists
        db.execSQL(
                "INSERT OR IGNORE INTO " + TABLE_SALARY + " (id, amount) VALUES (1, 0)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // ❌ DO NOT DROP salary (causes reset)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);

        // Recreate safely
        onCreate(db);
    }

    // =====================================================
    // TRANSACTIONS
    // =====================================================

    public List<Transaction> getAllTransactions() {

        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY time DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        R.drawable.ic_category_food,
                        R.color.category_default_bg
                );
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return transactions;
    }

    // ================= TOTAL INCOME =================
    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;

        Cursor cursor = db.rawQuery(
                "SELECT IFNULL(SUM(amount),0) FROM " + TABLE_TRANSACTIONS + " WHERE type='INCOME'",
                null
        );

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        return total;
    }

    // ================= TOTAL EXPENSE =================
    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;

        Cursor cursor = db.rawQuery(
                "SELECT IFNULL(SUM(amount),0) FROM " + TABLE_TRANSACTIONS + " WHERE type='EXPENSE'",
                null
        );

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        return total;
    }

    // =====================================================
    // SALARY
    // =====================================================

    public void saveSalary(double salary) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "UPDATE " + TABLE_SALARY + " SET amount = ? WHERE id = 1",
                new Object[]{salary}
        );
    }

    public double getSalary() {
        SQLiteDatabase db = this.getReadableDatabase();
        double salary = 0;

        Cursor cursor = db.rawQuery(
                "SELECT amount FROM " + TABLE_SALARY + " WHERE id = 1",
                null
        );

        if (cursor.moveToFirst()) {
            salary = cursor.getDouble(0);
        }

        cursor.close();
        return salary;
    }
}
