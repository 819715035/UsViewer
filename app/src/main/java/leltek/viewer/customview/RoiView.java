package leltek.viewer.customview;

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
import android.view.MotionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import leltek.viewer.R;
import leltek.viewer.model.Probe;

public class RoiView {
    final static Logger logger = LoggerFactory.getLogger(RoiView.class);
    enum RoiMode {
        NONE, MOVE, DRAG
    }
    private final static float rdRatio = (float) (Math.PI / 180);
    private final static float drRatio = (float) (180 / Math.PI);
    private final float minWidth = 100;
    private final PointF convexOrigin = new PointF();
    private final float roiDiffThetaMinLimit = 15 * rdRatio;
    private final float roiDepthMinLimit = 200;
    private Probe probe;
    private Matrix zoomMatrix = new Matrix();
    private Matrix savedZoomMatrix = new Matrix();
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private Paint paint = new Paint();
    private ArrayList<Ball> balls = new ArrayList<>();
    private Ball ball0;
    private Ball ball1;
    private Ball ball2;
    private Ball ball3;
    private int ballId;
    private int halfWidthOfBall;
    private RoiMode roiMode = RoiMode.NONE;
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
    private Context mContext = null;

    public void init(Context context, Probe probe) {

        this.probe = probe;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.rec15x8);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        ball0 = new Ball(bitmap, new PointF(), 0);
        ball1 = new Ball(bitmap, new PointF(), 1);
        ball2 = new Ball(bitmap, new PointF(), 2);
        ball3 = new Ball(bitmap, new PointF(), 3);
        balls.add(ball0);
        balls.add(ball1);
        balls.add(ball2);
        balls.add(ball3);
    }

    public void initRoi(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    public void start(float[] values) {

        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];

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
            
            float p0x = roiStart.x;
            float p0y = roiStart.y;
            float p1x = roiStart.x + roiWidth;
            float p1y = roiStart.y;
            float p2x = p1x + deltaX;
            float p2y = p0y + roiHeight;
            float p3x = p0x + deltaX;
            float p3y = p2y;
            
            p0x = (p0x - tranX) / scaleX;
            p0y = (p0y - tranY) / scaleY;
            p1x = (p1x - tranX) / scaleX;
            p1y = (p1y - tranY) / scaleY;
            p2x = (p2x - tranX) / scaleX;
            p2y = (p2y - tranY) / scaleY;
            p3x = (p3x - tranX) / scaleX;
            p3y = (p3y - tranY) / scaleY;            
            
            ball0.setX(p0x);
            ball0.setY(p0y);
            ball1.setX(p1x);
            ball1.setY(p1y);
            ball2.setX(p2x);
            ball2.setY(p2y);
            ball3.setX(p3x);
            ball3.setY(p3y);
            
        } else {
            
            roiDiffTheta = maxTheta;
            logger.debug("r:{}, roiDiffTheta:{}", r, roiDiffTheta);
            roiStartTheta = -maxTheta / 2f;
            roiDepth = height / 8f;
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

    public void onDraw(Canvas canvas, float[] values) {
    
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];
        
        float b0x = ball0.getX() * scaleX + tranX;
        float b0y = ball0.getY() * scaleY + tranY;
        float b1x = ball1.getX() * scaleX + tranX;
        float b1y = ball1.getY() * scaleY + tranY;
        float b2x = ball2.getX() * scaleX + tranX;
        float b2y = ball2.getY() * scaleY + tranY;
        float b3x = ball3.getX() * scaleX + tranX;
        float b3y = ball3.getY() * scaleY + tranY;
    
        if (r == 0) {
        
            if (probe.getColorAngleTan() == 0) {
                canvas.drawRect(b0x, b0y, b2x, b2y, paint);
            } else {
                Path path = new Path();
                path.moveTo(b0x, b0y);
                path.lineTo(b1x, b1y);
                path.lineTo(b2x, b2y);
                path.lineTo(b3x, b3y);
                path.lineTo(b0x, b0y);
                canvas.drawPath(path, paint);
            }
        } else {
            RectF rectF = new RectF();
            rectF.left = convexOrigin.x - roiStartR;
            rectF.top = convexOrigin.y - roiStartR;
            rectF.right = convexOrigin.x + roiStartR;
            rectF.bottom = convexOrigin.y + roiStartR;
            
            rectF.left = rectF.left * scaleX + tranX;
            rectF.top = rectF.top * scaleY + tranY;
            rectF.right = rectF.right * scaleX + tranX;
            rectF.bottom = rectF.bottom * scaleY + tranY;            

            canvas.drawArc(rectF, roiStartTheta * drRatio + 90, roiDiffTheta * drRatio, false, paint);

            rectF.left = convexOrigin.x - roiEndR;
            rectF.top = convexOrigin.y - roiEndR;
            rectF.right = convexOrigin.x + roiEndR;
            rectF.bottom = convexOrigin.y + roiEndR;
            
            rectF.left = rectF.left * scaleX + tranX;
            rectF.top = rectF.top * scaleY + tranY;
            rectF.right = rectF.right * scaleX + tranX;
            rectF.bottom = rectF.bottom * scaleY + tranY;            
            
            canvas.drawArc(rectF, roiStartTheta * drRatio + 90, roiDiffTheta * drRatio, false, paint);
            canvas.drawLine(b0x, b0y, b3x, b3y, paint);
            canvas.drawLine(b1x, b1y, b2x, b2y, paint);
        }

        for (Ball ball : balls) {
            float x = ball.getX() * scaleX + tranX;
            float y = ball.getY() * scaleY + tranY;            
            canvas.drawBitmap(ball.getBitmap(), x - halfWidthOfBall, y - halfWidthOfBall, null);            
        }
    }
   
    public boolean onTouchEvent(MotionEvent event, float[] values) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];
        
        float b0x = ball0.getX() * scaleX + tranX;
        float b0y = ball0.getY() * scaleY + tranY;
        float b1x = ball1.getX() * scaleX + tranX;
        float b1y = ball1.getY() * scaleY + tranY;
        float b2x = ball2.getX() * scaleX + tranX;
        float b2y = ball2.getY() * scaleY + tranY;
        float b3x = ball3.getX() * scaleX + tranX;
        float b3y = ball3.getY() * scaleY + tranY;       

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                ballId = -1;
                roiStartMovingPoint.x = x;
                roiStartMovingPoint.y = y;
                for (Ball ball : balls) {
                    float centerX = ball.getX() * scaleX + tranX;
                    float centerY = ball.getY() * scaleY + tranY;

                    float dist = calDist(x, y, centerX, centerY);

                    if (dist < (halfWidthOfBall*4)) {
                        roiMode = RoiMode.DRAG;                 
                        ballId = ball.getID();
                        break;
                    }
                }

                if (ballId < 0 && isInside(x, y, values)) {
                    roiMode = RoiMode.MOVE;
                    if (r > 0) {
                        setConvexMidRoi();
                        startMovingArc = calArc(roiEndR, roiDiffTheta);
                    }
                }

                break;

            case MotionEvent.ACTION_MOVE:
                if (roiMode == RoiMode.MOVE) {
                    float diffX = x - roiStartMovingPoint.x;
                    float diffY = y - roiStartMovingPoint.y;

                    if (r == 0) {
                        moveLinearRoi(diffX, diffY, values);
                    } else {
                        float newMidX = convexMidRoi.x + diffX/scaleX;
                        float newMidY = convexMidRoi.y + diffY/scaleY;
                        moveConvexRoi(newMidX, newMidY);
                    }

                    roiStartMovingPoint.x = x;
                    roiStartMovingPoint.y = y;
                } else if (roiMode == RoiMode.DRAG) {
                    if (r == 0) {
                        float imageWidth = probe.getImageWidthPx() * values[Matrix.MSCALE_X];
                        float skipWidth = imageWidth / 4f;
                        float leftLimit = Math.max(0f, tranX + skipWidth);
                        float rightLimit = Math.min(canvasWidth, tranX + imageWidth - skipWidth);

                        if (ballId == 0) {
                            if (y < 0f)
                                y = 0f;

                            float maxY = b2y - minWidth;
                            if (y > maxY)
                                y = maxY;

                            roiHeight = b2y - y;
                            float deltaX = calDeltaXByAngle();

                            if (probe.getColorAngleTan() < 0) {
                                leftLimit -= deltaX;
                            }

                            if (x < leftLimit)
                                x = leftLimit;

                            float maxX = b1x - minWidth;
                            if (x > maxX)
                                x = maxX;

                            roiWidth = b1x - x;
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = b1x - roiWidth;
                            }
                            float p0x = x;
                            float p0y = y;
                            float p1x = x + roiWidth;
                            float p1y = y;
                            float p2x = p1x + deltaX;
                            float p3x = x + deltaX;
                            
                            p0x = (p0x - tranX) / scaleX;
                            p0y = (p0y - tranY) / scaleY;
                            p1x = (p1x - tranX) / scaleX;
                            p1y = (p1y - tranY) / scaleY;
                            p2x = (p2x - tranX) / scaleX;
                            p3x = (p3x - tranX) / scaleX;
                            
                            ball0.setX(p0x);
                            ball0.setY(p0y);
                            ball1.setX(p1x);
                            ball1.setY(p1y);
                            ball2.setX(p2x);
                            ball3.setX(p3x);
                            
                        } else if (ballId == 1) {
                            if (y < 0)
                                y = 0;

                            float maxY = b2y - minWidth;
                            if (y > maxY)
                                y = maxY;

                            roiHeight = b2y - y;

                            float deltaX = calDeltaXByAngle();
                            if (probe.getColorAngleTan() > 0) {
                                rightLimit -= deltaX;
                            }

                            if (x > rightLimit)
                                x = rightLimit;

                            float minX = b0x + minWidth;
                            if (x < minX)
                                x = minX;

                            roiWidth = x - b0x;
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = roiWidth + b0x;
                            }
                            float p1x = x;
                            float p1y = y;
                            float p0x = x - roiWidth;
                            float p0y = y;
                            float p2x = x + deltaX;
                            float p3x = p0x + deltaX;
                            
                            p0x = (p0x - tranX) / scaleX;
                            p0y = (p0y - tranY) / scaleY;
                            p1x = (p1x - tranX) / scaleX;
                            p1y = (p1y - tranY) / scaleY;
                            p2x = (p2x - tranX) / scaleX;
                            p3x = (p3x - tranX) / scaleX;
                            
                            ball0.setX(p0x);
                            ball0.setY(p0y);
                            ball1.setX(p1x);                           
                            ball1.setY(p1y);                           
                            ball2.setX(p2x);
                            ball3.setX(p3x);                            
                            
                        } else if (ballId == 2) {
                            if (y > canvasHeight)
                                y = canvasHeight;

                            float minY = b1y + minWidth;
                            if (y < minY)
                                y = minY;

                            roiHeight = y - b1y;

                            float deltaX = calDeltaXByAngle();
                            if (probe.getColorAngleTan() < 0) {
                                rightLimit += deltaX;
                            }

                            if (x > rightLimit)
                                x = rightLimit;

                            float minX = b3x + minWidth;
                            if (x < minX)
                                x = minX;

                            roiWidth = x - b3x;
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = roiWidth + b3x;
                            }                            
                            float p2x = x;
                            float p2y = y;
                            float p3x = x - roiWidth;
                            float p3y = y;
                            float p0x = p3x - deltaX;
                            float p1x = x - deltaX;
                            
                            p0x = (p0x - tranX) / scaleX;
                            p1x = (p1x - tranX) / scaleX;
                            p2x = (p2x - tranX) / scaleX;
                            p2y = (p2y - tranY) / scaleY;
                            p3x = (p3x - tranX) / scaleX;
                            p3y = (p3y - tranY) / scaleY;
                            
                            ball2.setX(p2x);
                            ball2.setY(p2y);
                            ball3.setX(p3x);
                            ball3.setY(p3y);
                            ball0.setX(p0x);
                            ball1.setX(p1x);                            
                            
                            
                        } else if (ballId == 3) {
                            if (y > canvasHeight)
                                y = canvasHeight;

                            float minY = b0y + minWidth;
                            if (y < minY)
                                y = minY;

                            roiHeight = y - b0y;

                            float deltaX = calDeltaXByAngle();
                            if (probe.getColorAngleTan() > 0) {
                                leftLimit += deltaX;
                            }

                            if (x < leftLimit)
                                x = leftLimit;

                            float maxX = b2x - minWidth;                            
                            if (x > maxX)
                                x = maxX;

                            roiWidth = b2x - x;
                            float temp = imageWidth - skipWidth - skipWidth - Math.abs(deltaX);
                            if (roiWidth > temp) {
                                roiWidth = temp;
                            }

                            if (roiWidth > canvasWidth) {
                                roiWidth = canvasWidth;
                                x = b2x - roiWidth;
                            }
                            float p3x = x;
                            float p3y = y;
                            float p2x = x + roiWidth;
                            float p2y = y;
                            float p0x = x - deltaX;
                            float p1x = p2x - deltaX;
                            
                            p0x = (p0x - tranX) / scaleX;
                            p1x = (p1x - tranX) / scaleX;
                            p2x = (p2x - tranX) / scaleX;
                            p2y = (p2y - tranY) / scaleY;
                            p3x = (p3x - tranX) / scaleX;
                            p3y = (p3y - tranY) / scaleY;
                            
                            ball3.setX(p3x);
                            ball3.setY(p3y);
                            ball2.setX(p2x);
                            ball2.setY(p2y);
                            ball0.setX(p0x);
                            ball1.setX(p1x);
                        }

                        setLinearRoiData();
                    } else {
                        x = (x - tranX) / scaleX;
                        y = (y - tranY) / scaleY;
                        
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

                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                roiMode = RoiMode.NONE;
                break;
        }

        return (roiMode != RoiMode.NONE);
    }

    public void moveLinearRoi(float diffX, float diffY, float[] values) {
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];        
        
        float imageWidth = probe.getImageWidthPx() * values[Matrix.MSCALE_X];
        float skipWidth = imageWidth / 4f;
        float leftLimit = Math.max(0f, tranX + skipWidth);
        float rightLimit = Math.min(canvasWidth, tranX + imageWidth - skipWidth);
        
        float p0x = ball0.getX() * scaleX + tranX;
        float p0y = ball0.getY() * scaleY + tranY;
        float p1x = ball1.getX() * scaleX + tranX;
        float p1y = ball1.getY() * scaleY + tranY;
        float p2x = ball2.getX() * scaleX + tranX;
        float p2y = ball2.getY() * scaleY + tranY;
        float p3x = ball3.getX() * scaleX + tranX;
        float p3y = ball3.getY() * scaleY + tranY;
        
        if (probe.getColorAngleTan() <= 0f) {
            if (p3x + diffX < leftLimit) {
                diffX = leftLimit - p3x;
            } else if (p1x + diffX > rightLimit) {
                diffX = rightLimit - p1x;
            }
        } else {
            if (p0x + diffX < leftLimit) {
                diffX = leftLimit - p0x;
            } else if (p2x + diffX > rightLimit) {
                diffX = rightLimit - p2x;
            }
        }

        if (p0y + diffY < 0) {
            diffY = -p0y;
        } else if (p2y + diffY > canvasHeight) {
            diffY = canvasHeight - p2y;
        }
                
        diffX = diffX / scaleX;
        diffY = diffY / scaleY;
        
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

    public void moveConvexRoi(float newMidX, float newMidY) {
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
        probe.setLinearRoiData(ball0.getX(), ball0.getY(), ball2.getX(), ball2.getY(), probe.getColorAngleTan());
    }

    private void setConvexRoiData() {
        probe.setConvexRoiData(roiStartR, roiEndR,
                roiStartTheta, roiStartTheta + roiDiffTheta);
    }

    private float calTheta(float x, float y) {
        float diffX = convexOrigin.x - x;
        float diffY = Math.abs(y - convexOrigin.y);
        return atanF(diffX / diffY);
    }

    private boolean isInside(float x, float y, float[] values) {        
        
        float tranX = values[Matrix.MTRANS_X];
        float scaleX = values[Matrix.MSCALE_X];
        float tranY = values[Matrix.MTRANS_Y];
        float scaleY = values[Matrix.MSCALE_Y];
        
        float b0x = ball0.getX() * scaleX + tranX;
        float b0y = ball0.getY() * scaleY + tranY;
        float b1x = ball1.getX() * scaleX + tranX;
        float b1y = ball1.getY() * scaleY + tranY;
        float b2x = ball2.getX() * scaleX + tranX;
        float b2y = ball2.getY() * scaleY + tranY;
        float b3x = ball3.getX() * scaleX + tranX;
        float b3y = ball3.getY() * scaleY + tranY;
        
        if (r == 0) {
            if (probe.getColorAngleTan() == 0 && x >= b0x && x <= b1x
                    && y >= b0y && y <= b2y)
                return true;
            if (y < b0y || y > b2y)
                return false;
            float diff = (y - b0y) * probe.getColorAngleTan();
            if (x >= (b0x + diff) && x <= (b1x + diff)) {
                return true;
            }
        } else {
            x = (x - tranX) / scaleX;
            y = (y - tranY) / scaleY;            
            float dist = calDist(x, y, convexOrigin.x, convexOrigin.y);
            if (dist < roiStartR || dist > roiEndR)
                return false;

            float theta = calTheta(x, y);
            if (theta >= roiStartTheta && theta <= roiStartTheta + roiDiffTheta)
                return true;
        }

        return false;
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
        r = Math.round(rPx);
        convexOrigin.x = originXPx;
        convexOrigin.y = originYPx;
        
        float theta = probe.getTheta();
        maxTheta = theta * 0.5f;

        if (r == 0) {
            //moveLinearRoi(0, 0, values);
        } else {
            startMovingArc = calArc(roiEndR, roiDiffTheta);
            //moveConvexRoi(convexMidRoi.x, convexMidRoi.y, values);
        }
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