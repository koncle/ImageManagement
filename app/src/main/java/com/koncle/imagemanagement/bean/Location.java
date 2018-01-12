package com.koncle.imagemanagement.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by 10976 on 2018/1/11.
 */
@Entity
public class Location {
    @Id(autoincrement = true)
    Long id;
    @NotNull
    String country;
    @NotNull
    String province;
    @NotNull
    String city;

    @Generated(hash = 1792801946)
    public Location(Long id, @NotNull String country, @NotNull String province,
                    @NotNull String city) {
        this.id = id;
        this.country = country;
        this.province = province;
        this.city = city;
    }

    @Generated(hash = 375979639)
    public Location() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
