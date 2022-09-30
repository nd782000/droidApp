package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PayrollAdapter(list: MutableList<Payroll>, private val context: Context) : RecyclerView.Adapter<PayrollViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Payroll> = emptyList<Payroll>().toMutableList()
    lateinit  var globalVars:GlobalVars
    

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayrollViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PayrollViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null


    override fun onBindViewHolder(holder: PayrollViewHolder, position: Int) {


        globalVars = GlobalVars()

        val payroll: Payroll = filterList[position]
        holder.bind(context, payroll)

    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class PayrollViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.payroll_list_item, parent, false)) {


    private var mDateView: TextView? = null
    private var mStartView: TextView? = null
    private var mStopView: TextView? = null
    private var mQtyView: TextView? = null
    private var mLeftView: TextView? = null
    private var mRightView: TextView? = null

    //private val dateFormatterDate: DateTimeFormatter = DateTimeFormatter.ofPattern("M/dd (EEE)")
    //private val dateFormatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    init {
        mDateView = itemView.findViewById(R.id.date_tv)
        mStartView = itemView.findViewById(R.id.start_tv)
        mStopView = itemView.findViewById(R.id.stop_tv)
        mQtyView = itemView.findViewById(R.id.qty_tv)
        mLeftView = itemView.findViewById(R.id.left_tv)
        mRightView = itemView.findViewById(R.id.right_tv)
    }

    fun bind(context: Context, payroll: Payroll) {

        var valid = true

        mDateView?.text = context.getString(R.string.payroll_summary_date,payroll.dayShort, payroll.shortDate)

        if (payroll.startTimeShort.isNullOrBlank()) {
            valid = false
            mStartView?.text = "---"
        }
        else {
            mStartView?.text = payroll.startTimeShort
        }

        if (payroll.stopTimeShort.isNullOrBlank()) {
            valid = false
            mStopView?.text = "---"
        }
        else {
            mStopView?.text = payroll.stopTimeShort
        }


        if (payroll.total.isNullOrBlank()) {
            mQtyView?.text = "0.00"
        }
        else {
            mQtyView?.text = payroll.total
        }

        mLeftView?.text = payroll.dayTypeName

        if (valid) {
            if (payroll.verified == "1") {
                mRightView?.text = context.getString(R.string.payroll_summary_verified)
            }
            else {
                mRightView?.text = context.getString(R.string.payroll_summary_unverified)
            }
        }

        /*
        if (payroll.ID == "0") { // empty slot
            mStartView?.text = "-----"
            mStopView?.text = "-----"
            mQtyView?.text = "-----"
        }
        else {
            mStartView?.text = startTime.format(dateFormatterTime)
            val endTime : LocalDateTime = LocalDateTime.parse(payroll.stopTimeShort, GlobalVars.dateFormatterPHP)
            mStopView?.text = endTime.format(dateFormatterTime)
            mQtyView?.text = payroll.payrollQty
        }

         */




    }

}