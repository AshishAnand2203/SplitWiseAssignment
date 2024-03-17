package com.Splitwise.SplitWise.Controller;

import com.Splitwise.SplitWise.Entity.Expense;
import com.Splitwise.SplitWise.Entity.User;
import com.Splitwise.SplitWise.Repository.UserRepository;
import com.Splitwise.SplitWise.Service.ExpenseService;
import com.Splitwise.SplitWise.payload.ExpenseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<String> addExpense(@RequestBody ExpenseDto expenseDto) {
        expenseService.addExpense(expenseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Expense added successfully");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Expense>> getExpensesForUser(@PathVariable Long userId) {
        List<Expense> expenses = expenseService.getExpensesForUser(userId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/balances")
    public ResponseEntity<Map<User, BigDecimal>> getBalancesForAllUsers() {
        Map<User, BigDecimal> balances = expenseService.getBalancesForAllUsers();
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/simplify")
    public ResponseEntity<String> simplifyBalances() {
        expenseService.simplifyBalances();
        return ResponseEntity.ok("Balances simplified successfully");
    }

    @GetMapping("passbook/user/{userId}")
    public ResponseEntity<List<String>> getPassbookEntriesForUser(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        User user = optionalUser.orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<String> passbookEntries = expenseService.getTransactionHistoryForUser(user);
        return ResponseEntity.ok(passbookEntries);
    }
}
