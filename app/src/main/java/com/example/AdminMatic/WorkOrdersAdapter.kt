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

        val listCustName = holder.itemView.findViewById<TextView>(R.id.list_cust_name)
        val listTitle = holder.itemView.findViewById<TextView>(R.id.list_title)

        //println("queryText = $queryText")
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
                listCustName.text = spannable
            } else {
                listCustName.text = filterList[position].custName!!
            }
        } else {
            listCustName.text = filterList[position].custName!!
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
                listTitle.text = spannable
            } else {
                listTitle.text = filterList[position].title
            }
        } else {
            listTitle.text = filterList[position].title
        }


        val serviceStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_wo_status_icon_image_view)


        listTitle.text = context.getString(R.string.work_order_title, workOrder.woID, workOrder.title)



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

        val urgentIv: ImageView = holder.itemView.findViewById(R.id.list_urgent_iv)
        val urgentTv: TextView = holder.itemView.findViewById(R.id.list_urgent_tv)
        if (!workOrder.urgent.isNullOrBlank() && workOrder.urgent!! == "1") {
            urgentIv.visibility = View.VISIBLE
            urgentTv.visibility = View.VISIBLE
        }
        else {
            urgentIv.visibility = View.GONE
            urgentTv.visibility = View.GONE
        }


        val data = filterList[position]
        val unfilteredPosition = list.indexOf(filterList[position])

        holder.itemView.setOnClickListener {
            cellClickListener.onWorkOrderCellClickListener(data, unfilteredPosition)
        }


    }

    override fun getItemCount(): Int{

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }




    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                queryText = charSearch
                var resultList:MutableList<WorkOrder> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list
                } else {
                    for (row in list) {
                        if (row.custName!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.title.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = resultList

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
    private var mSortView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mCl = itemView.findViewById(R.id.work_order_list_item)
        mDateView = itemView.findViewById(R.id.list_date)
        mLockIv = itemView.findViewById(R.id.list_wo_lock_icon_iv)
        mSortView = itemView.findViewById(R.id.sort_text)
    }

    fun bind(workOrder: WorkOrder, context: Context) {
        mNameView?.text = workOrder.custName!!
        mDateView?.text = workOrder.dateNice
        if (workOrder.locked == "1") {
            mLockIv!!.visibility = View.VISIBLE
            mDateView!!.setTextColor(ContextCompat.getColor(context, R.color.red))
            mCl!!.background = context.getColor(R.color.backgroundHighlight).toDrawable()
        }

        /*
        if (workOrder.daySort != null) {
            if (workOrder.daySort != "0") {
                mSortView!!.text = context.getString(R.string.num, workOrder.daySort)
            }
        }

         */

    }

}