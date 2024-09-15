package com.projects.expensetrackerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ExpenseAdapter expenseAdapter;
    private ExpenseDatabase expenseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

// Notification channel___________________________________________________________
        createNotificationChannel();
// Firebase autorization_____________________________________________________________
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
// Expenses list___________________________________________________________________
        RecyclerView recyclerView = findViewById(R.id.expenseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        expenseAdapter = new ExpenseAdapter();
        recyclerView.setAdapter(expenseAdapter);

        expenseDatabase = ExpenseDatabase.getInstance(this);
        loadExpenses();

        checkBudget();

        findViewById(R.id.addExpenseButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddExpenseActivity.class)));
//        findViewById(R.id.saveBudgetButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BudgetSettingsActivity.class)));
        findViewById(R.id.viewChartButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DataVisualizationActivity.class));
        });
        findViewById(R.id.budgetSettingsButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BudgetSettingsActivity.class));
        });

    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "budget_channel";
            CharSequence name = "Budget Notifications";
            String description = "Notifications for budget warnings and exceedings";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId,name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkBudget() {
        new Thread(() -> {
            List<Expense> expenses = expenseDatabase.expenseDao().getExpensesForCurrentMonth();
            float totalExpenses = 0;
            for (Expense expense : expenses) {
                totalExpenses += (float) expense.getAmount();
            }
            SharedPreferences preferences = getSharedPreferences("budget_prefs", MODE_PRIVATE);
            float budget = preferences.getFloat("monthly_budget", 0);

            if (budget != 0 && totalExpenses >= budget) {
                runOnUiThread(this::sendBudgetExceedNotification);
            } else if (budget != 0 && totalExpenses >= (0.8 * budget)) {
                runOnUiThread(() -> sendBudgetWarningNotification());
            }
        }).start();
    }

    private void sendBudgetWarningNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this,"budget_channel")
                .setSmallIcon(R.drawable.ic_budget)
                .setContentTitle("Budget Warning")
                .setContentText("You have reached 80% of your monthly budget!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        notificationManager.notify(2,notification);
    }

    private void sendBudgetExceedNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this,"budget_channel")
                .setSmallIcon(R.drawable.ic_budget)
                .setContentTitle("Budget Exceeded")
                .setContentText("Your monthly budget has been exceeded!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        notificationManager.notify(1,notification);
    }

    private void loadExpenses() {
        expenseDatabase.expenseDao().getAllExpenses().observe(this, expenses -> {
            if (expenses != null) {
                expenseAdapter.setExpenseList(expenses);
            }
        });
    }
}