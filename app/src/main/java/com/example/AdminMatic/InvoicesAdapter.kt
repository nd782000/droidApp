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

        val listInvoiceNameTv = holder.itemView.findViewById<TextView>(R.id.list_invoice_name_tv)
        val listInvoiceIDTv = holder.itemView.findViewById<TextView>(R.id.list_invoice_id_tv)
        val listInvoiceDateTv = holder.itemView.findViewById<TextView>(R.id.list_invoice_date_tv)
        val listInvoicePriceTv = holder.itemView.findViewById<TextView>(R.id.list_invoice_price_tv)
        val listInvoiceStatusTv = holder.itemView.findViewById<TextView>(R.id.list_invoice_status_tv)


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
                listInvoiceNameTv.text = spannable
            } else {
                listInvoiceNameTv.text = filterList[position].custName
            }
        } else {
            listInvoiceNameTv.text = filterList[position].custName
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
                listInvoiceIDTv.text = spannable
            } else {
                listInvoiceIDTv.text = filterList[position].ID
            }
        } else {
            listInvoiceIDTv.text = filterList[position].ID
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
                listInvoiceDateTv.text = spannable
            } else {
                listInvoiceDateTv.text = filterList[position].invoiceDate
            }
        } else {
            listInvoiceDateTv.text = filterList[position].invoiceDate
        }

        listInvoicePriceTv.text = context.getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(filterList[position].total.toDouble()))

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
                listInvoiceStatusTv.text = spannableString
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
                listInvoiceStatusTv.text = spannableString
            }
            "2" -> {
                listInvoiceStatusTv.text = context.getString(R.string.invoice_final)
            }
            "3" -> {
                listInvoiceStatusTv.text = context.getString(R.string.invoice_sent)
            }
            "4" -> {
                listInvoiceStatusTv.text = context.getString(R.string.invoice_paid)
            }
            "5" -> {
                listInvoiceStatusTv.text = context.getString(R.string.invoice_void)
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

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }




    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                queryText = charSearch
                var resultList:MutableList<Invoice> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list
                } else {
                    for (row in list) {

                        if (row.custName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.ID.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.invoiceDate.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
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