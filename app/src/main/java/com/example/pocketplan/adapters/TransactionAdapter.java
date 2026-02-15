package com.example.pocketplan.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pocketplan.R;
import com.example.pocketplan.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private static final String TAG = "TransactionAdapter";

    private Context context;
    private List<Transaction> transactions;
    private List<Transaction> transactionsFiltered;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionClickListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.transactionsFiltered = new ArrayList<>(transactions);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionsFiltered.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionsFiltered.size();
    }

    // Filter methods
    public void filterByType(String type) {
        transactionsFiltered.clear();
        if (type.equals("ALL")) {
            transactionsFiltered.addAll(transactions);
        } else {
            for (Transaction transaction : transactions) {
                if (transaction.getType().equalsIgnoreCase(type)) {
                    transactionsFiltered.add(transaction);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Sort methods
    public void sortByDate(boolean descending) {
        if (descending) {
            transactionsFiltered.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
        } else {
            transactionsFiltered.sort((t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp()));
        }
        notifyDataSetChanged();
    }

    public void sortByAmount(boolean descending) {
        if (descending) {
            transactionsFiltered.sort((t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
        } else {
            transactionsFiltered.sort((t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));
        }
        notifyDataSetChanged();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        this.transactionsFiltered = new ArrayList<>(newTransactions);
        notifyDataSetChanged();
        Log.d(TAG, "Updated transactions: " + newTransactions.size() + " items");
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {

        private View viewCategoryIconBg;
        private ImageView ivCategoryIcon;
        private TextView tvTransactionTitle;
        private TextView tvCategoryName;
        private TextView tvDateTime;
        private TextView tvAmount;
        private View viewTypeIndicator;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            viewCategoryIconBg = itemView.findViewById(R.id.viewCategoryIconBg);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            viewTypeIndicator = itemView.findViewById(R.id.viewTypeIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionClick(transactionsFiltered.get(position));
                }
            });
        }

        public void bind(Transaction transaction) {
            try {
                // Set title and category
                tvTransactionTitle.setText(transaction.getTitle());
                tvCategoryName.setText(transaction.getCategory());

                // Set category icon with fallback
                int iconRes = transaction.getCategoryIconRes();
                if (iconRes == 0) {
                    iconRes = android.R.drawable.ic_dialog_info;
                }
                ivCategoryIcon.setImageResource(iconRes);

                // Set category icon background color with fallback
                int colorRes = transaction.getCategoryColorRes();
                if (colorRes == 0) {
                    colorRes = R.color.category_default;
                }

                try {
                    GradientDrawable iconBgDrawable = (GradientDrawable) viewCategoryIconBg.getBackground();
                    iconBgDrawable.setColor(ContextCompat.getColor(context, colorRes));
                } catch (Exception e) {
                    Log.e(TAG, "Error setting background color: " + e.getMessage());
                }

                // Set icon tint
                int iconTintColor = getCategoryIconColor(transaction);
                ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, iconTintColor));

                // Set date and time
                tvDateTime.setText(formatDateTime(transaction.getTimestamp()));

                // Set amount with sign and color
                boolean isIncome = transaction.isIncome();
                String amountText = (isIncome ? "+ " : "- ") + "₹" +
                        String.format(Locale.getDefault(), "%.2f", transaction.getAmount());
                tvAmount.setText(amountText);
                tvAmount.setTextColor(ContextCompat.getColor(context,
                        isIncome ? R.color.income_green : R.color.expense_red));

                // Set type indicator color
                try {
                    GradientDrawable indicatorDrawable = (GradientDrawable) viewTypeIndicator.getBackground();
                    indicatorDrawable.setColor(ContextCompat.getColor(context,
                            isIncome ? R.color.income_green : R.color.expense_red));
                } catch (Exception e) {
                    Log.e(TAG, "Error setting indicator color: " + e.getMessage());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error binding transaction: " + e.getMessage(), e);
            }
        }

        private int getCategoryIconColor(Transaction transaction) {
            // Return darker version of category color for icon
            if (transaction.isIncome()) {
                return R.color.income_green;
            }

            // Match category names exactly as they appear in AddTransactionActivity
            String category = transaction.getCategory();

            if (category.equalsIgnoreCase("Food & Dining")) {
                return R.color.icon_food;
            } else if (category.equalsIgnoreCase("Transportation")) {
                return R.color.icon_transport;
            } else if (category.equalsIgnoreCase("Shopping")) {
                return R.color.icon_shopping;
            } else if (category.equalsIgnoreCase("Entertainment")) {
                return R.color.icon_entertainment;
            } else if (category.equalsIgnoreCase("Bills & Utilities")) {
                return R.color.icon_bills;
            } else if (category.equalsIgnoreCase("Healthcare")) {
                return R.color.icon_health;
            } else if (category.equalsIgnoreCase("Education")) {
                return R.color.icon_education;
            } else if (category.equalsIgnoreCase("Travel")) {
                return R.color.icon_travel;
            } else if (category.equalsIgnoreCase("Groceries")) {
                return R.color.icon_groceries;
            } else {
                return R.color.icon_default;
            }
        }

        private String formatDateTime(long timestamp) {
            try {
                Date date = new Date(timestamp);
                Date now = new Date();

                long diffInMillis = now.getTime() - date.getTime();
                long diffInHours = diffInMillis / (1000 * 60 * 60);
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

                if (diffInHours < 24 && now.getDate() == date.getDate()) {
                    return "Today, " + timeFormat.format(date);
                } else if (diffInDays == 1) {
                    return "Yesterday, " + timeFormat.format(date);
                } else if (diffInDays < 7) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, h:mm a", Locale.getDefault());
                    return dayFormat.format(date);
                } else {
                    SimpleDateFormat fullFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
                    return fullFormat.format(date);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error formatting date: " + e.getMessage());
                return "Unknown";
            }
        }
    }
}