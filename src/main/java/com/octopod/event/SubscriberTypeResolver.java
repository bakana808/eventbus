package com.octopod.event;

import java.lang.reflect.Method;

public class SubscriberTypeResolver
{
	/**
	 * Attempts to get the Event type of a SubscriberMethod by returning the type of
	 * the first parameter of the provided method using reflection.
	 * @param method
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Event> byMethod(Method method)
	{
		if(SubscriberMethod.isSubscriber(method))
		{
			return (Class<? extends Event>) method.getParameterTypes()[0];
		}
		throw new IllegalArgumentException("This method is not a SubscriberMethod.");
	}

	/**
	 * Attempts to get the Event type of a Subscriber by returning the type of
	 * the first parameter of <code>handle()</code> using reflection.
	 *
	 * Because this depends on a Subscriber explicitly defining a type of Event
	 * in its <code>handle()</code> method, it may not return what you'd expect.
	 *
	 * @see Subscriber
	 *
	 * @param subscriber the Subscriber
	 * @param <E> the type of Event
	 *
	 * @return the type of event.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Event> Class<E> bySubscriber(Subscriber<E> subscriber)
	{
		// try to get the type with the getType() method
		Class<? extends Event> type = subscriber.getType();

		if(type == Event.class) // the getType() method returned Event.class, so try to find it another way
		{
			// try to get the type reflectively

			for(Method method: subscriber.getClass().getMethods())
			{
				if(
					method.getName().equals("handle") &&
						!method.isBridge() &&
						!method.isSynthetic() &&
						method.getParameterTypes()[0] != Event.class
					)
				{
					type = (Class<? extends Event>) method.getParameterTypes()[0];
				}
			}

/*			// try to resolve type if SubscriberType is present

			if(subscriber.getClass().isAnnotationPresent(SubscriberType.class))
			{
				Class<? extends Event> explicitType = subscriber.getClass().getAnnotation(SubscriberType.class).value();

				if(explicitType == Event.class) // reject Event.class
				{
					throw new IllegalArgumentException("The value of SubscriberType must be a subclass of Event.");
				}

				if(type != Event.class && explicitType != type) // the reflected type isn't Event and it doesn't match SubscriberType
				{
					throw new IllegalArgumentException("The value of SubscriberType does not match the type of this Subscriber.");
				}

				type = explicitType;

				System.out.println("Annotated type is " + type.getName());
			}*/
		}

		return (Class<E>) type;
	}
}
