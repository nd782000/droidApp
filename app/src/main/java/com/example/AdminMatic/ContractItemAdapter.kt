package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class ContractItemAdapter(list: MutableList<ContractItem>, private val context: Context, private val cellClickListener: ContractItemCellClickListener, private val buttonClickListener: AddContractItemButtonListener) : RecyclerView.Adapter<ContractItemViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<ContractItem> = emptyList<ContractItem>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }
/*
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContractItemViewHolder(inflater, parent)
    }

 */


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractItemViewHolder {
        val itemView: View = if (viewType == R.layout.contract_item_list_item) {
            LayoutInflater.from(parent.context).inflate(R.layout.contract_item_list_item, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_button, parent, false)
        }
        return ContractItemViewHolder(itemView)
    }


    //private var listener: LogOut? = null


    override fun onBindViewHolder(holder: ContractItemViewHolder, position: Int) {

        if (position == filterList.size) {
            holder.mButton!!.text = context.getString(R.string.add_item)
            holder.mButton!!.setOnClickListener {
                buttonClickListener.onAddContractItemButtonListener()
            }
        } else {
            val contractItem: ContractItem = filterList[position]
            holder.mNameView!!.text = contractItem.name
            holder.mPriceView!!.text = context.getString(R.string.contract_item_total_price, contractItem.qty, contractItem.price, contractItem.total)
            var descriptionString = ""
            val iterator = contractItem.tasks!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                descriptionString = descriptionString + "- " + item.taskDescription
                if (iterator.hasNext()) {
                    descriptionString += "\n"
                }
            }
            holder.mDescriptionView!!.text = descriptionString
            holder.mImagesView!!.text = context.getString(R.string.contract_item_image_count, contractItem.totalImages)
            holder.itemView.setOnClickListener {
                cellClickListener.onContractItemCellClickListener(contractItem)
            }
        }
    }

    /*
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

     */

    override fun getItemCount(): Int{
        println("getItemCount = ${filterList.size}")
        return filterList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == filterList.size) R.layout.list_item_button else R.layout.contract_item_list_item
    }

}


class ContractItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var mNameView: TextView? = null
    var mPriceView: TextView? = null
    var mDescriptionView: TextView? = null
    var mImagesView: TextView? = null
    var mButton: Button? = null


    init {
        mNameView = view.findViewById(R.id.contract_item_name_tv)
        mPriceView = view.findViewById(R.id.contract_item_price_tv)
        mDescriptionView = view.findViewById(R.id.contract_item_description_tv)
        mImagesView = view.findViewById(R.id.contract_item_images_tv)
        mButton = view.findViewById(R.id.list_item_button)
    }
}

/*
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

 */