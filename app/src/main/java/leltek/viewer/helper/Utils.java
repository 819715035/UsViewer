package leltek.viewer.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import leltek.viewer.R;

/**
 * Created by rajesh on 27/9/17.
 */

public class Utils {

    public static void saveImage(FrameLayout frameLayout, Context context) {
        frameLayout.setDrawingCacheEnabled(true);
        frameLayout.buildDrawingCache();
        Bitmap cache = frameLayout.getDrawingCache();
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "/Viewer/");
            directory.mkdir();
            File imagePath = new File(directory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
            cache.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            showToast(context, context.getString(R.string.image_saved, imagePath));
            PrefrenceData.getInstance().saveImage(imagePath.getAbsolutePath());
        } catch (Exception e) {
            Log.e("exp", "" + e);
            // TODO: handle exception
        } finally {
            frameLayout.destroyDrawingCache();
        }

    }

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
