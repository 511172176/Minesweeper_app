package edu.fju.minesweeper_app

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import java.util.regex.Pattern

@Suppress("unused")
object StatusBarUtil {
    var DEFAULT_COLOR = 0
    var DEFAULT_ALPHA = 0f //Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;
    const val MIN_API = 17

    //沉浸式
    @JvmOverloads
    fun immersive(
        activity: Activity,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA
    ) {
        immersive(activity.window, color, alpha)
    }

    /*fun immersive(activity: Activity, color: Int) {
        immersive(activity.window, color, 1f)
    }*/

    fun immersive(window: Window) {
        immersive(window, DEFAULT_COLOR, DEFAULT_ALPHA)
    }

    fun immersive(window: Window, color: Int) {
        immersive(window, color, 1f)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun immersive(window: Window, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = mixtureColor(color, alpha)
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentView(window.decorView as ViewGroup, color, alpha)
        } else if (Build.VERSION.SDK_INT >= MIN_API && Build.VERSION.SDK_INT > 16) {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }

    //</editor-fold>
    //<editor-fold desc="DarkMode">
    fun darkMode(activity: Activity, dark: Boolean) {
        if (isFlyme4Later) {
            darkModeForFlyme4(activity.window, dark)
        } else if (isMIUI6Later) {
            darkModeForMIUI6(activity.window, dark)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(activity.window, dark)
        }
    }

    /**
     * 設定狀態列darkMode,字體顏色及icon變黑(目前支援MIUI6以上,Flyme4以上,Android M以上)
     */
    fun darkMode(activity: Activity) {
        darkMode(activity.window, DEFAULT_COLOR, DEFAULT_ALPHA)
    }

    fun darkMode(activity: Activity, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        darkMode(activity.window, color, alpha)
    }

    /**
     * 設定狀態列darkMode,字體顏色及icon變黑(目前支援MIUI6以上,Flyme4以上,Android M以上)
     */
    fun darkMode(window: Window, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (isFlyme4Later) {
            darkModeForFlyme4(window, true)
            immersive(window, color, alpha)
        } else if (isMIUI6Later) {
            darkModeForMIUI6(window, true)
            immersive(window, color, alpha)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, true)
            immersive(window, color, alpha)
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentView(window.decorView as ViewGroup, color, alpha)
        } else {
            immersive(window, color, alpha)
        }
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        setTranslucentView(window.decorView as ViewGroup, color, alpha)
    }
    //------------------------->
    /**
     * android 6.0設定字體顏色
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun darkModeForM(window: Window, dark: Boolean) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (dark) {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }

    /**
     * 設定Flyme4+的darkMode,darkMode時候字體顏色及icon變黑
     * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
     */
    fun darkModeForFlyme4(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val e = window.attributes
                val darkFlag =
                    WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags =
                    WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(e)
                value = if (dark) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(e, value)
                window.setAttributes(e)
                result = true
            } catch (var8: Exception) {
                Log.e("StatusBar", "darkIcon: failed")
            }
        }
        return result
    }

    /**
     * 設定MIUI6+的狀態列是否為darkMode,darkMode時候字體顏色及icon變黑
     * http://dev.xiaomi.com/doc/p=4769/
     */
    @SuppressLint("PrivateApi")
    fun darkModeForMIUI6(window: Window, darkmode: Boolean): Boolean {
        val clazz: Class<out Window> = window.javaClass
        try {
            var darkModeFlag = 0
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod(
                "setExtraFlags",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            extraFlagField.invoke(window, if (darkmode) darkModeFlag else 0, darkModeFlag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, darkmode)
        }
        return true
    }

    val isFlyme4Later: Boolean
        /**
         * 判斷是否Flyme4以上
         */
        get() = (Build.FINGERPRINT.contains("Flyme_OS_4")
                || Build.VERSION.INCREMENTAL.contains("Flyme_OS_4")
                || Pattern.compile("Flyme OS [4|5]", Pattern.CASE_INSENSITIVE)
            .matcher(Build.DISPLAY).find())

    @get:SuppressLint("PrivateApi")
    val isMIUI6Later: Boolean
        /**
         * 判斷是否為MIUI6以上
         */
        get() = try {
            val clz = Class.forName("android.os.SystemProperties")
            val mtd = clz.getMethod("get", String::class.java)
            var `val` = (mtd.invoke(null, "ro.miui.ui.version.name") as String)
            `val` = `val`.replace("[vV]".toRegex(), "")
            val version = `val`.toInt()
            version >= 6
        } catch (e: Exception) {
            false
        }
    //</editor-fold>
    /**
     * 增加View的paddingTop,增加的值為狀態列高度
     */
    fun setPadding(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            view.setPadding(
                view.getPaddingLeft(), view.paddingTop + getStatusBarHeight(context),
                view.getPaddingRight(), view.paddingBottom
            )
        }
    }

    /**
     * 增加View的paddingTop,增加的值為狀態列高度 (智能判斷，並設定高度)
     */
    fun setPaddingSmart(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            if (lp != null && lp.height > 0) {
                lp.height += getStatusBarHeight(context) //增高
            }
            view.setPadding(
                view.getPaddingLeft(), view.paddingTop + getStatusBarHeight(context),
                view.getPaddingRight(), view.paddingBottom
            )
        }
    }

    /**
     * 增加View的高度以及paddingTop,增加的值為狀態列高度.一般是在沉浸式全螢幕給ToolBar用的
     */
    fun setHeightAndPadding(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            lp.height += getStatusBarHeight(context) //增高
            view.setPadding(
                view.getPaddingLeft(), view.paddingTop + getStatusBarHeight(context),
                view.getPaddingRight(), view.paddingBottom
            )
        }
    }

    /**
     * 增加View上边距（MarginTop）一般是给高度为 WARP_CONTENT 的小控件用的
     */
    fun setMargin(context: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            if (lp is MarginLayoutParams) {
                lp.topMargin += getStatusBarHeight(context) //增高
            }
            view.setLayoutParams(lp)
        }
    }

    /**
     * 创建假的透明栏
     */
    @SuppressLint("ObsoleteSdkInt")
    fun setTranslucentView(
        container: ViewGroup,
        color: Int,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float
    ) {
        if (Build.VERSION.SDK_INT >= 19) {
            val mixtureColor = mixtureColor(color, alpha)
            var translucentView = container.findViewById<View>(R.id.custom)
            if (translucentView == null && mixtureColor != 0) {
                translucentView = View(container.context)
                translucentView.setId(R.id.custom)
                val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.context)
                )
                container.addView(translucentView, lp)
            }
            translucentView?.setBackgroundColor(mixtureColor)
        }
    }

    fun mixtureColor(color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
        val a = if (color and -0x1000000 == 0) 0xff else color ushr 24
        return color and 0x00ffffff or ((a * alpha).toInt() shl 24)
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 24
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        result = if (resId > 0) {
            context.resources.getDimensionPixelSize(resId)
        } else {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                result.toFloat(), Resources.getSystem().displayMetrics
            ).toInt()
        }
        return result
    }
}
