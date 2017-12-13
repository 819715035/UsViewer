package leltek.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

import leltek.viewer.customview.MeasureView;
import leltek.viewer.customview.EllipseView;
import leltek.viewer.model.Probe;
import leltek.viewer.model.SimuProbe;
import leltek.viewer.model.SimuProbeLinear;
import leltek.viewer.model.WifiProbe;
import leltek.viewer.util.ImageUtils;

/**
 * Created by user on 2017/5/10.
 */

public class UsImageView extends AppCompatImageView {
    final static Logger logger = LoggerFactory.getLogger(UsImageView.class);
    private static final int NONE = 0;
    private static final int MOVE = 1;
    private static final int ZOOM = 2;
    private static final int DRAG = 3;
    private static final int MEASURE = 4;
    private static final int ELLIPSE = 5;
    private final static float sMaxScale = 4f;
    private final static float sMinScale = 1f;
    private final static float rdRatio = (float) (Math.PI / 180);
    private final static float drRatio = (float) (180 / Math.PI);
    private final float minWidth = 100;
    private final PointF convexOrigin = new PointF();
    private final float roiDiffThetaMinLimit = 15 * rdRatio;
    private final float roiDepthMinLimit = 200;
    private Probe probe;
    private Matrix zoomMatrix = new Matrix();
    private Matrix savedZoomMatrix = new Matrix();
    private int mode = NONE;
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oriDist = 1f;
    private float mMaxScale;
    private float mMinScale;
    private Matrix fitHeightMatrix;
    private Matrix fitWidthMatrix;
    private boolean fitWidth;
    private boolean initDone = false;
    private Paint paint = new Paint();
    private Canvas canvas = new Canvas();
    private ArrayList<Ball> balls = new ArrayList<>();
    private Ball ball0;
    private Ball ball1;
    private Ball ball2;
    private Ball ball3;
    private int ballId;
    private int halfWidthOfBall;
    private int roiMode = NONE;
    private PointF roiStartMovingPoint = new PointF();
    private PointF convexMidRoi = new PointF();
    private float startMovingArc;
    private int canvasWidth;
    private int canvasHeight;
    private float roiWidth = 200;
    private float roiHeight = 200;
    private PointF roiStart = new PointF();
    private int r;
    private float maxTheta;
    private float roiStartTheta;
    private float roiDiffTheta;
    private float roiStartR;
    private float roiEndR;
    private float roiDepth;
    private Paint rulerPaint, textPaint;
    private int startX = 6;
    //    private String unit = " cm";
    private String unit = " ";
    private int scaleWidth = 10;
    private ImageListener imageListener = null;
    private float[] imxValues = null;
    private Context mContext = null;
    public boolean scanOn = true;
    private MeasureView measureView = new MeasureView();
    private EllipseView ellipseView = new EllipseView();
    private boolean measureOn = false;
    private boolean ellipseOn = false;

    interface ImageListener {
        void onImageMatrixChanged();
    }

    public void setImageListener(ImageListener imageListener) {
        this.imageListener = imageListener;
    }

    public float[] getUsImageMatrixValues() {
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        return values;
    }

    public UsImageView(Context context) {
        super(context);
        init(context);
    }

    public UsImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UsImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private static float calDist(float x, float y, float x2, float y2) {
        return (float) Math.sqrt(((x2 - x) * (x2 - x)) + (y2 - y)
                * (y2 - y));
    }

    private static float calArc(float rho, float diffTheta) {
        return rho * diffTheta;
    }

    private static float calRho(float diffTheta, float arc) {
        return arc / diffTheta;
    }

    private static float calDiffTheta(float rho, float arc) {
        return arc / rho;
    }

    private static float cosF(float a) {
        return (float) Math.cos(a);
    }

    private static float sinF(float a) {
        return (float) Math.sin(a);
    }

    private static float tanF(float a) {
        return (float) Math.tan(a);
    }

    private static float atanF(float a) {
        return (float) Math.atan(a);
    }

    private void init(Context context) {
        mContext = context;

        probe = ProbeSelection.simu ? (ProbeSelection.simuLinear ? SimuProbeLinear.getDefault() : SimuProbe.getDefault()) : WifiProbe.getDefault();
        int heightPx = probe.getImageHeightPx();
        int widthPx = probe.getImageWidthPx();
        int pixCount = heightPx * widthPx;
        setImageBitmap(ImageUtils.createBitmap(new byte[pixCount], widthPx, heightPx, pixCount));

        fitWidth = false;

        setFocusable(true);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.rec15x8);
        ball0 = new Ball(bitmap, new PointF(), 0);
        ball1 = new Ball(bitmap, new PointF(), 1);
        ball2 = new Ball(bitmap, new PointF(), 2);
        ball3 = new Ball(bitmap, new PointF(), 3);
        balls.add(ball0);
        balls.add(ball1);
        balls.add(ball2);
        balls.add(ball3);

