package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.util.*


class SearchItemsAdapter(private val list: MutableList<SearchItem>, private val cellClickListener: SearchItemCellClickListener)

    : RecyclerView.Adapter<SearchItemViewHolder>(), Filterable {


    var filterList:MutableList<SearchItem> = emptyList<SearchItem>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SearchItemViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {

        val item: SearchItem = filterList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            cellClickListener.onSearchItemCellClickListener(item)
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
                var resultList:MutableList<SearchItem> = mutableListOf()

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

                filterList = results?.values as MutableList<SearchItem>
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





class SearchItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.search_list_item, parent, false)) {
    private var mNameView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
    }

    fun bind(item: SearchItem) {
        mNameView?.text = item.name
    }

}