package com.octopod.event.test;

import com.octopod.event.Event;
import com.octopod.event.EventBus;
import com.octopod.event.EventWaiter;
import com.octopod.event.EventHandler;
import org.junit.Assert;
import org.junit.Test;

public class EventCounterTests
{
	public static class EmptyEvent extends Event
	{

	}

	public static EventWaiter<EmptyEvent> handler = new EventWaiter<>(new EventHandler<EmptyEvent>()
	{
		@Override
		public void handle(EmptyEvent event)
		{
		}

		@Override
		public Class<EmptyEvent> getEventType()
		{
			return EmptyEvent.class;
		}
	});

	@Test
	public void benchmarkEventCounter() throws InterruptedException
	{
		EventBus eventBus = new EventBus();

		eventBus.registerHandler(handler);

		//constantly post events
		Thread t = new Thread(() ->
			{
				while(!Thread.currentThread().isInterrupted())
				{
					eventBus.postEvent(new EmptyEvent());
				}
			}
		);

		t.start();

		Assert.assertFalse(testEventCounterFor(eventBus, 1000).wasTimedOut());

		t.interrupt();
	}

	public EventWaiter.Result testEventCounterFor(EventBus eventBus, final int invocations) throws InterruptedException
	{
		EventWaiter.Result result = handler.waitFor(invocations, 1000);
		if(result.wasTimedOut())
		{
			System.out.println(result.getDesiredInvocations() + " invocations: TIMED OUT");
		}
		else
		{
			System.out.println(result.getDesiredInvocations() + " invocations: " + result.getElapsedTime() + " ms");
		}
		return result;
	}
}