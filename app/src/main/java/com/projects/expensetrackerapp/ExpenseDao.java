package com.projects.expensetrackerapp;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(Expense expense);

    @Update
    void updateExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE strftime('%m', date / 1000, 'unixepoch') = strftime('%m', 'now')")
    List<Expense> getExpensesForCurrentMonth();

    @Query("SELECT * FROM expenses")
    List<Expense> getAllExpensesSync();

    @Query("SELECT * FROM expenses WHERE strftime('%Y', date / 1000, 'unixepoch') = strftime('%Y', 'now')")
    List<Expense> getExpensesForCurrentYear();  // Synchronous method to get expenses for the current year

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate")
    List<Expense> getExpensesBetweenDates(long startDate, long endDate);
}
