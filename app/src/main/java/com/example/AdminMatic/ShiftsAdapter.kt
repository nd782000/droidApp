package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ShiftsAdapter(list: MutableList<Shift>, private val context: Context) : RecyclerView.Adapter<ShiftViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Shift> = emptyList<Shift>().toMutableList()
    lateinit  var globalVars:GlobalVars

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ShiftViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null


    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {


        globalVars = GlobalVars()

        val shift: Shift = filterList[position]
        holder.bind(shift)
        println("queryText = $queryText")

    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class ShiftViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.shift_list_item, parent, false)) {


    private var mDateView: TextView? = null
    private var mStartView: TextView? = null
    private var mStopView: TextView? = null
    private var mQtyView: TextView? = null

    private val dateFormatterDate: DateTimeFormatter = DateTimeFormatter.ofPattern("M/dd (EEE)")
    private val dateFormatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    init {
        mDateView = itemView.findViewById(R.id.list_shift_date_tv)
        mStartView = itemView.findViewById(R.id.list_shift_start_tv)
        mStopView = itemView.findViewById(R.id.list_shift_stop_tv)
        mQtyView = itemView.findViewById(R.id.list_shift_qty_tv)
    }

    fun bind(shift: Shift) {

        val startTime : LocalDateTime = LocalDateTime.parse(shift.startTime, GlobalVars.dateFormatterPHP)
        mDateView?.text = startTime.format(dateFormatterDate)


        if (shift.ID == "0") { // empty slot
            mStartView?.text = "-----"
            mStopView?.text = "-----"
            mQtyView?.text = "-----"
        }
        else {
            mStartView?.text = startTime.format(dateFormatterTime)
            val endTime : LocalDateTime = LocalDateTime.parse(shift.endTime, GlobalVars.dateFormatterPHP)
            mStopView?.text = endTime.format(dateFormatterTime)
            mQtyView?.text = shift.shiftQty
        }


    }

}