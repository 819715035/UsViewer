package leltek.viewer.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by rajesh on 5/10/17.
 */

public class AddIconOnEnd {

    private Bitmap bitmap;
    private Context mContext;
    private Point point;
    private int id;
    static int count = 0;

    public AddIconOnEnd(Context context, int resourceId, Point point, int id) {
        this.id = id;
        bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        mContext = context;
        this.point = point;
    }

    public int getWidthOfBall() {
        return bitmap.getWidth();
    }

    public int getHeightOfBall() {
        return bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return point.x;
    }

    public int getY() {
        return point.y;
    }

    public int getID() {
        return id;
    }

    public void setX(int x) {
        point.x = x;
    }

    public void setY(int y) {
        point.y = y;
    }

    public void addY(int y) {
        point.y = point.y + y;
    }

    public void addX(int x) {
        point.x = point.x + x;
    }
}