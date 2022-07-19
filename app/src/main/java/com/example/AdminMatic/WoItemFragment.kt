package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface TaskCellClickListener {
    fun onTaskCellClickListener(data:Task)
    fun showProgressView()
    fun hideProgressView()
    fun getWoItem(taskUpdated:Boolean)
    fun uploadImage(_task:Task)
    fun checkForWoStatus()

}




class WoItemFragment : Fragment(), TaskCellClickListener ,AdapterView.OnItemSelectedListener {


    private  var woItem: WoItem? = null
    lateinit  var workOrder:WorkOrder
    lateinit  var globalVars:GlobalVars
    private var listIndex: Int = -1



    lateinit var myView:View

    private lateinit var allCL: ConstraintLayout

    private lateinit var  pgsBar: ProgressBar

    private lateinit var woItemSearch: SearchView

    private lateinit var estCl:ConstraintLayout
    private lateinit var estEditTxt:EditText
    private lateinit var chargeSpinner:Spinner

    private lateinit var hideCl:ConstraintLayout
    private lateinit var hideQtySwitch: SwitchCompat
    private lateinit var qtyEditTxt:EditText

    private lateinit var taxCl:ConstraintLayout
    private lateinit var taxableSwitch:SwitchCompat
    private lateinit var priceEditTxt:EditText

    private lateinit var totalCl:ConstraintLayout
    private lateinit var totalEditTxt:EditText

    private lateinit var leadTaskBtn:Button
    private lateinit var tasksRv:RecyclerView
    private lateinit var descriptionCl:ConstraintLayout
    private lateinit var descriptionTv:TextView

    private lateinit var usageBtn:Button
    private lateinit var profitCl:ConstraintLayout

    private lateinit var priceTxt:TextView
    private lateinit var costTxt:TextView
    private lateinit var profitTxt:TextView
    private lateinit var profitPercentTxt:TextView
    private lateinit var profitBar:ProgressBar

    private lateinit var statusBtn:ImageButton
    private lateinit var submitBtn:Button


