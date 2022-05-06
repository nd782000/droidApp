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
import kotlinx.android.synthetic.main.contract_list_item.view.*
import kotlinx.android.synthetic.main.lead_list_item.view.*
import java.util.*


class ContractsAdapter(private val list: MutableList<Contract>, private val context: Context, private val cellClickListener: ContractCellClickListener, private val customerView: Boolean = false)

    : RecyclerView.Adapter<ContractViewHolder>(), Filterable {


    var filterList:MutableList<Contract> = emptyList<Contract>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContractViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {

        val contract: Contract = filterList[position]
        holder.bind(contract)

        println("queryText = $queryText")
        //text highlighting for first string

        val contractStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_contract_status_icon_iv)

        when (contract.status) {
            "0"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(contractStatusImageView)
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(contractStatusImageView)
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

            val startPos1: Int = (filterList[position].custName!! + " - " + filterList[position].title!!).lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos1 = startPos1 + queryText.length
            if (startPos1 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].custName!! + " - " + filterList[position].title!!)
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
                    //Todo: figure out how to pull strings from Resources within an adapter
                    holder.itemView.list_contract_name_tv.text = filterList[position].custName!! + " #" + filterList[position].ID
                    holder.itemView.list_contract_description_tv.text = filterList[position].title!!
                    holder.itemView.list_contract_date_tv.text = daysAged
                }else{
                    holder.itemView.list_contract_name_tv.text = filterList[position].title!!
                }

            }
        } else {
            if(!customerView){
                holder.itemView.list_contract_name_tv.text = filterList[position].custName!! + " #" + filterList[position].ID
                holder.itemView.list_contract_description_tv.text = filterList[position].title!!
                holder.itemView.list_contract_date_tv.text = daysAged
            }else{
                holder.itemView.list_contract_name_tv.text = filterList[position].title!!
            }
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onContractCellClickListener(data)
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

                    val resultList:MutableList<Contract> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.custName!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(
                                Locale.ROOT
                            ))) {

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

                filterList = results?.values as MutableList<Contract>
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
    fun addAll(list: MutableList<Contract>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class ContractViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.contract_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)

    }

    fun bind(contract: Contract) {
        //mNameView?.text = contract.custName!!

    }



}