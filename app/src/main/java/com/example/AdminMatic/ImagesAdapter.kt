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
import kotlinx.android.synthetic.main.image_list_item.view.*
import java.util.*
import com.squareup.picasso.Picasso



class ImagesAdapter(private val list: MutableList<Image>, private val context: Context,private val cellClickListener: ImageCellClickListener)

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
        holder.bind(image)
        //holder.itemView.list_sysname.text = filterList[position].sysname
        //holder.itemView.list_mainAddr.text = filterList[position].mainAddr
        println("queryText = $queryText")

        //text highlighting for second string
        if (queryText != null && !queryText.isEmpty() && queryText != "" && filterList[position].customerName != null) {


            val startPos2: Int = filterList[position].customerName!!.toLowerCase().indexOf(queryText.toLowerCase())
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].customerName!!)
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
                holder.itemView.list_name.text = spannable
            } else {

                if (filterList[position].customerName != null){
                    holder.itemView.list_name.text = filterList[position].customerName!!
                }
            }
        } else {
            if (filterList[position].customerName != null){
                holder.itemView.list_name.text = filterList[position].customerName!!
            }


        }

        var imageImageView:ImageView = holder.itemView.findViewById<ImageView>(R.id.image_item_image_view)


        Picasso.with(context)


            .load("${GlobalVars.thumbBase + filterList[position].fileName}")
            //.placeholder(R.drawable.ic_images) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(imageImageView)                       //Your image view object.


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onImageCellClickListener(data)
        }

    }


    fun getImageCount(): Int{

        print("getImageCount = ${list.size}")
        return list.size

    }


    override fun getItemCount(): Int{
       // print("getItemCount isSearching = ${isSearching.toString()}")
       // if (isSearching){
            print("getItemCount = ${filterList.size}")
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

                if (charSearch.isEmpty()) {
                    //filterList.clear()
                    filterList = list
                } else {

                    var resultList:MutableList<Image> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                       // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.customerName == null){
                            if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {

                                println("add row")

                                resultList.add(row)

                                println("resultList.count = ${resultList.count()}")
                            }
                        }else{
                            if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) || row.customerName!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {

                                println("add row")

                                resultList.add(row)

                                println("resultList.count = ${resultList.count()}")
                            }
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

    fun bind(image: Image) {
        mNameView?.text = image.name
        if(image.customerName != null){
            mTypeView?.text = image.customerName!!
        }


    }



}