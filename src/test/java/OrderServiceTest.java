// Author: Sara Echeverría
package com.discounts.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import com.discounts.dto.OrderRequest;
import com.discounts.dto.OrderResponse;
import com.discounts.exception.ResourceNotFoundException;
import com.discounts.model.Order;
import com.discounts.model.User;
import com.discounts.repository.OrderRepository;
import com.discounts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private OrderService orderService;

    private User regularUser;
    private User vipUser;

    @BeforeEach
    void setUp() {
        regularUser = new User(1L, "user1", "password", false);
        vipUser = new User(2L, "vipuser", "password", true);
    }

    private OrderResponse createOrder(User user, String amount) {
        OrderRequest request = new OrderRequest();
        request.setAmount(new BigDecimal(amount));
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });

        return orderService.createOrder(user.getUsername(), request);
    }

    // positive cases & business rules
    @Test
    @DisplayName("regular user without discount - amount less than 1000")
    void testRegularUserNoDiscount() {
        OrderResponse response = createOrder(regularUser, "500.00");

        assertEquals(new BigDecimal("500.00"), response.getOriginalAmount());
        assertEquals(BigDecimal.ZERO, response.getDiscountPercentage());
        assertEquals(new BigDecimal("500.00"), response.getFinalAmount());
        assertFalse(response.isVipDiscount());
        assertFalse(response.isAmountDiscount());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("VIP user receives 20% discount - amount less than 1000")
    void testVipUser20PercentDiscount() {
        OrderResponse response = createOrder(vipUser, "500.00");

        assertEquals(new BigDecimal("500.00"), response.getOriginalAmount());
        assertEquals(new BigDecimal("20"), response.getDiscountPercentage(), 
                     "VIP discount should be 20% but did not get that value from code");
        assertEquals(new BigDecimal("400.00"), response.getFinalAmount());
        assertTrue(response.isVipDiscount());
        assertFalse(response.isAmountDiscount());
    }

    @Test
    @DisplayName("regular user with amount > 1000 receives 5% discount")
    void testRegularUser5PercentDiscount() {
        OrderResponse response = createOrder(regularUser, "1500.00");

        assertEquals(new BigDecimal("1500.00"), response.getOriginalAmount());
        assertEquals(new BigDecimal("5"), response.getDiscountPercentage());
        assertEquals(new BigDecimal("1425.00"), response.getFinalAmount());
        assertFalse(response.isVipDiscount());
        assertTrue(response.isAmountDiscount());
    }

    @Test
    @DisplayName("VIP user with amount > 1000 receives 25% total")
    void testVipUserCombinedDiscount() {
        OrderResponse response = createOrder(vipUser, "2000.00");

        assertEquals(new BigDecimal("2000.00"), response.getOriginalAmount());
        assertEquals(new BigDecimal("25"), response.getDiscountPercentage(),
                     "Total discount should be 25% (20% VIP + 5% amount), but code did not return that value");
        assertEquals(new BigDecimal("1500.00"), response.getFinalAmount());
        assertTrue(response.isVipDiscount());
        assertTrue(response.isAmountDiscount());
    }

    // boundary cases
    @Test
    @DisplayName("amount exactly = 1000 (should NOT apply additional discount)")
    void testAmountEquals1000() {
        OrderResponse response = createOrder(regularUser, "1000.00");

        assertEquals(new BigDecimal("1000.00"), response.getOriginalAmount());
        assertEquals(BigDecimal.ZERO, response.getDiscountPercentage());
        assertEquals(new BigDecimal("1000.00"), response.getFinalAmount());
        assertFalse(response.isAmountDiscount());
    }

    @Test
    @DisplayName("amount = 1000.01 (should apply additional discount)")
    void testAmountJustOver1000() {
        OrderResponse response = createOrder(regularUser, "1000.01");

        assertTrue(response.isAmountDiscount());
        assertEquals(new BigDecimal("5"), response.getDiscountPercentage());
    }

    @Test
    @DisplayName("very small amount (0.01)")
    void testMinimumAmount() {
        OrderResponse response = createOrder(regularUser, "0.01");

        assertNotNull(response);
        assertEquals(new BigDecimal("0.01"), response.getOriginalAmount());
        assertEquals(new BigDecimal("0.01"), response.getFinalAmount());
    }

    @Test
    @DisplayName("VIP user with amount exactly = 1000")
    void testVipUserAt1000Threshold() {
        OrderResponse response = createOrder(vipUser, "1000.00");

        assertEquals(new BigDecimal("20"), response.getDiscountPercentage());
        assertTrue(response.isVipDiscount());
        assertFalse(response.isAmountDiscount());
    }

    // read operations
    @Test
    @DisplayName("get all orders for a user returns correct list")
    void testGetAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUsername("user1");
        order1.setOriginalAmount(new BigDecimal("500.00"));
        order1.setDiscountPercentage(BigDecimal.ZERO);
        order1.setFinalAmount(new BigDecimal("500.00"));
        order1.setVipDiscount(false);
        order1.setAmountDiscount(false);
        
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUsername("user1");
        order2.setOriginalAmount(new BigDecimal("1500.00"));
        order2.setDiscountPercentage(new BigDecimal("5"));
        order2.setFinalAmount(new BigDecimal("1425.00"));
        order2.setVipDiscount(false);
        order2.setAmountDiscount(true);
        
        when(orderRepository.findByUsername("user1")).thenReturn(Arrays.asList(order1, order2));
        
        List<OrderResponse> responses = orderService.getAllOrders("user1");
        
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
        verify(orderRepository, times(1)).findByUsername("user1");
    }

    // negative cases
    @Test
    @DisplayName("user does not exist")
    void testUserNotFound() {
        OrderRequest request = new OrderRequest();
        request.setAmount(new BigDecimal("500.00"));
        
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.createOrder("nonexistent", request)
        );
        
        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("get order that does not exist")
    void testOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.getOrder(999L)
        );
        
        assertEquals("Orden no encontrada", exception.getMessage());
    }
}