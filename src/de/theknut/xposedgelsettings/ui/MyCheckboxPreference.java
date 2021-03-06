package de.theknut.xposedgelsettings.ui;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;

public class MyCheckboxPreference extends CheckBoxPreference {
    public MyCheckboxPreference(Context context) {
        super(context);
    }

    public MyCheckboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCheckboxPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        
        CommonUI.setCustomStyle(view, true, true);
    }
}