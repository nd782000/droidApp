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


class TemplatesAdapter(private val list: Array<Template>, private val cellClickListener: TemplateCellClickListener): RecyclerView.Adapter<TemplateViewHolder>(), Filterable {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Template> = emptyList<Template>().toMutableList()


    var queryText = ""

    init {

        filterList = list.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TemplateViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {

        val template: Template = filterList[position]
        holder.bind(template)
        //println("queryText = $queryText")
        //text highlighting for first string

        val listName = holder.itemView.findViewById<TextView>(R.id.list_name)

        //text highlighting for second string
        if (queryText.isNotEmpty() && queryText != "") {


            val startPos2: Int = filterList[position].name.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault())
            )
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].name)
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
                listName.text = spannable
            } else {
                listName.text = filterList[position].name
            }
        } else {
            listName.text = filterList[position].name
        }

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onTemplateCellClickListener(data)
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
                var resultList:MutableList<Template> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list.toMutableList()
                } else {

                    for (row in list) {

                        //if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.mainAddr!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
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

                filterList = results?.values as MutableList<Template>
                notifyDataSetChanged()
            }

        }
    }

    // Clean all elements of the recycler
    /*
    fun clear() {
        //list.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: MutableList<Customer>) {
        list.addAll(list)
        notifyDataSetChanged()
    }

     */





}

class TemplateViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.template_list_item, parent, false)) {
    private var mNameView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
    }

    fun bind(template: Template) {
        mNameView?.text = template.name
    }



}