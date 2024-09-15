package com.projects.expensetrackerapp;

import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DataVisualizationActivity extends AppCompatActivity {

    private PieChart expensePieChart;
    private Button startDateButton, endDateButton;
    private long startDateMillis, endDateMillis;
    private ExpenseDatabase expenseDatabase;
    private BarChart expenseBarChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualization);

        expensePieChart = findViewById(R.id.expensePieChart);
        expenseBarChart = findViewById(R.id.expenseBarChart);
        expenseDatabase = ExpenseDatabase.getInstance(this);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);

        loadExpenseData();

        startDateButton.setOnClickListener(v -> showDatePickerDialog(true));
        endDateButton.setOnClickListener(v -> showDatePickerDialog(false));

        findViewById(R.id.exportDataButton).setOnClickListener(v -> exportDataToCSV());
        findViewById(R.id.exportPdfButton).setOnClickListener(v -> exportDataToPDF());
    }

    private void exportDataToPDF() {
        new Thread(() -> {
            List<Expense> expenses = expenseDatabase.expenseDao().getExpensesBetweenDates(startDateMillis, endDateMillis);

            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            int yPosition = 50;

            paint.setTextSize(18);
            canvas.drawText("Expense Report", 240, yPosition, paint);
            yPosition += 40;

            paint.setTextSize(14);
            canvas.drawText("Date", 50, yPosition, paint);
            canvas.drawText("Category", 200, yPosition, paint);
            canvas.drawText("Amount", 400, yPosition, paint);
            yPosition += 20;

            paint.setTextSize(12);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (Expense expense : expenses) {
                String date = sdf.format(new Date(expense.date));
                canvas.drawText(date, 50, yPosition, paint);
                canvas.drawText(expense.category, 200, yPosition, paint);
                canvas.drawText(String.valueOf(expense.amount), 400, yPosition, paint);
                yPosition += 20;
            }

            pdfDocument.finishPage(page);

            String pdfFileName = "Expense_Report.pdf";
            File file = new File(getExternalFilesDir(null), pdfFileName);

            try {
                pdfDocument.writeTo(new FileOutputStream(file));
                runOnUiThread(() -> {
                    Toast.makeText(this, "PDF exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to export PDF", Toast.LENGTH_SHORT).show();
                });
            } finally {
                pdfDocument.close();
            }
        }).start();
    }

    private void exportDataToCSV() {
        new Thread(() -> {
            List<Expense> expenses = expenseDatabase.expenseDao().getExpensesBetweenDates(startDateMillis, endDateMillis);

            String csvFileName = "expenses.csv";
            File file = new File(getExternalFilesDir(null), csvFileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Date,Category,Amount\n");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                for (Expense expense : expenses) {
                    String date = sdf.format(new Date(expense.date));
                    writer.append(date).append(",")
                            .append(expense.category).append(",")
                            .append(String.valueOf(expense.amount)).append("\n");
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Data exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);
            if (isStartDate) {
                startDateMillis = selectedDate.getTimeInMillis();
                startDateButton.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()));
            } else {
                endDateMillis = selectedDate.getTimeInMillis();
                endDateButton.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()));
            }
            if (startDateMillis > 0 && endDateMillis > 0) {
                filterExpensesByDateRange();
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void filterExpensesByDateRange() {
        new Thread(() -> {
            List<Expense> expenses = expenseDatabase.expenseDao().getExpensesBetweenDates(startDateMillis, endDateMillis);

            Map<String, Float> categoryTotals = new HashMap<>();
            for (Expense expense : expenses) {
                float amount = (float) expense.amount;
                String category = expense.category;

                if (categoryTotals.containsKey(category)) {
                    categoryTotals.put(category, categoryTotals.get(category) + amount);
                } else {
                    categoryTotals.put(category, amount);
                }
            }

            runOnUiThread(() -> displayPieChart(categoryTotals));
        }).start();
    }
    private void loadExpenseData() {
        new Thread(() -> {
            List<Expense> expenses = expenseDatabase.expenseDao().getAllExpensesSync(); // Synchronous method to get all expenses

            Map<String, Float> categoryTotals = new HashMap<>();
            for (Expense expense : expenses) {
                float amount =(float) expense.amount;
                String category = expense.category;

                if (categoryTotals.containsKey(category)) {
                    categoryTotals.put(category, categoryTotals.get(category) + amount);
                } else {
                    categoryTotals.put(category, amount);
                }
            }

            runOnUiThread(() -> displayPieChart(categoryTotals));
        }).start();
    }

    private void displayPieChart(Map<String, Float> categoryTotals) {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expense Categories");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);  // Set some nice colors
        PieData pieData = new PieData(pieDataSet);
        expensePieChart.setData(pieData);
        expensePieChart.invalidate();  // Refresh the chart
        expensePieChart.setCenterText("Expenses by Category");
        expensePieChart.setEntryLabelColor(Color.BLACK);
    }

    private void loadMonthlyExpenseData() {
        new Thread(() -> {
            List<Expense> expenses = expenseDatabase.expenseDao().getExpensesForCurrentYear();

            Map<String, Float> monthlyTotals = new TreeMap<>();  // TreeMap to keep the months in order
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

            // Initialize map with all months
            for (int i = 0; i < 12; i++) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, i);
                String monthName = monthFormat.format(cal.getTime());
                monthlyTotals.put(monthName, 0f);
            }

            for (Expense expense : expenses) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(expense.date);
                String monthName = monthFormat.format(cal.getTime());

                monthlyTotals.put(monthName, (float) (monthlyTotals.get(monthName) + expense.amount));
            }

            runOnUiThread(() -> displayBarChart(monthlyTotals));
        }).start();
    }

    private void displayBarChart(Map<String, Float> monthlyTotals) {
        List<BarEntry> barEntries = new ArrayList<>();
        int index = 0;
        List<String> months = new ArrayList<>(monthlyTotals.keySet());

        for (Map.Entry<String, Float> entry : monthlyTotals.entrySet()) {
            barEntries.add(new BarEntry(index++, entry.getValue()));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Expenses");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        expenseBarChart.setData(barData);

        XAxis xAxis = expenseBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        expenseBarChart.invalidate();  // Refresh the chart
    }
}

