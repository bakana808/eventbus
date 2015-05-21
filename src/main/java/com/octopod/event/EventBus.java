package com.octopod.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The manager of event handlers. Register any event handlers to an instance of EventBus
 * and invoke them by posting an event.
 *
 * @author octopod
 */
public class EventBus
{
	private final Map<Class<? extends Event>, Set<EventHandler<?>>> handlerMap = new ConcurrentHashMap<>();

	private <E extends Event> void checkHandlers(Class<E> type)
	{
		if (!handlerMap.containsKey(type))
		{
			handlerMap.put(type, new HashSet<>());
		}
	}

	@SuppressWarnings("unchecked")
	private <E extends Event> Set<EventHandler<E>> getEventHandlers(Class<E> type)
	{
		checkHandlers(type);
		return (Set<EventHandler<E>>) (Set) handlerMap.get(type);
	}

	public void unregisterAll()
	{
		handlerMap.clear();
	}

	public void unregisterAll(Class<? extends Event> event)
	{
		if (handlerMap.containsKey(event))
		{
			handlerMap.get(event).clear();
		}
	}

	/**
	 * Registers a single event handler. Returns true if the handler was added.
	 *
	 * @param handler an event handler
	 * @param <E>     the type of event
	 */
	public <E extends Event> boolean registerHandler(EventHandler<E> handler)
	{
		return getEventHandlers(handler.getEventType()).add(handler);
	}

	/**
	 * Unregisters a single event handler. Returns true if the handler was removed.
	 *
	 * @param handler an event handler
	 * @param <E>     the type of event
	 */
	public <E extends Event> boolean unregisterHandler(EventHandler<E> handler)
	{
		return getEventHandlers(handler.getEventType()).remove(handler);
	}

	/**
	 * Registers multiple event handlers methods contained in an object
	 * with the @EventHandlerMethod annotation.
	 *
	 * @param object the object containing event handler methods
	 */
	public int registerHandlers(Object object)
	{
		Set<EventHandler<?>> handlers = findEventHandlers(object);

		AtomicInteger count = new AtomicInteger(0);

		handlers.stream().forEach((handler) -> {
			if (registerHandler(handler))
			{
				count.incrementAndGet();
			}
		});

		return count.get();
	}

	public int unregisterHandlers(Object object)
	{
		Set<EventHandler<?>> handlers = findEventHandlers(object);

		AtomicInteger count = new AtomicInteger(0);

		handlers.stream().forEach((handler) -> {
			if (registerHandler(handler))
			{
				count.incrementAndGet();
			}
		});

		return count.get();
	}

	@SuppressWarnings("unchecked")
	private static Set<EventHandler<?>> findEventHandlers(Object object)
	{
		Set<EventHandler<?>> handlers = new HashSet<>();
		for (Method method : object.getClass().getMethods())
		{
			if (isEventHandlerMethod(method))
			{
				Class<?> type = method.getParameterTypes()[0];
				handlers.add(new ReflectedHandler(type, object, method));
			}
		}

		return handlers;
	}

	@SuppressWarnings("unchecked")
	private static boolean isEventHandlerMethod(Method method)
	{
		return method.getReturnType().equals(Void.TYPE) && //Method returns VOID
				method.getParameterTypes().length == 1 && //Method has one argument
				Event.class.isAssignableFrom(method.getParameterTypes()[0]) && //first argument is event
				method.isAnnotationPresent(EventHandlerMethod.class); //Method has annotation
	}

	@SuppressWarnings("unchecked")
	public synchronized <E extends Event> void postEvent(final E event)
	{
		if (handlerMap.containsKey(event.getClass()) || handlerMap.containsKey(Event.class))
		{
			for (EventHandler<E> handler : getEventHandlers((Class<E>) event.getClass()))
			{
				handler.handle(event);
			}
		}
	}

}