        rulerPaint = new Paint();
        rulerPaint.setStyle(Paint.Style.STROKE);
        rulerPaint.setStrokeWidth(6);
        rulerPaint.setAntiAlias(false);
        rulerPaint.setColor(Color.WHITE);
        textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(32);
        textPaint.setColor(Color.WHITE);
    }

    public void initRoi() {
        logger.debug("initRoi() called");
        canvasWidth = getWidth();
        canvasHeight = getHeight();

        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float imageWidth = probe.getImageWidthPx() * values[Matrix.MSCALE_X];
        float imageHeight = probe.getImageHeightPx() * values[Matrix.MSCALE_Y];

        float width = Math.min(canvasWidth, imageWidth);
        float height = Math.min(canvasHeight, imageHeight);

        if (r == 0) {
            roiWidth = width / 3f;
            roiHeight = height / 4f;
            roiStart.x = values[Matrix.MTRANS_X] + (width - roiWidth) / 2f;
            roiStart.y = roiHeight;

            float deltaX = calDeltaXByAngle();
            ball0.setX(roiStart.x);
            ball0.setY(roiStart.y);
            ball1.setX(roiStart.x + roiWidth);
            ball1.setY(roiStart.y);
            ball2.setX(ball1.getX() + deltaX);
            ball2.setY(ball0.getY() + roiHeight);
            ball3.setX(ball0.getX() + deltaX);
            ball3.setY(ball2.getY());
        } else {
            roiDiffTheta = maxTheta;
            logger.debug("r:{}, roiDiffTheta:{}", r, roiDiffTheta);
            roiStartTheta = -maxTheta / 2f;
            roiDepth = height / 4f;
            roiStartR = r + roiDepth;
            roiEndR = roiStartR + roiDepth;
            float deltaX = roiStartR * sinF(roiStartTheta);
            float y = convexOrigin.y + (roiStartR * cosF(roiStartTheta));
            ball0.setX(convexOrigin.x + deltaX);
            ball0.setY(y);
            ball1.setX(convexOrigin.x - deltaX);
            ball1.setY(y);

            deltaX = roiEndR * sinF(roiStartTheta);
            y = convexOrigin.y + (roiEndR * cosF(roiStartTheta));
            ball2.setX(convexOrigin.x - deltaX);
            ball2.setY(y);
            ball3.setX(convexOrigin.x + deltaX);
            ball3.setY(y);

            setConvexMidRoi();
        }

        if (r == 0) {
            setLinearRoiData();
        } else {
            setConvexRoiData();
        }

        halfWidthOfBall = ball0.getWidthOfBall() / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!scanOn) {
            bModeOnTouchEvent(event);
            return true;
        }
        if (probe.getMode() == Probe.EnumMode.MODE_B) {
            bModeOnTouchEvent(event);
        } else if (probe.getMode() == Probe.EnumMode.MODE_C) {
            cModeOnTouchEvent(event);
        } else if (probe.getMode() == Probe.EnumMode.MODE_M) {
            mModeOnTouchEvent(event);
        }

        return true;
    }

    public void bModeOnTouchEvent(MotionEvent event) {
        float[] values = new float[9];
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = NONE;
                if (measureOn) {
                    getImageMatrix().getValues(values);
                    if (measureView.onTouchEvent(event, values)) {
                        mode = MEASURE;
                        return;
                    }
                }
                if (ellipseOn) {
                    getImageMatrix().getValues(values);
                    if (ellipseView.onTouchEvent(event, values)) {
                        mode = ELLIPSE;
                        return;
                    }
                }
                zoomMatrix.set(getImageMatrix());
                savedZoomMatrix.set(zoomMatrix);
                startPoint.set(event.getX(), event.getY());
                mode = MOVE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oriDist = distance(event);
                if (oriDist > 10f) {
                    savedZoomMatrix.set(zoomMatrix);
                    midPoint = middle(event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == MEASURE || mode == ELLIPSE) {
                    mode = NONE;
                    return;
                }
                mode = NONE;
                zoomMatrix.getValues(values);

                if (values[Matrix.MTRANS_Y] > 0) {
                    zoomMatrix.postTranslate(0, -values[Matrix.MTRANS_Y]);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MEASURE) {
                    getImageMatrix().getValues(values);
                    if (measureView.onTouchEvent(event, values)) {
                        invalidate();
                        return;
                    }
                }
                if (mode == ELLIPSE) {
                    getImageMatrix().getValues(values);
                    if (ellipseView.onTouchEvent(event, values)) {
                        invalidate();
                        return;
                    }
                }

                if (mode == MOVE) {
                    zoomMatrix.set(savedZoomMatrix);

                    float tx = event.getX() - startPoint.x;
                    float ty = event.getY() - startPoint.y;

                    zoomMatrix.getValues(values);

                    ty = checkTyBound(values, ty);
                    tx = checkTxBound(values, tx);
                    zoomMatrix.postTranslate(tx, ty);
                } else if (mode == ZOOM) {
                    float newDist = distance(event);
                    if (newDist > 10f) {
                        float scale = newDist / oriDist;
                        zoomMatrix.set(savedZoomMatrix);

                        zoomMatrix.getValues(values);
                        scale = checkFitScale(scale, values);
                        zoomMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                }
                break;
        }
        setImageMatrix(zoomMatrix);
    }

    public void mModeOnTouchEvent(MotionEvent event) {
        bModeOnTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus)
            return;

        logger.debug("onWindowFocusChanged() called");

        if (initDone)
            return;

        initDone = true;

        float viewWidth = (float) getWidth();
        float viewHeight = (float) getHeight();

        int width = probe.getImageWidthPx();
        int height = probe.getImageHeightPx();

        float scaleWidth = viewWidth / width;
        fitWidthMatrix = new Matrix();
        fitWidthMatrix.postScale(scaleWidth, scaleWidth);
        fitWidthMatrix.postTranslate((viewWidth - width * scaleWidth) / 2, 0);

        float scaleHeight = viewHeight / height;
        fitHeightMatrix = new Matrix();
        fitHeightMatrix.postScale(scaleHeight, scaleHeight);
        fitHeightMatrix.postTranslate((viewWidth - width * scaleHeight) / 2, 0);

        float[] values = new float[9];
        if (fitWidth) {
            setImageMatrix(fitWidthMatrix);
            fitWidthMatrix.getValues(values);
        } else {
            setImageMatrix(fitHeightMatrix);
            fitHeightMatrix.getValues(values);
        }

        mMaxScale = values[Matrix.MSCALE_X] * sMaxScale;
        mMinScale = values[Matrix.MSCALE_X] * sMinScale;

        setParams(probe.getOriginXPx(), probe.getOriginYPx(),
                probe.getRPx());

        initRoi();
        initMeasure();
        initEllipse();
    }

    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    private float checkTyBound(float[] values, float ty) {
        float viewHeight = (float) getHeight();
        float height = probe.getImageHeightPx() * values[Matrix.MSCALE_Y];

        if (height < viewHeight) {
            return -values[Matrix.MTRANS_Y];
        }

        if (values[Matrix.MTRANS_Y] + ty > 0)
            ty = -values[Matrix.MTRANS_Y];
        else if (values[Matrix.MTRANS_Y] + ty < -(height - viewHeight))
            ty = -(height - viewHeight) - values[Matrix.MTRANS_Y];
        return ty;
    }

    private float checkTxBound(float[] values, float tx) {
        float viewWidth = getWidth();
        float width = probe.getImageWidthPx() * values[Matrix.MSCALE_X];

        if (width < viewWidth) {
            if (values[Matrix.MTRANS_X] + tx < 0)
                tx = -values[Matrix.MTRANS_X];
            else if (values[Matrix.MTRANS_X] + tx > viewWidth - width)
                tx = viewWidth - width - values[Matrix.MTRANS_X];

            return tx;
        }

        if (values[Matrix.MTRANS_X] + tx > 0)
            tx = -values[Matrix.MTRANS_X];
        else if (values[Matrix.MTRANS_X] + tx < viewWidth - width)
            tx = viewWidth - width - values[Matrix.MTRANS_X];
        return tx;
    }

    private float checkFitScale(float scale, float[] values) {
        if (scale * values[Matrix.MSCALE_X] > mMaxScale)
            scale = mMaxScale / values[Matrix.MSCALE_X];
        else if (scale * values[Matrix.MSCALE_X] < mMinScale)
            scale = mMinScale / values[Matrix.MSCALE_X];
        return scale;
    }

    public void switchFit() {
        if (fitWidth) {
            fitWidth = false;
            setImageMatrix(fitHeightMatrix);
        } else {
            fitWidth = true;
            setImageMatrix(fitWidthMatrix);
        }
        initRoi();
    }

    public boolean isFitWidth() {
        return fitWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (probe.getMode() == Probe.EnumMode.MODE_C) {
            if (scanOn)
                drawOutline(canvas);
        }
        drawRuler(canvas);
        drawMeasure(canvas);
        drawEllipse(canvas);

        if (imageListener != null) {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            if (imxValues == null)
                imxValues = values;
            if (!Arrays.equals(imxValues, values)) {
                imageListener.onImageMatrixChanged();
                imxValues = values;
            }
        }
    }

    public void cModeOnTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ballId = -1;
                roiStartMovingPoint.x = x;
                roiStartMovingPoint.y = y;
                for (Ball ball : balls) {
                    float centerX = ball.getX();
                    float centerY = ball.getY();

                    float dist = calDist(x, y, centerX, centerY);

                    if (dist < (halfWidthOfBall*4)) {
                        roiMode = DRAG;
                        ballId = ball.getID();
                        break;
                    }
                }

                if (ballId < 0 && isInside(x, y)) {
                    roiMode = MOVE;
                    if (r > 0) {
                        setConvexMidRoi();
                        startMovingArc = calArc(roiEndR, roiDiffTheta);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (roiMode == MOVE) {
                    float diffX = x - roiStartMovingPoint.x;
                    float diffY = y - roiStartMovingPoint.y;

                    if (r == 0) {
                        float[] values = new float[9];
                        getImageMatrix().getValues(values);
                        moveLinearRoi(diffX, diffY, values);
                    } else {
                        float newMidX = convexMidRoi.x + diffX;
                        float newMidY = convexMidRoi.y + diffY;
                        moveConvexRoi(newMidX, newMidY);
                    }

                    roiStartMovingPoint.x = x;
                    roiStartMovingPoint.y = y;
                } else if (roiMode == DRAG) {
                    if (r == 0) {
                        float[] values = new float[9];
                        getImageMatrix().getValues(values);
                        float imageWidth = probe.getImageWidthPx() * values[Matrix.MSCALE_X];
                        float tranX = values[Matrix.MTRANS_X];
                        float skipWidth = imageWidth / 4f;
                        float leftLimit = Math.max(0f, tranX + skipWidth);
                        float rightLimit = Math.min(canvasWidth, tranX + imageWidth - skipWidth);

                        if (ballId == 0) {
                            if (y < 0f)
                                y = 0f;

                            float maxY = ball2.getY() - minWidth;
                            if (y > maxY)
                                y = maxY;

                            roiHeight = ball2.getY() - y;
                            float deltaX = calDeltaXByAngle();

                            if (probe.getColorAngleTan() < 0) {
                                leftLimit -= deltaX;
                            }

                            if (x < leftLimit)
                                x = leftLimit;

                            float maxX = ball1.getX() - minWidth;
                            if (x > maxX)
                                x = maxX;

                            roiWidth = ball1.getX() - x;
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = ball1.getX() - roiWidth;
                            }

                            ball0.setX(x);
                            ball0.setY(y);
                            ball1.setX(x + roiWidth);
                            ball1.setY(y);
                            ball2.setX(ball1.getX() + deltaX);
                            ball3.setX(x + deltaX);
                        } else if (ballId == 1) {
                            if (y < 0)
                                y = 0;

                            float maxY = ball2.getY() - minWidth;
                            if (y > maxY)
                                y = maxY;

                            roiHeight = ball2.getY() - y;

                            float deltaX = calDeltaXByAngle();
                            if (probe.getColorAngleTan() > 0) {
                                rightLimit -= deltaX;
                            }

                            if (x > rightLimit)
                                x = rightLimit;

                            float minX = ball0.getX() + minWidth;
                            if (x < minX)
                                x = minX;

                            roiWidth = x - ball0.getX();
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = roiWidth + ball0.getX();
                            }

                            ball1.setX(x);
                            ball1.setY(y);
                            ball0.setX(x - roiWidth);
                            ball0.setY(y);
                            ball2.setX(x + deltaX);
                            ball3.setX(ball0.getX() + deltaX);
                        } else if (ballId == 2) {
                            if (y > canvasHeight)
                                y = canvasHeight;

                            float minY = ball1.getY() + minWidth;
                            if (y < minY)
                                y = minY;

                            roiHeight = y - ball1.getY();

                            float deltaX = calDeltaXByAngle();
                            if (probe.getColorAngleTan() < 0) {
                                rightLimit += deltaX;
                            }

                            if (x > rightLimit)
                                x = rightLimit;

                            float minX = ball3.getX() + minWidth;
                            if (x < minX)
                                x = minX;

                            roiWidth = x - ball3.getX();
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = roiWidth + ball3.getX();
                            }

                            ball2.setX(x);
                            ball2.setY(y);
                            ball3.setX(x - roiWidth);
                            ball3.setY(y);
                            ball0.setX(ball3.getX() - deltaX);
                            ball1.setX(x - deltaX);
                        } else if (ballId == 3) {
                            if (y > canvasHeight)
                                y = canvasHeight;

                            float minY = ball0.getY() + minWidth;
                            if (y < minY)
                                y = minY;

                            roiHeight = y - ball0.getY();

                            float deltaX = calDeltaXByAngle();
                            if (probe.getColorAngleTan() > 0) {
                                leftLimit += deltaX;
                            }

                            if (x < leftLimit)
                                x = leftLimit;

                            float maxX = ball2.getX() - minWidth;
                            if (x > maxX)
                                x = maxX;

                            roiWidth = ball2.getX() - x;
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = ball2.getX() - roiWidth;
                            }

                            ball3.setX(x);
                            ball3.setY(y);
                            ball2.setX(x + roiWidth);
                            ball2.setY(y);
                            ball0.setX(x - deltaX);
                            ball1.setX(ball2.getX() - deltaX);
                        }

                        setLinearRoiData();
                    } else {
                        if (ballId == 0) {
                            float newTheta = calTheta(x, y);
                            float newDiffTheta = newTheta - roiStartTheta;

                            if (newDiffTheta < roiDiffThetaMinLimit)
                                newTheta = roiDiffThetaMinLimit + roiStartTheta;
                            else if (newTheta > maxTheta)
                                newTheta = maxTheta;

                            roiDiffTheta = newTheta - roiStartTheta;

                            float newRho = calDist(x, y, convexOrigin.x, convexOrigin.y);
                            float maxRho = roiEndR - roiDepthMinLimit;
                            if (newRho < r) {
                                newRho = r;
                            } else if (newRho > maxRho) {
                                newRho = maxRho;
                            }

                            roiDepth += roiStartR - newRho;
                            roiStartR = newRho;
                            setConvexBalls();
                        } else if (ballId == 1) {
                            float newTheta = calTheta(x, y);
                            float roiEndTheta = roiStartTheta + roiDiffTheta;
                            float newDiffTheta = roiEndTheta - newTheta;
                            float minTheta = -maxTheta;
                            if (newTheta < minTheta)
                                newTheta = minTheta;
                            else if (newDiffTheta < roiDiffThetaMinLimit)
                                newTheta = roiEndTheta - roiDiffThetaMinLimit;

                            roiStartTheta = newTheta;
                            roiDiffTheta = roiEndTheta - newTheta;

                            float newRho = calDist(x, y, convexOrigin.x, convexOrigin.y);
                            float maxRho = roiEndR - roiDepthMinLimit;
                            if (newRho < r) {
                                newRho = r;
                            } else if (newRho > maxRho) {
                                newRho = maxRho;
                            }

                            roiDepth += roiStartR - newRho;
                            roiStartR = newRho;
                            setConvexBalls();
                        } else if (ballId == 2) {
                            float newTheta = calTheta(x, y);
                            float roiEndTheta = roiStartTheta + roiDiffTheta;
                            float newDiffTheta = roiEndTheta - newTheta;
                            float minTheta = -maxTheta;
                            if (newTheta < minTheta)
                                newTheta = minTheta;
                            else if (newDiffTheta < roiDiffThetaMinLimit)
                                newTheta = roiEndTheta - roiDiffThetaMinLimit;

                            roiStartTheta = newTheta;
                            roiDiffTheta = roiEndTheta - newTheta;

                            float newRho = calDist(x, y, convexOrigin.x, convexOrigin.y);
                            float minRho = roiStartR + roiDepthMinLimit;
                            float maxRho = canvasHeight - convexOrigin.y;
                            if (newRho < minRho) {
                                newRho = minRho;
                            } else if (newRho > maxRho) {
                                newRho = maxRho;
                            }

                            roiDepth = newRho - roiStartR;
                            roiEndR = roiStartR + roiDepth;
                            setConvexBalls();
                        } else if (ballId == 3) {
                            float newTheta = calTheta(x, y);
                            float newDiffTheta = newTheta - roiStartTheta;

                            if (newDiffTheta < roiDiffThetaMinLimit)
                                newTheta = roiDiffThetaMinLimit + roiStartTheta;
                            else if (newTheta > maxTheta)
                                newTheta = maxTheta;

                            roiDiffTheta = newTheta - roiStartTheta;

                            float newRho = calDist(x, y, convexOrigin.x, convexOrigin.y);
                            float minRho = roiStartR + roiDepthMinLimit;
                            float maxRho = canvasHeight - convexOrigin.y;
                            if (newRho < minRho) {
                                newRho = minRho;
                            } else if (newRho > maxRho) {
                                newRho = maxRho;
                            }

                            roiDepth = newRho - roiStartR;
                            roiEndR = roiStartR + roiDepth;
                            setConvexBalls();
                        }

                        setConvexRoiData();
                    }
                }

                drawOutline(canvas);
                break;

            case MotionEvent.ACTION_UP:
                roiMode = NONE;
                break;
        }

        invalidate();
    }

    private void moveLinearRoi(float diffX, float diffY, float[] values) {
        float imageWidth = probe.getImageWidthPx() * values[Matrix.MSCALE_X];
        float tranX = values[Matrix.MTRANS_X];
        float skipWidth = imageWidth / 4f;
        float leftLimit = Math.max(0f, tranX + skipWidth);
        float rightLimit = Math.min(canvasWidth, tranX + imageWidth - skipWidth);

        if (probe.getColorAngleTan() <= 0f) {
            if (ball3.getX() + diffX < leftLimit) {
                diffX = leftLimit - ball3.getX();
            } else if (ball1.getX() + diffX > rightLimit) {
                diffX = rightLimit - ball1.getX();
            }
        } else {
            if (ball0.getX() + diffX < leftLimit) {
                diffX = leftLimit - ball0.getX();
            } else if (ball2.getX() + diffX > rightLimit) {
                diffX = rightLimit - ball2.getX();
            }
        }

        if (ball0.getY() + diffY < 0) {
            diffY = -ball0.getY();
        } else if (ball2.getY() + diffY > canvasHeight) {
            diffY = canvasHeight - ball2.getY();
        }

        ball0.addX(diffX);
        ball0.addY(diffY);
        ball1.addX(diffX);
        ball1.addY(diffY);
        ball2.addX(diffX);
        ball2.addY(diffY);
        ball3.addX(diffX);
        ball3.addY(diffY);

        setLinearRoiData();
    }

    private void moveConvexRoi(float newMidX, float newMidY) {
        float newMidTheta = calTheta(newMidX, newMidY);
        float minTheta = -maxTheta;
        float halfDiffTheta = roiDiffTheta / 2f;

        if (newMidTheta - halfDiffTheta < minTheta)
            newMidTheta = minTheta + halfDiffTheta;
        else if (newMidTheta + halfDiffTheta > maxTheta)
            newMidTheta = maxTheta - halfDiffTheta;

        float minEndRho = calMinEndRho(roiEndR, roiDiffTheta);

        float newMidRho = calDist(newMidX, newMidY, convexOrigin.x, convexOrigin.y);
        float maxRho = canvasHeight - convexOrigin.y;
        float halfDepth = roiDepth / 2f;
        float newEndRho = newMidRho + halfDepth;
        if (newEndRho < minEndRho) {
            newMidRho = minEndRho - halfDepth;
        } else if (newEndRho > maxRho) {
            newMidRho = maxRho - halfDepth;
        }

        setParamsByMidRoi(startMovingArc, newMidRho, newMidTheta);
        setConvexMidRoi();
        setConvexBalls();

        setConvexRoiData();
    }

    private void setLinearRoiData() {
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];
        float roiXPx = (ball0.getX() - tranX) / scaleX;
        float roiYPx = (ball0.getY() - tranY) / scaleY;
        float roiX2Px = (ball2.getX() - tranX) / scaleX;
        float roiY2Px = (ball2.getY() - tranY) / scaleY;

        probe.setLinearRoiData(roiXPx, roiYPx, roiX2Px, roiY2Px, probe.getColorAngleTan());
    }

    private void setConvexRoiData() {
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float scaleY = values[Matrix.MSCALE_Y];

        float roiStartRPx = roiStartR / scaleY;
        float roiEndRPx = roiEndR / scaleY;

        probe.setConvexRoiData(roiStartRPx, roiEndRPx,
                roiStartTheta, roiStartTheta + roiDiffTheta);
    }

    private float calTheta(float x, float y) {
        float diffX = convexOrigin.x - x;
        float diffY = Math.abs(y - convexOrigin.y);
        return atanF(diffX / diffY);
    }

    private boolean isInside(float x, float y) {
        if (r == 0) {
            if (probe.getColorAngleTan() == 0 && x >= ball0.getX() && x <= ball1.getX()
                    && y >= ball0.getY() && y <= ball2.getY())
                return true;

            if (y < ball0.getY() || y > ball2.getY())
                return false;

            float diff = (y - ball0.getY()) * probe.getColorAngleTan();
            if (x >= (ball0.getX() + diff) && x <= (ball1.getX() + diff)) {
                return true;
            }
        } else {
            float dist = calDist(x, y, convexOrigin.x, convexOrigin.y);
            if (dist < roiStartR || dist > roiEndR)
                return false;

            float theta = calTheta(x, y);
            if (theta >= roiStartTheta && theta <= roiStartTheta + roiDiffTheta)
                return true;

        }

        return false;
    }

    private void drawOutline(Canvas canvas) {
        if (r == 0) {
            if (probe.getColorAngleTan() == 0) {
                canvas.drawRect(ball0.getX(), ball0.getY(), ball2.getX(), ball2.getY(),
                        paint);
            } else {
                Path path = new Path();
                path.moveTo(ball0.getX(), ball0.getY());
                path.lineTo(ball1.getX(), ball1.getY());
                path.lineTo(ball2.getX(), ball2.getY());
                path.lineTo(ball3.getX(), ball3.getY());
                path.lineTo(ball0.getX(), ball0.getY());
                canvas.drawPath(path, paint);
            }
        } else {
            RectF rectF = new RectF();
            rectF.left = convexOrigin.x - roiStartR;
            rectF.top = convexOrigin.y - roiStartR;
            rectF.right = convexOrigin.x + roiStartR;
            rectF.bottom = convexOrigin.y + roiStartR;

            canvas.drawArc(rectF, roiStartTheta * drRatio + 90, roiDiffTheta * drRatio, false, paint);

            rectF.left = convexOrigin.x - roiEndR;
            rectF.top = convexOrigin.y - roiEndR;
            rectF.right = convexOrigin.x + roiEndR;
            rectF.bottom = convexOrigin.y + roiEndR;
            canvas.drawArc(rectF, roiStartTheta * drRatio + 90, roiDiffTheta * drRatio, false, paint);
            canvas.drawLine(ball0.getX(), ball0.getY(), ball3.getX(), ball3.getY(), paint);
            canvas.drawLine(ball1.getX(), ball1.getY(), ball2.getX(), ball2.getY(), paint);
        }

        for (Ball ball : balls) {
            canvas.drawBitmap(ball.getBitmap(), ball.getX() - halfWidthOfBall,
                    ball.getY() - halfWidthOfBall,
                    null);
        }
    }

    private float calDeltaXByAngle() {
        if (probe.getColorAngleTan() == 0)
            return 0f;

        return roiHeight * probe.getColorAngleTan();
    }

    private void setConvexMidRoi() {
        setCoordinate(roiStartR + roiDepth / 2, roiStartTheta + roiDiffTheta / 2f, convexMidRoi);
    }

    private void setCoordinate(float rho, float theta, PointF p) {
        p.x = convexOrigin.x - (rho * sinF(theta));
        p.y = convexOrigin.y + (rho * cosF(theta));
    }

    private float calMinEndRho(float rho, float diffTheta) {
        float arc = calArc(rho, diffTheta);
        float minRho = calRho(maxTheta + maxTheta, arc);
        float rLimit = r + roiDepth;
        if (minRho < rLimit)
            return rLimit;

        return minRho;
    }

    private void setParamsByMidRoi(float arc, float rho, float theta) {
        float halfDepth = roiDepth / 2f;
        roiStartR = rho - halfDepth;
        roiEndR = rho + halfDepth;
        roiDiffTheta = calDiffTheta(roiEndR, arc);
        float halfDiffTheta = roiDiffTheta / 2f;
        roiStartTheta = theta - halfDiffTheta;
    }

    private void setConvexBalls() {
        float roiEndTheta = roiStartTheta + roiDiffTheta;
        setCoordinate(roiStartR, roiEndTheta, ball0.point);
        setCoordinate(roiStartR, roiStartTheta, ball1.point);
        setCoordinate(roiEndR, roiStartTheta, ball2.point);
        setCoordinate(roiEndR, roiEndTheta, ball3.point);
    }

    public void setParams(float originXPx, float originYPx, float rPx) {
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];

        r = Math.round(rPx * scaleY);
        convexOrigin.x = tranX + originXPx * scaleX;
        convexOrigin.y = tranY + originYPx * scaleY;
        float theta = probe.getTheta();
        maxTheta = theta * 0.5f;

        if (r == 0) {
            moveLinearRoi(0, 0, values);
        } else {
            startMovingArc = calArc(roiEndR, roiDiffTheta);
            moveConvexRoi(convexMidRoi.x, convexMidRoi.y);
        }
    }

    private void initMeasure() {
        measureView.initViews(mContext);
    }

    public void startMeasure() {
        if (measureOn)
            return;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        measureView.startMeasure(values);
        measureOn = true;
        invalidate();
    }

    public void stopMeasure() {
        measureOn = false;
        invalidate();
    }

    private double getCmPerPx(Canvas canvas) {
        int maxCm = (r == 0) ? 6 : 18;
        float[] values = new float[9];
        if (fitWidth) {
            if (fitWidthMatrix == null)
                return 0;
            fitWidthMatrix.getValues(values);
        } else {
            if (fitHeightMatrix == null)
                return 0;
            fitHeightMatrix.getValues(values);
        }
        float fitScaleY = values[Matrix.MSCALE_Y];
        getImageMatrix().getValues(values);
        float realScaleY = values[Matrix.MSCALE_Y] / fitScaleY;
        int h = canvas.getHeight();
        return maxCm/(h * realScaleY);
    }

    private void drawMeasure(Canvas canvas) {
        if (!measureOn)
            return;
        double cmPerPx = getCmPerPx(canvas);
        if (cmPerPx == 0)
            return;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        measureView.onDraw(canvas, values, cmPerPx);
    }

    private void initEllipse() {
        ellipseView.init(mContext);
    }

    public void startEllipse() {
        if (ellipseOn)
            return;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        ellipseView.startEllipse(values);
        ellipseOn = true;
        invalidate();
    }

    public void stopEllipse() {
        ellipseOn = false;
        invalidate();
    }

    private void drawEllipse(Canvas canvas) {
        if (!ellipseOn)
            return;
        double cmPerPx = getCmPerPx(canvas);
        if (cmPerPx == 0)
            return;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        ellipseView.onDraw(canvas, values, cmPerPx);
    }

    private void drawRuler(Canvas canvas) {
        int maxCm = (r == 0) ? 6 : 18;
        float[] values = new float[9];
        if (fitWidth) {
            if (fitWidthMatrix == null)
                return;
            fitWidthMatrix.getValues(values);
        } else {
            if (fitHeightMatrix == null)
                return;
            fitHeightMatrix.getValues(values);
        }
        float fitScaleY = values[Matrix.MSCALE_Y];
        getImageMatrix().getValues(values);
        float scaleY = values[Matrix.MSCALE_Y] / fitScaleY;

        int h = canvas.getHeight();
        int pxPerCm = (int) ((h * scaleY) / maxCm);

        int transY = -(int) values[Matrix.MTRANS_Y];

        int startY = pxPerCm - transY % pxPerCm;
        int startCm = transY / pxPerCm;
        if (startY != 0)
            startCm++;

        for (int i = 0; i <= maxCm; i++) {
            canvas.drawLine(startX, startY, startX + scaleWidth, startY, rulerPaint);
            canvas.drawText(startCm + unit, startX + scaleWidth + 10, startY, textPaint);
            startY += pxPerCm;
            startCm++;
            if (startY > h)
                break;
        }
    }

    public Matrix getUsImageMatrix() {
        return getImageMatrix();
    }

    public void setUsImageMatrix(Matrix matrix) {
        setImageMatrix(matrix);
    }

    public void fitHeight() {
        fitWidth = false;
        setImageMatrix(fitHeightMatrix);
        initRoi();
    }

    private class Ball {
        private Bitmap bitmap;
        private PointF point;
        private int id;

        public Ball(Bitmap bitmap, PointF point, int id) {
            this.id = id;
            this.bitmap = bitmap;
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

        public float getX() {
            return point.x;
        }

        public void setX(float x) {
            point.x = x;
        }

        public float getY() {
            return point.y;
        }

        public void setY(float y) {
            point.y = y;
        }

        public int getID() {
            return id;
        }

        public void addY(float y) {
            point.y = point.y + y;
        }

        public void addX(float x) {
            point.x = point.x + x;
        }
    }
}
