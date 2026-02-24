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
    private static final int DATABASE_VERSION = 3; // Bumped for user_id migration

    // Users Table
    public static final String TABLE_USERS    = "users";
    public static final String COL_USER_ID    = "id";
    public static final String COL_USER_NAME  = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASS  = "password";

    // Transactions Table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_ID        = "id";
    public static final String COL_TITLE     = "title";
    public static final String COL_CATEGORY  = "category";
    public static final String COL_AMOUNT    = "amount";
    public static final String COL_NOTE      = "note";
    public static final String COL_TYPE      = "type";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_USER_FK   = "user_id";

    private static final String TABLE_SALARY   = "salary";
    private static final String TABLE_SETTINGS = "settings";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME  + " TEXT NOT NULL, " +
                COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASS  + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE     + " TEXT NOT NULL, " +
                COL_CATEGORY  + " TEXT NOT NULL, " +
                COL_AMOUNT    + " REAL NOT NULL, " +
                COL_NOTE      + " TEXT, " +
                COL_TYPE      + " TEXT NOT NULL, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_USER_FK   + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SALARY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL DEFAULT 0, updated_at INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " (" +
                "key TEXT, value REAL, user_id INTEGER)");

        db.execSQL("INSERT INTO " + TABLE_SALARY + " (amount, updated_at) VALUES (0, " + System.currentTimeMillis() + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COL_USER_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_NAME  + " TEXT NOT NULL, " +
                    COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COL_USER_PASS  + " TEXT NOT NULL)");

            // Seed a legacy user so existing transactions (user_id=1) still link correctly
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS +
                    " (id, name, email, password) VALUES (1, 'User', 'legacy@pocketplan.app', 'legacy')");

            try {
                db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS +
                        " ADD COLUMN " + COL_USER_FK + " INTEGER NOT NULL DEFAULT 1");
            } catch (Exception e) {
                Log.w(TAG, "user_id column may already exist");
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS +
                    " (key TEXT, value REAL, user_id INTEGER)");
        }
    }

    // ═══ USER METHODS ═══════════════════════════════════════════════════════

    public long registerUser(String name, String email, String password) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_USER_NAME,  name);
            cv.put(COL_USER_EMAIL, email.toLowerCase().trim());
            cv.put(COL_USER_PASS,  password);
            return getWritableDatabase().insert(TABLE_USERS, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "registerUser: " + e.getMessage());
            return -1;
        }
    }

    public int loginUser(String email, String password) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS +
                            " WHERE " + COL_USER_EMAIL + " = ? AND " + COL_USER_PASS + " = ?",
                    new String[]{email.toLowerCase().trim(), password});
            if (c.moveToFirst()) return c.getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "loginUser: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return -1;
    }

    public boolean isEmailTaken(String email) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + " = ?",
                    new String[]{email.toLowerCase().trim()});
            return c.moveToFirst();
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.close();
        }
    }

    public String getUserName(int userId) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT " + COL_USER_NAME + " FROM " + TABLE_USERS +
                            " WHERE " + COL_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});
            if (c.moveToFirst()) return c.getString(0);
        } catch (Exception e) {
            Log.e(TAG, "getUserName: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return "User";
    }

    // ═══ TRANSACTION METHODS — scoped to userId ══════════════════════════════

    public List<Transaction> getAllTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT * FROM " + TABLE_TRANSACTIONS +
                            " WHERE " + COL_USER_FK + " = ?" +
                            " ORDER BY " + COL_TIMESTAMP + " DESC",
                    new String[]{String.valueOf(userId)});
            if (c.moveToFirst()) {
                do {
                    list.add(new Transaction(
                            c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                            c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                            c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)),
                            c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT)),
                            c.getString(c.getColumnIndexOrThrow(COL_NOTE)),
                            c.getString(c.getColumnIndexOrThrow(COL_TYPE)),
                            c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP))
                    ));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllTransactions: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return list;
    }

    public long addTransaction(String title, String category, double amount,
                               String note, String type, long timestamp, int userId) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_TITLE,     title);
            cv.put(COL_CATEGORY,  category);
            cv.put(COL_AMOUNT,    amount);
            cv.put(COL_NOTE,      note);
            cv.put(COL_TYPE,      type);
            cv.put(COL_TIMESTAMP, timestamp);
            cv.put(COL_USER_FK,   userId);
            return getWritableDatabase().insert(TABLE_TRANSACTIONS, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "addTransaction: " + e.getMessage());
            return -1;
        }
    }

    public boolean deleteTransaction(int id) {
        return getWritableDatabase().delete(TABLE_TRANSACTIONS,
                COL_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public double getTotalIncome(int userId) {
        return sumByType("INCOME", userId);
    }

    public double getTotalExpense(int userId) {
        return sumByType("EXPENSE", userId);
    }

    private double sumByType(String type, int userId) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                            " WHERE " + COL_TYPE + " = ? AND " + COL_USER_FK + " = ?",
                    new String[]{type, String.valueOf(userId)});
            if (c.moveToFirst()) return c.getDouble(0);
        } catch (Exception e) {
            Log.e(TAG, "sumByType: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    public boolean clearAllTransactions(int userId) {
        try {
            getWritableDatabase().delete(TABLE_TRANSACTIONS,
                    COL_USER_FK + " = ?", new String[]{String.valueOf(userId)});
            return true;
        } catch (Exception e) {
            Log.e(TAG, "clearAllTransactions: " + e.getMessage());
            return false;
        }
    }

    public double getExpenseByCategory(String category, int userId) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                            " WHERE " + COL_TYPE + " = 'EXPENSE' AND " + COL_CATEGORY + " = ?" +
                            " AND " + COL_USER_FK + " = ?",
                    new String[]{category, String.valueOf(userId)});
            if (c.moveToFirst() && !c.isNull(0)) return c.getDouble(0);
        } catch (Exception e) {
            Log.e(TAG, "getExpenseByCategory: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    public double getMonthlyExpense(int year, int month, int userId) {
        java.util.Calendar s = java.util.Calendar.getInstance();
        s.set(year, month, 1, 0, 0, 0); s.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Calendar e = (java.util.Calendar) s.clone();
        e.add(java.util.Calendar.MONTH, 1);
        return getExpenseForRange(s.getTimeInMillis(), e.getTimeInMillis(), userId);
    }

    public double getExpenseForRange(long startTime, long endTime, int userId) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                            " WHERE " + COL_TYPE + " = 'EXPENSE'" +
                            " AND " + COL_TIMESTAMP + " >= ? AND " + COL_TIMESTAMP + " < ?" +
                            " AND " + COL_USER_FK + " = ?",
                    new String[]{String.valueOf(startTime), String.valueOf(endTime), String.valueOf(userId)});
            if (c.moveToFirst() && !c.isNull(0)) return c.getDouble(0);
        } catch (Exception ex) {
            Log.e(TAG, "getExpenseForRange: " + ex.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    // ═══ SALARY / SETTINGS — scoped to user ══════════════════════════════════

    public double getSalary(int userId) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT value FROM " + TABLE_SETTINGS +
                            " WHERE key = 'salary' AND user_id = ?",
                    new String[]{String.valueOf(userId)});
            if (c.moveToFirst()) return c.getDouble(0);
        } catch (Exception e) {
            Log.e(TAG, "getSalary: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    public boolean setSalary(double salary, int userId) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("key",     "salary");
            cv.put("value",   salary);
            cv.put("user_id", userId);
            int updated = getWritableDatabase().update(TABLE_SETTINGS, cv,
                    "key = 'salary' AND user_id = ?", new String[]{String.valueOf(userId)});
            if (updated == 0) getWritableDatabase().insert(TABLE_SETTINGS, null, cv);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "setSalary: " + e.getMessage());
            return false;
        }
    }
}