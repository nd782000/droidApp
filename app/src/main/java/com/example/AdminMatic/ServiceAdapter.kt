package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.service_list_item.view.*
import java.time.LocalDateTime


class ServiceAdapter(list: MutableList<EquipmentService>, private val context: Context, private val isHistoryMode:Boolean, private val cellClickListener: ServiceCellClickListener)

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
        holder.bind(service, isHistoryMode, context)
        println("queryText = $queryText")
        //text highlighting for first string

    //if (filterList[position].name != null){
        holder.itemView.list_service_name.text = filterList[position].name
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
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onServiceCellClickListener(data)
        }







    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
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

    fun bind(service: EquipmentService, isHistoryMode: Boolean, context: Context) {

        val createDate = LocalDateTime.parse(service.createDate, GlobalVars.dateFormatterPHP)
        val currentDate = LocalDateTime.now()
        val nextDate = createDate.plusDays(service.frequency!!.toLong())

        //Todo: Make due text red
        mNameView?.text = service.name
        if (isHistoryMode) {
            mLeftTxt?.text = context.getString(R.string.service_completed_by, service.completedBy) //"By: ${service.completedBy}"
            mRightTxt?.text = context.getString(R.string.service_completed_on, service.completionDate) //"By: ${service.completedBy}"
        }
        else {
            when (service.type) {
                "0" -> { //one time
                    mLeftTxt?.text = context.getString(R.string.service_due_now)
                    mRightTxt?.text = context.getString(R.string.service_one_time)//"One Time Service"
                }
                "1" -> { //date based
                    mLeftTxt?.text = context.getString(R.string.service_due_x, nextDate.format(GlobalVars.dateFormatterShort))
                    mRightTxt?.text = context.getString(R.string.service_every_x_days, service.frequency)
                }
                "2" -> { //mile/km based
                    mLeftTxt?.text = context.getString(R.string.service_due_x, service.nextValue)
                    mRightTxt?.text = context.getString(R.string.service_every_x_mi_km, service.frequency)
                }
                "3" -> { //engine hour based
                    mLeftTxt?.text = context.getString(R.string.service_due_x, service.nextValue)
                    mRightTxt?.text = context.getString(R.string.service_every_x_engine_hours, service.frequency)
                }
                "4" -> { //inspection
                    mLeftTxt?.text = context.getString(R.string.service_due_before_use)
                    mRightTxt?.text = context.getString(R.string.service_type_inspection)
                }
            }
        }
    }
}