package guru.springframework.config.guards;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import guru.springframework.domain.PaymentEvent;
import guru.springframework.domain.PaymentState;
import guru.springframework.services.PaymentServiceImpl;

@Component
public class PaymentIdGuard implements Guard<PaymentState, PaymentEvent>{

	@Override
	public boolean evaluate(StateContext<PaymentState, PaymentEvent> context) {
		return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
	}

}
