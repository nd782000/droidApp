package com.example.AdminMatic

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
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

        val listName = holder.itemView.findViewById<TextView>(R.id.list_name)
        val listItemString = holder.itemView.findViewById<TextView>(R.id.list_itemString)

        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        //println("queryText = $queryText")
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
                listName.text = spannable
            } else {
                listName.text = filterList[position].name
            }
        } else {
            listName.text = filterList[position].name
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
                listItemString.text = spannable
            } else {
                listItemString.text = filterList[position].itemString!!
            }
        } else {
            listItemString.text = filterList[position].itemString!!
        }




        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onVendorCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
            println("menu click")

            val popUp = PopupMenu(myView.context,holder.itemView.findViewById<TextView>(R.id.textViewOptions))
            popUp.inflate(R.menu.vendor_options_menu)
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.call -> {
                        if (!data.mainPhone.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.mainPhone))
                            myView.context.startActivity(intent)
                        }
                        else {
                            Toast.makeText(myView.context, myView.context.getString(R.string.dialogue_no_phone_number), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                true
            }
            popUp.show()
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
                var resultList:MutableList<Vendor> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list
                } else {
                    for (row in list) {
                        if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.itemString!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
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
        //if (vendor.itemString != null) {
            mItemString?.text = vendor.itemString
        //}
    }



}