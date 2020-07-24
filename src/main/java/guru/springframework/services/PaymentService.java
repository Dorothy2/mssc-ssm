package guru.springframework.services;

import org.springframework.statemachine.StateMachine;

import guru.springframework.domain.Payment;
import guru.springframework.domain.PaymentEvent;
import guru.springframework.domain.PaymentState;

public interface PaymentService {

	Payment newPayment(Payment payment);
	
	StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);
	
	StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);
	
	StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
	
}
