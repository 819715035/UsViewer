package leltek.viewer.customview;

import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by rajesh on 26/9/17.
 */

public class MovableTouchListener implements View.OnTouchListener {
    private GestureDetector mGestureDetector;
    private View mView;
    private int leftMargin;
    private int topMargin;
    private float scaleX;
    private float scaleY;
    private float scrollX;
    private float scrollY;

    MovableTouchListener(final View view, final MovableEditText.ICallback iCallback) {
        GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
            private float mMotionDownX, mMotionDownY;

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                if (iCallback != null) {
                    iCallback.onLongPress(view);
                }
            }

            @Override
            public boolean onDown(MotionEvent e) {
                mMotionDownX = e.getRawX();
                mMotionDownY = e.getRawY();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
                leftMargin = params.leftMargin;
                topMargin = params.topMargin;
                float[] values = iCallback.getUsImageMatrixValues();
                scaleX = values[Matrix.MSCALE_X];
                scaleY = values[Matrix.MSCALE_Y];
                MovableEditText met = (MovableEditText)mView;
                scrollX = met.scrollX;
                scrollY = met.scrollY;
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
                params.leftMargin = (int)(leftMargin + e2.getRawX() - mMotionDownX);
                params.topMargin = (int)(topMargin + e2.getRawY() - mMotionDownY);
                mView.requestLayout();
                MovableEditText met = (MovableEditText)mView;
                met.scrollX = scrollX + (e2.getRawX() - mMotionDownX)/scaleX;
                met.scrollY = scrollY + (e2.getRawY() - mMotionDownY)/scaleY;
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (iCallback != null)
                    iCallback.onSingleTap(view);
                return true;
            }
        };
        mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
        mView = view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

}
