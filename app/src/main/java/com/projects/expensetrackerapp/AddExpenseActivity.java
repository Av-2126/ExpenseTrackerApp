package com.projects.expensetrackerapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText expenseNameEditText, expenseAmountEditText;
    private Spinner categorySpinner;
    private Button addExpenseButton;
    private ExpenseDatabase expenseDatabase;
    private String[] categories = {"Food", "Transport", "Entertainment", "Health", "Other"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_expense);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_expense), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        expenseNameEditText = findViewById(R.id.expenseNameEditText);
        expenseAmountEditText = findViewById(R.id.expenseAmountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        addExpenseButton = findViewById(R.id.addExpenseButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        expenseDatabase = ExpenseDatabase.getInstance(this);
        addExpenseButton.setOnClickListener(v -> addExpense());
    }

    private void addExpense() {
        String name = expenseNameEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        double amount = Double.parseDouble(expenseAmountEditText.getText().toString());

        Expense expense = new Expense(name, amount, category, System.currentTimeMillis());

        new Thread(() -> {
            expenseDatabase.expenseDao().insertExpense(expense);
            runOnUiThread(() -> Toast.makeText(AddExpenseActivity.this, "Expense Added", Toast.LENGTH_SHORT).show());
            finish();
        }).start();
    }
}
