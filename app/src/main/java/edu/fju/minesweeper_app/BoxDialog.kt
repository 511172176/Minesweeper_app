package edu.fju.minesweeper_app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * 引用@ https://github.com/ThirdGoddess/trap/tree/master
 */
class BoxDialog : Dialog {
    //Dialog View
    private var view: View?

    //Dialog彈出位置
    private var locationView = LocationView.CENTER

    /**
     * @param context 上下文
     * @param view    Dialog View
     */
    constructor(context: Context?, view: View?) : super(context!!, R.style.BoxDialog) {
        this.view = view
    }

    /**
     * @param context      上下文
     * @param view         Dialog View
     * @param locationView Dialog彈出位置
     */
    constructor(context: Context?, view: View?, locationView: LocationView) : super(
        context!!, R.style.BoxDialog
    ) {
        this.view = view
        this.locationView = locationView
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        if (null != view) {
            setContentView(view!!)
            setCancelable(false) //點選外部是否可以關閉Dialog
            setCanceledOnTouchOutside(false) //返回鍵是否可以關閉Dialog
            val window = this.window!!
            when (locationView) {
                LocationView.TOP -> window.setGravity(Gravity.TOP)
                LocationView.BOTTOM -> window.setGravity(Gravity.BOTTOM)
                LocationView.CENTER -> window.setGravity(Gravity.CENTER)
            }
            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.setAttributes(params)
        }
    }

    enum class LocationView {
        CENTER,
        TOP,
        BOTTOM
    }
}
