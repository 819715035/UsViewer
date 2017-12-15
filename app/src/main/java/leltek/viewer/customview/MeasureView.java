package leltek.viewer.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

import leltek.viewer.R;

/**
 * Created by rajesh on 1/10/17.
 */

public class MeasureView {
    Point point1;
    Point point2;
    Paint endPoints;
    Paint textDraw;

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    private ArrayList<PlusIcons> plusIcons = new ArrayList<>();
    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;
    Canvas canvas;
    DisplayMetrics displayMetrics;

    private float mMotionDownX, mMotionDownY;
    private float mScrollX, mScrollY;

    public void initViews(Context context) {
        textDraw = new Paint();
        textDraw.setColor(Color.WHITE);
        textDraw.setTextSize(25);
        displayMetrics = context.getResources().getDisplayMetrics();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        endPoints = new Paint();
        // setting the start point for the balls
        point1 = new Point();
        point1.x = 50;
        point1.y = 150;

        point2 = new Point();
        point2.x = 150;
        point2.y = 150;

        // declare each ball with the PlusIcons class
        plusIcons.add(new PlusIcons(context, R.drawable.ic_add_raster, point1));
        plusIcons.add(new PlusIcons(context, R.drawable.ic_add_raster, point2));
    }

    public void startMeasure(float[] values) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];
        point1.x = (int)((50 - tranX)/scaleX);
        point1.y = (int)((150 - tranY)/scaleY);
        point2.x = (int)((150 - tranX)/scaleX);
        point2.y = (int)((150 - tranY)/scaleY);
    }

    // the method that draws the balls
    public void onDraw(Canvas canvas, float[] values, double cmPerPx) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];

        float p1x = point1.x * scaleX + tranX;
        float p1y = point1.y * scaleY + tranY;
        float p2x = point2.x * scaleX + tranX;
        float p2y = point2.y * scaleY + tranY;

        double xDifference = (double) (p2x - p1x);
        double yDifference = (double) (p2y - p1y);
        double distance = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2)) * cmPerPx;
        String value = String.format(Locale.ENGLISH, "%.2f", distance);
        canvas.drawText("Distance " + value, 70, 30, textDraw);
        canvas.drawLine(p1x + plusIcons.get(0).getWidthOfBall() / 2,
                p1y + plusIcons.get(0).getWidthOfBall() / 2, p2x
                        + plusIcons.get(1).getWidthOfBall() / 2, p2y
                        + plusIcons.get(1).getWidthOfBall() / 2, paint);
        // draw the balls on the canvas
        for (PlusIcons ball : plusIcons) {
            float x = ball.getX() * scaleX + tranX;
            float y = ball.getY() * scaleY + tranY;
            canvas.drawBitmap(ball.getBitmap(), x, y, endPoints);
        }
    }

    public boolean onTouchEvent(MotionEvent event, float[] values) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                balID = -1;
                for (PlusIcons ball : plusIcons) {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    float x = ball.getX() * scaleX + tranX;
                    float y = ball.getY() * scaleY + tranY;
                    float centerX = x + ball.getWidthOfBall();
                    float centerY = y + ball.getHeightOfBall();
                    // calculate the radius from the touch to the center of the ball
                    double xDifference = centerX - X;
                    double yDifference = centerY - Y;
                    double radCircle = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));
                    if (radCircle < ball.getWidthOfBall()) {
                        balID = ball.getID();
                        mMotionDownX = X;
                        mMotionDownY = Y;
                        mScrollX = ball.getX();
                        mScrollY = ball.getY();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (balID > -1) {
                    float x = mScrollX + (X - mMotionDownX)/scaleX;
                    float y = mScrollY + (Y - mMotionDownY)/scaleY;
                    plusIcons.get(balID).setX((int)x);
                    plusIcons.get(balID).setY((int)y);
                }
                break;
        }
        return (balID != -1);
    }

    private static class PlusIcons {

        Bitmap bitmap;
        Context mContext;
        Point point;
        int id;
        static int count;

        private PlusIcons(Context context, int resourceId, Point point) {
            if (count > 1) count = 0;
            this.id = count++;
            bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            mContext = context;
            this.point = point;
        }

        private int getWidthOfBall() {
            return bitmap.getWidth();
        }

        private int getHeightOfBall() {
            return bitmap.getHeight();
        }

        private Bitmap getBitmap() {
            return bitmap;
        }

        private int getX() {
            return point.x;
        }

        private int getY() {
            return point.y;
        }

        private int getID() {
            return id;
        }

        private void setX(int x) {
            point.x = x;
        }

        private void setY(int y) {
            point.y = y;
        }
    }

}
