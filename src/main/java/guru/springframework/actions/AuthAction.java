package guru.springframework.actions;

import java.util.Random;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import guru.springframework.services.PaymentServiceImpl;

import guru.springframework.domain.PaymentEvent;
import guru.springframework.domain.PaymentState;

@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {

	@Override
	public void execute(StateContext<PaymentState, PaymentEvent> context) {
		System.out.println("Auth was called.");
		if (new Random().nextInt(10) < 8) {
			System.out.println("Auth Approved");
			context.getStateMachine()
					.sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
							.setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
									context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
							.build());
		} else {
			System.out.println("Declined. No Credit.");
			context.getStateMachine()
					.sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
							.setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
									context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
							.build());
		}
		System.out.println("=========================");

	}
}
