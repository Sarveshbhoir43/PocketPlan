package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // Database details
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USER = "users";
    public static final String TABLE_EXPENSE = "expenses";
    public static final String TABLE_BUDGET = "budget";

    // User table columns
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";

    // Expense table columns
    public static final String EXPENSE_ID = "expense_id";
    public static final String EXPENSE_AMOUNT = "amount";
    public static final String EXPENSE_CATEGORY = "category";
    public static final String EXPENSE_DATE = "date";
    public static final String EXPENSE_DESC = "description";
    public static final String EXPENSE_USER_ID = "user_id";

    // Budget table columns
    public static final String BUDGET_MONTH = "month";
    public static final String BUDGET_LIMIT = "limit_amount";
    public static final String BUDGET_USER_ID = "user_id";

    // Create table queries
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_USER + " (" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USER_NAME + " TEXT, " +
                    USER_EMAIL + " TEXT UNIQUE, " +
                    USER_PASSWORD + " TEXT)";

    private static final String CREATE_EXPENSE_TABLE =
            "CREATE TABLE " + TABLE_EXPENSE + " (" +
                    EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EXPENSE_AMOUNT + " REAL, " +
                    EXPENSE_CATEGORY + " TEXT, " +
                    EXPENSE_DATE + " TEXT, " +
                    EXPENSE_DESC + " TEXT, " +
                    EXPENSE_USER_ID + " INTEGER)";

    private static final String CREATE_BUDGET_TABLE =
            "CREATE TABLE " + TABLE_BUDGET + " (" +
                    BUDGET_MONTH + " TEXT, " +
                    BUDGET_LIMIT + " REAL, " +
                    BUDGET_USER_ID + " INTEGER)";

    // Constructor
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_EXPENSE_TABLE);
        db.execSQL(CREATE_BUDGET_TABLE);
    }

    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }

    // ================= USER METHODS =================

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password);

        long result = db.insert(TABLE_USER, null, values);
        return result != -1;
    }

    public int loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + USER_ID + " FROM " + TABLE_USER +
                        " WHERE " + USER_EMAIL + "=? AND " + USER_PASSWORD + "=?",
                new String[]{email, password});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // ================= EXPENSE METHODS =================

    public boolean addExpense(double amount, String category, String date, String desc, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EXPENSE_AMOUNT, amount);
        values.put(EXPENSE_CATEGORY, category);
        values.put(EXPENSE_DATE, date);
        values.put(EXPENSE_DESC, desc);
        values.put(EXPENSE_USER_ID, userId);

        long result = db.insert(TABLE_EXPENSE, null, values);
        return result != -1;
    }

    public double getTotalExpense(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSE +
                        " WHERE " + EXPENSE_USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // ================= BUDGET METHODS =================

    public void setBudget(String month, double limit, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BUDGET_MONTH, month);
        values.put(BUDGET_LIMIT, limit);
        values.put(BUDGET_USER_ID, userId);

        db.insert(TABLE_BUDGET, null, values);
    }

    public double getBudget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + BUDGET_LIMIT + " FROM " + TABLE_BUDGET +
                        " WHERE " + BUDGET_USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        double limit = 0;
        if (cursor.moveToFirst()) {
            limit = cursor.getDouble(0);
        }
        cursor.close();
        return limit;
    }
}

