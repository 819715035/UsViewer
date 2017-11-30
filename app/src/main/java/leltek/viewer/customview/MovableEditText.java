package leltek.viewer.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by rajesh on 25/9/17.
 */

public class MovableEditText extends android.support.v7.widget.AppCompatEditText {


    ICallback iCallback;


    public void initializeCallback(ICallback iCallback) {
        this.iCallback = iCallback;
        setMovable();
    }

    public interface ICallback {
        void onLongPress(View view);
        void onBack(View view);
        void onSingleTap(View view);
    }

    public MovableEditText(Context context) {
        super(context);
    }

    private void setMovable() {
        setOnTouchListener(new MovableTouchListener(this,iCallback));
    }


    public MovableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MovableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (iCallback != null)
                iCallback.onBack(this);
        }
        return super.dispatchKeyEvent(event);
    }

}
