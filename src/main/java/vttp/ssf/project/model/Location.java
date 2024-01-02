package vttp.ssf.project.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class Location {
    
    private String address;
    private double latitude;
    private double longtitude;

    @NotEmpty(message = "Postal Code is a Mandatory Field")
    @Pattern(regexp = "[0-9]{6}", message = "Invalid Postal Code")
    private String postalCode;
    private String name;

    public static final String[] amenities_values = {
        "kindergartens", "eldercare","disability",
        "healthierdining","registered_pharmacy",
        "libraries","communityclubs", "sso"
    };

    public static final String[] amenities_text = {
        "Kindergartens", "Elder Care Centres","Disability Care Centres",
        "Healthier Dining Options","Registered Pharmacies",
        "Libraries","Community Clubs", "Social Service Offices"
    };


    public Location() {}

    public Location(String address, double latitude, double longtitude) {
        this.address = address;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }


    public Location(String postalCode, String name, String address, double latitude, double longtitude) {
        this.address = address;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.postalCode = postalCode;
        this.name = name;
    }

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public String getPostalCode() {return postalCode;}
    public void setPostalCode(String postalCode) {this.postalCode = postalCode;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    
    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongtitude() {return longtitude;}
    public void setLongtitude(double longtitude) {this.longtitude = longtitude;}

    @Override
    public String toString() {
        return "Location [address=" + address + ", latitude=" + latitude + ", longtitude=" + longtitude
                + ", postalCode=" + postalCode + ", name=" + name + "]";
    }

    




}
