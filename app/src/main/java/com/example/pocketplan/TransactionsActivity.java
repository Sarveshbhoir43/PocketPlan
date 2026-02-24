package com.example.pocketplan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import utils.SessionManager;

public class TransactionsActivity extends AppCompatActivity
        implements TransactionAdapter.OnTransactionClickListener {

    private static final String TAG = "TransactionsActivity";
    private static final int ADD_TRANSACTION_REQUEST = 101;

    private TextView tvTransactionCount, tvTotalBalance, tvTotalIncome, tvTotalExpense;
    private ImageView imgProfile;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipIncome, chipExpense;
    private MaterialButton btnSort;
    private RecyclerView rvTransactions;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigation;

    private List<Transaction> transactions;
    private TransactionAdapter adapter;
    private String currentFilter = "ALL";
    private String currentSort   = "DATE_DESC";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = sessionManager.getUserId();

        setContentView(R.layout.activity_transactions);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupRecyclerView();
        loadTransactions();
        setupClickListeners();
        setupBottomNavigation();
        loadProfileImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
        loadProfileImage();
    }

    private void initializeViews() {
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        tvTotalBalance     = findViewById(R.id.tvTotalBalance);
        tvTotalIncome      = findViewById(R.id.tvTotalIncome);
        tvTotalExpense     = findViewById(R.id.tvTotalExpense);
        imgProfile         = findViewById(R.id.imgProfile);
        chipGroupFilter    = findViewById(R.id.chipGroupFilter);
        chipAll            = findViewById(R.id.chipAll);
        chipIncome         = findViewById(R.id.chipIncome);
        chipExpense        = findViewById(R.id.chipExpense);
        btnSort            = findViewById(R.id.btnSort);
        rvTransactions     = findViewById(R.id.rvTransactions);
        emptyStateLayout   = findViewById(R.id.emptyStateLayout);
        fabAddTransaction  = findViewById(R.id.fabAddTransaction);
        bottomNavigation   = findViewById(R.id.bottomNavigation);
    }

    private void loadProfileImage() {
        try {
            String key = "profile_image_" + currentUserId;
            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String imageBase64 = prefs.getString(key, null);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                byte[] bytes = Base64.decode(imageBase64, Base64.DEFAULT);
                imgProfile.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            } else {
                imgProfile.setImageResource(R.drawable.ic_profile);
            }
        } catch (Exception e) {
            imgProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupRecyclerView() {
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactions, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadTransactions() {
        try {
            transactions.clear();
            List<Transaction> db = databaseHelper.getAllTransactions(currentUserId);
            Log.d(TAG, "Loaded " + db.size() + " transactions for user " + currentUserId);
            transactions.addAll(db);
            adapter.updateTransactions(transactions);
            updateUI();
        } catch (Exception e) {
            Log.e(TAG, "loadTransactions: " + e.getMessage());
            Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        imgProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll)     { currentFilter = "ALL";     adapter.filterByType("ALL"); }
            else if (checkedId == R.id.chipIncome)  { currentFilter = "INCOME";  adapter.filterByType("INCOME"); }
            else if (checkedId == R.id.chipExpense) { currentFilter = "EXPENSE"; adapter.filterByType("EXPENSE"); }
            updateTransactionCount();
        });

        btnSort.setOnClickListener(v -> showSortDialog());
        fabAddTransaction.setOnClickListener(v ->
                startActivityForResult(new Intent(this, AddTransactionActivity.class), ADD_TRANSACTION_REQUEST));
    }

    private void showSortDialog() {
        String[] opts = {"Date (Newest First)","Date (Oldest First)","Amount (High to Low)","Amount (Low to High)"};
        int sel = currentSort.equals("DATE_DESC") ? 0 : currentSort.equals("DATE_ASC") ? 1
                : currentSort.equals("AMOUNT_DESC") ? 2 : 3;
        new AlertDialog.Builder(this).setTitle("Sort By")
                .setSingleChoiceItems(opts, sel, (d, which) -> {
                    switch (which) {
                        case 0: currentSort = "DATE_DESC";   adapter.sortByDate(true);    break;
                        case 1: currentSort = "DATE_ASC";    adapter.sortByDate(false);   break;
                        case 2: currentSort = "AMOUNT_DESC"; adapter.sortByAmount(true);  break;
                        case 3: currentSort = "AMOUNT_ASC";  adapter.sortByAmount(false); break;
                    }
                    d.dismiss();
                }).show();
    }

    private void updateUI() {
        try {
            double income  = databaseHelper.getTotalIncome(currentUserId);
            double expense = databaseHelper.getTotalExpense(currentUserId);
            double salary  = databaseHelper.getSalary(currentUserId);

            tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", salary + income - expense));
            tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
            tvTotalExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));

            updateTransactionCount();

            boolean empty = transactions.isEmpty();
            emptyStateLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "updateUI: " + e.getMessage());
        }
    }

    private void updateTransactionCount() {
        int count = adapter.getItemCount();
        String text = count + (count == 1 ? " transaction" : " transactions");
        if (!currentFilter.equals("ALL")) text += " (" + currentFilter.toLowerCase() + ")";
        else text += " this month";
        tvTransactionCount.setText(text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) loadTransactions();
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle(transaction.getTitle())
                .setMessage("Category: " + transaction.getCategory() + "\n" +
                        "Amount: ₹" + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()) + "\n" +
                        "Type: " + transaction.getType() +
                        (transaction.getNote() != null && !transaction.getNote().isEmpty() ?
                                "\nNote: " + transaction.getNote() : ""))
                .setPositiveButton("OK", null)
                .setNegativeButton("Delete", (d, w) -> deleteTransaction(transaction))
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (d, w) -> {
                    if (databaseHelper.deleteTransaction(transaction.getId())) {
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        loadTransactions();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_transactions);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class)); finish(); return true;
            } else if (item.getItemId() == R.id.nav_transactions) return true;
            else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class)); finish(); return true;
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