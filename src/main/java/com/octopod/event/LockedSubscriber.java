package com.octopod.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An Subscriber wrapper that waits for a Subscriber to be invoked.
 *
 * @author octopod
 */
public class LockedSubscriber<E extends Event> implements Subscriber<E>
{
	private final Lock lock = new ReentrantLock();
	private final Condition wait = lock.newCondition();

	/**
	 * The subscriber that is being wrapped.
	 */
	private final Subscriber<E> subscriber;

	private final Class<E> type;

	/**
	 * Generic LockedSubscriber constructor.
	 * <p>
	 * 		Tries to get the type of <code>subscriber</code> using
	 * </p>
	 * @param subscriber
	 */
	public LockedSubscriber(Subscriber<E> subscriber)
	{
		this.subscriber = subscriber;
		this.type = SubscriberTypeResolver.bySubscriber(subscriber);
	}

	public LockedSubscriber(Class<E> type, Subscriber<E> subscriber)
	{
		this.subscriber = subscriber;
		this.type = type;
	}

	@Override
	public void handle(E event)
	{
		lock.lock();

		subscriber.handle(event);
		wait.signalAll();

		lock.unlock();
	}

	@Override
	public Class<E> getType()
	{
		return type;
	}

	/**
	 * Hopefully this gets inlined or something.
	 *
	 * @return the current time in MS
	 */
	private static long time() { return System.currentTimeMillis(); }

	/**
	 * A class containing the results of a waitFor().
	 * Includes time elapsed, total/desired invocations, and if the request timed out.
	 */
	public static class ConditionResult
	{
		long elapsedTime;
		boolean timedOut;

		protected ConditionResult(long elapsedTime, boolean timedOut)
		{
			this.elapsedTime = elapsedTime;
			this.timedOut = timedOut;
		}

		public long getElapsedTime()
		{
			return elapsedTime;
		}

		public boolean wasTimedOut()
		{
			return timedOut;
		}
	}

	public static class InvocationResult extends ConditionResult
	{
		int invocations;
		int desiredInvocations;

		protected InvocationResult(long elapsedTime, boolean timedOut, int invocations, int desiredInvocations)
		{
			super(elapsedTime, timedOut);
			this.invocations = invocations;
			this.desiredInvocations = desiredInvocations;
		}

		public int getTotalInvocations()
		{
			return invocations;
		}

		public int getDesiredInvocations()
		{
			return desiredInvocations;
		}
	}

	/**
	 * Waits for the subscriber to be invoked a variable amount of times.
	 * Use <code>timeout</code> (in ms) to timeout afterwards and return anyway.
	 *
	 * @param timeout the timeout, in ms
	 * @return whether the listener successfully did its executions within the timeout
	 */
	public final InvocationResult waitFor(int invocations, long timeout) throws InterruptedException
	{
		long startTime = time();
		int invokeCount = 0;

		while(invokeCount < invocations)
		{
			lock.lock();

			if(wait.await(timeout, TimeUnit.MILLISECONDS)) // invoked
			{
				invokeCount++;
			}
			else // timed out
			{
				return new InvocationResult(time() - startTime, true, invokeCount, invocations);
			}

			lock.unlock();
		}
		return new InvocationResult(time() - startTime, false, invokeCount, invocations);
	}

	/**
	 * A shortcut for <code>waitFor(1, long)</code>, in which it will wait for this subscriber to
	 * be invoked once.
	 *
	 * @see #waitFor(int, long)
	 *
	 * @param timeout	the amount of time (in ms) to wait until this method times out
	 *
	 * @return a {@link InvocationResult}
	 *
	 * @throws InterruptedException
	 */
	public final InvocationResult waitFor(long timeout) throws InterruptedException
	{
		return waitFor(1, timeout);
	}

	/**
	 * An alias for <code>waitFor(int, long)</code>, in which it will swallow the
	 * <code>InterruptedException</code> potentially thrown by it. If this happens,
	 * this method will return null.
	 *
	 * @param invocations	the number of invocations to wait for
	 * @param timeout		the amount of time (in ms) to wait until this method times out
	 *
	 * @return a {@link InvocationResult}
	 */
	public final InvocationResult waitForQuietly(int invocations, long timeout)
	{
		try
		{
			return waitFor(invocations, timeout);
		}
		catch(InterruptedException e)
		{
			return null;
		}
	}

	public final InvocationResult waitForQuietly(long timeout)
	{
		return waitForQuietly(1, timeout);
	}

	/**
	 * Waits for a condition to be true.
	 *
	 * @param condition
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 */
	public final ConditionResult waitFor(boolean condition, long timeout) throws InterruptedException
	{
		long startTime = time();

		while(!condition)
		{
			lock.lock();

			if(!wait.await(timeout, TimeUnit.MILLISECONDS)) // timed out
			{
				return new ConditionResult(time() - startTime, true);
			}
		}

		return new ConditionResult(time() - startTime, false);
	}

	public final ConditionResult waitForQuietly(boolean condition, long timeout)
	{
		try
		{
			return waitFor(condition, timeout);
		}
		catch(InterruptedException e)
		{
			return null;
		}
	}

	public final Thread waitForAsync(final int invocations, final long timeout)
	{
		Thread t = new Thread()
		{
			public void run()
			{
				waitForQuietly(invocations, timeout);
			}
		};
		t.start();
		return t;
	}

	public final Thread waitForAsync(final long timeout)
	{
		return waitForAsync(1, timeout);
	}


}
