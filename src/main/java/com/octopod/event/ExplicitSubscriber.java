package com.octopod.event;

/**
 * A Subscriber wrapper that explicitly defines the Subscriber's type.
 *
 * @param <E>
 */
public class ExplicitSubscriber<E extends Event> implements Subscriber<E>
{
	Class<E> type;
	Subscriber<E> subscriber;

	public ExplicitSubscriber(Class<E> type, Subscriber<E> subscriber)
	{
		this.type = type;
		this.subscriber = subscriber;
	}

	@Override
	public void handle(E event)
	{
		subscriber.handle(event);
	}

	@Override
	public Class<E> getType()
	{
		return type;
	}
}
