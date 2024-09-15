package com.projects.expensetrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList = new ArrayList<>();

    public void setExpenseList(List<Expense> expenses){
        this.expenseList = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseAdapter.ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense,parent,false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseAdapter.ExpenseViewHolder holder, int position) {
        Expense currentExpense = expenseList.get(position);
        holder.expenseName.setText(currentExpense.getName());
        holder.expenseAmount.setText(String.valueOf(currentExpense.getAmount()));
        holder.expenseCategory.setText(currentExpense.getCategory());
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder{
        TextView expenseName, expenseAmount, expenseCategory;

        public ExpenseViewHolder(@NonNull View itemView){
            super(itemView);
            expenseName = itemView.findViewById(R.id.expenseName);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
            expenseCategory = itemView.findViewById(R.id.expenseCategory);
        }
    }
}
