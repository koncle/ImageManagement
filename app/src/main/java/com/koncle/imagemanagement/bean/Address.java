package com.koncle.imagemanagement.bean;

/**
 * Created by 10976 on 2018/1/11.
 */

public class Address {

    String formatted_address;
    String country;
    String province;
    String city;
    String district;
    String street;
    String streetnumber;
    String countrycode;

    public Address() {
    }

    public Address(String formatted_address, String country, String province,
                   String city, String district, String street, String streetnumber,
                   String countrycode) {
        super();
        this.formatted_address = formatted_address;
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.street = street;
        this.streetnumber = streetnumber;
        this.countrycode = countrycode;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetnumber() {
        return streetnumber;
    }

    public void setStreetnumber(String streetnumber) {
        this.streetnumber = streetnumber;
    }

    public String Countrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    @Override
    public String toString() {
        return country + province + city + district + street + streetnumber;
    }
}
