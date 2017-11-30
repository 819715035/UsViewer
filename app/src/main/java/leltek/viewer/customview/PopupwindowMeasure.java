package leltek.viewer.customview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import leltek.viewer.R;
import leltek.viewer.constants.AppConstant;

/**
 * Created by rajesh on 1/10/17.
 */

public class PopupwindowMeasure extends PopupWindow {
    private ICallback iCallback;

    public interface ICallback {
        void onPositionClick(int position);
    }


    public void init(Context context, View attachWith, ICallback iCallback) {
        this.iCallback = iCallback;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.popup_measure, null);
        setContentView(view);
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int OFFSET_X = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        int OFFSET_Y = 30;
        view.findViewById(R.id.tvCancel).setOnClickListener(new OnItemClick(AppConstant.CLEAR_ALL));
        view.findViewById(R.id.tvEllipse).setOnClickListener(new OnItemClick(AppConstant.ELLIPSE));
        view.findViewById(R.id.tvDistance).setOnClickListener(new OnItemClick(AppConstant.DISTANCE));
        int[] location = new int[2];
        attachWith.getLocationOnScreen(location);
        Point point = new Point();
        point.x = location[0];
        point.y = location[1];
        showAtLocation(view, Gravity.NO_GRAVITY, point.x + OFFSET_X, point.y + OFFSET_Y);
    }

    private class OnItemClick implements View.OnClickListener {
        int action;

        OnItemClick(int action) {
            this.action = action;
        }

        @Override
        public void onClick(View view) {
            iCallback.onPositionClick(action);
            dismiss();
        }
    }
}
