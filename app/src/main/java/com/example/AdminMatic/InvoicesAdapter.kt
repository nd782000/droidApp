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
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import kotlinx.android.synthetic.main.customer_list_item.view.*
import kotlinx.android.synthetic.main.invoice_list_item.view.*
import java.util.*



class InvoicesAdapter(private val list: MutableList<Invoice>, private val context: Context,private val cellClickListener: InvoiceCellClickListener)

    : RecyclerView.Adapter<InvoiceViewHolder>(), Filterable {


    var filterList:MutableList<Invoice> = emptyList<Invoice>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return InvoiceViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {




        val invoice: Invoice = filterList[position]
        holder.bind(invoice)
        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        println("queryText = $queryText")
        //text highlighting for first string
        if (queryText != null && !queryText.isEmpty() && queryText != "") {

            val startPos1: Int = filterList[position].custName!!.toLowerCase().indexOf(queryText.toLowerCase())
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
        if (queryText != null && !queryText.isEmpty() && queryText != "" && filterList[position].title != null) {


            val startPos2: Int = filterList[position].title!!.toLowerCase().indexOf(queryText.toLowerCase())
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].title!!)
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
                if (filterList[position].title != null){
                    holder.itemView.list_title.text = filterList[position].title!!
                }
            }
        } else {
            if (filterList[position].title != null){
                holder.itemView.list_title.text = filterList[position].title!!
            }
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onInvoiceCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener(){
            println("menu click")

            var popUp:PopupMenu = PopupMenu(myView.context,holder.itemView)
            popUp.inflate(R.menu.options_menu)
            popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

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
            })



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

                    var resultList:MutableList<Invoice> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.custName!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) || row.title!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {

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

                filterList = results?.values as MutableList<Invoice>
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
    fun addAll(list: MutableList<Invoice>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class InvoiceViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.invoice_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mTitleView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mTitleView = itemView.findViewById(R.id.list_title)

    }

    fun bind(invoice: Invoice) {
        mNameView?.text = invoice.custName!!
        mTitleView?.text = invoice.title

    }



}