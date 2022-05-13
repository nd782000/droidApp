package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import kotlin.contracts.contract


class ContractItemAdapter(list: MutableList<ContractItem>, private val context: Context, private val cellClickListener: ContractItemCellClickListener) : RecyclerView.Adapter<ContractItemViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<ContractItem> = emptyList<ContractItem>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContractItemViewHolder(inflater, parent)
    }

    private var listener: LogOut? = null


    override fun onBindViewHolder(holder: ContractItemViewHolder, position: Int) {


        val contractItem: ContractItem = filterList[position]
        holder.bind(contractItem, context)
        println("queryText = $queryText")
        //text highlighting for first string

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onContractItemCellClickListener(data)
        }


    }

    override fun getItemCount(): Int{
        print("getItemCount = ${filterList.size}")
        return filterList.size
    }

}

class ContractItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.contract_item_list_item, parent, false)) {

    private var mNameView: TextView? = null
    private var mPriceView: TextView? = null
    private var mDescriptionView: TextView? = null
    private var mImagesView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.contract_item_name_tv)
        mPriceView = itemView.findViewById(R.id.contract_item_price_tv)
        mDescriptionView = itemView.findViewById(R.id.contract_item_description_tv)
        mImagesView = itemView.findViewById(R.id.contract_item_images_tv)
    }

    fun bind(contractItem: ContractItem, context: Context) {
        mNameView?.text = contractItem.name
        mPriceView?.text = context.getString(R.string.contract_item_total_price, contractItem.qty, contractItem.price, contractItem.total)

        /*
        contractItem.tasks!!.forEach {
            descriptionString = descriptionString + "- " + it.taskDescription + "\n"
        }
         */
        var descriptionString = ""
        val iterator = contractItem.tasks!!.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            descriptionString = descriptionString + "- " + item.taskDescription
            if (iterator.hasNext()) {
                descriptionString += "\n"
            }
        }

        mDescriptionView?.text = descriptionString

        mImagesView?.text = context.getString(R.string.contract_item_image_count, contractItem.totalImages)
    }

}