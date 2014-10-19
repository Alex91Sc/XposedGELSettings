package de.theknut.xposedgelsettings.hooks;

import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.theknut.xposedgelsettings.R;

/**
 * Created by Alexander Schulz on 12.08.2014.
 */
public class ResourceReplacements extends XC_MethodHook implements IXposedHookInitPackageResources {

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!Common.PACKAGE_NAMES.contains(resparam.packageName)) {
            return;
        }

        ResourceReplacements.initAllReplacements(resparam);
    }

    public static void initAllReplacements(InitPackageResourcesParam resparam) {
        XSharedPreferences prefs = new XSharedPreferences(Common.PACKAGE_NAME);
        int glowColor = prefs.getInt("glowcolor", Color.argb(0xFF, 0xFF, 0xFF, 0xFF));

        if (resparam.packageName.equals(Common.TREBUCHET_PACKAGE)) {
            resparam.res.setReplacement("com.android.launcher3", "color", "outline_color", glowColor);
            resparam.res.setReplacement("com.android.launcher3", "bool", "allow_rotation", prefs.getBoolean("enablerotation", false));

            applyPageIndicatorColor("com.android.launcher3", resparam.res);
        } else {
            resparam.res.setReplacement(resparam.packageName, "color", "outline_color", glowColor);
            resparam.res.setReplacement(resparam.packageName, "bool", "allow_rotation", prefs.getBoolean("enablerotation", false));
            resparam.res.setReplacement(resparam.packageName, "integer", "config_tabTransitionDuration", 0);

            resparam.res.setReplacement(resparam.packageName, "drawable", "quantum_panel", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources xResources, int i) throws Throwable {
                    Drawable drawable = Common.XGELSCONTEXT.getResources().getDrawable(R.drawable.quantum_panel);
                    drawable.setColorFilter(Color.parseColor(ColorPickerPreference.convertToARGB(PreferencesHelper.appdrawerBackgroundColor)), PorterDuff.Mode.MULTIPLY);
                    return drawable;
                }
            });

            applyPageIndicatorColor(resparam.packageName, resparam.res);
        }
    }

    public static void applyPageIndicatorColor(String pkg, XResources res) {
        String[] resNames = new String[] {"ic_pageindicator_add", "ic_pageindicator_current", "ic_pageindicator_default"};

        for (int i = 0; i < resNames.length; i++) {
            int id = res.getIdentifier(resNames[i], "drawable", pkg);
            if (id != 0) {
                final Drawable d = res.getDrawable(id);
                d.setColorFilter(PreferencesHelper.pageIndicatorColor, PorterDuff.Mode.MULTIPLY);
                res.setReplacement(pkg, "drawable", resNames[i], new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) throws Throwable {
                        return d;
                    }
                });
            }
        }
    }
}
