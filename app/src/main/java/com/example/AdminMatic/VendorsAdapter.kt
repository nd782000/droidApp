package com.example.AdminMatic

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import kotlinx.android.synthetic.main.vendor_list_item.view.*
import java.util.*


class VendorsAdapter(
    private val list: MutableList<Vendor>,
    private val cellClickListener: VendorCellClickListener
)

    : RecyclerView.Adapter<VendorViewHolder>(), Filterable {


    var filterList:MutableList<Vendor> = emptyList<Vendor>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return VendorViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {




        val vendor: Vendor = filterList[position]
        holder.bind(vendor)
        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        println("queryText = $queryText")
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
                holder.itemView.list_name.text = spannable
            } else {
                holder.itemView.list_name.text = filterList[position].name
            }
        } else {
            holder.itemView.list_name.text = filterList[position].name
        }

        //text highlighting for second string
        if (queryText.isNotEmpty() && queryText != "") {


            val startPos2: Int = filterList[position].itemString!!.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].itemString!!)
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
                holder.itemView.list_itemString.text = spannable
            } else {
                holder.itemView.list_itemString.text = filterList[position].itemString!!
            }
        } else {
            holder.itemView.list_itemString.text = filterList[position].itemString!!
        }




        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onVendorCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
            println("menu click")

            val popUp = PopupMenu(myView.context,holder.itemView)
            popUp.inflate(R.menu.options_menu)
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.menu1 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                    R.id.menu2 -> {
                        Toast.makeText(myView.context, data.ID, Toast.LENGTH_SHORT).show()
                    }
                    R.id.menu3 -> {
                        Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                    }
                }

                true
            }



            popUp.show()

            /*
            fun onClick(view: View?) {
                println("menu click")
                //will show popup menu here
            }*/


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

                    val resultList:MutableList<Vendor> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.itemString!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

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

                filterList = results?.values as MutableList<Vendor>
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
    fun addAll(list: MutableList<Vendor>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class VendorViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.vendor_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mItemString: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mItemString = itemView.findViewById(R.id.list_itemString)

    }

    fun bind(vendor: Vendor) {
        mNameView?.text = vendor.name
        mItemString?.text = vendor.itemString!!

    }



}