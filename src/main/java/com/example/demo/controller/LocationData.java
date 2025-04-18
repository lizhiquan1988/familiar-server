package com.example.demo.controller;

import lombok.Getter;

public class LocationData {
	@Getter
    public static double latitude;
	@Getter
    public static double longitude;

    public static void setLatitude(double latitude) {
		LocationData.latitude = latitude;
	}

    public static void setLongitude(double longitude) {
		LocationData.longitude = longitude;
	}
	
	

}
