package model;

public class Expense {

    private int id;
    private double amount;
    private String category;
    private String date;
    private String description;

    public Expense(double amount, String category, String date, String description) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Getters
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
}
