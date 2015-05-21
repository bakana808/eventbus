package com.octopod.event;

import java.lang.reflect.Method;

/**
 * An interface for event handlers.
 * TODO: Add priority support for event handlers.
 *
 * @author octopod
 */
public interface EventHandler<E extends Event> //implements Comparable<EventHandler>
{
	public void handle(E event);

	@SuppressWarnings("unchecked")
	public default Class<E> getEventType()
	{
		Class<? extends Event> type = null;

		for(Method method: this.getClass().getMethods())
		{
			if(method.getName().equals("handle"))
			{
				type = (Class<? extends Event>)method.getParameterTypes()[0];
				System.out.println("type of handler: " + type);
			}
		}

		if(type == null)
		{
			throw new NullPointerException("Couldn't find the 'handle' method in this handler.");
		}
		else
		{
			return (Class<E>)type;
		}
	}
}
