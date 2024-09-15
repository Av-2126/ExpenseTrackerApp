package com.projects.expensetrackerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BudgetSettingsActivity extends AppCompatActivity {

    private EditText budgetEditText;
    private Button saveBudgetButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.budget_settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        budgetEditText = findViewById(R.id.budgetEditText);
        saveBudgetButton = findViewById(R.id.saveBudgetButton);

        loadBudget();

        saveBudgetButton.setOnClickListener(v -> saveBudget());

    }

    private void loadBudget() {
        SharedPreferences preferences = getSharedPreferences("budget_prefs",MODE_PRIVATE);
        float budget = preferences.getFloat("monthly_budget",0f);
        if(budget != 0){
            budgetEditText.setText(String.valueOf(budget));
        }
    }
    private void saveBudget() {
        String budgetValue = budgetEditText.getText().toString();
        if(!budgetValue.isEmpty()){
            float budget = Float.parseFloat(budgetValue);
            SharedPreferences preferences = getSharedPreferences("budget_prefs",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("monthly_budget",budget);
            editor.apply();
            Toast.makeText(this, "Budget Saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
