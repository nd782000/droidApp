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
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.customer_list_item.view.*
import kotlinx.android.synthetic.main.service_list_item.view.*
import kotlinx.android.synthetic.main.wo_item_list_item.view.*
import java.util.*



class ServiceAdapter(private val list: MutableList<EquipmentService>, private val context: Context,private val cellClickListener: ServiceCellClickListener)

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
        holder.bind(service)
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

class ServiceViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.service_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mByView: TextView? = null
    private var mDateView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_service_name)
        mByView = itemView.findViewById(R.id.list_service_by)
        mDateView = itemView.findViewById(R.id.list_service_on)
    }

    fun bind(service: EquipmentService) {
        mNameView?.text = service.name
        mByView?.text = "By: ${service.addedBy}"
        mDateView?.text = service.frequency
    }



}