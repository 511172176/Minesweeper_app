package edu.fju.minesweeper_app

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RcAdapter(private var context: Context) : RecyclerView.Adapter<RcAdapter.Holder>() {

    //當遊戲失敗後，失敗處的座標，此處要著重顯示
    private var overPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        return Holder(LayoutInflater.from(context).inflate(R.layout.item_lattice, parent, false))
    }

    override fun getItemCount(): Int {
        return 144
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        val d = position / 9
        val k = position - 9 * d

        //-1：地雷區域
        //0-8：周圍地雷數量
        val indexGame = MinefieldUtil.gameMap[d][k]

        //0：未開採
        //1：已開踩
        //2：標記小紅旗
        //3：問號
        val indexOperation = MinefieldUtil.operationMap[d][k]

        //判斷是否公開雷區
        if (MinefieldUtil.isOpen) {
            //公開雷區，遊戲結束

            when (indexOperation) {
                0, 3 -> {
                    if (indexGame == -1) {
                        holder.itemText.setBackgroundResource(R.mipmap.icon_trap_open)
                        holder.itemText.text = ""
                    } else {
                        holder.itemText.setBackgroundResource(R.mipmap.icon_lattice)
                        holder.itemText.text = ""
                    }
                }
                1 -> {
                    holder.itemText.setBackgroundResource(R.mipmap.icon_empty)
                    holder.itemText.text = indexGame.toString()

                    if (0 != indexGame) {
                        holder.itemText.text = indexGame.toString()
                    } else {
                        holder.itemText.text = ""
                    }

                    holder.itemText.setTextColor(
                        when (indexGame) {
                            1 -> ContextCompat.getColor(context, R.color.index1)
                            2 -> ContextCompat.getColor(context, R.color.index2)
                            3 -> ContextCompat.getColor(context, R.color.index3)
                            4 -> ContextCompat.getColor(context, R.color.index4)
                            5 -> ContextCompat.getColor(context, R.color.index5)
                            6 -> ContextCompat.getColor(context, R.color.index6)
                            7 -> ContextCompat.getColor(context, R.color.index7)
                            else -> ContextCompat.getColor(context, R.color.index8)
                        }
                    )
                }
                2 -> {
                    if (indexGame == -1) {
                        holder.itemText.setBackgroundResource(R.mipmap.icon_flag)
                        holder.itemText.text = ""
                    } else {
                        holder.itemText.setBackgroundResource(R.mipmap.icon_flag_error)
                        holder.itemText.text = ""
                    }
                }
            }

            if (indexOperation == 0 && -1 == indexGame) {
                holder.itemText.setBackgroundResource(R.mipmap.icon_trap_open)
                holder.itemText.text = ""
            }

            if (overPosition == position) {
                holder.itemText.setBackgroundResource(R.mipmap.icon_trap)
                holder.itemText.text = ""
            }

        } else {
            //隱藏雷區
            when (indexOperation) {
                0 -> {
                    holder.itemText.setBackgroundResource(R.mipmap.icon_lattice)
                    holder.itemText.text = ""
                    holder.itemText.setOnClickListener {
                        //開採區域
                        if (-1 == indexGame) {
                            //踩到地雷，遊戲結束
                            MinefieldUtil.isOpen = true
                            overPosition = position
                            notifyDataSetChanged()
                            dataCallBack?.gameOver()
                        } else {

                            //dataCallBack?.gameWins()

                            //回調遊戲開始
                            if (!MinefieldUtil.isEstablish) {
                                dataCallBack?.gameStart()
                            }

                            //本次點擊排除一個格子
                            MinefieldUtil.turnedOnNum++

                            //創建地雷，本局遊戲只會執行一次，內部已封裝好方法
                            MinefieldUtil.establish(d, k)

                            //遞歸開採其他模組
                            exploitation(d, k)


                            //判斷是否已經排除完地雷
                            if (381 == MinefieldUtil.turnedOnNum) {
                                dataCallBack?.gameWins()
                            }

                            //刷新
                            notifyDataSetChanged()

                        }
                    }
                    holder.itemText.setOnLongClickListener {
                        //在該區域插上小紅旗

                        //判斷小紅旗是否用完了
                        if (MinefieldUtil.flagNum <= 0) {
                            return@setOnLongClickListener true
                        }

                        MinefieldUtil.operationMap[d][k] = 2

                        //回調使用了小紅旗
                        dataCallBack?.useFlag()

                        notifyDataSetChanged()
                        return@setOnLongClickListener true
                    }
                }

                1 -> {
                    if (0 == indexGame) {
                        //已開採周圍沒有地雷的方塊
                        holder.itemText.setBackgroundResource(R.mipmap.icon_empty)
                        holder.itemText.text = ""
                    } else {
                        //已開採周圍有地雷的方塊
                        holder.itemText.setBackgroundResource(R.mipmap.icon_empty)
                        holder.itemText.text = indexGame.toString()
                        holder.itemText.setTextColor(
                            when (indexGame) {
                                1 -> ContextCompat.getColor(context, R.color.index1)
                                2 -> ContextCompat.getColor(context, R.color.index2)
                                3 -> ContextCompat.getColor(context, R.color.index3)
                                4 -> ContextCompat.getColor(context, R.color.index4)
                                5 -> ContextCompat.getColor(context, R.color.index5)
                                6 -> ContextCompat.getColor(context, R.color.index6)
                                7 -> ContextCompat.getColor(context, R.color.index7)
                                else -> ContextCompat.getColor(context, R.color.index8)
                            }
                        )
                    }
                }

                2 -> {
                    holder.itemText.setBackgroundResource(R.mipmap.icon_flag)
                    holder.itemText.text = ""
                    holder.itemText.setOnLongClickListener {

                        MinefieldUtil.operationMap[d][k] = 3

                        dataCallBack?.cancelFlag()

                        notifyDataSetChanged()



                        return@setOnLongClickListener true
                    }
                }

                3 -> {
                    holder.itemText.setBackgroundResource(R.mipmap.icon_doubt)
                    holder.itemText.text = ""
                    holder.itemText.setOnLongClickListener {
                        MinefieldUtil.operationMap[d][k] = 0
                        notifyDataSetChanged()
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.itemText)
    }


    //==============================================================================================
    /**
     * 開採領域，遞迴調用
     */
    private fun exploitation(d: Int, k: Int) {
        if (MinefieldUtil.gameMap[d][k] >= 0) {
            MinefieldUtil.operationMap[d][k] = 1


            if (0 != MinefieldUtil.gameMap[d][k]) {
                return
            }

            //判斷左側是否開採
            if (k >= 1 && MinefieldUtil.gameMap[d][k - 1] >= 0 && MinefieldUtil.operationMap[d][k - 1] != 1) {
                MinefieldUtil.operationMap[d][k - 1] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d][k - 1] == 0) {
                    exploitation(d, k - 1)
                }
            }

            //判斷上側是否開採
            if (d >= 1 && MinefieldUtil.gameMap[d - 1][k] >= 0 && MinefieldUtil.operationMap[d - 1][k] != 1) {
                MinefieldUtil.operationMap[d - 1][k] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d - 1][k] == 0) {
                    exploitation(d - 1, k)
                }
            }

            //判斷右側是否開採
            if (k <= 7 && MinefieldUtil.gameMap[d][k + 1] >= 0 && MinefieldUtil.operationMap[d][k + 1] != 1) {
                MinefieldUtil.operationMap[d][k + 1] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d][k + 1] == 0) {
                    exploitation(d, k + 1)
                }
            }

            //判斷下側是否開採
            if (d <= 14 && MinefieldUtil.gameMap[d + 1][k] >= 0 && MinefieldUtil.operationMap[d + 1][k] != 1) {
                MinefieldUtil.operationMap[d + 1][k] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d + 1][k] == 0) {
                    exploitation(d + 1, k)
                }
            }

            //判斷左上是否開採
            if (d >= 1 && k >= 1 && MinefieldUtil.gameMap[d - 1][k - 1] >= 0 && MinefieldUtil.operationMap[d - 1][k - 1] != 1) {
                MinefieldUtil.operationMap[d - 1][k - 1] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d - 1][k - 1] == 0) {
                    exploitation(d - 1, k - 1)
                }
            }

            //判斷右上是否開採
            if (d >= 1 && k <= 7 && MinefieldUtil.gameMap[d - 1][k + 1] >= 0 && MinefieldUtil.operationMap[d - 1][k + 1] != 1) {
                MinefieldUtil.operationMap[d - 1][k + 1] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d - 1][k + 1] == 0) {
                    exploitation(d - 1, k + 1)
                }
            }

            //判斷右下是否開採
            if (d <= 14 && k <= 7 && MinefieldUtil.gameMap[d + 1][k + 1] >= 0 && MinefieldUtil.operationMap[d + 1][k + 1] != 1) {
                MinefieldUtil.operationMap[d + 1][k + 1] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d + 1][k + 1] == 0) {
                    exploitation(d + 1, k + 1)
                }
            }

            //判斷左下是否開採
            if (d <= 14 && k >= 1 && MinefieldUtil.gameMap[d + 1][k - 1] >= 0 && MinefieldUtil.operationMap[d + 1][k - 1] != 1) {
                MinefieldUtil.operationMap[d + 1][k - 1] = 1
                MinefieldUtil.turnedOnNum++
                if (MinefieldUtil.gameMap[d + 1][k - 1] == 0) {
                    exploitation(d + 1, k - 1)
                }
            }

        }
    }


    //==============================================================================================
    //相關事件回調
    private var dataCallBack: DataCallBack? = null

    fun setDataCallBack(dataCallBack: DataCallBack) {
        this.dataCallBack = dataCallBack
    }

    interface DataCallBack {

        fun gameStart()

        //遊戲結束
        fun gameOver()

        //使用小紅旗
        fun useFlag()

        //取消使用小紅旗
        fun cancelFlag()

        //遊戲勝利
        fun gameWins()
    }


}