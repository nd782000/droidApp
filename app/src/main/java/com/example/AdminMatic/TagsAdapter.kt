package com.example.AdminMatic

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import java.util.*


class TagsAdapter(private val list: MutableList<Tag>, private val cellClickListener: TagCellClickListener): RecyclerView.Adapter<TagViewHolder>(), Filterable {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Tag> = emptyList<Tag>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TagViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {

        val tag: Tag = filterList[position]
        holder.bind(tag)
        //println("queryText = $queryText")
        //text highlighting for first string

        val listName = holder.itemView.findViewById<TextView>(R.id.list_name)


        if (queryText.isNotEmpty() && queryText != "") {
            val startPos1: Int = filterList[position].name.lowercase(Locale.getDefault())
                .indexOf(queryText.lowercase(Locale.getDefault()))
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
                listName.text = spannable
            } else {
                listName.text = filterList[position].name
            }
        } else {
            listName.text = filterList[position].name
        }

        holder.itemView.setOnClickListener {
            cellClickListener.onTagCellClickListener(tag)
        }

    }

    override fun getItemCount(): Int{

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }


    /*
    fun changeData(data: List<Customer>) {
        // checking the size of the list before
        println("filterList.size before: " + filterList.size)
        filterList = data.toMutableList()

        // checking the size of the list after
        println("filterList.size after: " + filterList.size)

        notifyDataSetChanged()
    }
*/




    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                queryText = charSearch
                var resultList:MutableList<Tag> = mutableListOf()

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

                //println("filterResults = ${filterResults.values}")
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                println("publishResults")

                filterList = results?.values as MutableList<Tag>
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
    fun addAll(list: MutableList<Customer>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class TagViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.tag_list_item, parent, false)) {
    private var mNameView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
    }

    fun bind(tag: Tag) {
        mNameView?.text = tag.name
    }



}