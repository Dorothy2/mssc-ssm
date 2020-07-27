package guru.springframework;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
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
		
		System.out.println("Should be PRE_AUTH or PRE_AUTH_ERROR");
		System.out.println(sm.getState().getId());
		System.out.println(preAuthedPayment);
		
		
	}
	
	@Transactional
	@Test
	@RepeatedTest(10)
	void testAuth() {
		Payment savedPayment = paymentService.newPayment(payment);
		StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
		Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

		if (sm.getState().getId() == PaymentState.PRE_AUTH) {
			System.out.println("Should be PRE_AUTH");
			System.out.println(savedPayment.getState());
			sm = paymentService.authorizePayment(savedPayment.getId());
			Payment authedPayment = paymentRepository.getOne(preAuthedPayment.getId());

			System.out.println("Should be AUTH_APPROVED or AUTH_DECLINED");
			System.out.println(sm.getState().getId());
			System.out.println(authedPayment);
		} else {
			System.out.println("Payment failed PRE-AUTH.");
		}

	}

}
