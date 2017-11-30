package leltek.viewer.customview;

import android.content.Context;
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

public class EllipseView extends View {

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
    Canvas canvas;

    public EllipseView(Context context) {
        super(context);
        init(context);
    }

    public EllipseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    public EllipseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        areaText = new Paint();
        areaText.setColor(Color.WHITE);
        areaText.setTextSize(25);

        displayMetrics = context.getResources().getDisplayMetrics();
        drawText = new Paint();
        paint = new Paint();
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
        // setting the start point for the balls
        point1 = new Point();
        point1.x = 50;
        point1.y = 20;

        point2 = new Point();
        point2.x = 150;
        point2.y = 20;

        point3 = new Point();
        point3.x = 150;
        point3.y = 120;

        point4 = new Point();
        point4.x = 50;
        point4.y = 120;

        // declare each ball with the ColorBall class
        addIconOnEnds = new ArrayList<>();
        // declare each ball with the AddIconOnEnd class
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point1, 0));
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point2, 1));
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point3, 2));
        addIconOnEnds.add(new AddIconOnEnd(context, R.drawable.ic_ecllipse_end, point4, 3));
    }

    void drawOval (Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        canvas.drawOval(Math.min(left, right), Math.min(top, bottom),
                Math.max(left, right), Math.max(top, bottom), paint);
    }

    // the method that draws the balls
    @Override
    protected void onDraw(Canvas canvas) {
        // canvas.drawColor(0xFFCCCCCC); //if you want another background color
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5);

        if (groupId == 1) {
            drawOval(canvas,
                    point1.x + addIconOnEnds.get(0).getWidthOfBall() / 2,
                    point3.y + addIconOnEnds.get(2).getWidthOfBall() / 2, point3.x
                            + addIconOnEnds.get(2).getWidthOfBall() / 2, point1.y
                            + addIconOnEnds.get(0).getWidthOfBall() / 2, paint);
        } else {
            drawOval(canvas,
                    point2.x + addIconOnEnds.get(1).getWidthOfBall() / 2,
                    point4.y + addIconOnEnds.get(3).getWidthOfBall() / 2, point4.x
                            + addIconOnEnds.get(3).getWidthOfBall() / 2, point2.y
                            + addIconOnEnds.get(1).getWidthOfBall() / 2, paint);
        }

        double xDifference = point3.x - point1.x;
        double yDifference = point4.y - point2.y;
        double area = Math.abs(Math.PI * xDifference * yDifference) / 100;
        canvas.drawText("Area " + String.format(Locale.ENGLISH, "%.2f", getRealCm(area)), 0, 80, areaText);

        // draw the balls on the canvas
        for (AddIconOnEnd ball : addIconOnEnds) {
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), drawText);
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
                startMovePoint = new Point(X, Y);
                for (AddIconOnEnd ball : addIconOnEnds) {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    int centerX = ball.getX() + ball.getWidthOfBall();
                    int centerY = ball.getY() + ball.getHeightOfBall();
                    paint.setColor(Color.CYAN);
                    // calculate the radius from the touch to the center of the ball
                    double radCircle = Math
                            .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                    * (centerY - Y)));

                    if (radCircle < ball.getWidthOfBall()) {

                        balID = ball.getID();
                        if (balID == 1 || balID == 3) {
                            groupId = 2;
                            canvas.drawOval(point1.x, point3.y, point3.x, point1.y,
                                    paint);
                        } else {
                            groupId = 1;
                            canvas.drawOval(point2.x, point4.y, point4.x, point2.y,
                                    paint);
                        }
                        invalidate();
                        break;
                    }
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_MOVE:

                // move the balls the same as the finger
                if (balID > -1) {
                    addIconOnEnds.get(balID).setX(X);
                    addIconOnEnds.get(balID).setY(Y);

                    paint.setColor(Color.CYAN);

                    if (groupId == 1) {
                        addIconOnEnds.get(1).setX(addIconOnEnds.get(0).getX());
                        addIconOnEnds.get(1).setY(addIconOnEnds.get(2).getY());
                        addIconOnEnds.get(3).setX(addIconOnEnds.get(2).getX());
                        addIconOnEnds.get(3).setY(addIconOnEnds.get(0).getY());
                        canvas.drawOval(point1.x, point3.y, point3.x, point1.y,
                                paint);
                    } else {
                        addIconOnEnds.get(0).setX(addIconOnEnds.get(1).getX());
                        addIconOnEnds.get(0).setY(addIconOnEnds.get(3).getY());
                        addIconOnEnds.get(2).setX(addIconOnEnds.get(3).getX());
                        addIconOnEnds.get(2).setY(addIconOnEnds.get(1).getY());
                        canvas.drawOval(point2.x, point4.y, point4.x, point2.y,
                                paint);
                    }

                    invalidate();
                } else {
                    if (startMovePoint != null) {
                        paint.setColor(Color.CYAN);
                        int diffX = X - startMovePoint.x;
                        int diffY = Y - startMovePoint.y;
                        startMovePoint.x = X;
                        startMovePoint.y = Y;
                        addIconOnEnds.get(0).addX(diffX);
                        addIconOnEnds.get(1).addX(diffX);
                        addIconOnEnds.get(2).addX(diffX);
                        addIconOnEnds.get(3).addX(diffX);
                        addIconOnEnds.get(0).addY(diffY);
                        addIconOnEnds.get(1).addY(diffY);
                        addIconOnEnds.get(2).addY(diffY);
                        addIconOnEnds.get(3).addY(diffY);
                        if (groupId == 1)
                            canvas.drawOval(point1.x, point3.y, point3.x, point1.y,
                                    paint);
                        else
                            canvas.drawOval(point2.x, point4.y, point4.x, point2.y,
                                    paint);
                        invalidate();
                    }
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
}