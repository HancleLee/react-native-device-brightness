/**
 * Created by Calvin Huang on 3/11/17.
 */

 package org.capslock.RNDeviceBrightness;
 import android.app.Activity;
 import android.content.Context;
 import android.os.PowerManager;
 import android.util.Log;
 import android.view.WindowManager;
 import android.provider.Settings;
 
 import com.facebook.react.bridge.NativeModule;
 import com.facebook.react.bridge.ReactApplicationContext;
 import com.facebook.react.bridge.ReactContext;
 import com.facebook.react.bridge.ReactContextBaseJavaModule;
 import com.facebook.react.bridge.ReactMethod;
 import com.facebook.react.bridge.Promise;
 import android.content.res.Resources;

 import java.lang.reflect.Field;

 public class RNDeviceBrightnessModule extends ReactContextBaseJavaModule {
   private static final int BRIGHTNESS_MAX = 255;
   private static final int BRIGHTNESS_MIN = 0;
 
   public RNDeviceBrightnessModule(ReactApplicationContext reactContext) {
     super(reactContext);
   }
 
   @Override
   public String getName() {
     return "RNDeviceBrightness";
   }
 
   @ReactMethod
   public void setBrightnessLevel(final float brightnessLevel) {
     final Activity activity = getCurrentActivity();
     if (activity == null) {
       return;
     }
     
     activity.runOnUiThread(new Runnable() {
       @Override
       public void run() {
         WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
         lp.screenBrightness = brightnessLevel;
         activity.getWindow().setAttributes(lp);
       }
     });
   }
 
   @ReactMethod
   public void getBrightnessLevel(Promise promise) {
     WindowManager.LayoutParams lp = getCurrentActivity().getWindow().getAttributes();
     promise.resolve(lp.screenBrightness);
   }
 
   /**
    * 获取最大亮度
    * @return [int] max
    */
   private int getBrightnessMax() {
       try {
         int maxBri = getMaxBrightness(getReactApplicationContext(), BRIGHTNESS_MAX);
         if (maxBri != 0) {
             return maxBri;
         }
         Resources system = Resources.getSystem();
         int resId = system.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");  // API17+
         if (resId != 0) {
           return  system.getInteger(resId);
         }
       }catch (Resources.NotFoundException e) {
         // ignore
       }
       return BRIGHTNESS_MAX;
   }

    /**
     * 获取最大亮度（通过PowerManager的方式）
     * @param context
     * @param defaultValue
     * @return [int]max brightness
     */
    private int getMaxBrightness(Context context, int defaultValue){
       PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
       if(powerManager != null) {
           Field[] fields = powerManager.getClass().getDeclaredFields();
           for (Field field: fields) {
               // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/os/PowerManager.java
               if(field.getName().equals("BRIGHTNESS_ON")) {
                   field.setAccessible(true);
                   try {
                       return (int) field.get(powerManager);
                   } catch (IllegalAccessException e) {
                       return defaultValue;
                   }
               }
           }
       }
       return defaultValue;
   }
 
   @ReactMethod
   public void getSystemBrightnessLevel(Promise promise){
     Float brightness = null;
     Integer sysBrightness;
     try {
         sysBrightness = Settings.System.getInt(getReactApplicationContext().getContentResolver(),
                 Settings.System.SCREEN_BRIGHTNESS);
     } catch (Settings.SettingNotFoundException e) {
         sysBrightness = null;
     }
     if (sysBrightness != null && sysBrightness > 1) {
         brightness = (float)sysBrightness/(float)getBrightnessMax();
     } else {
         brightness = (float)sysBrightness;
     }
     promise.resolve(brightness);
   }
 }
 