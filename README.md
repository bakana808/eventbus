EventBus
===
#### A minimal event system based on the EventBus model

Quick Start
---

 1. Create a new Event

	```
	public class ExampleEvent extends Event { }
	```

 2. Create a new EventBus

 	```java
	EventBus bus = new EventBus();
	```

 3. Create a new Subscriber

	```java
	// anonymous subscriber
	Subscriber<ExampleEvent> subscriber = new Subscriber<ExampleEvent>()
	{
		public void handle(ExampleEvent event) { }
	}

	// explicit anonymous subscriber
	Subscriber<ExampleEvent> subscriber = new ExplicitSubscriber<>(ExampleEvent.class, e -> {});

	// method subscriber
	public class Subscribers
	{
		public void onEvent(ExampleEvent event) { }
	}
	Subscribers subscriberContainer = new Subscribers();
	```
 4. Register the Subscriber

	```java
	bus.register(subscriber);
	bus.register(subscriberContainer);
	```
 5. Post the Event

	```java
	bus.post(new ExampleEvent());
	```

LockedSubscriber
---
This projects contains a special implementation of Subscriber that allows
you to wait until it handles an event a specific number of times.

This is the `LockedSubscriber`.

```java
Subscriber<ExampleEvent> subscriber = new LockedSubscriber<>(ExampleEvent.class, e -> { });

bus.register(subscriber);

try
{
	// waits one second for this subscriber to handle an event once
	subscriber.waitFor(1000);
}
catch(InterruptedException e) { }
```

This is useful for when Events are being posted on a different thread.
