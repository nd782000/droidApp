package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentWoItemBinding
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


    private var _binding: FragmentWoItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWoItemBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.work_order_item)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("woItem = ${woItem!!.item}")

        println("Work Order Item Customer: ${workOrder.customer}")
        binding.woItemUsageBtn.setOnClickListener{
            if (workOrder!!.invoiceID != "0") {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
            }
            else {
                if (woItem != null) {
                    val directions = WoItemFragmentDirections.navigateToUsageEntry(woItem!!, workOrder)
                    myView.findNavController().navigate(directions)
                }
            }

        }
        binding.statusBtn.setOnClickListener{
            showStatusMenu()
        }


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

        binding.priceTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(totalPrice))
        binding.costTv.text = getString(R.string.dollar_sign, woItem!!.totalCost)
        binding.profitTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(profit))
        binding.profitPercentTv.text = profitPercent.toString()
        binding.profitBar.progress = 100 - profitPercent
    }

    override fun getWoItem(taskUpdated: Boolean){
        println("get woItem")


        //if (!pgsBar.isVisible){
            showProgressView()
       // }


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/workOrderItem.php"

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
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        woItem = gson.fromJson(parentObject.toString(), WoItem::class.java)
                        binding.woItemDescriptionTv.text = woItem!!.empDesc

                        setStatus(woItem!!.status)

                        val taskJSON: JSONArray = parentObject.getJSONArray("tasks")
                        val taskList = gson.fromJson(taskJSON.toString(), Array<Task>::class.java)
                            .toMutableList()

                        binding.woItemTasksRv.apply {
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
                            binding.woItemTasksRv.addItemDecoration(itemDecoration)


                            //(adapter as TasksAdapter).notifyDataSetChanged()
                        }
                        fillProfitCl()
                        setUpViews()
                        hideProgressView()
                        if (taskUpdated) {
                            checkForWoStatus()
                        }
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

        val popUp = PopupMenu(myView.context, binding.statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))

        if (workOrder!!.invoiceID != "0") {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
        }
        else {
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                woItem!!.status = item!!.itemId.toString()



                setStatus(woItem!!.status)
                Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT)
                    .show()

                showProgressView()

                var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrderItemStatus.php"

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
                            if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                                globalVars.playSaveSound(myView.context)
                                checkForWoStatus()
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
    }

    private fun updateWorkOrderStatus(newStatus: String) {
        println("Updating work order status to $newStatus")
        workOrder.status = newStatus

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrderStatus.php"

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
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        if (listIndex >= 0) {
                            GlobalVars.globalWorkOrdersList?.set(listIndex, workOrder)
                        }
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
                binding.statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "2" -> {
                println("2")
                binding.statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "3" -> {
                println("3")
                binding.statusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "4" -> {
                println("4")
                binding.statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
        }
    }

    override fun showProgressView() {
        println("showProgressView")
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    override fun hideProgressView() {
        println("hideProgressView")
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    fun setUpViews() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, chargeTypeArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

        binding.woItemChargeSpinner.adapter = adapter


        if (woItem == null){
            println("woItem = null")

            binding.progressBar.visibility = View.INVISIBLE


            binding.woItemSearch.visibility = View.VISIBLE
            binding.woItemEstCl.visibility = View.VISIBLE


            binding.woItemLeadTaskBtn.visibility = View.GONE
            binding.woItemProfitCl.visibility = View.GONE
            binding.woItemUsageBtn.visibility = View.GONE
            binding.woItemTasksRv.visibility = View.GONE
            binding.woItemDescriptionCl.visibility = View.GONE

            binding.woItemSubmitBtn.visibility = View.VISIBLE



        }else{

            // set text, spinner and switch values
            binding.woItemSearch.isIconified = false // Expand it
            binding.woItemSearch.setQuery(woItem!!.item,true)
            binding.woItemSearch.clearFocus()

            binding.woItemEstValEt.setText(woItem!!.est)
            binding.woItemProfitCl.visibility = View.VISIBLE



            if (woItem != null){
                binding.woItemChargeSpinner.setSelection(woItem!!.charge.toInt() - 1)
            }



            if (woItem!!.hideUnits == "1"){
                binding.woItemHideQtySwitch.isChecked = true
            }

            binding.woItemQtyValEt.setText(woItem!!.act)

            if (woItem!!.taxType == "1"){
                binding.woItemTaxableSwitch.isChecked = true
            }
            binding.woItemPriceValEt.setText(woItem!!.price)

            binding.woItemTotalValEt.setText(woItem!!.total)

            if (editMode){
                println("editMode = true")

                // woItemSearch.isEnabled = true
                //woItemSearch.isClickable = true
                setViewAndChildrenEnabled(binding.woItemSearch,true)
                binding.woItemEstValEt.isEnabled = true
                binding.woItemEstValEt.isClickable = true
                binding.woItemChargeSpinner.isEnabled = true
                binding.woItemChargeSpinner.isClickable = true
                binding.woItemChargeSpinner.onItemSelectedListener = this@WoItemFragment



                binding.progressBar.visibility = View.INVISIBLE


                binding.woItemSearch.visibility = View.VISIBLE
                binding.woItemEstCl.visibility = View.VISIBLE


                binding.woItemHideCl.visibility = View.VISIBLE
                binding.woItemTaxCl.visibility = View.VISIBLE
                binding.woItemTotalCl.visibility = View.VISIBLE
                binding.woItemLeadTaskBtn.visibility = View.VISIBLE

                binding.woItemProfitCl.visibility = View.GONE

                binding.woItemUsageBtn.visibility = View.GONE

                binding.woItemSubmitBtn.visibility = View.VISIBLE


                binding.woItemProfitCl.visibility = View.GONE

            }else{
                println("editMode = false")

                //woItemSearch.isEnabled = false
                //woItemSearch.isClickable = false
                setViewAndChildrenEnabled(binding.woItemSearch,false)


                binding.woItemEstValEt.isEnabled = false
                binding.woItemEstValEt.isClickable = false
                binding.woItemChargeSpinner.isEnabled = false
                binding.woItemChargeSpinner.isClickable = false
                binding.woItemChargeSpinner.onItemSelectedListener = null




                binding.progressBar.visibility = View.INVISIBLE


                binding.woItemSearch.visibility = View.VISIBLE
                binding.woItemEstCl.visibility = View.VISIBLE



                binding.woItemHideCl.visibility = View.GONE
                binding.woItemTaxCl.visibility = View.GONE
                binding.woItemTotalCl.visibility = View.GONE
                binding.woItemLeadTaskBtn.visibility = View.GONE

                binding.woItemProfitCl.visibility = View.VISIBLE

                binding.woItemUsageBtn.visibility = View.VISIBLE

                binding.woItemSubmitBtn.visibility = View.GONE

                //profitCl.visibility = View.GONE
            }

            if (woItem!!.type == "1"){
                //labor
                binding.woItemTasksRv.visibility = View.VISIBLE
                binding.woItemDescriptionCl.visibility = View.GONE
            }else{
                //material
                binding.woItemTasksRv.visibility = View.GONE
                binding.woItemDescriptionCl.visibility = View.VISIBLE
            }
        }

        if (GlobalVars.permissions!!.scheduleMoney == "0") {
            binding.woItemProfitCl.visibility = View.GONE
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