    private var chargeTypeArray:Array<String> = arrayOf("No Charge", "Flat", "T & M")

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItem = it.getParcelable("woItem")
            workOrder = it.getParcelable("workOrder")!!
            listIndex = it.getInt("listIndex")
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_customer, container, false)
        // employee = args
        myView = inflater.inflate(R.layout.fragment_wo_item, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.work_order_item)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("woItem = ${woItem!!.item}")


        pgsBar = view.findViewById(R.id.progress_bar)
        allCL = view.findViewById(R.id.all_cl)

        woItemSearch = myView.findViewById(R.id.wo_item_search)
        estCl = myView.findViewById(R.id.wo_item_est_cl)
        estEditTxt = myView.findViewById(R.id.wo_item_est_val_et)
        chargeSpinner = myView.findViewById(R.id.wo_item_charge_spinner)

        hideCl = myView.findViewById(R.id.wo_item_hide_cl)
        hideQtySwitch = myView.findViewById(R.id.wo_item_hide_qty_switch)
        qtyEditTxt = myView.findViewById(R.id.wo_item_qty_val_et)

        taxCl = myView.findViewById(R.id.wo_item_tax_cl)
        taxableSwitch = myView.findViewById(R.id.wo_item_taxable_switch)
        priceEditTxt = myView.findViewById(R.id.wo_item_price_val_et)

        totalCl = myView.findViewById(R.id.wo_item_total_cl)
        totalEditTxt = myView.findViewById(R.id.wo_item_total_val_et)

        leadTaskBtn = myView.findViewById(R.id.wo_item_lead_task_btn)
        tasksRv = myView.findViewById(R.id.wo_item_tasks_rv)
        descriptionCl = myView.findViewById(R.id.wo_item_description_cl)
        descriptionTv = myView.findViewById(R.id.wo_item_description_tv)
        usageBtn = myView.findViewById(R.id.wo_item_usage_btn)
        statusBtn = myView.findViewById(R.id.status_btn)

        println("Work Order Item Customer: ${workOrder.customer}")
        usageBtn.setOnClickListener{

            if (woItem != null){
                val directions = WoItemFragmentDirections.navigateToUsageEntry(woItem!!,workOrder)
                myView.findNavController().navigate(directions)
            }

        }
        statusBtn.setOnClickListener{
            showStatusMenu()
        }
        profitCl = myView.findViewById(R.id.wo_item_profit_cl)
        priceTxt = myView.findViewById(R.id.price_tv)
        costTxt = myView.findViewById(R.id.cost_tv)
        profitTxt = myView.findViewById(R.id.profit_tv)
        profitPercentTxt = myView.findViewById(R.id.profit_percent_tv)
        profitBar = myView.findViewById(R.id.profit_bar)

        submitBtn = myView.findViewById(R.id.wo_item_submit_btn)


        if (woItem == null) {
            //fillProfitCl()
            setUpViews()
            hideProgressView()
        }
        else{
            getWoItem(false)
        }






    }

    private var listener: Callbacks? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is LogOut) {
            context as Callbacks
        } else {
            throw ClassCastException(
                "$context must implement LogOut"
            )
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("woItem")
    }

    private fun fillProfitCl() {
        var totalPrice = 0.0
        //var totalCost = 0.0


        woItem!!.usage.forEach {
            totalPrice += it.totalPrice!!.toDouble()
        }

        val profit:Float = totalPrice.toFloat() - woItem!!.totalCost.toFloat()
        val profitPercent:Int = (profit / totalPrice.toFloat() * 100).toInt()

        priceTxt.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(totalPrice))
        costTxt.text = getString(R.string.dollar_sign, woItem!!.totalCost)
        profitTxt.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(profit))
        profitPercentTxt.text = profitPercent.toString()
        profitBar.progress = 100 - profitPercent
    }

    override fun getWoItem(taskUpdated: Boolean){
        println("get woItem")


        //if (!pgsBar.isVisible){
            showProgressView()
       // }


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/workOrderItem.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")


                    val gson = GsonBuilder().create()
                    woItem = gson.fromJson(parentObject.toString(), WoItem::class.java)
                    descriptionTv.text = woItem!!.empDesc

                    setStatus(woItem!!.status)

                    val taskJSON: JSONArray = parentObject.getJSONArray("tasks")
                    val taskList = gson.fromJson(taskJSON.toString(), Array<Task>::class.java)
                        .toMutableList()

                    tasksRv.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            TasksAdapter(
                                taskList,
                                it,
                                requireActivity().application,
                                this@WoItemFragment,
                                woItem as WoItem
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(
                                myView.context,
                                DividerItemDecoration.VERTICAL
                            )
                        tasksRv.addItemDecoration(itemDecoration)



                        //(adapter as TasksAdapter).notifyDataSetChanged()
                    }
                    fillProfitCl()
                    setUpViews()
                    hideProgressView()
                    if (taskUpdated) {
                        checkForWoStatus()
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["woItemID"] = woItem!!.ID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "woItem"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)


    }

    override fun checkForWoStatus() {

        //Plug the new woitem status into the workOrder object
        workOrder.items!!.forEach {
            if (it.ID == woItem!!.ID) {
                it.status = woItem!!.status
            }
        }

        // If you set it to canceled, do nothing and return
        if (woItem!!.status == "4") {
            return
        }

        // If wo status is "not started" and any items are in progress, prompt to change
        if (workOrder.status == "1") {
            workOrder.items!!.forEach {
                if (it.status == "2") {
                    updateWorkOrderStatus(woItem!!.status)
                    return
                }
            }
        }

        // If wo status is "not started" or "in progress" and all items are finished, prompt to change
        if (workOrder.status == "1" || workOrder.status == "2") {
            var allFinished = true
            workOrder.items!!.forEach {
                if (it.status == "1" || it.status == "2") { // canceled counts as complete
                    allFinished = false
                }
            }

            if (allFinished && workOrder.status != "3") {
                updateWorkOrderStatus("3")
            }
        }
    }

    override fun uploadImage(_task:Task){

        val images:Array<Image> = if(_task.images == null){
            arrayOf()
        }else{
            _task.images!!
        }

        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"",
            _task.ID,"${_task.task}","","", "")
        myView.findNavController().navigate(directions)
    }


    override fun onTaskCellClickListener(data:Task) {

        println("Cell clicked with task: ${data.task}")
        val images:Array<Image> = if(data.images == null){
            arrayOf()
        }else{
            data.images!!
        }

        data.let {
            val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"", it.ID,"${it.task}","","", "")
            myView.findNavController().navigate(directions)
        }
    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context,statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))


        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            woItem!!.status = item!!.itemId.toString()



            setStatus(woItem!!.status)
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT)
                .show()

            showProgressView()

            var urlString = "https://www.adminmatic.com/cp/app/functions/update/workOrderItemStatus.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")

                        hideProgressView()

                        checkForWoStatus()

                        /* Here 'response' is a String containing the response you received from the website... */
                    } catch (e: JSONException) {
                        println("JSONException")
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { // error

                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = java.util.HashMap()
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["status"] = woItem!!.status
                    params["woID"] = workOrder.woID
                    params["woItemID"] = woItem!!.ID
                    params["empID"] = GlobalVars.loggedInEmployee!!.ID
                    println("params = $params")
                    return params
                }
            }
            postRequest1.tag = "woItem"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
            true
        }


        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun updateWorkOrderStatus(newStatus: String) {
        println("Updating work order status to $newStatus")
        workOrder.status = newStatus

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/update/workOrderStatus.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (listIndex >= 0) {
                        GlobalVars.globalWorkOrdersList?.set(listIndex, workOrder)
                    }
                    hideProgressView()

                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["status"] = workOrder.status
                params["woID"] = workOrder.woID
                params["empID"] = GlobalVars.loggedInEmployee!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "woItem"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "1" -> {
                println("1")
                statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "2" -> {
                println("2")
                statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "3" -> {
                println("3")
                statusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "4" -> {
                println("4")
                statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
        }
    }

    override fun showProgressView() {

        println("showProgressView")

        pgsBar.visibility = View.VISIBLE
        allCL.visibility = View.INVISIBLE

        /*
        estCl.visibility = View.INVISIBLE
        hideCl.visibility = View.INVISIBLE
        taxCl.visibility = View.INVISIBLE
        totalCl.visibility = View.INVISIBLE
        leadTaskBtn.visibility = View.INVISIBLE
        tasksRv.visibility = View.INVISIBLE
        descriptionCl.visibility = View.INVISIBLE
        usageBtn.visibility = View.INVISIBLE
        profitCl.visibility = View.INVISIBLE
        submitBtn.visibility = View.INVISIBLE

         */



    }

    override fun hideProgressView() {
        println("hideProgressView")
        pgsBar.visibility = View.INVISIBLE
        allCL.visibility = View.VISIBLE
    }

    fun setUpViews() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, chargeTypeArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

        chargeSpinner.adapter = adapter


        if (woItem == null){
            println("woItem = null")

            pgsBar.visibility = View.INVISIBLE


            woItemSearch.visibility = View.VISIBLE
            estCl.visibility = View.VISIBLE


            leadTaskBtn.visibility = View.GONE
            profitCl.visibility = View.GONE
            usageBtn.visibility = View.GONE
            tasksRv.visibility = View.GONE
            descriptionCl.visibility = View.GONE

            submitBtn.visibility = View.VISIBLE



        }else{

            // set text, spinner and switch values
            woItemSearch.isIconified = false // Expand it
            woItemSearch.setQuery(woItem!!.item,true)
            woItemSearch.clearFocus()

            estEditTxt.setText(woItem!!.est)
            profitCl.visibility = View.VISIBLE



            if (woItem != null){
                chargeSpinner.setSelection(woItem!!.charge.toInt() - 1)
            }



            if (woItem!!.hideUnits == "1"){
                hideQtySwitch.isChecked = true
            }

            qtyEditTxt.setText(woItem!!.act)

            if (woItem!!.taxType == "1"){
                taxableSwitch.isChecked = true
            }
            priceEditTxt.setText(woItem!!.price)

            totalEditTxt.setText(woItem!!.total)

            if (editMode){
                println("editMode = true")

                // woItemSearch.isEnabled = true
                //woItemSearch.isClickable = true
                setViewAndChildrenEnabled(woItemSearch,true)
                estEditTxt.isEnabled = true
                estEditTxt.isClickable = true
                chargeSpinner.isEnabled = true
                chargeSpinner.isClickable = true
                chargeSpinner.onItemSelectedListener = this@WoItemFragment



                pgsBar.visibility = View.INVISIBLE


                woItemSearch.visibility = View.VISIBLE
                estCl.visibility = View.VISIBLE


                hideCl.visibility = View.VISIBLE
                taxCl.visibility = View.VISIBLE
                totalCl.visibility = View.VISIBLE
                leadTaskBtn.visibility = View.VISIBLE

                profitCl.visibility = View.GONE

                usageBtn.visibility = View.GONE

                submitBtn.visibility = View.VISIBLE


                profitCl.visibility = View.GONE

            }else{
                println("editMode = false")

                //woItemSearch.isEnabled = false
                //woItemSearch.isClickable = false
                setViewAndChildrenEnabled(woItemSearch,false)


                estEditTxt.isEnabled = false
                estEditTxt.isClickable = false
                chargeSpinner.isEnabled = false
                chargeSpinner.isClickable = false
                chargeSpinner.onItemSelectedListener = null




                pgsBar.visibility = View.INVISIBLE


                woItemSearch.visibility = View.VISIBLE
                estCl.visibility = View.VISIBLE



                hideCl.visibility = View.GONE
                taxCl.visibility = View.GONE
                totalCl.visibility = View.GONE
                leadTaskBtn.visibility = View.GONE

                profitCl.visibility = View.VISIBLE

                usageBtn.visibility = View.VISIBLE

                submitBtn.visibility = View.GONE

                //profitCl.visibility = View.GONE
            }

            if (woItem!!.type == "1"){
                //labor
                tasksRv.visibility = View.VISIBLE
                descriptionCl.visibility = View.GONE
            }else{
                //material
                tasksRv.visibility = View.GONE
                descriptionCl.visibility = View.VISIBLE
            }
        }
    }



    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")

        if (woItem != null){
            woItem!!.charge = (position + 1).toString()
        }

    }


    /*
    override fun onDestroy() {
        println("onDestroy")

        val parentFrag: WorkOrderFragment = this@WoItemFragment.getParentFragment() as WorkOrderFragment
        parentFrag.refreshWo()


        super.onDestroy()
    }
*/


    private fun setViewAndChildrenEnabled(
        view: View,
        enabled: Boolean
    ) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }

}