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
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.lead_list_item.view.*
import java.util.*


class LeadsAdapter(private val list: MutableList<Lead>, private val context: Context, private val cellClickListener: LeadCellClickListener, private val customerView: Boolean = false) : RecyclerView.Adapter<LeadViewHolder>(), Filterable {


    var filterList:MutableList<Lead> = emptyList<Lead>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LeadViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {




        val lead: Lead = filterList[position]
        holder.bind(lead)

        println("queryText = $queryText")
        //text highlighting for first string

        val leadStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_lead_status_icon_iv)

        when (lead.statusID) {
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(leadStatusImageView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(leadStatusImageView)
        }

        val daysAged:String = when (filterList[position].daysAged) {
            "0" -> {
                "Today"
            }
            "1" -> {
                "Yesterday"
            }
            else -> {
                filterList[position].daysAged + " Days"
            }
        }

        if (queryText.isNotEmpty() && queryText != "") {

            val startPos1: Int = (filterList[position].custName!! + " - " + filterList[position].description!!).lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos1 = startPos1 + queryText.length
            if (startPos1 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].custName!! + " - " + filterList[position].description!!)
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
                holder.itemView.list_lead_name_tv.text = spannable
            } else {
                if(!customerView){
                    holder.itemView.list_lead_name_tv.text = context.getString(R.string.name_number, filterList[position].custName, filterList[position].ID)
                    holder.itemView.list_lead_description_tv.text = filterList[position].description!!
                    holder.itemView.list_lead_date_tv.text = daysAged
                }else{
                    holder.itemView.list_lead_name_tv.text = filterList[position].description!!
                }

            }
        } else {
            if(!customerView){
                holder.itemView.list_lead_name_tv.text = context.getString(R.string.name_number, filterList[position].custName, filterList[position].ID)
                holder.itemView.list_lead_description_tv.text = filterList[position].description!!
                holder.itemView.list_lead_date_tv.text = daysAged
            }else{
                holder.itemView.list_lead_name_tv.text = filterList[position].description!!
            }
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onLeadCellClickListener(data)
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

                    val resultList:MutableList<Lead> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if ((row.custName!! + " - " + row.description!!).lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

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

                filterList = results?.values as MutableList<Lead>
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
    fun addAll(list: MutableList<Lead>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class LeadViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.lead_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_lead_name_tv)

    }

    fun bind(lead: Lead) {
        mNameView?.text = lead.custName!!
    }



}