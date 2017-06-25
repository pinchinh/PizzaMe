package com.phuang.pizzame.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Store implements Serializable, Comparable<Store> {
    public String id;
    public String Title;
    public String Address;
    public String City;
    public String State;
    public String Phone;
    public String BusinessUrl;
    public double Distance;
    public double Latitude;
    public double Longitude;

    @Override
    public int compareTo(@NonNull Store o) {
        return Double.compare(Distance, o.Distance);
    }
}
