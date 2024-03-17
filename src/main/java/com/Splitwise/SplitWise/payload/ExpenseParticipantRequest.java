package com.Splitwise.SplitWise.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseParticipantRequest {
    private Long userId;
    private BigDecimal share;
}
