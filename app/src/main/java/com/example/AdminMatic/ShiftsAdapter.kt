package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.HashMap


class ShiftsAdapter(list: MutableList<Shift>, private val context: Context, ) : RecyclerView.Adapter<ShiftViewHolder>() {

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

    private var listener: LogOut? = null





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

    val dateFormatterDate: DateTimeFormatter = DateTimeFormatter.ofPattern("M/dd (EEE)")
    val dateFormatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

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