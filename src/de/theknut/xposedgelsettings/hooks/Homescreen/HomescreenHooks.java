package de.theknut.xposedgelsettings.hooks.homescreen;

import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.theknut.xposedgelsettings.hooks.Common;
import de.theknut.xposedgelsettings.hooks.HooksBaseClass;
import de.theknut.xposedgelsettings.hooks.ObfuscationHelper.Classes;
import de.theknut.xposedgelsettings.hooks.ObfuscationHelper.Fields;
import de.theknut.xposedgelsettings.hooks.ObfuscationHelper.Methods;
import de.theknut.xposedgelsettings.hooks.PreferencesHelper;
import de.theknut.xposedgelsettings.hooks.general.MoveToDefaultScreenHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

public class HomescreenHooks extends HooksBaseClass {

	public static void initAllHooks(LoadPackageParam lpparam) {
		
		// change the default homescreen
		findAndHookMethod(Classes.Workspace, Methods.workspaceMoveToDefaultScreen, boolean.class, new MoveToDefaultScreenHook());
			
		// modify homescreen grid
		XposedBridge.hookAllConstructors(Classes.DeviceProfile, new DeviceProfileConstructorHook());
		
		if (PreferencesHelper.iconSettingsSwitchHome || PreferencesHelper.homescreenFolderSwitch || PreferencesHelper.appdockSettingsSwitch) {			
			// changing the appearence of the icons on the homescreen
			findAndHookMethod(Classes.CellLayout, Methods.clAddViewToCellLayout, View.class, Integer.TYPE, Integer.TYPE, Classes.CellLayoutLayoutParams, boolean.class, new AddViewToCellLayoutHook());
		}
		
		XposedBridge.hookAllConstructors(Classes.AppsCustomizePagedView, new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// saving the content type
				Common.CONTENT_TYPE = getObjectField(param.thisObject, Fields.acpvContentType);
			};
		});
		
		if (PreferencesHelper.continuousScroll) {
			
			// over scroll to app drawer or first page
			findAndHookMethod(Classes.Workspace, Methods.wsOverScroll, float.class, new OverScrollWorkspaceHook());
			//findAndHookMethod(Classes.Launcher, Methods.launcherShowWorkspace, boolean.class, Runnable.class, new OnWorkspaceShownHook());
		}
		
		if (PreferencesHelper.appdockSettingsSwitch || PreferencesHelper.changeGridSizeHome) {
			
			// hide the app dock
			findAndHookMethod(Classes.DeviceProfile, Methods.dpGetWorkspacePadding, Integer.TYPE, new GetWorkspacePaddingHook());
			
			if (PreferencesHelper.appdockSettingsSwitch) {
				XposedBridge.hookAllConstructors(Classes.Hotseat, new HotseatConstructorHook());
			}
		}
		
		if (Common.HOOKED_PACKAGE.equals(Common.TREBUCHET_PACKAGE)) {
			// move to default homescreen after workspace has finished loading
			XposedBridge.hookAllMethods(Classes.Launcher, "onFinishBindingItems", new FinishBindingItemsHook());
		}
		else {
			// move to default homescreen after workspace has finished loading
			findAndHookMethod(Classes.Launcher, Methods.lFinishBindingItems, boolean.class, new FinishBindingItemsHook());
		}

        if (PreferencesHelper.smartFolderMode != 0) {
            findAndHookMethod(Classes.FolderIcon, "onTouchEvent", MotionEvent.class, new SmartFolderHook());
        }

        if (PreferencesHelper.unlimitedFolderSize) {
            XposedBridge.hookAllConstructors(Classes.Folder, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    setIntField(param.thisObject, Fields.fMaxCountY, Integer.MAX_VALUE);
                    setIntField(param.thisObject, Fields.fMaxNumItems, Integer.MAX_VALUE);
                }
            });

            // very dirty hack :(
            if (!Common.HOOKED_PACKAGE.equals(Common.TREBUCHET_PACKAGE)) {
                findAndHookMethod(Classes.Folder, "onMeasure", Integer.TYPE, Integer.TYPE, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Common.ON_MEASURE = true;
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Common.ON_MEASURE = false;
                    }
                });

                findAndHookMethod(Classes.LauncherAppState, Methods.lasIsDisableAllApps, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        return Common.ON_MEASURE;
                    }
                });
            }
        }
	}
}
