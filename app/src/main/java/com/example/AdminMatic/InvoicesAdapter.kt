package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.invoice_list_item.view.*
import java.util.*


class InvoicesAdapter(private val list: MutableList<Invoice>, private val context: Context, private val cellClickListener: InvoiceCellClickListener) : RecyclerView.Adapter<InvoiceViewHolder>(), Filterable {


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
        //text highlighting for customer name
        if (queryText.isNotEmpty() && queryText != "") {

            val startPos1: Int = filterList[position].custName.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
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
                holder.itemView.list_invoice_name_tv.text = spannable
            } else {
                holder.itemView.list_invoice_name_tv.text = filterList[position].custName
            }
        } else {
            holder.itemView.list_invoice_name_tv.text = filterList[position].custName
        }

        //text highlighting for ID
        if (queryText.isNotEmpty() && queryText != "") {
            val startPos2: Int = filterList[position].ID.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].ID)
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
                holder.itemView.list_invoice_id_tv.text = spannable
            } else {
                holder.itemView.list_invoice_id_tv.text = filterList[position].ID
            }
        } else {
            holder.itemView.list_invoice_id_tv.text = filterList[position].ID
        }

        //text highlighting for Date
        if (queryText.isNotEmpty() && queryText != "") {
            val startPos2: Int = filterList[position].invoiceDate.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].invoiceDate)
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
                holder.itemView.list_invoice_date_tv.text = spannable
            } else {
                holder.itemView.list_invoice_date_tv.text = filterList[position].invoiceDate
            }
        } else {
            holder.itemView.list_invoice_date_tv.text = filterList[position].invoiceDate
        }

        holder.itemView.list_invoice_price_tv.text = context.getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(filterList[position].total.toDouble()))

        when (filterList[position].invoiceStatus) {
            "0" -> {
                val spannableString = SpannableString(context.getString(R.string.invoice_syncing))
                val backgroundSpan = ForegroundColorSpan(context.getColor(R.color.red))
                spannableString.setSpan(
                    backgroundSpan,
                    0,
                    spannableString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.itemView.list_invoice_status_tv.text = spannableString
            }
            "1" -> {
                val spannableString = SpannableString(context.getString(R.string.invoice_pending))
                val backgroundSpan = ForegroundColorSpan(context.getColor(R.color.red))
                spannableString.setSpan(
                    backgroundSpan,
                    0,
                    spannableString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.itemView.list_invoice_status_tv.text = spannableString
            }
            "2" -> {
                holder.itemView.list_invoice_status_tv.text = context.getString(R.string.invoice_final)
            }
            "3" -> {
                holder.itemView.list_invoice_status_tv.text = context.getString(R.string.invoice_sent)
            }
            "4" -> {
                holder.itemView.list_invoice_status_tv.text = context.getString(R.string.invoice_paid)
            }
            "5" -> {
                holder.itemView.list_invoice_status_tv.text = context.getString(R.string.invoice_void)
            }

        }

        val invoiceStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_invoice_status_icon_iv)

        when (filterList[position].invoiceStatus) {
            "0"-> Picasso.with(context)
                .load(R.drawable.ic_sync)
                .into(invoiceStatusImageView)
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_pending)
                .into(invoiceStatusImageView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(invoiceStatusImageView)
            "3"-> Picasso.with(context)
                .load(R.drawable.ic_awarded)
                .into(invoiceStatusImageView)
            "4"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(invoiceStatusImageView)
            "5"-> Picasso.with(context)
                .load(R.drawable.ic_canceled)
                .into(invoiceStatusImageView)
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onInvoiceCellClickListener(data)
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

                    val resultList:MutableList<Invoice> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.custName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.ID.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.invoiceDate.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

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
        //mNameView = itemView.findViewById(R.id.list_name)
        //mTitleView = itemView.findViewById(R.id.list_title)
    }

    fun bind(invoice: Invoice) {
        //mNameView?.text = invoice.custName
        //mTitleView?.text = invoice.title
    }



}