package com.apricity.asmlifecycledemo.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apricity.asmlifecycledemo.grayApp.GrayFrameLayout;

public class BaseActivity extends AppCompatActivity {

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        try {
            if ("FrameLayout".equals(name)) {
                int count = attrs.getAttributeCount();
                for (int i = 0; i < count; i++) {
                    String attributeName = attrs.getAttributeName(i);
                    String attributeValue = attrs.getAttributeValue(i);
                    if (attributeName.equals("id")) {
                        int id = Integer.parseInt(attributeValue.substring(1));
                        String idVal = getResources().getResourceName(id);
                        if ("android:id/content".equals(idVal)) {
                            GrayFrameLayout grayFrameLayout = new GrayFrameLayout(context, attrs);
                            return grayFrameLayout;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
