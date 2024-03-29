package com.example.AdminMatic

import android.app.AlertDialog
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
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import java.util.*
import com.squareup.picasso.Picasso
import org.json.JSONObject


class EquipmentAdapter(private val list: MutableList<Equipment>, private val context: Context, private val appContext: Context, private val cellClickListener: EquipmentCellClickListener, private val depMode:Boolean)

    : RecyclerView.Adapter<EquipmentViewHolder>(), Filterable {


    var filterList:MutableList<Equipment> = emptyList<Equipment>().toMutableList()

    lateinit var globalVars:GlobalVars

    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EquipmentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {

        globalVars = GlobalVars()

        val equipment: Equipment = filterList[position]

        val listName = holder.itemView.findViewById<TextView>(R.id.list_name)
        val listType = holder.itemView.findViewById<TextView>(R.id.list_type)

        holder.bind(equipment)
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
                listType.text = spannable
            } else {

                if (filterList[position].typeName != null){
                    listType.text = filterList[position].typeName!!
                }
            }
        } else {
            if (filterList[position].typeName != null){
                listType.text = filterList[position].typeName!!
            }


        }

        //Image Loading
        val equipmentImageView:ImageView = holder.itemView.findViewById(R.id.equipment_item_image_view)
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

        val equipmentStatusImageView:ImageView = holder.itemView.findViewById(R.id.list_status_icon_image_view)

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

        if (!depMode) {

            holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
                println("menu click")

                val popUp = PopupMenu(myView.context, holder.itemView.findViewById<TextView>(R.id.textViewOptions))
                popUp.inflate(R.menu.equipment_list_menu)
                popUp.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item!!.itemId) {
                        R.id.deactivate -> {

                            if (GlobalVars.permissions!!.equipmentEdit == "0") {
                                globalVars.simpleAlert(
                                    myView.context,
                                    myView.context.getString(R.string.access_denied),
                                    myView.context.getString(R.string.no_permission_equipment_edit)
                                )
                            } else {

                                val builder = AlertDialog.Builder(myView.context)
                                builder.setTitle(myView.context.getString(R.string.dialogue_deactivate_equipment_title))
                                builder.setMessage(myView.context.getString(R.string.dialogue_deactivate_equipment_body))
                                builder.setPositiveButton(myView.context.getString(R.string.yes)) { _, _ ->

                                    var urlString =
                                        "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentActive.php"

                                    val currentTimestamp = System.currentTimeMillis()
                                    println("urlString = ${"$urlString?cb=$currentTimestamp"}")
                                    urlString = "$urlString?cb=$currentTimestamp"

                                    val postRequest1: StringRequest = object : StringRequest(
                                        Method.POST, urlString,
                                        Response.Listener { response -> // response
                                            //Log.d("Response", response)

                                            println("Response $response")

                                            val parentObject = JSONObject(response)

                                            if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                                                globalVars.playSaveSound(myView.context)
                                                filterList.removeAt(position)
                                                notifyDataSetChanged()
                                            }


                                        },
                                        Response.ErrorListener { // error


                                            // Log.e("VOLLEY", error.toString())
                                            // Log.d("Error.Response", error())
                                        }
                                    ) {
                                        override fun getParams(): Map<String, String> {
                                            val params: MutableMap<String, String> = HashMap()
                                            params["companyUnique"] =
                                                GlobalVars.loggedInEmployee!!.companyUnique
                                            params["sessionKey"] =
                                                GlobalVars.loggedInEmployee!!.sessionKey
                                            params["equipmentID"] = equipment.ID
                                            println("params = $params")
                                            return params
                                        }
                                    }
                                    postRequest1.tag = "woItem"
                                    VolleyRequestQueue.getInstance(appContext).addToRequestQueue(postRequest1)
                                }
                                builder.setNegativeButton(myView.context.getString(R.string.no)) { _, _ ->

                                }
                                builder.show()
                            }
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
        else { //depmode
            holder.itemView.findViewById<TextView>(R.id.textViewOptions).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.list_crew).visibility = View.INVISIBLE
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
                var resultList:MutableList<Equipment> = mutableListOf()

                if (charSearch.isEmpty()) {
                    //filterList.clear()
                    resultList = list
                } else {
                    for (row in list) {
                        if (row.typeName == null){
                            if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                                resultList.add(row)
                            }
                        }else{
                            if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) || row.typeName!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
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