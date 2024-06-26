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
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.util.*


class ItemsAdapter(private val list: MutableList<Item>, private val context: Context, private val cellClickListener: ItemCellClickListener, private val minimalView: Boolean)

    : RecyclerView.Adapter<ItemViewHolder>(), Filterable {


    var filterList:MutableList<Item> = emptyList<Item>().toMutableList()
    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item: Item = filterList[position]
        holder.bind(item, context, minimalView)

        val listItemName = holder.itemView.findViewById<TextView>(R.id.list_item_name_tv)

        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        //text highlighting for first string
        if (queryText.isNotEmpty() && queryText != "") {

            val startPos1: Int = filterList[position].name.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos1 = startPos1 + queryText.length
            if (startPos1 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].name)
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
                listItemName.text = spannable
            } else {
                listItemName.text = filterList[position].name
            }
        } else {
            listItemName.text = filterList[position].name
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onItemCellClickListener(data)
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
                var resultList:MutableList<Item> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list
                } else {
                    for (row in list) {
                        if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
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

                filterList = results?.values as MutableList<Item>
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
    fun addAll(list: MutableList<Item>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class ItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mTypeView: TextView? = null
    private var mPriceView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_item_name_tv)
        mTypeView = itemView.findViewById(R.id.list_item_type_tv)
        mPriceView = itemView.findViewById(R.id.list_item_price_tv)
    }

    fun bind(item: Item, context:Context, minimalView: Boolean) {
        mNameView?.text = item.name
        mTypeView?.text = item.type

        if (minimalView) {
            mTypeView?.visibility = View.GONE
            mPriceView?.visibility = View.GONE
        }
        else {
            if (item.unit != null) {
                mPriceView?.text = context.getString(R.string.item_price_each, item.price, item.unitName)
            } else {
                mPriceView?.text = "---"
            }

            if (GlobalVars.permissions!!.itemsMoney == "0") {
                mPriceView?.visibility = View.GONE
            }

            if (item.typeName!= null) {
                mTypeView?.text = item.typeName
            }
        }

    }



}