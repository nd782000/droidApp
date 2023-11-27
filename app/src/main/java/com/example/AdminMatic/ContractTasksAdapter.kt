package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso


class ContractTasksAdapter(list: MutableList<ContractTask>, private val context: Context, private val cellClickListener: ContractTaskCellClickListener) : RecyclerView.Adapter<ContractTaskViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<ContractTask> = emptyList<ContractTask>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractTaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContractTaskViewHolder(inflater, parent)
    }
    //private var listener: LogOut? = null


    override fun onBindViewHolder(holder: ContractTaskViewHolder, position: Int) {


        val contractTask: ContractTask = filterList[position]
        holder.bind(contractTask, context)
        //println("queryText = $queryText")
        //text highlighting for first string

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onContractTaskCellClickListener(data)
        }

        if (data.images != null){
            if (data.images!!.isNotEmpty()){
                val taskImageView:ImageView = holder.itemView.findViewById(R.id.contract_task_item_image_view)


                Picasso.with(context)
                    .load(GlobalVars.thumbBase + data.images!![0].fileName)
                    .placeholder(R.drawable.ic_images) //optional
                    //.resize(imgWidth, imgHeight)         //optional
                    //.centerCrop()                        //optional
                    .into(taskImageView)                       //Your image view object.

                if (data.images!!.count() > 1) {
                    val countTv:TextView = holder.itemView.findViewById(R.id.image_count_tv)
                    countTv.text = "+${data.images!!.count()-1}"
                }
            }
        }


    }

    override fun getItemCount(): Int{
        //print("getItemCount = ${filterList.size}")
        return filterList.size
    }

}

class ContractTaskViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.contract_task_list_item, parent, false)) {

    private var mImageView: ImageView? = null
    private var mNameView: TextView? = null
    private var mImagesCountView: TextView? = null


    init {
        mImageView = itemView.findViewById(R.id.contract_task_item_image_view)
        mNameView = itemView.findViewById(R.id.contract_task_item_name)
        mImagesCountView = itemView.findViewById(R.id.contract_task_item_image_count)
    }

    fun bind(contractTask: ContractTask, context: Context) {
        mNameView!!.text = contractTask.taskDescription

        if (contractTask.images == null || contractTask.images!!.isEmpty()) {
            mImagesCountView!!.text = context.getString(R.string.no_images)
        }
        else {
            mImagesCountView!!.text = context.getString(R.string.x_images, contractTask.images!!.size)
        }
    }

}