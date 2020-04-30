package com.apricity.asmlifecycledemo;

import android.util.Log;

public class Test {

    public void log(){
        Log.i("TAG", "-------> onCreate : " + this.getClass().getSimpleName());
    }
}
