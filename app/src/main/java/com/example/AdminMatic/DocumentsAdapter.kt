package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
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
import com.squareup.picasso.Picasso
import java.time.LocalDate


class DocumentsAdapter(list: MutableList<Document>, private val context: Context, private val cellClickListener: DocumentCellClickListener) : RecyclerView.Adapter<DocumentViewHolder>() {

    var filterList:MutableList<Document> = emptyList<Document>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DocumentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {

        val document: Document = filterList[position]


        val licenseStatusImageView: ImageView = holder.itemView.findViewById(R.id.icon_iv)

        //TODO: figure out preloading on these
        when (document.type) {
            "ai"-> Picasso.with(context)
                .load(R.drawable.ic_file_ai)
                .into(licenseStatusImageView)
            "avi"-> Picasso.with(context)
                .load(R.drawable.ic_file_avi)
                .into(licenseStatusImageView)
            "doc"-> Picasso.with(context)
                .load(R.drawable.ic_file_doc)
                .into(licenseStatusImageView)
            "dwg"-> Picasso.with(context)
                .load(R.drawable.ic_file_dwg)
                .into(licenseStatusImageView)
            "pdf"-> Picasso.with(context)
                .load(R.drawable.ic_file_pdf)
                .into(licenseStatusImageView)
            else -> Picasso.with(context)
                .load(R.drawable.ic_file)
                .into(licenseStatusImageView)
        }


        holder.itemView.setOnClickListener {
            cellClickListener.onDocumentCellClickListener(document)
        }

        holder.itemView.findViewById<TextView>(R.id.share_tv).setOnClickListener {
            cellClickListener.onShareButtonClicked(document)
        }


        holder.bind(document, context)



    }

    override fun getItemCount(): Int{
        //print("getItemCount = ${filterList.size}")
        return filterList.size
    }


}

class DocumentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.document_list_item, parent, false)) {

    private var mNameView: TextView? = null
    private var mFileTypeView: TextView? = null
    private var mAddedByView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.name_tv)
        mFileTypeView = itemView.findViewById(R.id.file_type_tv)
        mAddedByView = itemView.findViewById(R.id.added_by_tv)
    }

    fun bind(document: Document, context: Context) {
        mNameView!!.text = document.name
        mFileTypeView!!.text = context.getString(R.string.documents_file_type, document.type, document.fsize)
        val dateAdded = LocalDate.parse(document.dateAdded, GlobalVars.dateFormatterPHP)
        mAddedByView!!.text = context.getString(R.string.documents_added_by, document.empName, dateAdded.format(GlobalVars.dateFormatterShort))

    }
}