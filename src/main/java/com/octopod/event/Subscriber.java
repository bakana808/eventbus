package com.octopod.event;

/**
 * <h2>An interface for event subscribers.</h2>
 * <p>
 * When an Event is posted, all registered Subscribers of the same type are invoked.
 * </p>
 * <p>
 * When registering a Subscriber, there are three ways for the EventBus to know its type:
 * <ul>
 *     <li>
 *         The argument type of <code>handle()</code> is defined explicitly.
 *     </li>
 *     <li>
 *         The <code>Subscriber</code> overrides <code>getType()</code> (which returns Event.class by default),
 *         in which case the argument type of <code>handle()</code> does not need to be defined explicitly.
 *     </li>
 *     <li>
 *         The <code>Subscriber</code> is an instance of <code>ExplicitSubscriber</code>, in which case the
 *         argument type of <code>handle()</code> does not need to be defined explicitly.
 *     </li>
 * </ul>
 * </p>
 *
 * @author octopod
 */
public interface Subscriber<E extends Event>
{
	public void handle(E event);

	@SuppressWarnings("unchecked")
	public default Class<E> getType() {return (Class<E>) Event.class;}
}