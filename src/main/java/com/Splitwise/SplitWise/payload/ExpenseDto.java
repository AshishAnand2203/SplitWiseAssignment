package com.Splitwise.SplitWise.payload;

import com.Splitwise.SplitWise.Entity.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDto {
    private String description;
    private BigDecimal amount;
    private ExpenseType type;
    private String expenseName;
    private String notes;
    private String images;
    private Long payerId; // Assuming payerId is used to identify the payer
    private List<ExpenseParticipantRequest> participants;
}
