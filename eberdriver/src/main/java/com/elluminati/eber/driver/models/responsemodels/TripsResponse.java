package com.elluminati.eber.driver.models.responsemodels;

import com.elluminati.eber.driver.models.datamodels.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TripsResponse{

	@SerializedName("trip_id")
	private String tripId;

	@SerializedName("destination_address")
	private String destinationAddress;

	@SerializedName("is_trip_end")
	private int isTripEnd;

	@SerializedName("success")
	private boolean success;

	@SerializedName("time_left_to_responds_trip")
	private int timeLeftToRespondsTrip;

	@SerializedName("source_address")
	private String sourceAddress;

	@SerializedName("destinationLocation")
	private List<Double> destinationLocation;

	@SerializedName("sourceLocation")
	private List<Double> sourceLocation;

	@SerializedName("message")
	private String message;

	@SerializedName("user")
	private User user;

	public void setTripId(String tripId){
		this.tripId = tripId;
	}

	public String getTripId(){
		return tripId;
	}

	public void setDestinationAddress(String destinationAddress){
		this.destinationAddress = destinationAddress;
	}

	public String getDestinationAddress(){
		return destinationAddress;
	}

	public void setIsTripEnd(int isTripEnd){
		this.isTripEnd = isTripEnd;
	}

	public int getIsTripEnd(){
		return isTripEnd;
	}

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setTimeLeftToRespondsTrip(int timeLeftToRespondsTrip){
		this.timeLeftToRespondsTrip = timeLeftToRespondsTrip;
	}

	public int getTimeLeftToRespondsTrip(){
		return timeLeftToRespondsTrip;
	}

	public void setSourceAddress(String sourceAddress){
		this.sourceAddress = sourceAddress;
	}

	public String getSourceAddress(){
		return sourceAddress;
	}

	public void setDestinationLocation(List<Double> destinationLocation){
		this.destinationLocation = destinationLocation;
	}

	public List<Double> getDestinationLocation(){
		return destinationLocation;
	}

	public void setSourceLocation(List<Double> sourceLocation){
		this.sourceLocation = sourceLocation;
	}

	public List<Double> getSourceLocation(){
		return sourceLocation;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setUser(User user){
		this.user = user;
	}

	public User getUser(){
		return user;
	}

	@Override
 	public String toString(){
		return 
			"TripsResponse{" + 
			"trip_id = '" + tripId + '\'' + 
			",destination_address = '" + destinationAddress + '\'' + 
			",is_trip_end = '" + isTripEnd + '\'' + 
			",success = '" + success + '\'' + 
			",time_left_to_responds_trip = '" + timeLeftToRespondsTrip + '\'' + 
			",source_address = '" + sourceAddress + '\'' + 
			",destinationLocation = '" + destinationLocation + '\'' + 
			",sourceLocation = '" + sourceLocation + '\'' + 
			",message = '" + message + '\'' + 
			",user = '" + user + '\'' + 
			"}";
		}
}