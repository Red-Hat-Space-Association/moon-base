package com.redhat.rover.carsim.drivers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.rover.carsim.CarEvent;
import com.redhat.rover.carsim.CarEventListener;
import com.redhat.rover.carsim.routes.Route;
import com.redhat.rover.carsim.routes.RoutePoint;

public class Driver implements Runnable{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);
	private final List<CarEventListener> listeners = new ArrayList<>();
	private Route route;
	
	private Optional<RoutePoint> lastPoint = Optional.empty();
	private final DrivingStrategy drivingStrategy;
	private final Optional<DriverMetrics> metrics;
	private final UUID id;
	private final String routeName;
	private final boolean repeat;
	private final long startDelay;
	
	private ZonedDateTime start;
	private ZonedDateTime end;
	
	private Driver(Builder builder) {
		this.route = builder.route;
		this.drivingStrategy = builder.drivingStrategy;
		this.routeName = builder.route.getName();
		this.repeat = builder.repeat;
		this.startDelay = builder.startDelay;
		this.metrics = Optional.ofNullable(builder.metrics);
		this.id = builder.id;
		if(!drivingStrategy.supports(builder.route)) {
			throw new RouteNotSupportedException("Route not supported for driving strategy", builder.route);
		}
	}

	
	public void registerCarEventListener(CarEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void run() {
		try {
			delayIfNecessary();
		} catch (InterruptedException e) {
    		LOGGER.warn("Error delaying route by pausing thread", e);
    		Thread.currentThread().interrupt();
		}
		metrics.ifPresent(DriverMetrics::incrementCarsDriving);
		setStart(ZonedDateTime.now());
		LOGGER.debug("I am driving route {}", route.getName());
		do {
			route.getPoints().forEach(to -> {
				LOGGER.debug("to {}", to);
				drivingStrategy.drive(lastPoint, to, this::notifyListeners);
				metrics.ifPresent(DriverMetrics::incrementPointsVisited);
				lastPoint = Optional.of(to);
			});
			lastPoint = Optional.empty();
		} while (repeat);
		metrics.ifPresent(DriverMetrics::decrementCarsDriving);
		setEnd(ZonedDateTime.now());
	}
	
	private void notifyListeners(CarEvent event) {
		for (CarEventListener carEventListener : listeners) {
			carEventListener.update(event);
		}
	}
	
	private void delayIfNecessary() throws InterruptedException {
		if (startDelay > 0) {
			LOGGER.info("Next car starts with delay of {}ms", startDelay);
			TimeUnit.MILLISECONDS.sleep(startDelay);
		}
	}

	public UUID getId() {
		return id;
	}

	public String getRouteName() {
		return routeName;
	}
	
	public ZonedDateTime getStart() {
		return start;
	}
	public void setStart(ZonedDateTime start) {
		this.start = start;
	}
	public ZonedDateTime getEnd() {
		return end;
	}
	public void setEnd(ZonedDateTime end) {
		this.end = end;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Route route;
		private DrivingStrategy drivingStrategy;
		private DriverMetrics metrics;
		private long startDelay = 0;
		private boolean repeat;
		private UUID id;

		private Builder() {
		}

		public Builder withRoute(Route route) {
			this.route = route;
			return this;
		}

		public Builder withDrivingStrategy(DrivingStrategy drivingStrategy) {
			this.drivingStrategy = drivingStrategy;
			return this;
		}
		
		public Builder withMetrics(DriverMetrics metrics) {
			this.metrics = metrics;
			return this;
		}

		public Builder withRepeat(boolean repeat) {
			this.repeat = repeat;
			return this;
		}
		
		public Builder withStartDelay(long startDelay) {
			this.startDelay = startDelay;
			return this;
		}
		
		public Builder withId(UUID id) {
			this.id = id;
			return this;
		}

		public Driver build() {
			return new Driver(this);
		}
	}

}
