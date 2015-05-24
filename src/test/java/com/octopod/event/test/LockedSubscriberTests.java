package com.octopod.event.test;

import com.octopod.event.*;
import com.octopod.event.test.Events.EventA;
import org.junit.Assert;
import org.junit.Test;

public class LockedSubscriberTests
{
	public static LockedSubscriber<EventA> subscriber = new LockedSubscriber<>(EventA.class, e -> {});

	@Test
	public void benchmarkEventCounter() throws InterruptedException
	{
		EventBus eventBus = new EventBus();

		eventBus.register(subscriber);

		//constantly post events
		Thread t = new Thread(() ->
			{
				while(!Thread.currentThread().isInterrupted())
				{
					eventBus.post(new EventA());
				}
			}
		);

		t.start();

		Assert.assertFalse(testEventCounterFor(1000).wasTimedOut());

		t.interrupt();
	}

	public LockedSubscriber.Result testEventCounterFor(final int invocations) throws InterruptedException
	{
		LockedSubscriber.Result result = subscriber.waitFor(invocations, 1000);
		if(result.wasTimedOut())
		{
			System.out.println(result.getDesiredInvocations() + " invocations: TIMED OUT (" + result.getTotalInvocations() + ", " + result.getElapsedTime() + " ms)");
		}
		else
		{
			System.out.println(result.getDesiredInvocations() + " invocations: " + result.getElapsedTime() + " ms");
		}
		return result;
	}
}