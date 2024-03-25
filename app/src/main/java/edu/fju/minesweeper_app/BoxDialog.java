package edu.fju.minesweeper_app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 引用@ https://github.com/ThirdGoddess/trap/tree/master
 */
public class BoxDialog extends Dialog {

    //Dialog View
    private View view;

    //Dialog彈出位置
    private LocationView locationView = LocationView.CENTER;

    /**
     * @param context 上下文
     * @param view    Dialog View
     */
    public BoxDialog(Context context, View view) {
        super(context, R.style.BoxDialog);
        this.view = view;
    }

    /**
     * @param context      上下文
     * @param view         Dialog View
     * @param locationView Dialog彈出位置
     */
    public BoxDialog(Context context, View view, LocationView locationView) {
        super(context, R.style.BoxDialog);
        this.view = view;
        this.locationView = locationView;
    }


    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != view) {
            setContentView(view);
            setCancelable(false);//點選外部是否可以關閉Dialog
            setCanceledOnTouchOutside(false);//返回鍵是否可以關閉Dialog
            Window window = this.getWindow();
            assert window != null;
            switch (locationView) {
                case TOP:
                    window.setGravity(Gravity.TOP);
                    break;
                case BOTTOM:
                    window.setGravity(Gravity.BOTTOM);
                    break;
                case CENTER:
                    window.setGravity(Gravity.CENTER);
                    break;
            }
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    public enum LocationView {
        CENTER, TOP, BOTTOM
    }
}

