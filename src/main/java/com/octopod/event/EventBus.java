package com.octopod.event;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * The manager of event handlers. Register any event handlers to an instance of EventBus
 * and invoke them by posting an event.
 *
 * @author octopod
 */
public class EventBus
{
	/**
	 * A map of Event types and Subscriber Sets.
	 * 
	 * That is, the Set of Subscribers set to an Event type
	 * is assumed to be used for that Event type.
	 */
	private final ConcurrentMap<Class<? extends Event>, Set<Subscriber<? extends Event>>> subscriberMap = new ConcurrentHashMap<>();

	/**
	 * Gets a Set of Subscribers by an Event.
	 *
	 * @param event the event
	 * @param <E> the type of event
	 * @return a Set of Subscribers
	 */
	@SuppressWarnings("unchecked")
	private <E extends Event> Set<Subscriber<E>> getSubscribers(E event)
	{
		return (Set<Subscriber<E>>) (Object) getSubscribers(event.getClass());
	}

	/**
	 * Gets a Set of Subscribers by an Event type.
	 *
	 * @param type the event class
	 * @param <E> the type of event
	 * @return a Set of Subscribers
	 */
	@SuppressWarnings("unchecked")
	private <E extends Event> Set<Subscriber<E>> getSubscribers(Class<E> type)
	{
		Set<Subscriber<? extends Event>> subscribers = subscriberMap.computeIfAbsent(type, k -> new HashSet<>());

		synchronized(subscriberMap)
		{
			return subscribers.stream().
				map(subscriber -> (Subscriber<E>)subscriber).
				collect(Collectors.toSet());
		}
	}

	/**
	 * Posts an Event to all Subscribers registered to that Event type.
	 *
	 * @param event the Event
	 * @param <E> the type of Event
	 */
	public synchronized <E extends Event> void post(final E event)
	{
		Set<Subscriber<E>> subscribers = getSubscribers(event);

		if(subscriberMap.containsKey(event.getClass()))
		{
			for(Subscriber<E> subscriber : subscribers)
			{
				subscriber.handle(event);
			}
		}
	}

	/**
	 * Unregisters all Subscribers, essentially resetting the EventBus.
	 */
	public void unregisterAll()
	{
		subscriberMap.clear();
	}

	/**
	 * Unregisters all Subscribers of a specific Event type.
	 * @param type the type of Event
	 */
	public void unregisterAll(Class<? extends Event> type)
	{
		subscriberMap.remove(type);
	}

	/**
	 * Registers a single event handler.
	 * Returns true if the handler was added.
	 *
	 * @param subscriber an event handler
	 */
	public <E extends Event> boolean register(Subscriber<E> subscriber)
	{
		return register(subscriber, SubscriberTypeResolver.bySubscriber(subscriber));
	}

	/**
	 * Registers a single event handler under an explicit Event type.
	 * Returns true if the handler was added.
	 *
	 * @param subscriber the Subscriber
	 * @param type the type of Event
	 */
	public <E extends Event> boolean register(Subscriber<E> subscriber, Class<E> type)
	{
		if(type == Event.class)
		{
			throw new IllegalArgumentException("The provided type is not a subclass of Event.");
		}

		return subscriberMap.computeIfAbsent(type, k -> new HashSet<>()).add(subscriber);
	}

	/**
	 * Registers all SubscriberMethods in an Object.
	 *
	 * @param object the Object
	 * @return the number of SubscriberMethods added
	 */
	@SuppressWarnings("unchecked")
	public int register(Object object)
	{
		int count = 0; // the number of Subscribers registered
		for (Method method : object.getClass().getMethods())
		{
			if (SubscriberMethod.isSubscriber(method))
			{
				if(register(new SubscriberMethod(object, method), SubscriberTypeResolver.byMethod(method)))
				{
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Unregisters a single event handler.
	 * Returns true if the handler was removed.
	 *
	 * @param subscriber an event handler
	 * @param <E>     the type of event
	 */
	public <E extends Event> boolean unregister(Subscriber<E> subscriber)
	{
		return unregister(subscriber, SubscriberTypeResolver.bySubscriber(subscriber));
	}

	public <E extends Event> boolean unregister(Subscriber<E> subscriber, Class<E> type)
	{
		if(type == Event.class)
		{
			throw new IllegalArgumentException("The provided type is not a subclass of Event.");
		}

		return getSubscribers(type).remove(subscriber);
	}

	@SuppressWarnings("unchecked")
	public int unregister(Object object)
	{
		int count = 0; // the number of Subscribers registered
		for (Method method : object.getClass().getMethods())
		{
			if (SubscriberMethod.isSubscriber(method))
			{
				if(unregister(new SubscriberMethod(object, method), SubscriberTypeResolver.byMethod(method)))
				{
					count++;
				}
			}
		}
		return count;
	}

}