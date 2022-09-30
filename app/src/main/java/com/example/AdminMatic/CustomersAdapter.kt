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


class CustomersAdapter(private val list: MutableList<Customer>, private val cellClickListener: CustomerCellClickListener, private val searchAddressOnly:Boolean): RecyclerView.Adapter<CustomerViewHolder>(), Filterable {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Customer> = emptyList<Customer>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CustomerViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {

        val customer: Customer = filterList[position]
        holder.bind(customer)
        println("queryText = $queryText")
        //text highlighting for first string

        val listSysname = holder.itemView.findViewById<TextView>(R.id.list_sysname)
        val listMainAddr = holder.itemView.findViewById<TextView>(R.id.list_mainAddr)

        if (!searchAddressOnly) {

            if (queryText.isNotEmpty() && queryText != "") {

                val startPos1: Int = filterList[position].sysname.lowercase(Locale.getDefault())
                    .indexOf(queryText.lowercase(Locale.getDefault()))
                val endPos1 = startPos1 + queryText.length
                if (startPos1 != -1) {
                    val spannable: Spannable = SpannableString(filterList[position].sysname)
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
                    listSysname.text = spannable
                } else {
                    listSysname.text = filterList[position].sysname
                }
            } else {
                listSysname.text = filterList[position].sysname
            }
        }

        //text highlighting for second string
        if (queryText.isNotEmpty() && queryText != "") {


            val startPos2: Int = filterList[position].mainAddr!!.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault())
            )
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].mainAddr)
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
                listMainAddr.text = spannable
            } else {
                listMainAddr.text = filterList[position].mainAddr
            }
        } else {
            listMainAddr.text = filterList[position].mainAddr
        }

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onCustomerCellClickListener(data)
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
                var resultList:MutableList<Customer> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list
                } else {

                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")

                        if (searchAddressOnly) {
                            if (row.mainAddr!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                                resultList.add(row)
                            }
                        }
                        else {
                            if (row.sysname.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.mainAddr!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                                resultList.add(row)
                            }
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

                filterList = results?.values as MutableList<Customer>
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

class CustomerViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.customer_list_item, parent, false)) {
    private var mSysNameView: TextView? = null
    private var mMainAddrView: TextView? = null


    init {
        mSysNameView = itemView.findViewById(R.id.list_sysname)
        mMainAddrView = itemView.findViewById(R.id.list_mainAddr)
    }

    fun bind(customer: Customer) {
        mSysNameView?.text = customer.sysname
        mMainAddrView?.text = customer.mainAddr
    }



}