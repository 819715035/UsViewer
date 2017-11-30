package leltek.viewer.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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

public class MeasureView extends View {
    Point point1;
    Point point2;
    Paint endPoints;
    Paint textDraw;

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    int groupId = -1;
    private ArrayList<PlusIcons> plusIcons = new ArrayList<>();
    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;
    Canvas canvas;
    DisplayMetrics displayMetrics;

    public MeasureView(Context context) {
        super(context);
        initViews(context);
    }

    public MeasureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);

    }

    public MeasureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    private void initViews(Context context) {
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
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
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

    // the method that draws the balls
    @Override
    protected void onDraw(Canvas canvas) {
        double xDifference = (double) (point2.x - point1.x);
        double yDifference = (double) (point2.y - point1.y);
        double distance = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));
        String value = String.format(Locale.ENGLISH, "%.2f", getRealCm(distance));
        canvas.drawText("Distance " + value, 0, 80, textDraw);
        canvas.drawLine(point1.x + plusIcons.get(0).getWidthOfBall() / 2,
                point1.y + plusIcons.get(0).getWidthOfBall() / 2, point2.x
                        + plusIcons.get(1).getWidthOfBall() / 2, point2.y
                        + plusIcons.get(1).getWidthOfBall() / 2, paint);
        // draw the balls on the canvas
        for (PlusIcons ball : plusIcons) {
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), endPoints);
        }
    }

    public double getRealCm(double pixels) {
        float dpi = (float) displayMetrics.densityDpi;
        double inches = pixels / dpi;
        return inches * 2.54f; //inches to cm
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventaction) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                // a ball
                balID = -1;
                groupId = -1;
                for (PlusIcons ball : plusIcons) {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    int centerX = ball.getX() + ball.getWidthOfBall();
                    int centerY = ball.getY() + ball.getHeightOfBall();
                    // calculate the radius from the touch to the center of the ball
                    double xDifference = centerX - X;
                    double yDifference = centerY - Y;
                    double radCircle = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));

                    if (radCircle < ball.getWidthOfBall()) {

                        balID = ball.getID();
                        if (balID == 1) {
                            groupId = 2;
                            canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint);
                        } else {
                            groupId = 1;
                        }
                        invalidate();
                        break;
                    }
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_MOVE: // touch drag with the ball
                // move the balls the same as the finger
                if (balID > -1) {
                    plusIcons.get(balID).setX(X);
                    plusIcons.get(balID).setY(Y);
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_UP:
                // touch drop - just do things here after dropping

                break;
        }
        // redraw the canvas
        invalidate();
        return true;

    }


    public static class PlusIcons {

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
