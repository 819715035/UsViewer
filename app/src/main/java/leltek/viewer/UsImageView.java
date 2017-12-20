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
import leltek.viewer.customview.RoiView;
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
    enum TouchMode {
        NONE, MOVE, ZOOM, DRAG, MEASURE, ELLIPSE, ROI
    }
    private final static float sMaxScale = 4f;
    private final static float sMinScale = 1f;
    private Probe probe;
    private Matrix zoomMatrix = new Matrix();
    private Matrix savedZoomMatrix = new Matrix();
    private TouchMode mode = TouchMode.NONE;
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
    private int r;
    private Paint rulerPaint, textPaint;
    private int startX = 6;
    private String unit = " ";
    private int scaleWidth = 10;
    private ImageListener imageListener = null;
    private float[] imxValues = null;
    private Context mContext = null;
    public boolean scanOn = true;
    private MeasureView measureView = new MeasureView();
    private EllipseView ellipseView = new EllipseView();
    private RoiView roiView = new RoiView();
    private boolean measureOn = false;
    private boolean ellipseOn = false;
    private boolean roiOn = false;

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

        roiView.init(context, probe);
    }

    public void initRoi() {
        roiView.initRoi(getWidth(), getHeight());
    }

    public void startRoi() {
        if (roiOn)
            return;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        roiView.start(values);
        roiOn = true;
        invalidate();
    }

    public void stopRoi() {
        roiOn = false;
        invalidate();
    }

    private void drawRoi(Canvas canvas) {
        if (!roiOn)
            return;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        roiView.onDraw(canvas, values);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        modeOnTouchEvent(event);
        return true;
    }

    private void modeOnTouchEvent(MotionEvent event) {
        float[] values = new float[9];
        int eventAction = event.getAction() & MotionEvent.ACTION_MASK;
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                mode = TouchMode.NONE;
                if (measureOn) {
                    getImageMatrix().getValues(values);
                    if (measureView.onTouchEvent(event, values)) {
                        mode = TouchMode.MEASURE;
                        return;
                    }
                }
                if (ellipseOn) {
                    getImageMatrix().getValues(values);
                    if (ellipseView.onTouchEvent(event, values)) {
                        mode = TouchMode.ELLIPSE;
                        return;
                    }
                }
                if (roiOn&&scanOn) {
                    getImageMatrix().getValues(values);
                    if (roiView.onTouchEvent(event, values)) {
                        mode = TouchMode.ROI;
                        return;
                    }
                }
                zoomMatrix.set(getImageMatrix());
                savedZoomMatrix.set(zoomMatrix);
                startPoint.set(event.getX(), event.getY());
                mode = TouchMode.MOVE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mode == TouchMode.MOVE) {
                    oriDist = distance(event);
                    if (oriDist > 10f) {
                        savedZoomMatrix.set(zoomMatrix);
                        midPoint = middle(event);
                        mode = TouchMode.ZOOM;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == TouchMode.ROI) {
                    roiView.onTouchEvent(event, values);
                    mode = TouchMode.NONE;
                    return;
                }                
                if (mode == TouchMode.MEASURE || mode == TouchMode.ELLIPSE) {
                    mode = TouchMode.NONE;
                    return;
                }
                if (mode == TouchMode.MOVE || mode == TouchMode.ZOOM) {
                    mode = TouchMode.NONE;
                    zoomMatrix.getValues(values);

                    if (values[Matrix.MTRANS_Y] > 0) {
                        zoomMatrix.postTranslate(0, -values[Matrix.MTRANS_Y]);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == TouchMode.MEASURE) {
                    getImageMatrix().getValues(values);
                    if (measureView.onTouchEvent(event, values)) {
                        invalidate();
                        return;
                    }
                }
                if (mode == TouchMode.ELLIPSE) {
                    getImageMatrix().getValues(values);
                    if (ellipseView.onTouchEvent(event, values)) {
                        invalidate();
                        return;
                    }
                }
                if (mode == TouchMode.ROI) {
                    getImageMatrix().getValues(values);
                    if (roiView.onTouchEvent(event, values)) {
                        invalidate();
                        return;
                    }
                }
                if (mode == TouchMode.MOVE) {
                    zoomMatrix.set(savedZoomMatrix);

                    float tx = event.getX() - startPoint.x;
                    float ty = event.getY() - startPoint.y;

                    zoomMatrix.getValues(values);

                    ty = checkTyBound(values, ty);
                    tx = checkTxBound(values, tx);
                    zoomMatrix.postTranslate(tx, ty);
                } else if (mode == TouchMode.ZOOM) {
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

        drawRuler(canvas);
        drawMeasure(canvas);
        drawEllipse(canvas);
        drawRoi(canvas);

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

    public void setParams(float originXPx, float originYPx, float rPx) {
        r = Math.round(rPx);
        roiView.setParams(originXPx, originYPx, rPx);
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
}
