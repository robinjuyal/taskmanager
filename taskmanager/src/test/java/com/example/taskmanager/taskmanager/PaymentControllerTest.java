package com.paymentApi;

import com.paymentApi.controller.PaymentController;
import com.paymentApi.request.PaymentServiceRequest;
import com.paymentApi.response.PaymentServiceResponse;
import com.paymentApi.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetServiceSuccessfulPayment() {
        // Arrange
        PaymentServiceRequest request = createMockPaymentServiceRequest();

        PaymentServiceResponse mockResponse = PaymentServiceResponse.success(
                new PaymentServiceResponse.ResponseData()
        );

        when(paymentService.processPayment(request)).thenReturn(mockResponse);

        // Act
        ResponseEntity<PaymentServiceResponse> responseEntity = paymentController.getService(request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("success", responseEntity.getBody().getStatus());

        // Verify that processPayment was called once
        verify(paymentService, times(1)).processPayment(request);
    }

    @Test
    void testGetServiceErrorScenario() {
        // Arrange
        PaymentServiceRequest request = createMockPaymentServiceRequest();

        List<String> errorMessages = Arrays.asList("Payment failed", "Insufficient funds");
        PaymentServiceResponse mockErrorResponse = PaymentServiceResponse.error(errorMessages);

        when(paymentService.processPayment(request)).thenReturn(mockErrorResponse);

        // Act
        ResponseEntity<PaymentServiceResponse> responseEntity = paymentController.getService(request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("error", responseEntity.getBody().getStatus());
        assertEquals(errorMessages, responseEntity.getBody().getMessages());

        // Verify that processPayment was called once
        verify(paymentService, times(1)).processPayment(request);
    }

    @Test
    void testGetServiceWithMinimalRequest() {
        // Arrange
        PaymentServiceRequest request = new PaymentServiceRequest();
        request.setRequestMethod("TEST");

        PaymentServiceResponse mockResponse = PaymentServiceResponse.success(
                new PaymentServiceResponse.ResponseData()
        );

        when(paymentService.processPayment(request)).thenReturn(mockResponse);

        // Act
        ResponseEntity<PaymentServiceResponse> responseEntity = paymentController.getService(request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        // Verify that processPayment was called once
        verify(paymentService, times(1)).processPayment(request);
    }

    // Helper method to create a mock PaymentServiceRequest
    private PaymentServiceRequest createMockPaymentServiceRequest() {
        PaymentServiceRequest request = new PaymentServiceRequest();

        PaymentServiceRequest.Details details = new PaymentServiceRequest.Details();
        details.setUser("0103");
        details.setInFlag(false);

        PaymentServiceRequest.Us userService = new PaymentServiceRequest.Us();
        userService.setCode("UCC_CODE01");

        request.setDetails(details);
        request.setUs(userService);
        request.setOrderIds(Arrays.asList(50000123L, 50000124L));
        request.setRequestMethod("pg_page");
        request.setPaymentMode(Arrays.asList("upi", "net_banking", "mandate"));
        request.setLoopbackUrl("");

        return request;
    }
}
