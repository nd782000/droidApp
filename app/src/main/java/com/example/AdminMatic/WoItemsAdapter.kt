package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONObject


class WoItemsAdapter(list: MutableList<WoItem>, private val context: Context, private val appContext: Context, private val workOrder:WorkOrder, private val cellClickListener: WoItemCellClickListener) : RecyclerView.Adapter<WoItemViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<WoItem> = emptyList<WoItem>().toMutableList()


    lateinit  var globalVars:GlobalVars


    var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WoItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WoItemViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: WoItemViewHolder, position: Int) {

        globalVars = GlobalVars()



        val allCl:ConstraintLayout = holder.itemView.findViewById(R.id.all_cl)
        val addNewText:TextView = holder.itemView.findViewById(R.id.add_new_item_tv)
        if (position == filterList.size) {
            allCl.visibility = View.INVISIBLE
            addNewText.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                cellClickListener.onAddNewItemClickListener()
            }
            return
        }
        else {
            allCl.visibility = View.VISIBLE
            addNewText.visibility = View.INVISIBLE
        }

        val woItem: WoItem = filterList[holder.bindingAdapterPosition]

        holder.bind(woItem)
        //println("queryText = $queryText")
        //text highlighting for first string


        //holder.itemView.findViewById<TextView>(R.id.list_name).text = filterList[position].item

        val mIconView:ImageView = holder.itemView.findViewById(R.id.status_icon_iv)
        when (woItem.status) {

            "0"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(mIconView)
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(mIconView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(mIconView)
            "3"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(mIconView)
            "4"-> Picasso.with(context)
                .load(R.drawable.ic_canceled)
                .into(mIconView)

        }



        // Populate description string


        var descriptionString = ""
        val iterator = woItem.tasks!!.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()

            if (item.taskTranslated == null) {
                descriptionString = if (item.task!!.startsWith('-')) {
                    descriptionString + item.task
                } else {
                    descriptionString + "-" + item.task
                }
            }
            else {
                descriptionString = if (item.taskTranslated!!.startsWith('-')) {
                    descriptionString + item.taskTranslated
                } else {
                    descriptionString + "-" + item.taskTranslated
                }
            }


            if (iterator.hasNext()) {
                descriptionString += "\n"
            }
        }
        holder.mDescriptionView!!.text = descriptionString




        val data = filterList[holder.bindingAdapterPosition]
        holder.itemView.setOnClickListener {
            cellClickListener.onWoItemCellClickListener(data)
        }

        val autoUsageCl = holder.itemView.findViewById<ConstraintLayout>(R.id.quick_complete_enabled_cl)
        if ((data.autoUsage ?: "0") == "1") {
            autoUsageCl.visibility = View.VISIBLE
        }
        else {
            autoUsageCl.visibility = View.GONE
        }



        //options btn click
        val optionsTv = holder.itemView.findViewById<TextView>(R.id.textViewOptions)

        optionsTv.setOnClickListener {

            val popUp = PopupMenu(myView.context,optionsTv)
            popUp.gravity = Gravity.CENTER
            popUp.inflate(R.menu.wo_item_list_menu)
            if ((data.autoUsage ?: "0") == "0") {
                popUp.menu.removeItem(R.id.quick_complete)
            }
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.delete -> {
                        //Toast.makeText(myView.context, item.title, Toast.LENGTH_SHORT).show()

                        if (workOrder.invoiceID == "0") {

                            if (GlobalVars.permissions!!.scheduleEdit == "0") {
                                globalVars.simpleAlert(myView.context, context.getString(R.string.access_denied), context.getString(R.string.no_permission_schedule_edit))
                            }
                            else {

                                if (filterList.size > 1) {

                                    val builder = AlertDialog.Builder(myView.context)
                                    builder.setTitle(context.getString(R.string.dialogue_delete_wo_item_title))
                                    builder.setMessage(context.getString(R.string.dialogue_delete_wo_item_body))
                                    builder.setPositiveButton(context.getString(R.string.yes)) { _, _ ->

                                        var urlString =
                                            "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/delete/workOrderItem.php"

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

                                                    val gson = GsonBuilder().create()
                                                    val hasUsage: String = gson.fromJson(parentObject["hasUsage"].toString(), String::class.java)

                                                    if (hasUsage == "0") {
                                                        globalVars.playSaveSound(myView.context)
                                                        filterList.removeAt(holder.bindingAdapterPosition)
                                                        notifyDataSetChanged()
                                                    }
                                                    else {
                                                        globalVars.simpleAlert(myView.context,context.getString(R.string.dialogue_usage_exists_title),context.getString(R.string.dialogue_usage_exists_body))
                                                    }


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
                                                params["workOrderID"] = woItem.woID
                                                params["workOrderItemID"] = woItem.ID
                                                println("params = $params")
                                                return params
                                            }
                                        }
                                        postRequest1.tag = "woItem"
                                        VolleyRequestQueue.getInstance(appContext).addToRequestQueue(postRequest1)
                                    }
                                    builder.setNegativeButton(context.getString(R.string.no)) { _, _ ->

                                    }
                                    builder.show()

                                }
                                else {
                                    globalVars.simpleAlert(myView.context,context.getString(R.string.dialogue_only_wo_item_title),context.getString(R.string.dialogue_only_wo_item_body))

                                }
                            }
                        }
                        else { // invoiceID != 0
                            globalVars.simpleAlert(myView.context, context.getString(R.string.dialogue_error), context.getString(R.string.invoiced_wo_cant_edit))
                        }

                    }
                    R.id.quick_complete -> {
                        cellClickListener.onWoItemQuickComplete(data)
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

    override fun getItemCount(): Int{

        //print("getItemCount = ${filterList.size}")
        return filterList.size + 1

    }





}

class WoItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.wo_item_list_item, parent, false)) {

    private var mIconView: ImageView? = null
    private var mNameView: TextView? = null
    private var mEstView: TextView? = null
    private var mActView: TextView? = null
    var mDescriptionView: TextView? = null

    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mIconView = itemView.findViewById(R.id.status_icon_iv)
        mEstView = itemView.findViewById(R.id.list_est_tv)
        mActView = itemView.findViewById(R.id.list_act_tv)
        mDescriptionView = itemView.findViewById(R.id.list_description_tv)
    }

    fun bind(woItem: WoItem) {

        if (woItem.itemTranslated == null) {
            mNameView?.text = woItem.item
            println("using untranslated string")
        }
        else {
            mNameView?.text = woItem.itemTranslated
            println("using translated string")
        }
        mEstView?.text = woItem.est
        mActView?.text = woItem.usageQty
        mDescriptionView?.text = woItem.empDesc
    }

}