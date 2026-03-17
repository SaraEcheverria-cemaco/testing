package com.discounts.service;

import com.discounts.dto.OrderRequest;
import com.discounts.dto.OrderResponse;
import com.discounts.exception.ResourceNotFoundException;
import com.discounts.model.Order;
import com.discounts.model.User;
import com.discounts.repository.OrderRepository;
import com.discounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    private static final BigDecimal VIP_DISCOUNT = new BigDecimal("20");
    private static final BigDecimal AMOUNT_DISCOUNT = new BigDecimal("5");
    private static final BigDecimal AMOUNT_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Transactional
    public OrderResponse createOrder(String username, OrderRequest request) {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        BigDecimal originalAmount = request.getAmount();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        boolean vipDiscount = false;
        boolean amountDiscount = false;

        // Aplicar descuento VIP (20%)
        if (user.isVip()) {
            totalDiscount = totalDiscount.add(VIP_DISCOUNT);
            vipDiscount = true;
        }

        // Aplicar descuento adicional si el monto > 1000 (5%)
        if (originalAmount.compareTo(AMOUNT_THRESHOLD) > 0) {
            totalDiscount = totalDiscount.add(AMOUNT_DISCOUNT);
            amountDiscount = true;
        }

        // Calcular monto final
        BigDecimal discountAmount = originalAmount
                .multiply(totalDiscount)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        // Crear la orden
        Order order = new Order();
        order.setUsername(username);
        order.setOriginalAmount(originalAmount);
        order.setDiscountPercentage(totalDiscount);
        order.setFinalAmount(finalAmount);
        order.setVipDiscount(vipDiscount);
        order.setAmountDiscount(amountDiscount);

        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        return mapToResponse(order);
    }

    public List<OrderResponse> getAllOrders(String username) {
        List<Order> orders = orderRepository.findByUsername(username);
        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUsername(),
                order.getOriginalAmount(),
                order.getDiscountPercentage(),
                order.getFinalAmount(),
                order.isVipDiscount(),
                order.isAmountDiscount(),
                order.getCreatedAt()
        );
    }
}
