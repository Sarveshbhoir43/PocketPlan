package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pocketplan.adapters.TransactionAdapter;
import com.example.pocketplan.models.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionsActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {

    // UI Components
    private TextView tvTransactionCount;
    private TextView tvTotalBalance;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipIncome, chipExpense;
    private MaterialButton btnSort;
    private RecyclerView rvTransactions;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigation;

    // Data
    private List<Transaction> transactions;
    private TransactionAdapter adapter;
    private String currentFilter = "ALL";
    private String currentSort = "DATE_DESC"; // DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        initializeViews();
        setupRecyclerView();
        loadTransactions();
        setupClickListeners();
        setupBottomNavigation();
        updateUI();
    }

    private void initializeViews() {
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipAll = findViewById(R.id.chipAll);
        chipIncome = findViewById(R.id.chipIncome);
        chipExpense = findViewById(R.id.chipExpense);
        btnSort = findViewById(R.id.btnSort);
        rvTransactions = findViewById(R.id.rvTransactions);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactions, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadTransactions() {
        // TODO: Load from database or SharedPreferences
        // For now, adding sample data
        transactions.clear();
        transactions.addAll(getSampleTransactions());
        adapter.updateTransactions(transactions);
    }

    private void setupClickListeners() {
        // Filter chips
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilter = "ALL";
                adapter.filterByType("ALL");
            } else if (checkedId == R.id.chipIncome) {
                currentFilter = "INCOME";
                adapter.filterByType("INCOME");
            } else if (checkedId == R.id.chipExpense) {
                currentFilter = "EXPENSE";
                adapter.filterByType("EXPENSE");
            }
            updateTransactionCount();
        });

        // Sort button
        btnSort.setOnClickListener(v -> showSortDialog());

        // FAB
        fabAddTransaction.setOnClickListener(v -> {
            // TODO: Open add transaction activity
            Toast.makeText(this, "Add Transaction", Toast.LENGTH_SHORT).show();
        });
    }

    private void showSortDialog() {
        String[] sortOptions = {"Date (Newest First)", "Date (Oldest First)", "Amount (High to Low)", "Amount (Low to High)"};
        int selectedIndex = getSortSelectedIndex();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By");
        builder.setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
            switch (which) {
                case 0:
                    currentSort = "DATE_DESC";
                    adapter.sortByDate(true);
                    break;
                case 1:
                    currentSort = "DATE_ASC";
                    adapter.sortByDate(false);
                    break;
                case 2:
                    currentSort = "AMOUNT_DESC";
                    adapter.sortByAmount(true);
                    break;
                case 3:
                    currentSort = "AMOUNT_ASC";
                    adapter.sortByAmount(false);
                    break;
            }
            dialog.dismiss();
        });
        builder.show();
    }

    private int getSortSelectedIndex() {
        switch (currentSort) {
            case "DATE_DESC": return 0;
            case "DATE_ASC": return 1;
            case "AMOUNT_DESC": return 2;
            case "AMOUNT_ASC": return 3;
            default: return 0;
        }
    }

    private void updateUI() {
        // Calculate totals
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        // Update UI
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", balance));
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.0f", totalIncome));
        tvTotalExpense.setText(String.format(Locale.getDefault(), "₹%.0f", totalExpense));
        updateTransactionCount();

        // Show/hide empty state
        if (transactions.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void updateTransactionCount() {
        int count = adapter.getItemCount();
        String text = count + (count == 1 ? " transaction" : " transactions");
        
        if (!currentFilter.equals("ALL")) {
            text += " (" + currentFilter.toLowerCase() + ")";
        } else {
            text += " this month";
        }
        
        tvTransactionCount.setText(text);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // TODO: Open transaction details activity
        Toast.makeText(this, "Clicked: " + transaction.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_transactions);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    Intent intent = new Intent(TransactionsActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_transactions) {
                    // Already on transactions
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(TransactionsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TransactionsActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    // Sample data generator
    private List<Transaction> getSampleTransactions() {
        List<Transaction> sampleList = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneHour = 1000 * 60 * 60;
        long oneDay = oneHour * 24;

        sampleList.add(new Transaction(
                "1", "Grocery Shopping", "Food & Dining", 2450.00, "EXPENSE",
                now - (2 * oneHour), "Supermarket purchase",
                R.drawable.ic_category_food, R.color.category_food_bg
        ));

        sampleList.add(new Transaction(
                "2", "Salary", "Income", 45000.00, "INCOME",
                now - (1 * oneDay), "Monthly salary",
                R.drawable.ic_category_income, R.color.category_income_bg
        ));

        sampleList.add(new Transaction(
                "3", "Uber Ride", "Transport", 350.00, "EXPENSE",
                now - (1 * oneDay + 5 * oneHour), "Trip to office",
                R.drawable.ic_category_transport, R.color.category_transport_bg
        ));

        sampleList.add(new Transaction(
                "4", "Netflix Subscription", "Entertainment", 649.00, "EXPENSE",
                now - (2 * oneDay), "Monthly subscription",
                R.drawable.ic_category_entertainment, R.color.category_entertainment_bg
        ));

        sampleList.add(new Transaction(
                "5", "Shopping", "Shopping", 5200.00, "EXPENSE",
                now - (3 * oneDay), "Online shopping",
                R.drawable.ic_category_shopping, R.color.category_shopping_bg
        ));

        sampleList.add(new Transaction(
                "6", "Electricity Bill", "Bills", 1850.00, "EXPENSE",
                now - (4 * oneDay), "Monthly bill",
                R.drawable.ic_category_bills, R.color.category_bills_bg
        ));

        sampleList.add(new Transaction(
                "7", "Freelance Project", "Income", 15000.00, "INCOME",
                now - (5 * oneDay), "Client payment",
                R.drawable.ic_category_income, R.color.category_income_bg
        ));

        sampleList.add(new Transaction(
                "8", "Restaurant", "Food & Dining", 1250.00, "EXPENSE",
                now - (6 * oneDay), "Dinner with friends",
                R.drawable.ic_category_food, R.color.category_food_bg
        ));

        return sampleList;
    }
}
