package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class TransactionsActivity extends AppCompatActivity
        implements TransactionAdapter.OnTransactionClickListener {

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
    private String currentSort = "DATE_DESC";

    private static final int ADD_TRANSACTION_REQUEST = 101;

    // Database Helper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupRecyclerView();
        loadTransactions();
        setupClickListeners();
        setupBottomNavigation();
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

    // ✅ Load transactions
    private void loadTransactions() {
        transactions.clear();
        transactions.addAll(databaseHelper.getAllTransactions());
        adapter.updateTransactions(transactions);
        updateUI();
    }

    private void setupClickListeners() {

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

        btnSort.setOnClickListener(v -> showSortDialog());

        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST);
        });
    }

    private void showSortDialog() {
        String[] sortOptions = {
                "Date (Newest First)",
                "Date (Oldest First)",
                "Amount (High to Low)",
                "Amount (Low to High)"
        };

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

    // ✅ FIXED BALANCE LOGIC (MATCHES DASHBOARD)
    private void updateUI() {

        double totalIncome = databaseHelper.getTotalIncome();
        double totalExpense = databaseHelper.getTotalExpense();
        double salary = databaseHelper.getSalary();

        double balance = salary + totalIncome - totalExpense;

        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", balance));
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", totalIncome));
        tvTotalExpense.setText(String.format(Locale.getDefault(), "₹%.2f", totalExpense));

        updateTransactionCount();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            loadTransactions();
        }
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Toast.makeText(this, transaction.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_transactions);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_transactions) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
