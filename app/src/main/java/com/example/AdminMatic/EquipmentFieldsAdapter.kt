package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject


class EquipmentFieldsAdapter(private val list: MutableList<EquipmentField>, private val appContext: Context, private val type:String, private val cellClickListener: EquipmentFieldCellClickListener): RecyclerView.Adapter<EquipmentFieldViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<EquipmentField> = emptyList<EquipmentField>().toMutableList()

    lateinit var globalVars:GlobalVars

    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentFieldViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EquipmentFieldViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EquipmentFieldViewHolder, position: Int) {

        globalVars = GlobalVars()

        val field: EquipmentField = filterList[position]
        holder.bind(field)


        holder.itemView.setOnClickListener {
            cellClickListener.onEquipmentFieldCellClickListener(field)
        }



        //options btn click
        val optionsTv = holder.itemView.findViewById<TextView>(R.id.textViewOptions)

        optionsTv.setOnClickListener {
            println("menu click")

            val popUp = PopupMenu(myView.context,optionsTv)
            popUp.gravity = Gravity.CENTER
            popUp.inflate(R.menu.delete_menu)
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.menu1 -> {
                        //Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()
                        // Todo: reorganize these ugly nested ifs



                        if (GlobalVars.permissions!!.equipmentEdit == "0") {
                            globalVars.simpleAlert(myView.context, myView.context.getString(R.string.access_denied), myView.context.getString(R.string.no_permission_equipment_edit))
                        }
                        else {

                            if (filterList.size > 1) {
                                if (field.name != "N/A" &&  field.name != "Other") {

                                    val builder = AlertDialog.Builder(myView.context)
                                    builder.setTitle(myView.context.getString(R.string.dialogue_delete_equipment_field_title))
                                    builder.setMessage(myView.context.getString(R.string.dialogue_delete_equipment_field_body))
                                    builder.setPositiveButton(myView.context.getString(R.string.yes)) { _, _ ->

                                        var urlString =
                                            "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/delete/equipmentField.php"

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
                                                params["field"] = type
                                                params["ID"] = field.ID
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
                                else {
                                    globalVars.simpleAlert(myView.context,myView.context.getString(R.string.dialogue_error),myView.context.getString(R.string.dialogue_other_equipment_field_body))
                                }
                            }
                            else {
                                globalVars.simpleAlert(myView.context,myView.context.getString(R.string.dialogue_error),myView.context.getString(R.string.dialogue_only_equipment_field_body))

                            }
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

class EquipmentFieldViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.equipment_field_list_item, parent, false)) {
    private var mNameView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
    }

    fun bind(field: EquipmentField) {
        mNameView?.text = field.name
    }



}