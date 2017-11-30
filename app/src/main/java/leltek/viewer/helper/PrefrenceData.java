package leltek.viewer.helper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import leltek.viewer.ApplicationInitializer;
import leltek.viewer.constants.AppConstant;

/**
 * Created by rajesh on 4/10/17.
 */

public class PrefrenceData {
    private static PrefrenceData prefrenceData;
    String newImage;

    private PrefrenceData() {
    }

    public static PrefrenceData getInstance() {
        if (prefrenceData == null) {
            prefrenceData = new PrefrenceData();
        }
        return prefrenceData;
    }

    private SharedPreferences.Editor getSharePrefrenceEditor() {
        return getSharedPrefrence().edit();
    }

    private SharedPreferences getSharedPrefrence() {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationInitializer.getInstance().getAppContext());
    }

    public String getNewImage() {
        return newImage;
    }

    public void saveImage(String newImage) {
        this.newImage = newImage;
        String oldImages = getSharedPrefrence().getString(AppConstant.PrefrenceKey.IMAGE,
                AppConstant.EMPTY_STRING);
        if (oldImages.length() > 0) {
            oldImages = oldImages.concat(",").concat(newImage);
        } else {
            oldImages=newImage;
        }
        getSharePrefrenceEditor().putString(AppConstant.PrefrenceKey.IMAGE, oldImages).apply();
    }

    public String getSavedImage() {
        return getSharedPrefrence().getString(AppConstant.PrefrenceKey.IMAGE, AppConstant.EMPTY_STRING);
    }
    
    public void cleanSavedImage() {
        getSharePrefrenceEditor().putString(AppConstant.PrefrenceKey.IMAGE, "").apply();
    }

}
