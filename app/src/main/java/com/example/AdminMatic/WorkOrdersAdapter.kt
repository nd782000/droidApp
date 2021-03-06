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
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.work_order_list_item.view.*
import java.util.*


class WorkOrdersAdapter(
    private val list: MutableList<WorkOrder>,
    private val context: Context,
    private val cellClickListener: WorkOrderCellClickListener,
    private val customerView: Boolean = false
)

    : RecyclerView.Adapter<WorkOrderViewHolder>(), Filterable {


    var filterList:MutableList<WorkOrder> = emptyList<WorkOrder>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WorkOrderViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: WorkOrderViewHolder, position: Int) {




        val workOrder: WorkOrder = filterList[position]
        holder.bind(workOrder, context)
        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        println("queryText = $queryText")
        //text highlighting for first string
        if (queryText.isNotEmpty() && queryText != "") {

            val startPos1: Int = filterList[position].custName!!.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos1 = startPos1 + queryText.length
            if (startPos1 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].custName)
                val colorStateList = ColorStateList(
                    arrayOf(intArrayOf()),
                    intArrayOf(Color.parseColor("#005100"))
                )
                val textAppearanceSpan =
                    TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null)
                spannable.setSpan(
                    textAppearanceSpan,
                    startPos1,
                    endPos1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.itemView.list_cust_name.text = spannable
            } else {
                holder.itemView.list_cust_name.text = filterList[position].custName!!
            }
        } else {
            holder.itemView.list_cust_name.text = filterList[position].custName!!
        }

        //text highlighting for second string
        if (queryText.isNotEmpty() && queryText != "") {


            val startPos2: Int = filterList[position].title.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].title)
                val colorStateList = ColorStateList(
                    arrayOf(intArrayOf()),
                    intArrayOf(Color.parseColor("#005100"))
                )
                val textAppearanceSpan =
                    TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null)
                spannable.setSpan(
                    textAppearanceSpan,
                    startPos2,
                    endPos2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.itemView.list_title.text = spannable
            } else {
                holder.itemView.list_title.text = filterList[position].title
            }
        } else {
            holder.itemView.list_title.text = filterList[position].title
        }


        val serviceStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_wo_status_icon_image_view)

        val mTitleView:TextView = holder.itemView.findViewById(R.id.list_title)
        mTitleView.text = context.getString(R.string.work_order_title, workOrder.woID, workOrder.title)



        when (workOrder.status) {
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(serviceStatusImageView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(serviceStatusImageView)
            "3"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(serviceStatusImageView)
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onWorkOrderCellClickListener(data, position)
        }


    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }




    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                queryText = charSearch

                if (charSearch.isEmpty()) {
                    //filterList.clear()
                    filterList = list
                } else {

                    val resultList:MutableList<WorkOrder> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.custName!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.title.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

                            println("add row")

                            resultList.add(row)

                            println("resultList.count = ${resultList.count()}")
                        }
                    }
                    filterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList

               println("filterResults = ${filterResults.values}")
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                println("publishResults")

                filterList = results?.values as MutableList<WorkOrder>
                notifyDataSetChanged()
            }

        }
    }

    // Clean all elements of the recycler
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: MutableList<WorkOrder>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class WorkOrderViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.work_order_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mCl: ConstraintLayout? = null
    private var mDateView: TextView? = null
    private var mLockIv: ImageView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mCl = itemView.findViewById(R.id.work_order_list_item)
        mDateView = itemView.findViewById(R.id.list_date)
        mLockIv = itemView.findViewById(R.id.list_wo_lock_icon_iv)
    }

    fun bind(workOrder: WorkOrder, context: Context) {
        mNameView?.text = workOrder.custName!!
        mDateView?.text = workOrder.dateNice
        if (workOrder.locked == "1") {
            mLockIv!!.visibility = View.VISIBLE
            mDateView!!.setTextColor(ContextCompat.getColor(context, R.color.red));
            mCl!!.background = context.getColor(R.color.backgroundHighlight).toDrawable()
        }
    }

}