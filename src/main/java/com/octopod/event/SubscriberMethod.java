package com.octopod.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * An implementation of Subscriber for cached Subscriber methods.
 * @author octopod
 */
class SubscriberMethod<E extends Event> implements Subscriber<E>
{
	/**
	 * Returns if the provided method is a Subscriber.
	 * A method is a subscriber if it:
	 * <ul>
	 *     <li>is public.</li>
	 *     <li>returns <code>void</code>.</li>
	 *     <li>has one argument, a subclass of <code>Event</code>.</li>
	 * </ul>
	 *
	 * @param method the method
	 * @return true if this method is a Subscriber.
	 */
	public static boolean isSubscriber(Method method)
	{
		return (
			Modifier.isPublic(method.getModifiers()) &&					// method is public
			method.getReturnType().equals(Void.TYPE) &&					// method returns void
			method.getParameterTypes().length == 1 &&					// method has one argument
			method.getParameterTypes()[0] != Event.class &&				// first argument is not Event
			Event.class.isAssignableFrom(method.getParameterTypes()[0])	// first argument is subclass of Event
		);
	}

	/**
	 * The Subscriber Method.
	 */
	final Method method;

	/**
	 * The instance to invoke this Method on.
	 */
	final Object instance;

	public SubscriberMethod(Object instance, Method method)
	{
		if(!isSubscriber(method))
		{
			throw new IllegalArgumentException("This method does not meet the requirements for being a SubscriberMethod.");
		}
		this.method = method;
		this.instance = instance;
	}

	@Override
	public void handle(E event)
	{
		try
		{
			method.invoke(instance, event);
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object other)
	{
		return (
			other != null &&	// not null
			other == this ||	// it's this instance or
			(
				other instanceof SubscriberMethod &&			// it's a SubscriberMethod instance
				((SubscriberMethod)other).method == method &&	// it's the same method
				((SubscriberMethod)other).instance == instance	// it's the same object instance
			)
		);
	}
}
