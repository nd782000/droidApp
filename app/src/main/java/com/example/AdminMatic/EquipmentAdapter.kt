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
import kotlinx.android.synthetic.main.equipment_list_item.view.*
import java.util.*
import com.squareup.picasso.Picasso



class EquipmentAdapter(private val list: MutableList<Equipment>, private val context: Context,private val cellClickListener: EquipmentCellClickListener)

    : RecyclerView.Adapter<EquipmentViewHolder>(), Filterable {


    var filterList:MutableList<Equipment> = emptyList<Equipment>().toMutableList()


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EquipmentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {




        val equipment: Equipment = filterList[position]
        holder.bind(equipment)
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
        if (queryText.isNotEmpty() && queryText != "" && filterList[position].typeName != null) {


            val startPos2: Int = filterList[position].typeName!!.lowercase(Locale.getDefault()).indexOf(queryText.lowercase(Locale.getDefault()))
            val endPos2 = startPos2 + queryText.length
            if (startPos2 != -1) {
                val spannable: Spannable = SpannableString(filterList[position].typeName!!)
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
                holder.itemView.list_type.text = spannable
            } else {

                if (filterList[position].typeName != null){
                    holder.itemView.list_type.text = filterList[position].typeName!!
                }
            }
        } else {
            if (filterList[position].typeName != null){
                holder.itemView.list_type.text = filterList[position].typeName!!
            }


        }

        //Image Loading
        val equipmentImageView:ImageView = holder.itemView.findViewById<ImageView>(R.id.equipment_item_image_view)
        var imagePath:String = "drawable://" + R.drawable.ic_images
        if (filterList[position].image != null){
            imagePath = GlobalVars.thumbBase + filterList[position].image!!.fileName
        }
        Picasso.with(context)
            .load(imagePath)
            .placeholder(R.drawable.ic_images) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(equipmentImageView)                       //Your image view object.

        val equipmentStatusImageView:ImageView = holder.itemView.findViewById<ImageView>(R.id.list_status_icon_image_view)

        when (equipment.status) {
            "0"->Picasso.with(context)
                .load(R.drawable.ic_online)
                .into(equipmentStatusImageView)
            "1"->Picasso.with(context)
                .load(R.drawable.ic_needs_repair)
                .into(equipmentStatusImageView)
            "2"->Picasso.with(context)
                .load(R.drawable.ic_broken)
                .into(equipmentStatusImageView)
            "3"->Picasso.with(context)
                .load(R.drawable.ic_winterized)
                .into(equipmentStatusImageView)
        }

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onEquipmentCellClickListener(data)
        }



        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener(){
            println("menu click")

            val popUp:PopupMenu = PopupMenu(myView.context,holder.itemView)
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

                    val resultList:MutableList<Equipment> = mutableListOf()
                    for (row in list) {
                        //println("row.sysname.toLowerCase(Locale.ROOT) = ${row.sysname.toLowerCase(Locale.ROOT)}")
                        // println("charSearch.toLowerCase(Locale.ROOT) = ${charSearch.toLowerCase(Locale.ROOT)}")
                        if (row.typeName == null){
                            if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

                                println("add row")

                                resultList.add(row)

                                println("resultList.count = ${resultList.count()}")
                            }
                        }else{
                            if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.typeName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {

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

                filterList = results?.values as MutableList<Equipment>
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
    fun addAll(list: MutableList<Equipment>) {
        list.addAll(list)
        notifyDataSetChanged()
    }





}

class EquipmentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.equipment_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mTypeView: TextView? = null
    private var mCrewView: TextView? = null
    private var mStatusIconView: ImageView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mTypeView = itemView.findViewById(R.id.list_type)
        mCrewView = itemView.findViewById(R.id.list_crew)
        mStatusIconView = itemView.findViewById(R.id.list_status_icon_image_view)

    }

    fun bind(equipment: Equipment) {
        mNameView?.text = equipment.name
        if(equipment.typeName != null){
            mTypeView?.text = equipment.typeName
        }
        if(equipment.crewName != null){
            mCrewView?.text = equipment.crewName
        }
        println(equipment.name + equipment.status)
        /*
            when (equipment.status) {
                "0"->Picasso.with(context)
                    .load(R.drawable.ic_images)
                    .into(mStatusIconView)
            }

            if(equipment.status == "0") {

            }
            else if (equipment.status == "1") {

            }
            else if (equipment.status == "2") {

            }
            else if (equipment.status == "3") {

            }
            */
        //mCrewView?.text = equipment.crewName!!

    }



}