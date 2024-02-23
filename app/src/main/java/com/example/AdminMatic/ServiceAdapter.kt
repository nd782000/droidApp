package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import java.time.LocalDate


class ServiceAdapter(list: MutableList<EquipmentService>, private val context: Context, private val isHistoryMode:Boolean, private val usageType:String, private val equipmentUsage:String, private val cellClickListener: ServiceCellClickListener)

    : RecyclerView.Adapter<ServiceViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<EquipmentService> = emptyList<EquipmentService>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ServiceViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {

        val service: EquipmentService = filterList[position]
        holder.bind(service, context, isHistoryMode, usageType, equipmentUsage)
        //println("queryText = $queryText")
        //text highlighting for first string

    //if (filterList[position].name != null){
        holder.itemView.findViewById<TextView>(R.id.list_service_name).text = filterList[position].name
    //}


        val serviceStatusImageView:ImageView = holder.itemView.findViewById(R.id.list_service_status_icon_image_view)

        when (service.status) {
            "0"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(serviceStatusImageView)
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(serviceStatusImageView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(serviceStatusImageView)
            "3"-> Picasso.with(context)
                .load(R.drawable.ic_canceled)
                .into(serviceStatusImageView)
            "4"-> Picasso.with(context)
                .load(R.drawable.ic_skipped)
                .into(serviceStatusImageView)
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onServiceCellClickListener(data)
        }







    }

    override fun getItemCount(): Int{

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class ServiceViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.service_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mLeftTxt: TextView? = null
    private var mRightTxt: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_service_name)
        mLeftTxt = itemView.findViewById(R.id.list_service_left_txt)
        mRightTxt = itemView.findViewById(R.id.list_service_right_txt)
    }

    fun bind(service: EquipmentService, context:Context, isHistoryMode: Boolean, usageType:String, equipmentUsage: String) {

        //val createDate = LocalDateTime.parse(service.createDate, GlobalVars.dateFormatterPHP)
        //val currentDate = LocalDateTime.now()
        //val nextDate = createDate.plusDays(service.frequency!!.toLong())

        mNameView?.text = service.name
        if (isHistoryMode) {
            mLeftTxt?.text = context.getString(R.string.service_by_x, service.completedByName)
            mRightTxt?.text = context.getString(R.string.service_on_x, service.completionDate)
        }
        else {
            when (service.type) {
                "0" -> { //one time
                    mLeftTxt?.text = context.getString(R.string.service_due_now)
                    mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                    mRightTxt?.text = context.getString(R.string.service_one_time)
                }
                "1" -> { //date based
                    val targetDate = LocalDate.parse(service.targetDate, GlobalVars.dateFormatterYYYYMMDD)
                    mLeftTxt?.text = context.getString(R.string.service_due_x, targetDate.format(GlobalVars.dateFormatterShort))
                    if (targetDate <= LocalDate.now()) {
                        mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                    }
                    else if (targetDate <= LocalDate.now().plusDays(service.warningOffset!!.toLong())) {
                        mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.orange))
                    }
                    else {
                        mLeftTxt?.setTextColor(mRightTxt!!.currentTextColor)
                    }
                    mRightTxt?.text = context.getString(R.string.service_every_x_days, service.frequency)
                }
                "2" -> { //usage based

                    when (usageType) {
                        "hours" -> {
                            mLeftTxt?.text = context.getString(R.string.service_due, service.nextValue, context.getString(R.string.hours))
                            mRightTxt?.text = context.getString(R.string.service_every_x_engine_hours, service.frequency)
                        }
                        "km" -> {
                            mLeftTxt?.text = context.getString(R.string.service_due, service.nextValue, context.getString(R.string.kilometers))
                            mRightTxt?.text = context.getString(R.string.service_every_x_km, service.frequency)
                        }
                        else -> {
                            mLeftTxt?.text = context.getString(R.string.service_due, service.nextValue, context.getString(R.string.miles))
                            mRightTxt?.text = context.getString(R.string.service_every_x_miles, service.frequency)
                        }
                    }

                    if (service.nextValue!!.toInt() <= equipmentUsage.toInt()) {
                        mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                    }
                    else if (service.nextValue!!.toInt() - service.warningOffset!!.toInt() <= equipmentUsage.toInt()) {
                        mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.orange))
                    }
                    else {
                        mLeftTxt?.setTextColor(mRightTxt!!.currentTextColor)
                    }

                }
                "4" -> { //inspection
                    val targetDate = LocalDate.parse(service.targetDate, GlobalVars.dateFormatterYYYYMMDD)
                    mLeftTxt?.text = context.getString(R.string.service_due_x, targetDate.format(GlobalVars.dateFormatterShort))
                    if (targetDate <= LocalDate.now()) {
                        mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                    }
                    else if (targetDate <= LocalDate.now().plusDays(service.warningOffset!!.toLong())) {
                        mLeftTxt?.setTextColor(ContextCompat.getColor(myView.context, R.color.orange))
                    }
                    else {
                        mLeftTxt?.setTextColor(mRightTxt!!.currentTextColor)
                    }
                    mRightTxt?.text = context.getString(R.string.service_type_inspection)
                }
            }
        }
    }
}