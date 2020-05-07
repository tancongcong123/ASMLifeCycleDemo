package com.apricity.asmlifecycledemo;

import android.util.Log;
import android.view.View;

public class Test {

    public void log(){
        Log.i("TAG", "-------> onCreate : " + this.getClass().getSimpleName());
    }

    public void onClick(View view) {
        Log.i("TAG", "-------> onClick : desc=" + view.getContentDescription());
    }

}
