package com.octopod.event.test;

import com.octopod.event.*;
import org.junit.Test;

public class EventHandlerTests
{
	public static class EmptyEvent extends Event
	{

	}

	//a handler class
	public static Handler<EmptyEvent> handler = new Handler<EmptyEvent>()
	{
		@Override
		public void handle(EmptyEvent event)
		{
			System.out.println("handler class handled");
		}

		@Override
		public Class<EmptyEvent> getEventType()
		{
			return EmptyEvent.class;
		}
	};

	//a handler counter class
	public static Handler<EmptyEvent> counter = new EventWaiter<>(new Handler<EmptyEvent>()
	{
		@Override
		public void handle(EmptyEvent event)
		{
			System.out.println("handler counter class handled");
		}

		@Override
		public Class<EmptyEvent> getEventType()
		{
			return EmptyEvent.class;
		}
	});

	//a handler container
	public static class HandlerContainer
	{
		@EventHandler
		public void onEmptyEvent(EmptyEvent event)
		{
			System.out.println("handler method invoked");
		}
	}

	@Test
	public void testEventHandlers()
	{
		EventBus eventBus = new EventBus();

		eventBus.registerHandler(handler);
		eventBus.registerHandler(counter);
		eventBus.registerHandlers(new HandlerContainer());

		eventBus.postEvent(new EmptyEvent());
	}

}
