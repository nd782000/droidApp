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
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import java.util.*


class ImagesAdapter(private val list: MutableList<Image>, private val context: Context, private val showTitleInsteadOfCustomer: Boolean, private val cellClickListener: ImageCellClickListener)

    : RecyclerView.Adapter<ImageViewHolder>(), Filterable {


    var filterList:MutableList<Image> = emptyList<Image>().toMutableList()


    var queryText = ""

    //var isSearching:Boolean = false

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val image: Image = filterList[position]
        holder.bind(image, showTitleInsteadOfCustomer)

        val listName = holder.itemView.findViewById<TextView>(R.id.list_name)
        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        //println("queryText = $queryText")

        //text highlighting for second string
        if (queryText.isNotEmpty() && queryText != "" && filterList[position].customerName != null) {



            val startPos2: Int = filterList[position].customerName!!.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {

                val spannable: Spannable = if (showTitleInsteadOfCustomer) {
                    SpannableString(filterList[position].name)
                }
                else {
                    SpannableString(filterList[position].customerName!!)
                }

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
                println("setting name to spannable 1")
                listName.text = spannable
            } else {

                if (filterList[position].customerName != null){
                    if (showTitleInsteadOfCustomer) {
                        listName.text = filterList[position].name
                    }
                    else {
                        listName.text = filterList[position].customerName!!
                    }
                }
            }
        } else {
            if (filterList[position].customerName != null){
                if (showTitleInsteadOfCustomer) {
                    listName.text = filterList[position].name
                }
                else {
                    listName.text = filterList[position].customerName!!
                }
            }


        }

        val imageImageView:ImageView = holder.itemView.findViewById(R.id.image_item_image_view)


        Picasso.with(context)


            .load(GlobalVars.thumbBase + filterList[position].fileName)
            //.placeholder(R.drawable.ic_images) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(imageImageView)                       //Your image view object.


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onImageCellClickListener(data)
        }

    }

    /*
    fun getImageCount(): Int{

        print("getImageCount = ${list.size}")
        return list.size

    }
    */


    override fun getItemCount(): Int{
       // print("getItemCount isSearching = ${isSearching.toString()}")
       // if (isSearching){
            //print("getItemCount = ${filterList.size}")
            return filterList.size
       // }else{
           // print("getItemCount = ${list.size}")
           // return list.size
       // }

        //return 100

    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                //isSearching = true
                val charSearch = constraint.toString()
                queryText = charSearch
                var resultList:MutableList<Image> = mutableListOf()

                if (charSearch.isEmpty()) {
                    resultList = list
                } else {
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.customerName == null){
                            if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                                resultList.add(row)
                            }
                        }else{
                            if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.customerName!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
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

                filterList = results?.values as MutableList<Image>
                notifyDataSetChanged()
            }

        }
    }

    // Clean all elements of the recycler
    fun clear() {
       // isSearching = false
        list.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: MutableList<Image>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class ImageViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.image_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mTypeView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mTypeView = itemView.findViewById(R.id.list_type)

    }

    fun bind(image: Image, showTitleInsteadOfCustomer: Boolean) {


        if(image.customerName != null && !showTitleInsteadOfCustomer){
            mNameView?.text = image.customerName
        }
        else {
            mNameView?.text = image.name
        }

    }

}