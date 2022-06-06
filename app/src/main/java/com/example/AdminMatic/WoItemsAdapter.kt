package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.wo_item_list_item.view.*


class WoItemsAdapter(list: MutableList<WoItem>, private val context: Context, private val cellClickListener: WoItemCellClickListener) : RecyclerView.Adapter<WoItemViewHolder>() {

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

        val woItem: WoItem = filterList[position]
        holder.bind(woItem)
        println("queryText = $queryText")
        //text highlighting for first string


        holder.itemView.list_name.text = filterList[position].item

        val mIconView:ImageView = holder.itemView.findViewById(R.id.status_icon_iv)
        when (woItem.status) {
            "0"-> Picasso.with(context)
                .load(R.drawable.ic_canceled)
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

        }




        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onWoItemCellClickListener(data)
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
                        if (GlobalVars.permissions!!.scheduleEdit == "0") {
                            globalVars.simpleAlert(myView.context, context.getString(R.string.access_denied), context.getString(R.string.no_permission_schedule_edit))
                        }
                        else {

                            if (filterList.size > 1) {
                                if (woItem.usageQty == "0.00") {

                                    val builder = AlertDialog.Builder(myView.context)
                                    builder.setTitle(context.getString(R.string.dialogue_delete_wo_item_title))
                                    builder.setMessage(context.getString(R.string.dialogue_delete_wo_item_body))
                                    builder.setPositiveButton(context.getString(R.string.yes)) { _, _ ->

                                        var urlString =
                                            "https://www.adminmatic.com/cp/app/functions/delete/workOrderItem.php"

                                        val currentTimestamp = System.currentTimeMillis()
                                        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
                                        urlString = "$urlString?cb=$currentTimestamp"
                                        val queue = Volley.newRequestQueue(myView.context)

                                        val postRequest1: StringRequest = object : StringRequest(
                                            Method.POST, urlString,
                                            Response.Listener { response -> // response
                                                //Log.d("Response", response)

                                                println("Response $response")

                                                filterList.removeAt(position)
                                                notifyDataSetChanged()

                                                /*
                                            try {
                                                val parentObject = JSONObject(response)
                                                println("parentObject = $parentObject")
                                                val workOrders: JSONArray = parentObject.getJSONArray("workOrders")
                                                println("workOrders = $workOrders")
                                                println("workOrders count = ${workOrders.length()}")

                                                if (GlobalVars.globalWorkOrdersList != null) {
                                                    GlobalVars.globalWorkOrdersList!!.clear()
                                                }


                                                val gson = GsonBuilder().create()

                                                GlobalVars.globalWorkOrdersList =
                                                    gson.fromJson(workOrders.toString(), Array<WorkOrder>::class.java)
                                                        .toMutableList()

                                                /* Here 'response' is a String containing the response you received from the website... */
                                            } catch (e: JSONException) {
                                                println("JSONException")
                                                e.printStackTrace()
                                            }

                                             */

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
                                        queue.add(postRequest1)
                                    }
                                    builder.setNegativeButton(context.getString(R.string.no)) { _, _ ->

                                    }
                                    builder.show()
                                }
                                else {
                                    globalVars.simpleAlert(myView.context,context.getString(R.string.dialogue_usage_exists_title),context.getString(R.string.dialogue_usage_exists_body))
                                }
                            }
                            else {
                                globalVars.simpleAlert(myView.context,context.getString(R.string.dialogue_only_wo_item_title),context.getString(R.string.dialogue_only_wo_item_body))

                            }
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

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class WoItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.wo_item_list_item, parent, false)) {

    private var mIconView: ImageView? = null
    private var mNameView: TextView? = null
    private var mEstView: TextView? = null
    private var mActView: TextView? = null
    private var mDescriptionView: TextView? = null

    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mIconView = itemView.findViewById(R.id.status_icon_iv)
        mEstView = itemView.findViewById(R.id.list_est_tv)
        mActView = itemView.findViewById(R.id.list_act_tv)
        mDescriptionView = itemView.findViewById(R.id.list_description_tv)
    }

    fun bind(woItem: WoItem) {
        mNameView?.text = woItem.item
        mEstView?.text = woItem.est
        mActView?.text = woItem.usageQty
        mDescriptionView?.text = woItem.empDesc
    }

}