package leltek.viewer.customview;

import android.content.Context;
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

public class EllipseView {

    Paint areaText;
    Point point1, point3;
    Point point2, point4;
    Point startMovePoint;
    Paint drawText;
    DisplayMetrics displayMetrics;
    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    int groupId = 2;
    private ArrayList<AddIconOnEnd> addIconOnEnds;
    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;

    private float mMotionDownX, mMotionDownY;
    private float mScrollX, mScrollY;

    public void init(Context context) {
        areaText = new Paint();
        areaText.setColor(Color.WHITE);
        areaText.setTextSize(25);

        displayMetrics = context.getResources().getDisplayMetrics();
        drawText = new Paint();
        paint = new Paint();
        // setting the start point for the balls
        point1 = new Point();
        point2 = new Point();
        point3 = new Point();
        point4 = new Point();

        // declare each ball with the ColorBall class
        addIconOnEnds = new ArrayList<>();
        // declare each ball with the AddIconOnEnd class
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point1, 0));
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point2, 1));
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point3, 2));
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point4, 3));
    }

    public void startEllipse(float[] values) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];
        float ofsY = 100;
        float ofsX = 20;
        point1.x = (int)((50+ofsX - tranX)/scaleX);
        point1.y = (int)((20+ofsY - tranY)/scaleY);
        point2.x = (int)((150+ofsX - tranX)/scaleX);
        point2.y = (int)((20+ofsY - tranY)/scaleY);
        point3.x = (int)((150+ofsX - tranX)/scaleX);
        point3.y = (int)((120+ofsY - tranY)/scaleY);
        point4.x = (int)((50+ofsX - tranX)/scaleX);
        point4.y = (int)((120+ofsY - tranY)/scaleY);
    }

    void drawOval (Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        canvas.drawOval(Math.min(left, right), Math.min(top, bottom),
                Math.max(left, right), Math.max(top, bottom), paint);
    }

    // the method that draws the balls
    public void onDraw(Canvas canvas, float[] values, double cmPerPx) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];

        // canvas.drawColor(0xFFCCCCCC); //if you want another background color
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5);

        float p1x = point1.x * scaleX + tranX;
        float p1y = point1.y * scaleY + tranY;
        float p2x = point2.x * scaleX + tranX;
        float p2y = point2.y * scaleY + tranY;
        float p3x = point3.x * scaleX + tranX;
        float p3y = point3.y * scaleY + tranY;
        float p4x = point4.x * scaleX + tranX;
        float p4y = point4.y * scaleY + tranY;

        if (groupId == 1) {
            drawOval(canvas,
                    p1x + addIconOnEnds.get(0).getWidthOfBall() / 2,
                    p3y + addIconOnEnds.get(2).getWidthOfBall() / 2, p3x
                            + addIconOnEnds.get(2).getWidthOfBall() / 2, p1y
                            + addIconOnEnds.get(0).getWidthOfBall() / 2, paint);
        } else {
            drawOval(canvas,
                    p2x + addIconOnEnds.get(1).getWidthOfBall() / 2,
                    p4y + addIconOnEnds.get(3).getWidthOfBall() / 2, p4x
                            + addIconOnEnds.get(3).getWidthOfBall() / 2, p2y
                            + addIconOnEnds.get(1).getWidthOfBall() / 2, paint);
        }

        double xDifference = Math.abs(p3x - p1x)/2*cmPerPx;
        double yDifference = Math.abs(p4y - p2y)/2*cmPerPx;
        double area = Math.abs(Math.PI * xDifference * yDifference);
        canvas.drawText("Area " + String.format(Locale.ENGLISH, "%.2f", area), 70, 30+35, areaText);

        // draw the balls on the canvas
        for (AddIconOnEnd ball : addIconOnEnds) {
            float x = ball.getX() * scaleX + tranX;
            float y = ball.getY() * scaleY + tranY;
            canvas.drawBitmap(ball.getBitmap(), x, y, drawText);
        }
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event, float[] values) {
        int eventaction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];

        switch (eventaction) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                // a ball
                balID = -1;
                startMovePoint = new Point(X, Y);
                for (AddIconOnEnd ball : addIconOnEnds) {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    float x = ball.getX() * scaleX + tranX;
                    float y = ball.getY() * scaleY + tranY;
                    float centerX = x + ball.getWidthOfBall();
                    float centerY = y + ball.getHeightOfBall();
                    paint.setColor(Color.CYAN);
                    // calculate the radius from the touch to the center of the ball
                    double radCircle = Math
                            .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                    * (centerY - Y)));

                    if (radCircle < ball.getWidthOfBall()) {

                        balID = ball.getID();
                        if (balID == 1 || balID == 3) {
                            groupId = 2;
                        } else {
                            groupId = 1;
                        }
                        mMotionDownX = X;
                        mMotionDownY = Y;
                        mScrollX = ball.getX();
                        mScrollY = ball.getY();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:

                // move the balls the same as the finger
                if (balID > -1) {
                    float x = mScrollX + (X - mMotionDownX)/scaleX;
                    float y = mScrollY + (Y - mMotionDownY)/scaleY;

                    addIconOnEnds.get(balID).setX((int)x);
                    addIconOnEnds.get(balID).setY((int)y);

                    if (groupId == 1) {
                        addIconOnEnds.get(1).setX(addIconOnEnds.get(0).getX());
                        addIconOnEnds.get(1).setY(addIconOnEnds.get(2).getY());
                        addIconOnEnds.get(3).setX(addIconOnEnds.get(2).getX());
                        addIconOnEnds.get(3).setY(addIconOnEnds.get(0).getY());
                    } else {
                        addIconOnEnds.get(0).setX(addIconOnEnds.get(1).getX());
                        addIconOnEnds.get(0).setY(addIconOnEnds.get(3).getY());
                        addIconOnEnds.get(2).setX(addIconOnEnds.get(3).getX());
                        addIconOnEnds.get(2).setY(addIconOnEnds.get(1).getY());
                    }
                }
                break;
        }
        return (balID != -1);
    }
}