package guru.springframework;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import guru.springframework.domain.Payment;
import guru.springframework.domain.PaymentEvent;
import guru.springframework.domain.PaymentState;
import guru.springframework.repository.PaymentRepository;
import guru.springframework.services.PaymentService;

@SpringBootTest
class PaymentServiceImplTest {
	
	@Autowired
	PaymentService paymentService;
	
	@Autowired
	PaymentRepository paymentRepository;
	
	Payment payment;

	@BeforeEach
	void setUp() throws Exception {
		payment = Payment.builder()
				.amount(new BigDecimal("12.99")).build();
	}

	@Transactional
	@Test
	void testpreAuth() {
		Payment savedPayment = paymentService.newPayment(payment);
		
		System.out.println("Should be NEW");
		System.out.println(savedPayment.getState());
		
		StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
		Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());
		
		System.out.println("Should be PRE-AUTH");
		System.out.println(sm.getState().getId());
		System.out.println(preAuthedPayment);
		
		
	}

}
