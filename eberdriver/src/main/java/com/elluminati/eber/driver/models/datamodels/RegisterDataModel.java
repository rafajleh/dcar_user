package com.elluminati.eber.driver.models.datamodels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.annotation.Annotation;

public class RegisterDataModel implements Serializable {
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("email_address")
    private String emailAddress;
    @SerializedName("mobile_no")
    private String mobileNumber;
    @SerializedName("operator")
    private String operator;
    @SerializedName("pco_licence")
    private String pcoLicence;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPcoLicence() {
        return pcoLicence;
    }

    public void setPcoLicence(String pcoLicence) {
        this.pcoLicence = pcoLicence;
    }

    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert the ArrayList to JSON
        String json = gson.toJson(this);
        return json;
    }


//    public String toString(){
//        Gson gson = new Gson(); // Or use new GsonBuilder().create();
//        String json = gson.toJson(this); // serializes target to Json
//        return  json;
//    }
}
