package com.Splitwise.SplitWise.Repository;

import com.Splitwise.SplitWise.Entity.Expense;
import com.Splitwise.SplitWise.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    List<Expense> findAllByPayerOrParticipantsContaining(User user, User user1);

    @Query("SELECT e FROM Expense e WHERE e.payer.id = ?1 OR EXISTS (SELECT 1 FROM e.participants p WHERE p.id = ?2)")
    List<Expense> findAllByPayerIdOrParticipantsId(Long payerId, Long participantId);
}
