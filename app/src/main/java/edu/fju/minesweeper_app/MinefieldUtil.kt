package edu.fju.minesweeper_app

import android.util.Log
import java.util.*

/**
 * 雷區工具類
 */
object MinefieldUtil {

    private const val TAG = "log-trap"

    //是否已經建立了遊戲
    var isEstablish = false

    //剩餘小紅旗數量
    var flagNum = 0

    //已排除的格子數量
    var turnedOnNum = 0

    //是否公開雷區，公開雷區也意味著遊戲結束，不能再點擊
    var isOpen = false

    //建立一張二維數組代表地雷佈置
    //-1：地雷區域
    //0-8：周圍地雷數量
    val gameMap = Array(16) { Array(9) { 0 } }

    //使用者操作圖記錄，與地圖大小相等
    //0：未開採
    //1：已開踩
    //2：標記小紅旗
    //3：問號
    val operationMap = Array(16) { Array(9) { 0 } }

    //特殊座標，此座標不允許建立雷區
    private lateinit var specialCoordinate: MutableList<Int>

    /**
     * 創建雷區，當開採第一個方塊後，才會開始佈置雷區，防止用戶上來就炸，並且用戶點擊處和周圍1格不再佈置雷區
     */
    fun establish(dTemp: Int, kTemp: Int) {

        if (!isEstablish) {
            isEstablish = true
        } else {
            return
        }

        //創建特殊座標
        createSpecialCoordinates(dTemp, kTemp)

        //重置使用者操作圖
        for (d in operationMap.indices) {
            for (k in operationMap[d].indices) {
                operationMap[d][k] = 0
            }
        }

        //剩餘小紅旗數量重置
        flagNum = 30

        val random = Random()
        val temp = mutableSetOf<Int>()

        //生成要埋地雷的下標
        while (true) {
            val nextInt = random.nextInt(144)

            dTemp * 9 + kTemp


            //如果不是使用者點擊處以及周圍1格，才會採取該座標
            if (!specialCoordinate.contains(nextInt)) {
                temp.add(nextInt)
            }

            if (30 == temp.size) {
                break
            }
        }

        //埋下地雷
        for (i in temp) {
            val d = i / 9
            val k = i - 9 * d
            gameMap[d][k] = -1
        }

        //計算周圍地雷數量
        createTrapsNumber()

        //====log
        Log.d(
            TAG,
            "\t\t\t0\t1\t2\t3\t4\t5\t6\t7\t8\t9\t10\t11\t12\t13\t14\t15\t16\t17\t18\t19\t20\t21\t22\t23\t24\t25\t26\t27\t28\t29"
        )
        Log.d(
            TAG,
            "\t\t--------------------------------------------------------------------------------------------------------------------------"
        )
        for ((c, i) in gameMap.withIndex()) {
            var str = "$c->\t\t"
            for (k in i) {
                str += k.toString() + "\t"
            }
            Log.d(TAG, str)
        }

    }

    /**
     * 取得陷阱數量
     */
    private fun createTrapsNumber() {
        for (i in gameMap.indices) { // 遍歷所有行
            for (j in gameMap[i].indices) { // 遍歷所有列
                // 當前座標不是地雷時開始計算
                if (gameMap[i][j] != -1) {
                    var trapNum = 0

                    // 檢查左侧
                    if (j - 1 >= 0 && gameMap[i][j - 1] == -1) {
                        trapNum++
                    }
                    // 檢查上方
                    if (i - 1 >= 0 && gameMap[i - 1][j] == -1) {
                        trapNum++
                    }
                    // 檢查右侧
                    if (j + 1 < gameMap[i].size && gameMap[i][j + 1] == -1) { // 确保不超出列界限
                        trapNum++
                    }
                    // 檢查下方
                    if (i + 1 < gameMap.size && gameMap[i + 1][j] == -1) { // 确保不超出行界限
                        trapNum++
                    }
                    // 檢查左上角
                    if (i - 1 >= 0 && j - 1 >= 0 && gameMap[i - 1][j - 1] == -1) {
                        trapNum++
                    }
                    // 檢查右上角
                    if (i - 1 >= 0 && j + 1 < gameMap[i].size && gameMap[i - 1][j + 1] == -1) {
                        trapNum++
                    }
                    // 檢查右下角
                    if (i + 1 < gameMap.size && j + 1 < gameMap[i].size && gameMap[i + 1][j + 1] == -1) {
                        trapNum++
                    }
                    // 檢查左下角
                    if (i + 1 < gameMap.size && j - 1 >= 0 && gameMap[i + 1][j - 1] == -1) {
                        trapNum++
                    }

                    // 賦值周圍地雷數量
                    gameMap[i][j] = trapNum
                }
            }
        }
    }



    /**
     * 建立特殊座標
     */
    private fun createSpecialCoordinates(dTemp: Int, kTemp: Int) {
        specialCoordinate = mutableListOf()

        //點擊位置
        specialCoordinate.add(dTemp * 9 + kTemp)

        //點擊座標左側
        if (kTemp >= 1) {
            specialCoordinate.add(dTemp * 9 + kTemp - 1)
        }

        //點擊座標上側
        if (dTemp >= 1) {
            specialCoordinate.add((dTemp - 1) * 9 + kTemp)
        }

        //點擊座標右側
        if (kTemp <= 7) {
            specialCoordinate.add(dTemp * 9 + kTemp + 1)
        }

        //點擊座標下側
        if (dTemp <= 14) {
            specialCoordinate.add((dTemp + 1) * 9 + kTemp)
        }

        //點擊座標的左上
        if (dTemp >= 1 && kTemp >= 1) {
            specialCoordinate.add((dTemp - 1) * 9 + kTemp - 1)
        }

        //點擊座標的右上
        if (dTemp >= 1 && kTemp <= 7) {
            specialCoordinate.add((dTemp - 1) * 9 + kTemp + 1)
        }

        //點擊座標的右下
        if (dTemp <= 14 && kTemp <= 7) {
            specialCoordinate.add((dTemp + 1) * 9 + kTemp + 1)
        }

        //點擊座標的左下
        if (dTemp <= 14 && kTemp >= 1) {
            specialCoordinate.add((dTemp + 1) * 9 + kTemp - 1)
        }

        for (i in specialCoordinate) {
            Log.d(TAG, i.toString())
        }

    }

    /**
     * 重置
     */
    fun reset() {
        isEstablish = false

        isOpen = false

        turnedOnNum = 0

        for (d in gameMap.indices) {
            for (k in gameMap[d].indices) {
                gameMap[d][k] = 0
                operationMap[d][k] = 0
            }
        }

        flagNum = 0
    }


}