package com.example.pocketplan.models;

import java.io.Serializable;

public class Transaction implements Serializable {
    
    private String id;
    private String title;
    private String category;
    private double amount;
    private String type; // "INCOME" or "EXPENSE"
    private long timestamp;
    private String note;
    private int categoryIconRes;
    private int categoryColorRes;

    public Transaction() {
        // Default constructor
    }

    public Transaction(String id, String title, String category, double amount, 
                      String type, long timestamp, String note, 
                      int categoryIconRes, int categoryColorRes) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.note = note;
        this.categoryIconRes = categoryIconRes;
        this.categoryColorRes = categoryColorRes;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getNote() { return note; }
    public int getCategoryIconRes() { return categoryIconRes; }
    public int getCategoryColorRes() { return categoryColorRes; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setType(String type) { this.type = type; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setNote(String note) { this.note = note; }
    public void setCategoryIconRes(int categoryIconRes) { this.categoryIconRes = categoryIconRes; }
    public void setCategoryColorRes(int categoryColorRes) { this.categoryColorRes = categoryColorRes; }

    // Helper method to check if transaction is income
    public boolean isIncome() {
        return "INCOME".equalsIgnoreCase(type);
    }

    // Helper method to check if transaction is expense
    public boolean isExpense() {
        return "EXPENSE".equalsIgnoreCase(type);
    }
}
