package com.example.pocketplan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private static final String TAG = "ReportsActivity";
    private DatabaseHelper databaseHelper;

    private TextView tvReportIncome, tvReportExpense, tvReportSavings;
    private PieChart pieChart, categoryPieChart;
    private BarChart barChartMonthly;
    private LineChart lineChartWeekly;

    private TextView tvCatFood, tvCatTransport, tvCatShopping, tvCatBills,
            tvCatEntertainment, tvCatHealth, tvCatTravel, tvCatOther;
    private ProgressBar progressCatFood, progressCatTransport, progressCatShopping,
            progressCatBills, progressCatEntertainment, progressCatHealth,
            progressCatTravel, progressCatOther;

    private ProgressBar progressSavingsRate;
    private TextView tvSavingsRateLabel, tvTotalTransactions, tvIncomeCount, tvExpenseCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        databaseHelper = new DatabaseHelper(this);
        setupToolbar();
        initializeViews();
        loadAllData();
        MaterialButton btnViewAll = findViewById(R.id.btnViewAllTransactions);
        if (btnViewAll != null)
            btnViewAll.setOnClickListener(v -> startActivity(new Intent(this, TransactionsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        tvReportIncome  = findViewById(R.id.tvReportIncome);
        tvReportExpense = findViewById(R.id.tvReportExpense);
        tvReportSavings = findViewById(R.id.tvReportSavings);
        pieChart         = findViewById(R.id.pieChart);
        categoryPieChart = findViewById(R.id.categoryPieChart);
        barChartMonthly  = findViewById(R.id.barChartMonthly);
        lineChartWeekly  = findViewById(R.id.lineChartWeekly);
        tvCatFood          = findViewById(R.id.tvCatFood);
        tvCatTransport     = findViewById(R.id.tvCatTransport);
        tvCatShopping      = findViewById(R.id.tvCatShopping);
        tvCatBills         = findViewById(R.id.tvCatBills);
        tvCatEntertainment = findViewById(R.id.tvCatEntertainment);
        tvCatHealth        = findViewById(R.id.tvCatHealth);
        tvCatTravel        = findViewById(R.id.tvCatTravel);
        tvCatOther         = findViewById(R.id.tvCatOther);
        progressCatFood          = findViewById(R.id.progressCatFood);
        progressCatTransport     = findViewById(R.id.progressCatTransport);
        progressCatShopping      = findViewById(R.id.progressCatShopping);
        progressCatBills         = findViewById(R.id.progressCatBills);
        progressCatEntertainment = findViewById(R.id.progressCatEntertainment);
        progressCatHealth        = findViewById(R.id.progressCatHealth);
        progressCatTravel        = findViewById(R.id.progressCatTravel);
        progressCatOther         = findViewById(R.id.progressCatOther);
        progressSavingsRate = findViewById(R.id.progressSavingsRate);
        tvSavingsRateLabel  = findViewById(R.id.tvSavingsRateLabel);
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions);
        tvIncomeCount       = findViewById(R.id.tvIncomeCount);
        tvExpenseCount      = findViewById(R.id.tvExpenseCount);
    }

    private void loadAllData() {
        try {
            double salary      = databaseHelper.getSalary();
            double income      = databaseHelper.getTotalIncome();
            double expense     = databaseHelper.getTotalExpense();
            double totalIncome = salary + income;
            double savings     = totalIncome - expense;

            tvReportIncome.setText(fmt(totalIncome));
            tvReportExpense.setText(fmt(expense));
            tvReportSavings.setText(fmt(savings));

            if (totalIncome > 0) {
                int pct = (int) Math.max(0, Math.min((savings / totalIncome) * 100, 100));
                progressSavingsRate.setProgress(pct);
                tvSavingsRateLabel.setText(pct + "% of income saved");
            } else {
                progressSavingsRate.setProgress(0);
                tvSavingsRateLabel.setText("No income recorded yet");
            }

            double food          = databaseHelper.getExpenseByCategory("Food & Dining");
            double transport     = databaseHelper.getExpenseByCategory("Transportation");
            double shopping      = databaseHelper.getExpenseByCategory("Shopping");
            double bills         = databaseHelper.getExpenseByCategory("Bills & Utilities");
            double entertainment = databaseHelper.getExpenseByCategory("Entertainment");
            double health        = databaseHelper.getExpenseByCategory("Healthcare");
            double travel        = databaseHelper.getExpenseByCategory("Travel");
            double groceries     = databaseHelper.getExpenseByCategory("Groceries");
            double other         = databaseHelper.getExpenseByCategory("Other") + groceries;

            double maxCat = Math.max(1, Math.max(food,
                    Math.max(transport, Math.max(shopping,
                            Math.max(bills, Math.max(entertainment,
                                    Math.max(health, Math.max(travel, other))))))));

            updateCategoryRow(tvCatFood,          progressCatFood,          food,          maxCat);
            updateCategoryRow(tvCatTransport,     progressCatTransport,     transport,     maxCat);
            updateCategoryRow(tvCatShopping,      progressCatShopping,      shopping,      maxCat);
            updateCategoryRow(tvCatBills,         progressCatBills,         bills,         maxCat);
            updateCategoryRow(tvCatEntertainment, progressCatEntertainment, entertainment, maxCat);
            updateCategoryRow(tvCatHealth,        progressCatHealth,        health,        maxCat);
            updateCategoryRow(tvCatTravel,        progressCatTravel,        travel,        maxCat);
            updateCategoryRow(tvCatOther,         progressCatOther,         other,         maxCat);

            List<com.example.pocketplan.models.Transaction> all = databaseHelper.getAllTransactions();
            int incomeCount = 0, expenseCount = 0;
            for (com.example.pocketplan.models.Transaction t : all) {
                if (t.isIncome()) incomeCount++; else expenseCount++;
            }
            tvTotalTransactions.setText(String.valueOf(all.size()));
            tvIncomeCount.setText(String.valueOf(incomeCount));
            tvExpenseCount.setText(String.valueOf(expenseCount));

            setupIncomeExpensePieChart(totalIncome, expense);
            setupCategoryPieChart(food, transport, shopping, bills, entertainment, health, travel, other);
            setupMonthlyBarChart();
            setupWeeklyLineChart();

        } catch (Exception e) {
            Log.e(TAG, "Error loading report data: " + e.getMessage(), e);
        }
    }

    private void setupIncomeExpensePieChart(double income, double expense) {
        List<PieEntry> entries = new ArrayList<>();
        if (income > 0)  entries.add(new PieEntry((float) income,  "Income"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Expense"));
        if (entries.isEmpty()) entries.add(new PieEntry(1f, "No Data"));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"), Color.parseColor("#BDBDBD"));
        ds.setSliceSpace(3f);
        ds.setSelectionShift(6f);
        ds.setValueTextSize(13f);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(new PieData(ds));
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(52f);
        pieChart.setTransparentCircleRadius(57f);
        pieChart.setCenterText("Balance");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.parseColor("#1A1F36"));
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.animateY(1000, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void setupCategoryPieChart(double food, double transport, double shopping,
                                       double bills, double entertainment, double health,
                                       double travel, double other) {
        List<PieEntry> entries = new ArrayList<>();
        if (food > 0)          entries.add(new PieEntry((float) food,          "Food"));
        if (transport > 0)     entries.add(new PieEntry((float) transport,     "Transport"));
        if (shopping > 0)      entries.add(new PieEntry((float) shopping,      "Shopping"));
        if (bills > 0)         entries.add(new PieEntry((float) bills,         "Bills"));
        if (entertainment > 0) entries.add(new PieEntry((float) entertainment, "Fun"));
        if (health > 0)        entries.add(new PieEntry((float) health,        "Health"));
        if (travel > 0)        entries.add(new PieEntry((float) travel,        "Travel"));
        if (other > 0)         entries.add(new PieEntry((float) other,         "Other"));
        if (entries.isEmpty()) entries.add(new PieEntry(1f, "No Expenses"));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(
                Color.parseColor("#FF9800"), Color.parseColor("#2196F3"),
                Color.parseColor("#E91E63"), Color.parseColor("#F44336"),
                Color.parseColor("#9C27B0"), Color.parseColor("#4CAF50"),
                Color.parseColor("#00BCD4"), Color.parseColor("#607D8B"),
                Color.parseColor("#BDBDBD")
        );
        ds.setSliceSpace(2f);
        ds.setSelectionShift(5f);
        ds.setValueTextSize(10f);
        ds.setValueTextColor(Color.WHITE);

        categoryPieChart.setData(new PieData(ds));
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setDrawHoleEnabled(true);
        categoryPieChart.setHoleColor(Color.WHITE);
        categoryPieChart.setHoleRadius(40f);
        categoryPieChart.setTransparentCircleRadius(45f);
        categoryPieChart.setCenterText("Categories");
        categoryPieChart.setCenterTextSize(12f);
        categoryPieChart.setCenterTextColor(Color.parseColor("#1A1F36"));
        categoryPieChart.getLegend().setEnabled(false);
        categoryPieChart.setEntryLabelColor(Color.WHITE);
        categoryPieChart.setEntryLabelTextSize(10f);
        categoryPieChart.animateY(1200, Easing.EaseInOutQuad);
        categoryPieChart.invalidate();
    }

    private void setupMonthlyBarChart() {
        Calendar cal = Calendar.getInstance();
        List<BarEntry> entries = new ArrayList<>();
        String[] monthLabels = new String[6];
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMM", Locale.getDefault());

        for (int i = 5; i >= 0; i--) {
            Calendar mc = (Calendar) cal.clone();
            mc.add(Calendar.MONTH, -i);
            int month = mc.get(Calendar.MONTH) + 1;
            int year  = mc.get(Calendar.YEAR);
            double val = databaseHelper.getMonthlyExpense(month, year);
            entries.add(new BarEntry(5 - i, (float) val));
            monthLabels[5 - i] = monthFmt.format(mc.getTime());
        }

        BarDataSet ds = new BarDataSet(entries, "Expenses");
        ds.setColor(Color.parseColor("#5E35B1"));
        ds.setValueTextColor(Color.parseColor("#1A1F36"));
        ds.setValueTextSize(10f);

        BarData barData = new BarData(ds);
        barData.setBarWidth(0.55f);

        barChartMonthly.setData(barData);
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.getLegend().setEnabled(false);
        barChartMonthly.setDrawGridBackground(false);
        barChartMonthly.setDrawBorders(false);

        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#9E9E9E"));
        xAxis.setTextSize(11f);

        YAxis leftAxis = barChartMonthly.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#9E9E9E"));
        leftAxis.setGridColor(Color.parseColor("#F0F0F0"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(10f);

        barChartMonthly.getAxisRight().setEnabled(false);
        barChartMonthly.animateY(1000, Easing.EaseInOutQuad);
        barChartMonthly.invalidate();
    }

    private void setupWeeklyLineChart() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMon = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMon);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            long dayStart = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            long dayEnd = cal.getTimeInMillis() - 1;
            double val = databaseHelper.getExpenseForRange(dayStart, dayEnd);
            entries.add(new Entry(i, (float) val));
        }

        LineDataSet ds = new LineDataSet(entries, "Daily Spending");
        ds.setColor(Color.parseColor("#4527A0"));
        ds.setCircleColor(Color.parseColor("#7C4DFF"));
        ds.setCircleRadius(5f);
        ds.setLineWidth(2.5f);
        ds.setDrawFilled(true);
        ds.setFillColor(Color.parseColor("#EDE7F6"));
        ds.setFillAlpha(100);
        ds.setValueTextColor(Color.parseColor("#1A1F36"));
        ds.setValueTextSize(9f);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChartWeekly.setData(new LineData(ds));
        lineChartWeekly.getDescription().setEnabled(false);
        lineChartWeekly.getLegend().setEnabled(false);
        lineChartWeekly.setDrawGridBackground(false);
        lineChartWeekly.setTouchEnabled(true);

        XAxis xAxis = lineChartWeekly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#9E9E9E"));
        xAxis.setTextSize(11f);

        YAxis leftAxis = lineChartWeekly.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#9E9E9E"));
        leftAxis.setGridColor(Color.parseColor("#F0F0F0"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(10f);

        lineChartWeekly.getAxisRight().setEnabled(false);
        lineChartWeekly.animateX(1000, Easing.EaseInOutQuad);
        lineChartWeekly.invalidate();
    }

    private void updateCategoryRow(TextView tv, ProgressBar pb, double amount, double max) {
        tv.setText(fmt(amount));
        pb.setProgress((int) ((amount / max) * 100));
    }

    private String fmt(double amount) {
        return String.format(Locale.getDefault(), "Rs %.0f", amount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}