package guru.springframework.services;

import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import guru.springframework.domain.Payment;
import guru.springframework.domain.PaymentEvent;
import guru.springframework.domain.PaymentState;
import guru.springframework.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

	/**
	 * Before a state change, if a message is present, get the
	 * payment id off the header, get the payment from the repository, 
	 * update with the state change, and save it
	 */
	private final PaymentRepository paymentRepository;
	@Override
	public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
			Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine ) {
	
		Optional.ofNullable(message).ifPresent(msg -> {
			Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
		    .ifPresent(paymentId -> {
		    	// fetch payment from db
		    	Payment payment =  paymentRepository.getOne(paymentId);
		    	System.out.println("State before: " + payment.getState());
		    	System.out.println("State argument: " + state.getId());
		    	payment.setState(state.getId());
		    	System.out.println("State after: " + payment.getState());
		    	// update payment status
		    	paymentRepository.save(payment);
		    });
		
		
		});

	}
}
