package com.phuang.pizzame;

import android.app.Application;

public class PizzaMe extends Application {

    private static PizzaMe sInstance;

    public PizzaMe() {
        sInstance = this;
    }

    public static PizzaMe get() {
        return sInstance;
    }
}
