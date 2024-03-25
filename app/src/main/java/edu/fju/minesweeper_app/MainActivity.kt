package edu.fju.minesweeper_app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 定義 MainActivity，繼承自 AppCompatActivity 進行 UI 控制
class MainActivity : AppCompatActivity() {

    // 定義遊戲所需的變量和控件參考
    lateinit var rcAdapter: RcAdapter // 適配器，用於控制 RecyclerView 的數據和表現
    lateinit var flagNum: TextView // 顯示剩餘標記數量的 TextView
    lateinit var time: TextView // 顯示計時器時間的 TextView
    lateinit var reset: TextView // 觸發重置遊戲的 TextView

    private var time1 = 0L // 計時器的分鐘數
    private var time2 = 0L // 計時器的秒數

    private val handler: Handler = Handler() // 用於實現計時功能的 Handler

    // 勝利彈框的延遲初始化
    private lateinit var boxDialog: BoxDialog

    // 計時器，每秒更新一次時間顯示
    private val mCounter: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            handler.postDelayed(this, 1000)
            time2++
            if (60L == time2) {
                time1++
                time2 = 0
            }
            // 更新時間顯示，格式化為 00:00
            time.text =
                (if (time1 < 10) "0$time1" else time1.toString()) + ":" + if (time2 < 10) "0$time2" else time2.toString()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 狀態欄設置
        val titleBar = findViewById<LinearLayout>(R.id.titleBar)
        StatusBarUtil.immersive(this)
        StatusBarUtil.darkMode(this)
        StatusBarUtil.setPaddingSmart(this, titleBar)

        // 控件實例化
        flagNum = findViewById(R.id.flagNum)
        time = findViewById(R.id.time)
        reset = findViewById(R.id.reset)

        // 加載遊戲布局
        initView()

        // 重置點擊事件設置
        reset.setOnClickListener {
            // 重新創建遊戲
            MinefieldUtil.reset()

            // 計時器重置
            handler.removeCallbacks(mCounter)
            time.text = "00:00"

            // 小紅旗重置
            flagNum.text = "--"

            // 列表刷新
            rcAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initView() {
        val rc = findViewById<RecyclerView>(R.id.rc)
        val layoutParams = rc.layoutParams
        val pxValue = dip2px(this, 38F)
        layoutParams.width = pxValue * 9
        layoutParams.height = pxValue * 16

        rc.layoutManager = GridLayoutManager(this, 9)
        rcAdapter = RcAdapter(this)
        rc.adapter = rcAdapter

        rcAdapter.setDataCallBack(object : RcAdapter.DataCallBack {
            // 遊戲開始
            override fun gameStart() {
                time1 = 0
                time2 = 0
                handler.post(mCounter)
            }

            // 遊戲結束
            override fun gameOver() {
                MinefieldUtil.isEstablish = false
                flagNum.text = "--"

                // 停止計時
                handler.removeCallbacks(mCounter)
            }

            // 使用小紅旗
            override fun useFlag() {
                if (MinefieldUtil.isEstablish) {
                    MinefieldUtil.flagNum--
                    flagNum.text = MinefieldUtil.flagNum.toString()
                }
            }

            // 取消使用小紅旗
            override fun cancelFlag() {
                if (MinefieldUtil.isEstablish) {
                    MinefieldUtil.flagNum++
                    flagNum.text = MinefieldUtil.flagNum.toString()
                }
            }

            // 遊戲勝利
            @SuppressLint("SetTextI18n")
            override fun gameWins() {
                // 停止計時器
                handler.removeCallbacks(mCounter)

                // 顯示勝利彈窗
                val inflate: View = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.dialog_win, null, false)
                val consume = inflate.findViewById<TextView>(R.id.consume)

                // 顯示用時
                consume.text = "用时：" + (if (time1 < 10) "0$time1" else time1.toString()) + ":" + if (time2 < 10) "0$time2" else time2.toString()

                // 再來一局按鈕設定
                val again = inflate.findViewById<TextView>(R.id.again)
                again.setOnClickListener {
                    // 重新開始遊戲
                    time1 = 0
                    time2 = 0
                    MinefieldUtil.reset()
                    rcAdapter.notifyDataSetChanged()
                    boxDialog.dismiss()
                }

                // 顯示彈窗
                boxDialog = BoxDialog(this@MainActivity, inflate, BoxDialog.LocationView.CENTER)
                boxDialog.setCancelable(false) // 是否可以點擊彈窗外關閉彈窗
                boxDialog.setCanceledOnTouchOutside(false) // 是否可以按返回鍵關閉彈窗
                boxDialog.show()
            }
        })
    }

    // dp轉px的工具方法
    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}