package com.example.AdminMatic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
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
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Timer
import kotlin.concurrent.schedule


interface TaskCellClickListener {
    fun onTaskCellClickListener(data:Task)
    fun showProgressView()
    fun hideProgressView()
    fun getWoItem(_newWoStatus:String?)
    fun uploadImage(_task:Task)
}

class WoItemFragment : Fragment(), TaskCellClickListener, ItemCellClickListener, AdapterView.OnItemSelectedListener {

    private  var woItem: WoItem? = null
    lateinit  var workOrder:WorkOrder
    lateinit  var globalVars:GlobalVars
    private var listIndex: Int = -1

    lateinit var myView:View

    private var chargeTypeArray:Array<String> = arrayOf()

    private var editMode = false
    private var editsMade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            woItem = it.getParcelable("woItem")
            workOrder = it.getParcelable("workOrder")!!
            listIndex = it.getInt("listIndex")
        }
        if (woItem == null) {
            editMode = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.wo_item_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.edit)

        if (editMode) {
            item.isEnabled = false
        }
        else {
            item.isEnabled = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.

        when (item.itemId) {
            R.id.edit -> {
                if (!editMode) { // This menu option is always disabled when editmode, but here's a failsafe in case of double clicking etc
                    if (GlobalVars.permissions!!.scheduleEdit == "1") {
                        editMode = true
                        editsMade = false
                        setUpViews()
                    }
                    else {
                        globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_schedule_edit))
                    }
                    return true
                }
            }

        }

        return super.onOptionsItemSelected(item)
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

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if (editMode && editsMade) {
                    println("edits made")
                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        myView.findNavController().navigateUp()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    myView.findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        setHasOptionsMenu(true)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("Work Order Item Customer: ${workOrder.customer}")
        binding.woItemUsageBtn.setOnClickListener{
            if (workOrder.invoiceID != "0") {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
            }
            else {
                if (woItem != null) {
                    val directions = WoItemFragmentDirections.navigateToUsageEntry(woItem!!, workOrder)
                    myView.findNavController().navigate(directions)
                }
            }

        }
        binding.statusBtn.setOnClickListener {
            showStatusMenu()
        }

        binding.woItemEstValEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.woItemEstValEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.woItemEstValEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            println("Est focus change")
            println("item id: ${woItem!!.itemID}")
            println("charge: ${woItem!!.charge}")


            if (!hasFocus) {
                woItem?.est = binding.woItemEstValEt.text.toString()
                if (woItem != null && woItem!!.ID == "0") {
                    if (woItem!!.charge == "1" || woItem!!.charge == "2") {
                        woItem!!.act = woItem!!.est
                        binding.woItemQtyValEt.setText(woItem!!.est)
                        println("act: ${woItem!!.act}")
                        println("price: ${woItem!!.price}")
                        if (woItem!!.act.isNotBlank() && woItem!!.price.isNotBlank()) {
                            woItem!!.total = String.format("%.2f", woItem!!.act.toDouble() * woItem!!.price.toDouble())
                            binding.woItemTotalValEt.setText(woItem!!.total)
                        }
                    }
                }
            }
        }

        binding.woItemQtyValEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.woItemQtyValEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.woItemQtyValEt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                woItem?.act = binding.woItemQtyValEt.text.toString()
                if (woItem!!.act.isNotBlank() && woItem!!.price.isNotBlank()) {
                    woItem!!.total = String.format("%.2f", woItem!!.act.toDouble() * woItem!!.price.toDouble())
                    binding.woItemTotalValEt.setText(woItem!!.total)
                }
            }
        }

        binding.woItemPriceValEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.woItemPriceValEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })

        binding.woItemHideQtySwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (woItem != null) {
                if (isChecked) {
                    woItem!!.hideUnits = "1"
                }
                else {
                    woItem!!.hideUnits = "0"
                }
            }
        }

        binding.woItemTaxableSwitch.setOnCheckedChangeListener { _, isChecked ->
            editsMade = true
            if (woItem != null) {
                if (isChecked) {
                    woItem!!.tax = "1"
                }
                else {
                    woItem!!.tax = "0"
                }
            }
        }

        binding.woItemDescriptionEt.setOnEditorActionListener(MainActivity.DoneOnEditorActionListener())
        binding.woItemDescriptionEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editsMade = true
            }
        })


        binding.addNewTaskBtn.setOnClickListener {
            if (woItem == null || woItem!!.ID == "0") {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.submit_item_first_title), getString(R.string.submit_item_first_body))
                return@setOnClickListener
            }

            navigateToNewTask()

        }

        binding.woItemImageIv.setOnClickListener {

            if (woItem == null || woItem!!.ID == "0") {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.submit_item_first_title), getString(R.string.submit_item_first_images_body))
            }
            else {
                var hasImages = false

                if (!woItem!!.tasks.isNullOrEmpty()) {
                    if (!woItem!!.tasks!![0].images.isNullOrEmpty()) {
                        hasImages = true
                    }
                }

                if (editMode) {
                    if (hasImages) {
                        val task = woItem!!.tasks!![0]
                        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload(
                            "TASK", task.images, workOrder.customer!!, workOrder.custName!!, workOrder.woID, woItem!!.ID, "", "",
                            task.ID, "${task.task}", task.status, "", "", ""
                        )
                        myView.findNavController().navigate(directions)
                    }
                    else {
                        // New task
                        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload(
                            "TASK", arrayOf(), workOrder.customer!!, workOrder.custName!!, workOrder.woID, woItem!!.ID, "", "",
                            "0", binding.woItemDescriptionEt.text.toString(), "1", "", "", ""

                        )
                        myView.findNavController().navigate(directions)
                    }
                }
                else {
                    if (hasImages) {
                        val directions = WoItemFragmentDirections.navigateWoItemToImage(woItem!!.tasks!![0].images!!, 0)
                        myView.findNavController().navigate(directions)
                    }
                    else {
                        globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.edit_to_add_images_title), getString(R.string.edit_to_add_images_body))
                    }
                }

            }

        }

        binding.woItemSubmitBtn.setOnClickListener {
            if (woItem == null) {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.select_an_item_body))
                return@setOnClickListener
            }

            if (!editsMade) {
                val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                builder.setTitle(getString(R.string.no_edits_made))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->

                }
                builder.setNegativeButton(getString(R.string.cancel_editing)) { _, _ ->
                    editMode = false
                    setUpViews()
                }
                builder.show()
                return@setOnClickListener
            }

            if (woItem!!.charge == "2" && binding.woItemQtyValEt.text.isBlank()) {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.set_a_quantity))
                return@setOnClickListener
            }

            if (binding.woItemEstValEt.text.isBlank()) {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.set_estimated_quantity))
                return@setOnClickListener
            }

            updateWoItem()

        }

        getItems()

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


        woItem!!.usage!!.forEach {
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

    override fun getWoItem(_newWoStatus: String?){
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

                        if (woItem!!.tasks != null) {
                            if (woItem!!.tasks!!.isNotEmpty()) {
                                binding.woItemDescriptionEt.setText(woItem!!.tasks!![0].task)
                            }
                        }


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

                        if (woItem != null) {
                            if (!woItem!!.tasks.isNullOrEmpty()) {
                                println("Tasks wasn't null!")
                                if (!woItem!!.tasks!![0].images.isNullOrEmpty()) {
                                    println("Images wasn't null!")
                                    if (woItem!!.tasks!![0].images!!.size > 1) {
                                        binding.woItemImageCountLabelTv.text = getString(R.string.plus_x, woItem!!.tasks!![0].images!!.size - 1)
                                    }

                                    Picasso.with(context)
                                        .load(GlobalVars.thumbBase + woItem!!.tasks!![0].images!![0].fileName)
                                        .placeholder(R.drawable.ic_images)
                                        .into(binding.woItemImageIv)

                                }
                            }
                        }

                        fillProfitCl()
                        setUpViews()
                        hideProgressView()
                        if (_newWoStatus != null) {
                            if (_newWoStatus != workOrder.status) {
                                updateWorkOrderStatus(_newWoStatus)
                            }
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

    override fun uploadImage(_task:Task){

        val images:Array<Image> = if(_task.images == null){
            arrayOf()
        }else{
            _task.images!!
        }

        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"","",
            _task.ID,"${_task.task}", _task.status,"","", "")
        myView.findNavController().navigate(directions)
    }


    override fun onTaskCellClickListener(data:Task) {

        println("Cell clicked with task: ${data.task}")
        val images:Array<Image> = if(data.images == null){
            arrayOf()
        }
        else {
            data.images!!
        }

        data.let {
            val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"","", it.ID,"${it.task}", data.status,"","", "")
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

        if (workOrder.invoiceID != "0") {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
        }
        else {
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                if (woItem == null) {
                    globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.submit_item_first_title), getString(R.string.submit_item_first_status_body))
                    return@setOnMenuItemClickListener true
                }

                woItem!!.status = item!!.itemId.toString()

                setStatus(woItem!!.status)

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
                            println("parentObject update work order item = $parentObject")
                            if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                                globalVars.playSaveSound(myView.context)

                                val gson = GsonBuilder().create()
                                val newWoStatus: String = gson.fromJson(parentObject["newWoStatus"].toString(), String::class.java)

                                if (newWoStatus != workOrder.status) {
                                    updateWorkOrderStatus(newWoStatus)
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

    @SuppressLint("SetTextI18n")
    private fun updateWoItem() {

        showProgressView()

        if (binding.woItemEstValEt.text.toString() == "") {
            binding.woItemEstValEt.setText("0.00")
        }

        var materialDescription = ""
        if (woItem!!.type != "1") {
            materialDescription = binding.woItemDescriptionEt.text.toString()
        }

        val wasNewWoItem = woItem!!.ID == "0"


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrderItem.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Submit Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        val gson = GsonBuilder().create()
                        val newItemID: String = gson.fromJson(parentObject["itemID"].toString(), String::class.java)
                        woItem!!.ID = newItemID

                        editsMade = false
                        globalVars.playSaveSound(myView.context)

                        if (wasNewWoItem) {

                            editMode = false
                            setUpViews()

                            val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                            builder.setTitle(getString(R.string.add_task_or_description))
                            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                                navigateToNewTask()
                            }
                            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                                myView.findNavController().navigateUp()
                            }
                            builder.show()
                        }
                        else {
                            myView.findNavController().navigateUp()
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
                params["workOrderItemID"] = woItem!!.ID
                params["workOrderID"] = workOrder.woID
                params["itemID"] = woItem!!.itemID
                params["type"] = woItem!!.type
                params["subcontractor"] = woItem!!.subcontractor
                params["taxType"] = woItem!!.taxType
                params["chargeType"] = woItem!!.charge
                params["createdBy"] = GlobalVars.loggedInEmployee!!.ID
                params["hideUnits"] = woItem!!.hideUnits
                params["est"] = woItem!!.est
                params["act"] = woItem!!.act
                params["price"] = woItem!!.price
                params["total"] = woItem!!.total
                params["item"] = woItem!!.item
                params["status"] = woItem!!.status
                params["materialDescription"] = materialDescription
                
                println("submit params = $params")
                return params

            }
        }
        postRequest1.tag = "woItem"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun navigateToNewTask() {
        var taskStatus = "1"
        if (woItem?.tasks!!.isNotEmpty()) {
            taskStatus = woItem?.tasks!![0].status
        }

        if (GlobalVars.permissions!!.scheduleEdit == "1") {
            val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",arrayOf(),workOrder.customer!!,workOrder.custName!!,workOrder.woID,woItem!!.ID,"","", "","", taskStatus,"","", "")
            myView.findNavController().navigate(directions)
        }
        else {
            globalVars.simpleAlert(myView.context, getString(R.string.access_denied), getString(R.string.no_permission_schedule_edit))
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
        when (status) {
            "0" -> {
                println("1")
                Picasso.with(context)
                    .load(R.drawable.ic_not_started)
                    .into(binding.statusIv)
                binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusGray))
                binding.statusTv.text = getString(R.string.not_started)
            }
            "1" -> {
                println("1")
                Picasso.with(context)
                    .load(R.drawable.ic_not_started)
                    .into(binding.statusIv)
                binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusGray))
                binding.statusTv.text = getString(R.string.not_started)
            }
            "2" -> {
                println("2")
                Picasso.with(context)
                    .load(R.drawable.ic_in_progress)
                    .into(binding.statusIv)
                binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusOrange))
                binding.statusTv.text = getString(R.string.in_progress)
            }
            "3" -> {
                println("3")
                Picasso.with(context)
                    .load(R.drawable.ic_done)
                    .into(binding.statusIv)
                binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusGreen))
                binding.statusTv.text = getString(R.string.finished)
            }
            "4" -> {
                println("4")
                Picasso.with(context)
                    .load(R.drawable.ic_canceled)
                    .into(binding.statusIv)
                binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusRed))
                binding.statusTv.text = getString(R.string.canceled)
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

        // To prevent the methods filling out this information from flagging edits when the user didn't edit
        val editsMadeBefore = editsMade

        chargeTypeArray = arrayOf(getString(R.string.wo_charge_nc), getString(R.string.wo_charge_fl), getString(R.string.wo_charge_tm))

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item, chargeTypeArray

        )
        adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

        binding.woItemChargeSpinner.adapter = adapter

        if (woItem == null) {
            println("woItem = null")

            binding.progressBar.visibility = View.INVISIBLE


            binding.woItemSearch.visibility = View.VISIBLE
            binding.woItemEstCl.visibility = View.VISIBLE


            binding.addNewTaskBtn.visibility = View.GONE
            binding.woItemProfitCl.visibility = View.GONE
            binding.woItemUsageBtn.visibility = View.GONE
            binding.woItemTasksRv.visibility = View.GONE
            binding.woItemDescriptionCl.visibility = View.GONE
            binding.woItemImageCl.visibility = View.GONE

            binding.woItemSubmitBtn.visibility = View.VISIBLE

            binding.usageQuantityCl.visibility = View.GONE


            // Disable all fields until an item is selected
            binding.woItemEstValEt.isEnabled = false
            binding.woItemHideQtySwitch.isEnabled = false
            binding.woItemTaxableSwitch.isEnabled = false
            binding.woItemChargeSpinner.isEnabled = false
            binding.woItemQtyValEt.isEnabled = false
            binding.woItemPriceValEt.isEnabled = false


        }
        else {

            binding.woItemEstValEt.isEnabled = true
            binding.woItemHideQtySwitch.isEnabled = true
            binding.woItemTaxableSwitch.isEnabled = true
            binding.woItemChargeSpinner.isEnabled = true
            binding.woItemQtyValEt.isEnabled = true
            binding.woItemPriceValEt.isEnabled = true

            if (woItem!!.ID == "0") {
                binding.usageQuantityCl.visibility = View.GONE
            }
            else {
                binding.usageQuantityCl.visibility = View.VISIBLE
            }

            // set text, spinner and switch values
            binding.woItemSearch.isIconified = false // Expand it
            binding.woItemSearch.setQuery(woItem!!.item,true)
            binding.woItemSearch.clearFocus()

            binding.woItemEstValEt.setText(woItem!!.est)
            binding.woItemProfitCl.visibility = View.VISIBLE

            binding.usageQtyTv.text = woItem!!.usageQty
            binding.remainingQtyTv.text = woItem!!.remaining

            if (woItem!!.usageQty.isNotBlank() && woItem!!.est.isNotBlank()) {
                if (woItem!!.usageQty.toDouble() > woItem!!.est.toDouble()) {
                    binding.usageQtyTv.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                    binding.remainingQtyTv.setTextColor(ContextCompat.getColor(myView.context, R.color.red))
                }
            }

            if (woItem != null){
                binding.woItemChargeSpinner.setSelection(woItem!!.charge.toInt() - 1)
            }


            if (woItem!!.hideUnits == "1") {
                binding.woItemHideQtySwitch.isChecked = true
            }

            binding.woItemQtyValEt.setText(woItem!!.act)

            if (woItem!!.taxType == "1") {
                binding.woItemTaxableSwitch.isChecked = true
            }
            binding.woItemPriceValEt.setText(woItem!!.price)

            binding.woItemTotalValEt.setText(woItem!!.total)

            if (editMode) {
                println("editMode = true")

                // woItemSearch.isEnabled = true
                //woItemSearch.isClickable = true

                if (woItem!!.ID == "0") {
                    setViewAndChildrenEnabled(binding.woItemSearch, true)
                }

                binding.woItemEstValEt.isEnabled = true
                binding.woItemEstValEt.isClickable = true
                binding.woItemChargeSpinner.isEnabled = true
                binding.woItemChargeSpinner.isClickable = true
                binding.woItemChargeSpinner.onItemSelectedListener = this@WoItemFragment
                binding.woItemDescriptionEt.isEnabled = true

                binding.progressBar.visibility = View.INVISIBLE

                binding.woItemSearch.visibility = View.VISIBLE
                binding.woItemEstCl.visibility = View.VISIBLE

                binding.woItemHideCl.visibility = View.VISIBLE
                binding.woItemTaxCl.visibility = View.VISIBLE
                binding.woItemTotalCl.visibility = View.VISIBLE
                binding.addNewTaskBtn.visibility = View.VISIBLE

                binding.woItemProfitCl.visibility = View.GONE

                binding.woItemUsageBtn.visibility = View.GONE

                binding.woItemSubmitBtn.visibility = View.VISIBLE


                binding.woItemProfitCl.visibility = View.GONE

            }
            else {
                println("editMode = false")

                //woItemSearch.isEnabled = false
                //woItemSearch.isClickable = false

                setViewAndChildrenEnabled(binding.woItemSearch,false)


                binding.woItemEstValEt.isEnabled = false
                binding.woItemEstValEt.isClickable = false
                binding.woItemChargeSpinner.isEnabled = false
                binding.woItemChargeSpinner.isClickable = false
                binding.woItemChargeSpinner.onItemSelectedListener = null
                binding.woItemDescriptionEt.isEnabled = false




                binding.progressBar.visibility = View.INVISIBLE


                binding.woItemSearch.visibility = View.VISIBLE
                binding.woItemEstCl.visibility = View.VISIBLE



                binding.woItemHideCl.visibility = View.GONE
                binding.woItemTaxCl.visibility = View.GONE
                binding.woItemTotalCl.visibility = View.GONE
                binding.addNewTaskBtn.visibility = View.GONE

                binding.woItemProfitCl.visibility = View.VISIBLE

                binding.woItemUsageBtn.visibility = View.VISIBLE

                binding.woItemSubmitBtn.visibility = View.GONE

                //profitCl.visibility = View.GONE
            }

            if (woItem!!.type == "1"){
                //labor
                binding.woItemTasksRv.visibility = View.VISIBLE
                binding.woItemDescriptionCl.visibility = View.GONE
                binding.woItemImageCl.visibility = View.GONE
                binding.addNewTaskBtn.visibility = View.VISIBLE
            }
            else {
                //material
                binding.woItemTasksRv.visibility = View.GONE
                binding.woItemDescriptionCl.visibility = View.VISIBLE
                binding.woItemImageCl.visibility = View.VISIBLE
                binding.addNewTaskBtn.visibility = View.GONE
            }
        }

        if (GlobalVars.permissions!!.scheduleMoney == "0") {
            binding.woItemProfitCl.visibility = View.GONE
        }

        editsMade = editsMadeBefore
    }

    private fun getItems() {
        println("getItems")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/items.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Get items response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val items: JSONArray = parentObject.getJSONArray("items")
                        println("items = $items")
                        println("items count = ${items.length()}")

                        val gson = GsonBuilder().create()
                        val itemsList = gson.fromJson(items.toString(), Array<Item>::class.java).toMutableList()

                        binding.woItemSearchResultsRv.apply {
                            layoutManager = LinearLayoutManager(activity)

                            adapter = activity?.let {
                                ItemsAdapter(itemsList.toMutableList(), myView.context, this@WoItemFragment, true)
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            binding.woItemSearchResultsRv.addItemDecoration(itemDecoration)

                            binding.woItemSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    binding.woItemSearchResultsRv.visibility = View.INVISIBLE
                                    myView.hideKeyboard()
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    (adapter as ItemsAdapter).filter.filter(newText)
                                    if (newText == "") {
                                        binding.woItemSearchResultsRv.visibility = View.INVISIBLE
                                    }
                                    else {
                                        binding.woItemSearchResultsRv.visibility = View.VISIBLE
                                    }
                                    return false
                                }

                            })

                            val closeButton: View? = binding.woItemSearch.findViewById(androidx.appcompat.R.id.search_close_btn)

                            closeButton?.setOnClickListener {
                                binding.woItemSearch.setQuery("", false)
                                myView.hideKeyboard()
                                binding.woItemSearch.clearFocus()
                                binding.woItemSearchResultsRv.visibility = View.INVISIBLE
                                woItem = null
                                editsMade = false
                                setUpViews()
                            }

                            binding.woItemSearch.setOnQueryTextFocusChangeListener { _, isFocused ->
                                if (!isFocused) {
                                    binding.woItemSearch.setQuery(woItem!!.item, false)
                                    binding.woItemSearchResultsRv.visibility = View.INVISIBLE
                                }
                            }
                        }

                        if (woItem == null) {
                            setUpViews()
                            setStatus("1")
                            hideProgressView()
                        }
                        else {
                            getWoItem(null)
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
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "woItem"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (woItem != null) {
            woItem!!.charge = (position + 1).toString()
            if (woItem!!.charge == "3") {
                binding.woItemQtyValEt.isEnabled = false
                binding.woItemQtyValEt.setText("0.00")
                woItem?.act = "0.00"
                binding.woItemTotalValEt.setText("0.00")
                woItem?.total = "0.00"
            }
            else {
                binding.woItemQtyValEt.isEnabled = true
            }
        }
    }


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

    override fun onItemCellClickListener(data: Item) {
        println("Selected ${data.name}")
        if (woItem == null) {

            woItem = WoItem("0", data.name, "", data.ID)
            woItem!!.charge = workOrder.charge!!
            woItem!!.woID = workOrder.woID
            woItem!!.usageQty = "0.00"
            woItem!!.price = data.price!!
            woItem!!.total = "0.00"
            woItem!!.type = data.type!!
            woItem!!.tax = data.tax
            woItem!!.subcontractor = data.subcontractor
            woItem!!.hideUnits = "0"

        }
        else {
            woItem!!.itemID = data.ID
            woItem!!.item = data.name
            woItem!!.type = data.type!!
            woItem!!.price = data.price!!
            woItem!!.tax = data.tax
            woItem!!.subcontractor = data.subcontractor

            if (woItem!!.est == "0" || woItem!!.est == "0.00" || woItem!!.est == "" || woItem!!.price == "0" || woItem!!.price == "0.00" || woItem!!.price == "") {
                woItem!!.total = "0.00"
            }
            else {
                woItem!!.total = String.format("%.2f", woItem!!.act.toDouble() * woItem!!.price.toDouble())
            }

            binding.woItemTotalValEt.setText(woItem!!.total)

        }

        if (woItem!!.charge == "1") {
            woItem!!.price = "0.00"
            //binding.woItemPriceValEt.setText(woItem!!.price)
            woItem!!.total = "0.00"
            //binding.woItemTotalValEt.setText(woItem!!.total)
        }
        //else {
        //    binding.woItemTotalValEt.setText(data.price)
        //}

        //if (data.tax == "1") {
        //    binding.woItemTaxableSwitch.isChecked = true
        //}
        //else {
        //    binding.woItemTaxableSwitch.isChecked = false
        //}

        //binding.woItemChargeSpinner.setSelection(woItem!!.charge.toInt() - 1)

        //binding.woItemSearch.setQuery(woItem!!.item, false)
        binding.woItemSearch.clearFocus()
        myView.hideKeyboard()

        editsMade = true

        setUpViews()

    }

}