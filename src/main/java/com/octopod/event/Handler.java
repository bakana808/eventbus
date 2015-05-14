package com.octopod.event;

/**
 * An interface for event handlers.
 * TODO: Add priority support for event handlers.
 *
 * @author octopod
 */
public interface Handler<E extends Event> //implements Comparable<EventHandler>
{
	public void handle(E event);

	public Class<E> getEventType();
}
