package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.TypedArrayUtils.getString
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.customer_list_item.view.*
import kotlinx.android.synthetic.main.service_list_item.view.*
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext


class ServiceAdapter(private val list: MutableList<EquipmentService>, private val context: Context, private val isHistoryMode:Boolean, private val cellClickListener: ServiceCellClickListener)

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
        holder.bind(service, isHistoryMode)
        println("queryText = $queryText")
        //text highlighting for first string

    //if (filterList[position].name != null){
        holder.itemView.list_service_name.text = filterList[position].name
    //}


        var serviceStatusImageView:ImageView = holder.itemView.findViewById<ImageView>(R.id.list_service_status_icon_image_view)

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

class ServiceViewHolder(inflater: LayoutInflater, parent: ViewGroup, ) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.service_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mLeftTxt: TextView? = null
    private var mRightTxt: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_service_name)
        mLeftTxt = itemView.findViewById(R.id.list_service_left_txt)
        mRightTxt = itemView.findViewById(R.id.list_service_right_txt)
    }

    fun bind(service: EquipmentService, isHistoryMode: Boolean) {

        val createDate = LocalDateTime.parse(service.createDate, GlobalVars.dateFormatterPHP)
        val currentDate = LocalDateTime.now()
        val nextDate = createDate.plusDays(service.frequency!!.toLong())

        //Todo: Figure out how to get getString() working here so these can be proper templates. None of the import options alone get it working
        //Todo: Make due text red
        mNameView?.text = service.name
        if (isHistoryMode) {
            mLeftTxt?.text = "By: ${service.completedBy}"
            mRightTxt?.text = "On: ${service.completionDate}"
        }
        else {
            when (service.type) {
                "0" -> { //one time
                    mLeftTxt?.text = "Due: Now"
                    mRightTxt?.text = "One Time Service"
                }
                "1" -> { //date based
                    mLeftTxt?.text = "Due: ${nextDate.format(GlobalVars.dateFormatterShort)}"
                    mRightTxt?.text = "Every ${service.frequency} Days"
                }
                "2" -> { //mile/km based
                    mLeftTxt?.text = "Due: ${service.nextValue}"
                    mRightTxt?.text = "Every ${service.frequency} Mi./Km."
                }
                "3" -> { //engine hour based
                    mLeftTxt?.text = "Due: ${service.nextValue}"
                    mRightTxt?.text = "Every ${service.frequency} Engine Hours"
                }
                "4" -> { //inspection
                    mLeftTxt?.text = "Due: Before Use"
                    mRightTxt?.text = "Inspection"
                }
            }
        }
    }
}