package com.Splitwise.SplitWise.Service;

import com.Splitwise.SplitWise.Entity.Expense;
import com.Splitwise.SplitWise.Entity.User;
import com.Splitwise.SplitWise.payload.ExpenseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExpenseService {


    void addExpense(ExpenseDto expenseDto);

    void splitExpense(Expense expense);

    List<Expense> getPassbookEntriesForUser(User user);
    void simplifyBalances();

    Map<User, BigDecimal> getBalancesForAllUsers();
    Map<User, BigDecimal> calculateBalancesBetweenUsers();

    void validateExpenseRequest(ExpenseDto expenseRequest);

    List<String> getTransactionHistoryForUser(User user);

    List<Expense> getExpensesForUser(Long userId);
}
