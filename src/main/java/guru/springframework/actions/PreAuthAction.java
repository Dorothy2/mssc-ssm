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
public class PreAuthAction implements Action<PaymentState, PaymentEvent> {

	@Override
	public void execute(StateContext<PaymentState, PaymentEvent> context) {
		System.out.println("Preauth was called.");
		if (new Random().nextInt(10) < 8) {
			System.out.println("PreAuth Approved");
			context.getStateMachine()
					.sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
							.setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
									context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
							.build());
		} else {
			System.out.println("PreAuth Declined. No Credit.");
			context.getStateMachine()
					.sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
							.setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
									context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
							.build());
		}

	}

}
