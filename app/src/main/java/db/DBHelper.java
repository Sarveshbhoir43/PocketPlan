package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pocketplan.db";
    private static final int DATABASE_VERSION = 1;

    // User table
    public static final String TABLE_USER = "users";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";
    public static final String USER_PHONE = "phone";

    // Expense table
    public static final String TABLE_EXPENSE = "expenses";
    public static final String EXPENSE_ID = "expense_id";
    public static final String EXPENSE_AMOUNT = "amount";
    public static final String EXPENSE_CATEGORY = "category";
    public static final String EXPENSE_DATE = "date";
    public static final String EXPENSE_DESC = "description";
    public static final String EXPENSE_USER_ID = "user_id";

    // Budget table
    public static final String TABLE_BUDGET = "budget";
    public static final String BUDGET_MONTH = "month";
    public static final String BUDGET_LIMIT = "limit_amount";
    public static final String BUDGET_USER_ID = "user_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_USER + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_NAME + " TEXT, " +
                        USER_EMAIL + " TEXT UNIQUE, " +
                        USER_PASSWORD + " TEXT, " +
                        USER_PHONE + " TEXT UNIQUE)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_EXPENSE + " (" +
                        EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EXPENSE_AMOUNT + " REAL, " +
                        EXPENSE_CATEGORY + " TEXT, " +
                        EXPENSE_DATE + " TEXT, " +
                        EXPENSE_DESC + " TEXT, " +
                        EXPENSE_USER_ID + " INTEGER)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_BUDGET + " (" +
                        BUDGET_MONTH + " TEXT, " +
                        BUDGET_LIMIT + " REAL, " +
                        BUDGET_USER_ID + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }

    // ===== USER =====
    public boolean isUserExists(String email, String phone) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE email=? OR phone=?",
                new String[]{email, phone});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean registerUser(String name, String email, String password, String phone) {
        if (isUserExists(email, phone)) return false;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USER_NAME, name);
        cv.put(USER_EMAIL, email);
        cv.put(USER_PASSWORD, password);
        cv.put(USER_PHONE, phone);

        boolean success = db.insert(TABLE_USER, null, cv) != -1;
        db.close();
        return success;
    }

    public int loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT user_id FROM " + TABLE_USER + " WHERE email=? AND password=?",
                new String[]{email, password}
        );
        int id = -1;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        return id;
    }

    // ===== EXPENSE =====
    public boolean addExpense(double amount, String category, String date, String desc, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(EXPENSE_AMOUNT, amount);
        cv.put(EXPENSE_CATEGORY, category);
        cv.put(EXPENSE_DATE, date);
        cv.put(EXPENSE_DESC, desc);
        cv.put(EXPENSE_USER_ID, userId);

        boolean success = db.insert(TABLE_EXPENSE, null, cv) != -1;
        db.close();
        return success;
    }

    public double getTotalExpense(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    // ===== BUDGET =====
    public void setBudget(String month, double limit, int userId) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_BUDGET + " WHERE month=? AND user_id=?",
                new String[]{month, String.valueOf(userId)});
        ContentValues cv = new ContentValues();
        cv.put(BUDGET_MONTH, month);
        cv.put(BUDGET_LIMIT, limit);
        cv.put(BUDGET_USER_ID, userId);

        if (c.moveToFirst()) {
            db.update(TABLE_BUDGET, cv, "month=? AND user_id=?", new String[]{month, String.valueOf(userId)});
        } else {
            db.insert(TABLE_BUDGET, null, cv);
        }

        c.close();
        db.close();
    }

    public double getBudget(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT limit_amount FROM " + TABLE_BUDGET + " WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        double limit = 0;
        if (c.moveToFirst()) limit = c.getDouble(0);
        c.close();
        return limit;
    }

    public boolean isBudgetExceeded(int userId) {
        return getTotalExpense(userId) > getBudget(userId);
    }

}
