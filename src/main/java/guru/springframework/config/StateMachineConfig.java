package guru.springframework.config;

import java.util.EnumSet;
import java.util.Random;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import guru.springframework.actions.AuthAction;
import guru.springframework.actions.AuthApprovedAction;
import guru.springframework.actions.AuthDeclinedAction;
import guru.springframework.actions.PreAuthAction;
import guru.springframework.actions.PreAuthApprovedAction;
import guru.springframework.actions.PreAuthDeclinedAction;
import guru.springframework.config.guards.PaymentIdGuard;
import guru.springframework.domain.PaymentEvent;
import guru.springframework.domain.PaymentState;
import guru.springframework.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableStateMachineFactory
@RequiredArgsConstructor
@Configuration
public class StateMachineConfig  extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent>{
	
		private final AuthAction authAction;
		private final AuthApprovedAction authApprovedAction;
		private final AuthDeclinedAction authDeclinedAction;
		private final PreAuthAction preAuthAction;
		private final PreAuthApprovedAction preAuthApprovedAction;
		private final PreAuthDeclinedAction preAuthDeclinedAction;
		private final PaymentIdGuard paymentIdGuard;

		@Override
		public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
			states.withStates()		
			.initial(PaymentState.NEW)
			.states(EnumSet.allOf(PaymentState.class))
			.end(PaymentState.AUTH)
			.end(PaymentState.PRE_AUTH_ERROR)
			.end(PaymentState.AUTH_ERROR);
		}
		
		 @Override
	    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
	        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
	        		.action(preAuthAction)
	        		.guard(paymentIdGuard)
	                .and()
	                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
	                .action(preAuthApprovedAction)
	                .and()
	                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
	                .action(preAuthDeclinedAction)
	        		.and()
	        		// pre-auth to auth
	        		.withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
	        		.action(authAction)
	        		.guard(paymentIdGuard)
	        		.and()
	                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
	                .action(authApprovedAction)
	                .and()
	                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED)
	                .action(authDeclinedAction);
	        		
	    }
		 
		 @Override
		 public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
			 StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<> () {
				 
				 @Override
				 public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
					 log.info(String.format("State changed from %s to %s", from, to));
				 
				 }
			 };
			 config.withConfiguration().listener(adapter);
		 }
		 
		public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
			 	return context -> {
				return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
		 	};
	 	}
		 
		public Action<PaymentState, PaymentEvent> preAuthAction() {
			return context -> {
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
			};
		}
		
		public Action<PaymentState, PaymentEvent> authAction() {
			return context -> {
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
			};
		}
	}