package com.redhat.bobbycar.carsim.clients.model;

import javax.json.bind.annotation.JsonbProperty;

public class KafkaCarPosition {
	
	@JsonbProperty("lat")
	private double latitude;
	@JsonbProperty("long")
	private double longitude;
	@JsonbProperty("elev")
	private double elevation;
	
	public KafkaCarPosition() {
		
	}
	
	public KafkaCarPosition(double latitude, double longitude, double elevation) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
}
