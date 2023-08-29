package com.elluminati.eber.driver.models.responsemodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PickupLocations {

	@SerializedName("sourceLocation")
	private List<Double> sourceLocation;

	public void setSourceLocation(List<Double> sourceLocation){
		this.sourceLocation = sourceLocation;
	}

	public List<Double> getSourceLocation(){
		return sourceLocation;
	}

	@Override
 	public String toString(){
		return 
			"PickupLocations{" +
			"sourceLocation = '" + sourceLocation + '\'' + 
			"}";
		}
}