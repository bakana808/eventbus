package com.octopod.event.test;

import com.octopod.event.*;
import com.octopod.event.test.Events.*;
import org.junit.Assert;
import org.junit.Test;

public class SubscriberTests
{
	//a subscriber class
	public static Subscriber<EventA> handler = new Subscriber<EventA>()
	{
		@Override
		public void handle(EventA event)
		{
			System.out.println("subscriber class handled");
		}
	};

	//a subscriber counter class
	public static Subscriber<EventA> counter = new LockedSubscriber<>(new Subscriber<EventA>()
	{
		@Override
		public void handle(EventA event)
		{
			System.out.println("subscriber counter class handled");
		}
	});

	//a subscriber container
	public static class HandlerContainer
	{
		public void onEvent(EventA event)
		{
			System.out.println("subscriber method invoked");
		}
	}

	@Test
	public void testSubscriberTypes()
	{
		Subscriber<EventA> a = new Subscriber<EventA>()
		{
			public void handle(EventA event) { }
		};

		Subscriber<EventB> b = new LockedSubscriber<>(EventB.class, e -> {});
		Subscriber<EventC> c = new ExplicitSubscriber<>(EventC.class, e -> {});

		Assert.assertEquals(EventA.class, SubscriberTypeResolver.bySubscriber(a));
		Assert.assertEquals(EventB.class, SubscriberTypeResolver.bySubscriber(b));
		Assert.assertEquals(EventC.class, SubscriberTypeResolver.bySubscriber(c));
	}

	@Test
	public void testEventHandlers()
	{
		EventBus eventBus = new EventBus();

		eventBus.register(handler);
		eventBus.register(counter, EventA.class);
		eventBus.register(new HandlerContainer());

		eventBus.post(new EventA());
	}

	public static Subscriber<EventA> genericHandler = new Subscriber<EventA>()
	{
		@Override
		public void handle(EventA event) { }
	};

	@Test
	public void testEventGenerics()
	{
		Assert.assertEquals(EventA.class, SubscriberTypeResolver.bySubscriber(genericHandler));
	}
}
