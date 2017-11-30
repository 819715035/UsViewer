package leltek.viewer.customview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by rajesh on 1/10/17.
 */

public class TouchView extends FrameLayout {
    public TouchView(@NonNull Context context) {
        super(context);
        initTouch();
    }

    public TouchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTouch();

    }

    public TouchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTouch();

    }

    public TouchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initTouch();
    }

    private void initTouch() {
        setOnTouchListener(new MovableTouchListener(this,null));
    }
}
