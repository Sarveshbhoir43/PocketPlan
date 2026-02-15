package com.example.pocketplan.models;

import com.example.pocketplan.R;

public class Transaction {
    private int id;
    private String title;
    private String category;
    private double amount;
    private String note;
    private String type; // "INCOME" or "EXPENSE"
    private long timestamp;

    public Transaction(int id, String title, String category, double amount,
                       String note, String type, long timestamp) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }

    public boolean isIncome() {
        return "INCOME".equalsIgnoreCase(type);
    }

    // Category icon resource
    public int getCategoryIconRes() {
        switch (category) {
            case "Food & Dining":
                return android.R.drawable.ic_menu_recent_history;
            case "Transportation":
                return android.R.drawable.ic_menu_directions;
            case "Shopping":
                return android.R.drawable.ic_menu_gallery;
            case "Entertainment":
                return android.R.drawable.ic_media_play;
            case "Bills & Utilities":
                return android.R.drawable.ic_menu_agenda;
            case "Healthcare":
                return android.R.drawable.ic_menu_add;
            case "Education":
                return android.R.drawable.ic_menu_info_details;
            case "Travel":
                return android.R.drawable.ic_menu_compass;
            case "Groceries":
                return android.R.drawable.ic_menu_today;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }

    // Category color resource
    public int getCategoryColorRes() {
        switch (category) {
            case "Food & Dining":
                return R.color.category_food;
            case "Transportation":
                return R.color.category_transport;
            case "Shopping":
                return R.color.category_shopping;
            case "Entertainment":
                return R.color.category_entertainment;
            case "Bills & Utilities":
                return R.color.category_bills;
            case "Healthcare":
                return R.color.category_health;
            case "Education":
                return R.color.category_education;
            case "Travel":
                return R.color.category_travel;
            case "Groceries":
                return R.color.category_groceries;
            default:
                return R.color.category_default;
        }
    }
}