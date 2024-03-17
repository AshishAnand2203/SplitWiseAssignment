package com.Splitwise.SplitWise.Service.Impl;

import com.Splitwise.SplitWise.Entity.*;
import com.Splitwise.SplitWise.Repository.ExpenseRepository;
import com.Splitwise.SplitWise.Repository.UserRepository;
import com.Splitwise.SplitWise.Service.EmailService;
import com.Splitwise.SplitWise.Service.ExpenseService;
import com.Splitwise.SplitWise.payload.ExpenseDto;
import com.Splitwise.SplitWise.payload.ExpenseParticipantRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;
//    @Autowired
//    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    @Async
    public void addExpense(ExpenseDto expenseDto) {
        Expense expense = modelMapper.map(expenseDto, Expense.class);

        User payer = userRepository.findById(expenseDto.getPayerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid payer ID"));
        expense.setPayer(payer);

        // Assuming participants are provided as a list of participant IDs in the request
        List<ExpenseParticipantRequest> participantRequests = expenseDto.getParticipants();
        List<User> participants = userRepository.findAllById(participantRequests.stream()
                .map(ExpenseParticipantRequest::getUserId)
                .collect(Collectors.toList()));
        expense.setParticipants(participants);

       // sendEmailNotifications(participants, expense);
        // Save the expense to the database
        expenseRepository.save(expense);
    }

    @Override
    public void splitExpense(Expense expense) {
        BigDecimal totalAmount = expense.getAmount();
        int numParticipants = expense.getParticipants().size();

        if (expense.getType() == ExpenseType.EQUAL) {
            splitEqually(expense, totalAmount, numParticipants);
        } else if (expense.getType() == ExpenseType.EXACT) {
            splitExact(expense);
        } else if (expense.getType() == ExpenseType.PERCENTAGE) {
            splitByPercentage(expense, totalAmount);
        }
    }

    @Override
    public List<Expense> getPassbookEntriesForUser(User user) {
        List<Expense> passbookEntries = new ArrayList<>();
        List<Expense> expenses = expenseRepository.findAllByPayerOrParticipantsContaining(user, user);

        for (Expense expense : expenses) {
            if (expense.getPayer().equals(user)) {
                passbookEntries.add(expense);
            } else {
                // Check if user is a participant
                for (User participant : expense.getParticipants()) {
                    if (participant.equals(user)) {
                        passbookEntries.add(expense);
                        break;
                    }
                }
            }
        }

        return passbookEntries;
    }

    @Override
    public void simplifyBalances() {

        List<User> users = userRepository.findAll();
        Map<User, BigDecimal> balances = new HashMap<>();

        // Initialize balances
        for (User user : users) {
            balances.put(user, BigDecimal.ZERO);
        }

        // Calculate balances
        List<Expense> expenses = expenseRepository.findAll();
        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            User payer = expense.getPayer();
            List<User> participants = expense.getParticipants();
            BigDecimal amountPerParticipant = amount.divide(BigDecimal.valueOf(participants.size()), 2, BigDecimal.ROUND_HALF_UP);

            // Deduct the amount from payer's balance
            balances.put(payer, balances.get(payer).subtract(amount));

            // Add the amount to each participant's balance
            for (User participant : participants) {
                balances.put(participant, balances.get(participant).add(amountPerParticipant));
            }
        }

        // Simplify balances
        for (User debtor : users) {
            BigDecimal debt = balances.get(debtor);
            if (debt.compareTo(BigDecimal.ZERO) < 0) { // User owes money
                for (User creditor : users) {
                    if (creditor.equals(debtor)) {
                        continue; // Skip if debtor is the same as creditor
                    }
                    BigDecimal credit = balances.get(creditor);
                    if (credit.compareTo(BigDecimal.ZERO) > 0) { // Creditor has money to receive
                        BigDecimal transferAmount = debt.min(credit.abs());
                        balances.put(debtor, debt.add(transferAmount));
                        balances.put(creditor, credit.subtract(transferAmount));
                        // Log the simplified balance transfer
                        System.out.println(debtor.getName() + " owes " + transferAmount + " to " + creditor.getName());
                        if (debt.compareTo(BigDecimal.ZERO) == 0) {
                            break; // If debt is cleared, move to the next debtor
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<User, BigDecimal> getBalancesForAllUsers() {
        List<User> users = userRepository.findAll();
        Map<User, BigDecimal> balances = new HashMap<>();

        // Initialize balances for all users
        for (User user : users) {
            balances.put(user, BigDecimal.ZERO);
        }

        // Calculate balances based on expenses
        List<Expense> expenses = expenseRepository.findAll();
        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            User payer = expense.getPayer();
            List<User> participants = expense.getParticipants();
            BigDecimal amountPerParticipant = amount.divide(BigDecimal.valueOf(participants.size()), 2, BigDecimal.ROUND_HALF_UP);

            // Deduct the amount from payer's balance
            balances.put(payer, balances.get(payer).subtract(amount));

            // Add the amount to each participant's balance
            for (User participant : participants) {
                balances.put(participant, balances.get(participant).add(amountPerParticipant));
            }
        }

        return balances;

    }

    @Override
    public Map<User, BigDecimal> calculateBalancesBetweenUsers() {
        List<User> users = userRepository.findAll();
        Map<User, BigDecimal> balances = new HashMap<>();

        // Initialize balances for all users
        for (User user : users) {
            balances.put(user, BigDecimal.ZERO);
        }

        // Calculate balances based on expenses
        List<Expense> expenses = expenseRepository.findAll();
        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP); // Round to two decimal places
            User payer = expense.getPayer();
            List<User> participants = expense.getParticipants();
            BigDecimal amountPerParticipant = amount.divide(BigDecimal.valueOf(participants.size()), 2, BigDecimal.ROUND_HALF_UP);

            // Deduct the amount from payer's balance
            balances.put(payer, balances.get(payer).subtract(amount));

            // Add the amount to each participant's balance
            for (User participant : participants) {
                balances.put(participant, balances.get(participant).add(amountPerParticipant));
            }
        }

        // Simplify balances
        simplifyBalances(balances);

        return balances;
    }

    @Override
    public void validateExpenseRequest(ExpenseDto expenseRequest) {
        BigDecimal totalAmount = expenseRequest.getAmount();
        List<ExpenseParticipantRequest> participants = expenseRequest.getParticipants();
        ExpenseType type = expenseRequest.getType();

        // Validate maximum number of participants
        if (participants.size() > 1000) {
            throw new IllegalArgumentException("Maximum number of participants exceeded");
        }

        // Validate maximum expense amount
        if (totalAmount.compareTo(BigDecimal.valueOf(10000000)) > 0) { // Assuming 10000000 is the maximum expense amount
            throw new IllegalArgumentException("Maximum expense amount exceeded");
        }

        // Validate total sum of shares in percentage splits
        if (type == ExpenseType.PERCENTAGE) {
            BigDecimal totalPercentage = BigDecimal.ZERO;
            for (ExpenseParticipantRequest participant : participants) {
                totalPercentage = totalPercentage.add(participant.getShare());
            }
            if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new IllegalArgumentException("Total sum of shares in percentage splits must equal 100");
            }
        }
    }

    @Override
    public List<String> getTransactionHistoryForUser(User user) {
        List<String> transactionHistory = new ArrayList<>();
        List<Expense> expenses = expenseRepository.findAllByPayerOrParticipantsContaining(user, user);

        for (Expense expense : expenses) {
            StringBuilder transactionEntry = new StringBuilder();
            transactionEntry.append("Expense Name: ").append(expense.getExpenseName()).append("\n");
            transactionEntry.append("Description: ").append(expense.getDescription()).append("\n");
            transactionEntry.append("Amount: ").append(expense.getAmount()).append("\n");
            transactionEntry.append("Type: ").append(expense.getType()).append("\n");
            transactionEntry.append("Payer: ").append(expense.getPayer().getName()).append("\n");

            List<User> participants = expense.getParticipants();
            transactionEntry.append("Participants: ");
            for (User participant : participants) {
                transactionEntry.append(participant.getName()).append(", ");
            }
            transactionEntry.deleteCharAt(transactionEntry.length() - 1); // Remove trailing comma
            transactionEntry.deleteCharAt(transactionEntry.length() - 1); // Remove trailing space
            transactionEntry.append("\n");

            // Add more details if needed
            // transactionEntry.append("Notes: ").append(expense.getNotes()).append("\n");
            // transactionEntry.append("Images: ").append(expense.getImages()).append("\n");

            transactionHistory.add(transactionEntry.toString());
        }

        return transactionHistory;

    }

    @Override
    public List<Expense> getExpensesForUser(Long userId) {
        return expenseRepository.findAllByPayerIdOrParticipantsId(userId, userId);
    }

    private void simplifyBalances(Map<User, BigDecimal> balances) {
        for (User debtor : balances.keySet()) {
            BigDecimal debt = balances.get(debtor);
            if (debt.compareTo(BigDecimal.ZERO) < 0) { // User owes money
                for (User creditor : balances.keySet()) {
                    if (creditor.equals(debtor)) {
                        continue; // Skip if debtor is the same as creditor
                    }
                    BigDecimal credit = balances.get(creditor);
                    if (credit.compareTo(BigDecimal.ZERO) > 0) { // Creditor has money to receive
                        BigDecimal transferAmount = debt.min(credit.abs());
                        balances.put(debtor, debt.add(transferAmount));
                        balances.put(creditor, credit.subtract(transferAmount));
                        // Log the simplified balance transfer
                        System.out.println(debtor.getName() + " owes " + transferAmount + " to " + creditor.getName());
                        if (debt.compareTo(BigDecimal.ZERO) == 0) {
                            break; // If debt is cleared, move to the next debtor
                        }
                    }
                }
            }
        }

    }


    private void splitEqually(Expense expense, BigDecimal totalAmount, int numParticipants) {
        BigDecimal amountPerParticipant = totalAmount.divide(BigDecimal.valueOf(numParticipants), 2, RoundingMode.HALF_UP);
        Map<User, BigDecimal> balancesToUpdate = new HashMap<>();

        for (User participant : expense.getParticipants()) {
            balancesToUpdate.put(participant, amountPerParticipant);
        }

        updateBalances(balancesToUpdate);
    }


    private void splitByPercentage(Expense expense, BigDecimal totalAmount) {
        BigDecimal totalPercentage = BigDecimal.ZERO;
        Map<User, BigDecimal> balancesToUpdate = new HashMap<>();

        for (UserPercentage userPercentage : expense.getPercentages()) {
            BigDecimal percentageAmount = totalAmount.multiply(userPercentage.getPercentage().divide(BigDecimal.valueOf(100)));
            balancesToUpdate.put(userPercentage.getUser(), percentageAmount);
            totalPercentage = totalPercentage.add(userPercentage.getPercentage());
        }

        // Validate that the total percentage equals 100
        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Total percentage does not equal 100");
        }

        updateBalances(balancesToUpdate);
    }

    private void splitExact(Expense expense) {
        BigDecimal totalAmount = expense.getAmount();
        BigDecimal totalExactShares = BigDecimal.ZERO;

        for (User participant : expense.getParticipants()) {
            totalExactShares = totalExactShares.add(participant.getBalance());
        }

        if (totalExactShares.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException("Total sum of exact shares does not match the expense amount");
        }


    }

    private void updateBalances(Map<User, BigDecimal> balancesToUpdate) {
        for (Map.Entry<User, BigDecimal> entry : balancesToUpdate.entrySet()) {
            User user = entry.getKey();
            BigDecimal amount = entry.getValue();
            user.setBalance(user.getBalance().add(amount));
            userRepository.save(user);
        }
    }

//    private void sendEmailNotifications(List<User> participants, Expense expense) {
//        String emailSubject = "You've been added to an expense";
//        String emailText = "You've been added to an expense. The total amount you owe for this expense is: " + expense.getAmount();
//
//        for (User participant : participants) {
//            String participantEmail = participant.getEmail();
//            emailService.sendEmail(participantEmail, emailSubject, emailText);
//        }
//    }
}