package com.elluminati.eber.driver.models.datamodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CityDetail {

	@SerializedName("isCountryBusiness")
	private int isCountryBusiness;

	@SerializedName("isPromoApplyForCash")
	private int isPromoApplyForCash;

	@SerializedName("timezone")
	private String timezone;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("provider_min_wallet_amount_set_for_received_cash_request")
	private int providerMinWalletAmountSetForReceivedCashRequest;

	@SerializedName("is_provider_earning_set_in_wallet_on_cash_payment")
	private boolean isProviderEarningSetInWalletOnCashPayment;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("citycode")
	private String citycode;

	@SerializedName("isBusiness")
	private int isBusiness;

	@SerializedName("cityRadius")
	private double cityRadius;

	@SerializedName("isPromoApplyForCard")
	private int isPromoApplyForCard;

	@SerializedName("payment_gateway")
	private List<Integer> paymentGateway;

	@SerializedName("cityname")
	private String cityname;

	@SerializedName("is_ask_user_for_fixed_fare")
	private boolean isAskUserForFixedFare;

	@SerializedName("countryname")
	private String countryname;

	@SerializedName("is_payment_mode_cash")
	private int isPaymentModeCash;

	@SerializedName("countryid")
	private String countryid;

	@SerializedName("daily_cron_date")
	private String dailyCronDate;

	@SerializedName("zipcode")
	private String zipcode;

	@SerializedName("is_provider_earning_set_in_wallet_on_other_payment")
	private boolean isProviderEarningSetInWalletOnOtherPayment;

	@SerializedName("unit")
	private int unit;

	@SerializedName("zone_business")
	private int zoneBusiness;

	@SerializedName("is_use_city_boundary")
	private boolean isUseCityBoundary;

	@SerializedName("full_cityname")
	private String fullCityname;

	@SerializedName("city_locations")
	private List<List<Double>> cityLocations;

	@SerializedName("is_payment_mode_card")
	private int isPaymentModeCard;

	@SerializedName("cityLatLong")
	private List<Double> cityLatLong;

	@SerializedName("_id")
	private String id;

	@SerializedName("is_check_provider_wallet_amount_for_received_cash_request")
	private boolean isCheckProviderWalletAmountForReceivedCashRequest;

	@SerializedName("city_business")
	private int cityBusiness;

	@SerializedName("airport_business")
	private int airportBusiness;

	public void setIsCountryBusiness(int isCountryBusiness){
		this.isCountryBusiness = isCountryBusiness;
	}

	public int getIsCountryBusiness(){
		return isCountryBusiness;
	}

	public void setIsPromoApplyForCash(int isPromoApplyForCash){
		this.isPromoApplyForCash = isPromoApplyForCash;
	}

	public int getIsPromoApplyForCash(){
		return isPromoApplyForCash;
	}

	public void setTimezone(String timezone){
		this.timezone = timezone;
	}

	public String getTimezone(){
		return timezone;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setProviderMinWalletAmountSetForReceivedCashRequest(int providerMinWalletAmountSetForReceivedCashRequest){
		this.providerMinWalletAmountSetForReceivedCashRequest = providerMinWalletAmountSetForReceivedCashRequest;
	}

	public int getProviderMinWalletAmountSetForReceivedCashRequest(){
		return providerMinWalletAmountSetForReceivedCashRequest;
	}

	public void setIsProviderEarningSetInWalletOnCashPayment(boolean isProviderEarningSetInWalletOnCashPayment){
		this.isProviderEarningSetInWalletOnCashPayment = isProviderEarningSetInWalletOnCashPayment;
	}

	public boolean isIsProviderEarningSetInWalletOnCashPayment(){
		return isProviderEarningSetInWalletOnCashPayment;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public void setCitycode(String citycode){
		this.citycode = citycode;
	}

	public String getCitycode(){
		return citycode;
	}

	public void setIsBusiness(int isBusiness){
		this.isBusiness = isBusiness;
	}

	public int getIsBusiness(){
		return isBusiness;
	}

	public void setCityRadius(double cityRadius){
		this.cityRadius = cityRadius;
	}

	public double getCityRadius(){
		return cityRadius;
	}

	public void setIsPromoApplyForCard(int isPromoApplyForCard){
		this.isPromoApplyForCard = isPromoApplyForCard;
	}

	public int getIsPromoApplyForCard(){
		return isPromoApplyForCard;
	}

	public void setPaymentGateway(List<Integer> paymentGateway){
		this.paymentGateway = paymentGateway;
	}

	public List<Integer> getPaymentGateway(){
		return paymentGateway;
	}

	public void setCityname(String cityname){
		this.cityname = cityname;
	}

	public String getCityname(){
		return cityname;
	}

	public void setIsAskUserForFixedFare(boolean isAskUserForFixedFare){
		this.isAskUserForFixedFare = isAskUserForFixedFare;
	}

	public boolean isIsAskUserForFixedFare(){
		return isAskUserForFixedFare;
	}

	public void setCountryname(String countryname){
		this.countryname = countryname;
	}

	public String getCountryname(){
		return countryname;
	}

	public void setIsPaymentModeCash(int isPaymentModeCash){
		this.isPaymentModeCash = isPaymentModeCash;
	}

	public int getIsPaymentModeCash(){
		return isPaymentModeCash;
	}

	public void setCountryid(String countryid){
		this.countryid = countryid;
	}

	public String getCountryid(){
		return countryid;
	}

	public void setDailyCronDate(String dailyCronDate){
		this.dailyCronDate = dailyCronDate;
	}

	public String getDailyCronDate(){
		return dailyCronDate;
	}

	public void setZipcode(String zipcode){
		this.zipcode = zipcode;
	}

	public String getZipcode(){
		return zipcode;
	}

	public void setIsProviderEarningSetInWalletOnOtherPayment(boolean isProviderEarningSetInWalletOnOtherPayment){
		this.isProviderEarningSetInWalletOnOtherPayment = isProviderEarningSetInWalletOnOtherPayment;
	}

	public boolean isIsProviderEarningSetInWalletOnOtherPayment(){
		return isProviderEarningSetInWalletOnOtherPayment;
	}

	public void setUnit(int unit){
		this.unit = unit;
	}

	public int getUnit(){
		return unit;
	}

	public void setZoneBusiness(int zoneBusiness){
		this.zoneBusiness = zoneBusiness;
	}

	public int getZoneBusiness(){
		return zoneBusiness;
	}

	public void setIsUseCityBoundary(boolean isUseCityBoundary){
		this.isUseCityBoundary = isUseCityBoundary;
	}

	public boolean isIsUseCityBoundary(){
		return isUseCityBoundary;
	}

	public void setFullCityname(String fullCityname){
		this.fullCityname = fullCityname;
	}

	public String getFullCityname(){
		return fullCityname;
	}

	public void setCityLocations(List<List<Double>> cityLocations){
		this.cityLocations = cityLocations;
	}

	public List<List<Double>> getCityLocations(){
		return cityLocations;
	}

	public void setIsPaymentModeCard(int isPaymentModeCard){
		this.isPaymentModeCard = isPaymentModeCard;
	}

	public int getIsPaymentModeCard(){
		return isPaymentModeCard;
	}

	public void setCityLatLong(List<Double> cityLatLong){
		this.cityLatLong = cityLatLong;
	}

	public List<Double> getCityLatLong(){
		return cityLatLong;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setIsCheckProviderWalletAmountForReceivedCashRequest(boolean isCheckProviderWalletAmountForReceivedCashRequest){
		this.isCheckProviderWalletAmountForReceivedCashRequest = isCheckProviderWalletAmountForReceivedCashRequest;
	}

	public boolean isIsCheckProviderWalletAmountForReceivedCashRequest(){
		return isCheckProviderWalletAmountForReceivedCashRequest;
	}

	public void setCityBusiness(int cityBusiness){
		this.cityBusiness = cityBusiness;
	}

	public int getCityBusiness(){
		return cityBusiness;
	}

	public void setAirportBusiness(int airportBusiness){
		this.airportBusiness = airportBusiness;
	}

	public int getAirportBusiness(){
		return airportBusiness;
	}

	@Override
 	public String toString(){
		return 
			"CityDetail{" + 
			"isCountryBusiness = '" + isCountryBusiness + '\'' + 
			",isPromoApplyForCash = '" + isPromoApplyForCash + '\'' + 
			",timezone = '" + timezone + '\'' + 
			",created_at = '" + createdAt + '\'' + 
			",provider_min_wallet_amount_set_for_received_cash_request = '" + providerMinWalletAmountSetForReceivedCashRequest + '\'' + 
			",is_provider_earning_set_in_wallet_on_cash_payment = '" + isProviderEarningSetInWalletOnCashPayment + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",citycode = '" + citycode + '\'' + 
			",isBusiness = '" + isBusiness + '\'' + 
			",cityRadius = '" + cityRadius + '\'' + 
			",isPromoApplyForCard = '" + isPromoApplyForCard + '\'' + 
			",payment_gateway = '" + paymentGateway + '\'' + 
			",cityname = '" + cityname + '\'' + 
			",is_ask_user_for_fixed_fare = '" + isAskUserForFixedFare + '\'' + 
			",countryname = '" + countryname + '\'' + 
			",is_payment_mode_cash = '" + isPaymentModeCash + '\'' + 
			",countryid = '" + countryid + '\'' + 
			",daily_cron_date = '" + dailyCronDate + '\'' + 
			",zipcode = '" + zipcode + '\'' + 
			",is_provider_earning_set_in_wallet_on_other_payment = '" + isProviderEarningSetInWalletOnOtherPayment + '\'' + 
			",unit = '" + unit + '\'' + 
			",zone_business = '" + zoneBusiness + '\'' + 
			",is_use_city_boundary = '" + isUseCityBoundary + '\'' + 
			",full_cityname = '" + fullCityname + '\'' + 
			",city_locations = '" + cityLocations + '\'' + 
			",is_payment_mode_card = '" + isPaymentModeCard + '\'' + 
			",cityLatLong = '" + cityLatLong + '\'' + 
			",_id = '" + id + '\'' + 
			",is_check_provider_wallet_amount_for_received_cash_request = '" + isCheckProviderWalletAmountForReceivedCashRequest + '\'' + 
			",city_business = '" + cityBusiness + '\'' + 
			",airport_business = '" + airportBusiness + '\'' + 
			"}";
		}
}