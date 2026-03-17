package com.discounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String username;
    private BigDecimal originalAmount;
    private BigDecimal discountPercentage;
    private BigDecimal finalAmount;
    private boolean vipDiscount;
    private boolean amountDiscount;
    private LocalDateTime createdAt;

    public OrderResponse(Long id, String username, BigDecimal originalAmount, BigDecimal discountPercentage,
                         BigDecimal finalAmount, boolean vipDiscount, boolean amountDiscount, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.originalAmount = originalAmount;
        this.discountPercentage = discountPercentage;
        this.finalAmount = finalAmount;
        this.vipDiscount = vipDiscount;
        this.amountDiscount = amountDiscount;
        this.createdAt = createdAt;
    }
}
