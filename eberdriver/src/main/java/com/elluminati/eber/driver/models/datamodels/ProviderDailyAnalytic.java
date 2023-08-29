package com.elluminati.eber.driver.models.datamodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProviderDailyAnalytic{

	@SerializedName("rejection_ratio")
	private double rejectionRatio;

	@SerializedName("date_server_timezone")
	private String dateServerTimezone;

	@SerializedName("unique_id")
	private int uniqueId;

	@SerializedName("completed_ratio")
	private double completedRatio;

	@SerializedName("online_times")
	private List<Object> onlineTimes;

	@SerializedName("rejected")
	private int rejected;

	@SerializedName("cancellation_ratio")
	private double cancellationRatio;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("accepted")
	private int accepted;

	@SerializedName("date_tag")
	private String dateTag;

	@SerializedName("received")
	private int received;

	@SerializedName("completed")
	private int completed;

	@SerializedName("not_answered")
	private int notAnswered;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("total_online_time")
	private int totalOnlineTime;

	@SerializedName("provider_id")
	private String providerId;

	@SerializedName("cancelled")
	private int cancelled;

	@SerializedName("_id")
	private String id;

	@SerializedName("acception_ratio")
	private double acceptionRatio;

	public void setRejectionRatio(double rejectionRatio){
		this.rejectionRatio = rejectionRatio;
	}

	public double getRejectionRatio(){
		return rejectionRatio;
	}

	public void setDateServerTimezone(String dateServerTimezone){
		this.dateServerTimezone = dateServerTimezone;
	}

	public String getDateServerTimezone(){
		return dateServerTimezone;
	}

	public void setUniqueId(int uniqueId){
		this.uniqueId = uniqueId;
	}

	public int getUniqueId(){
		return uniqueId;
	}

	public void setCompletedRatio(double completedRatio){
		this.completedRatio = completedRatio;
	}

	public double getCompletedRatio(){
		return completedRatio;
	}

	public void setOnlineTimes(List<Object> onlineTimes){
		this.onlineTimes = onlineTimes;
	}

	public List<Object> getOnlineTimes(){
		return onlineTimes;
	}

	public void setRejected(int rejected){
		this.rejected = rejected;
	}

	public int getRejected(){
		return rejected;
	}

	public void setCancellationRatio(double cancellationRatio){
		this.cancellationRatio = cancellationRatio;
	}

	public double getCancellationRatio(){
		return cancellationRatio;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setAccepted(int accepted){
		this.accepted = accepted;
	}

	public int getAccepted(){
		return accepted;
	}

	public void setDateTag(String dateTag){
		this.dateTag = dateTag;
	}

	public String getDateTag(){
		return dateTag;
	}

	public void setReceived(int received){
		this.received = received;
	}

	public int getReceived(){
		return received;
	}

	public void setCompleted(int completed){
		this.completed = completed;
	}

	public int getCompleted(){
		return completed;
	}

	public void setNotAnswered(int notAnswered){
		this.notAnswered = notAnswered;
	}

	public int getNotAnswered(){
		return notAnswered;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public void setTotalOnlineTime(int totalOnlineTime){
		this.totalOnlineTime = totalOnlineTime;
	}

	public int getTotalOnlineTime(){
		return totalOnlineTime;
	}

	public void setProviderId(String providerId){
		this.providerId = providerId;
	}

	public String getProviderId(){
		return providerId;
	}

	public void setCancelled(int cancelled){
		this.cancelled = cancelled;
	}

	public int getCancelled(){
		return cancelled;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setAcceptionRatio(double acceptionRatio){
		this.acceptionRatio = acceptionRatio;
	}

	public double getAcceptionRatio(){
		return acceptionRatio;
	}

	@Override
 	public String toString(){
		return 
			"ProviderDailyAnalytic{" + 
			"rejection_ratio = '" + rejectionRatio + '\'' + 
			",date_server_timezone = '" + dateServerTimezone + '\'' + 
			",unique_id = '" + uniqueId + '\'' + 
			",completed_ratio = '" + completedRatio + '\'' + 
			",online_times = '" + onlineTimes + '\'' + 
			",rejected = '" + rejected + '\'' + 
			",cancellation_ratio = '" + cancellationRatio + '\'' + 
			",created_at = '" + createdAt + '\'' + 
			",accepted = '" + accepted + '\'' + 
			",date_tag = '" + dateTag + '\'' + 
			",received = '" + received + '\'' + 
			",completed = '" + completed + '\'' + 
			",not_answered = '" + notAnswered + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",total_online_time = '" + totalOnlineTime + '\'' + 
			",provider_id = '" + providerId + '\'' + 
			",cancelled = '" + cancelled + '\'' + 
			",_id = '" + id + '\'' + 
			",acception_ratio = '" + acceptionRatio + '\'' + 
			"}";
		}
}