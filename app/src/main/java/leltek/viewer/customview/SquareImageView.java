package leltek.viewer.customview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.FrameLayout;

import static leltek.viewer.constants.AppConstant.MAX_LENGTH;

/**
 * Square imageView
 */

public class SquareImageView extends FrameLayout {
    public SquareImageView(@NonNull Context context) {
        super(context);
    }

    public SquareImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Pair<Integer, Integer> result = onMeasureImpl(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(result.first, result.second.intValue());
    }

    static Pair<Integer, Integer> onMeasureImpl(int widthMeasureSpec, int heightMeasureSpec) {
        int i = MAX_LENGTH;
        if (widthMeasureSpec == 0 && heightMeasureSpec == 0) {
            return Pair.create(widthMeasureSpec, heightMeasureSpec);
        }
        int newWidthMeasureSpec;
        int newHeightMeasureSpec;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int minSize = Math.min(widthSize, heightSize);
        int i2;
        if (minSize <= 1) {
            int maxSize = Math.max(widthSize, heightSize);
            if (widthMode == MAX_LENGTH) {
                i2 = MAX_LENGTH;
            } else {
                i2 = Integer.MIN_VALUE;
            }
            newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxSize, i2);
            if (heightMode != MAX_LENGTH) {
                i = Integer.MIN_VALUE;
            }
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxSize, i);
        } else {
            if (widthMode == MAX_LENGTH) {
                i2 = MAX_LENGTH;
            } else {
                i2 = Integer.MIN_VALUE;
            }
            newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(minSize, i2);
            if (heightMode != MAX_LENGTH) {
                i = Integer.MIN_VALUE;
            }
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(minSize, i);
        }
        return Pair.create(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}
