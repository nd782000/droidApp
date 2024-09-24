package com.example.AdminMatic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentWorkOrderBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime


interface WoItemCellClickListener {
    fun onWoItemCellClickListener(data:WoItem)
    fun onWoItemQuickComplete(data:WoItem)
    fun onAddNewItemClickListener()
}

class WorkOrderFragment : Fragment(), StackDelegate, WoItemCellClickListener{
    private var listIndex: Int = -1
    private var workOrderID: String = ""
    private  var workOrder: WorkOrder? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    //lateinit var context: AppCompatActivity

    private lateinit var  stackFragment: StackFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workOrder = it.getParcelable("workOrder")
            workOrderID = it.getString("workOrderID")!!
            listIndex = it.getInt("listIndex")

        }
    }

    private var _binding: FragmentWorkOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()

        _binding = FragmentWorkOrderBinding.inflate(inflater, container, false)
        myView = binding.root

        println("Work Order: $workOrder")
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return myView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("WorkOrder View")
        getWorkOrder()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.work_order_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_work_order_item) {
            if (GlobalVars.permissions!!.scheduleEdit == "1") {
                val directions = WorkOrderFragmentDirections.navigateToNewEditWorkOrder(workOrder)
                myView.findNavController().navigate(directions)
            }
            else if (workOrder!!.invoiceID != "0") {
                globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
            }
            else {
                com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule_edit))
            }
            return true
        }
        else if (id == R.id.planned_dates_item) {
            if (GlobalVars.permissions!!.scheduleEdit == "1") {
                val directions = WorkOrderFragmentDirections.navigateToPlannedDates(workOrder, null, null)
                myView.findNavController().navigate(directions)
            }
            else {
                com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule_edit))
            }
            return true
        }
        else if (id == R.id.invoice_work_order_item) {
            if (GlobalVars.permissions!!.scheduleEdit == "1" && GlobalVars.permissions!!.invoices == "1") {
                createInvoice()
            }
            else {
                com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_create_invoice))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("workOrder")
    }

    private fun getWorkOrder(){
        println("getWorkOrder")
        //println("DEPARTMENT BEFORE GET/WORKORDER ${workOrder!!.department}")
        //println("CREW BEFORE GET/WORKORDER ${workOrder!!.crew}")
        var woID = workOrderID
        if (workOrder != null) {
            woID = workOrder!!.woID
        }

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/work.php"
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


                        //workOrder = gson.fromJson(parentObject.toString(), WorkOrder::class.java)
                        val woObject:JSONObject = parentObject.getJSONObject("workOrder")
                        workOrder = gson.fromJson(woObject.toString(), WorkOrder::class.java)
                        println("Total: ${workOrder!!.total}")

                        setStatusIcon(workOrder!!.status)

                        if (workOrder!!.titleTranslated == null) {
                            binding.titleValTv.text = workOrder!!.title
                        }
                        else {
                            binding.titleValTv.text = workOrder!!.titleTranslated
                        }


                        if (!workOrder!!.nextPlannedDate.isNullOrBlank()) {
                            binding.scheduleValTv.text = workOrder!!.nextPlannedDate
                        }
                        else {
                            binding.scheduleValTv.text = getString(R.string.not_scheduled)
                        }
                        /*
                    if(workOrder!!.department != null){
                        deptTxt.text = workOrder!!.department!!
                    }

                     */
                        if (!workOrder!!.crewName.isNullOrBlank()) {
                            binding.crewValTv.text = workOrder!!.crewName!!
                        }
                        else {
                            binding.crewValTv.text = getString(R.string.no_crew)
                        }

                        if (!workOrder!!.salesRepName.isNullOrBlank()) {
                            binding.repValTv.text = workOrder!!.salesRepName!!
                        }
                        else {
                            binding.repValTv.text = getString(R.string.no_sales_rep)
                        }

                        println("Charge Name: ${workOrder!!.chargeName}")
                        when (workOrder!!.charge) {
                            "1" -> {
                                workOrder!!.chargeName = getString(R.string.wo_charge_nc)
                            }
                            "2" -> {
                                workOrder!!.chargeName = getString(R.string.wo_charge_fl)
                            }
                            "3" -> {
                                workOrder!!.chargeName = getString(R.string.wo_charge_tm)
                            }
                            else -> {
                                workOrder!!.chargeName = ""
                            }
                        }
                        binding.chargeValTv.text = workOrder!!.chargeName

                        if (workOrder?.urgent == "1") {
                            binding.urgentIv.visibility = View.VISIBLE
                            binding.urgentTv.visibility = View.VISIBLE
                        }
                        else {
                            binding.urgentIv.visibility = View.GONE
                            binding.urgentTv.visibility = View.GONE
                        }


                        binding.priceTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(workOrder!!.total.toFloat()))
                        binding.costTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(workOrder!!.totalCost.toFloat()))
                        binding.profitTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(workOrder!!.profitAmount.toFloat()))
                        binding.profitPercentTv.text = workOrder!!.profit
                        binding.profitBar.progress = 100 - workOrder!!.profit.toFloat().toInt()

                        /*
                        val profit: Float = workOrder!!.total.toFloat() - workOrder!!.totalCost.toFloat()
                        val profitPercent: Int = (profit / workOrder!!.total.toFloat() * 100).toInt()

                        binding.priceTv.text = workOrder!!.total
                        binding.costTv.text = workOrder!!.totalCost
                        binding.profitTv.text = getString(R.string.dollar_sign, GlobalVars.moneyFormatter.format(profit))
                        binding.profitPercentTv.text = profitPercent.toString()
                        binding.profitBar.progress = 100 - profitPercent

                         */


                        //val woItemJSON: JSONArray = parentObject.getJSONArray("items")
                        //val itemList = gson.fromJson(woItemJSON.toString(), Array<WoItem>::class.java).toMutableList()
                        //println("woItemJSON $woItemJSON")


                        val itemList = workOrder!!.items

                        itemList?.forEach {
                            it.woID = workOrder!!.woID
                        }

                        binding.workOrderItemsRv.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = activity?.let {
                                WoItemsAdapter(
                                    itemList!!.toMutableList(),
                                    context,
                                    requireActivity().application,
                                    workOrder!!,
                                    this@WorkOrderFragment
                                )
                            }

                            val itemDecoration: RecyclerView.ItemDecoration =
                                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                            binding.workOrderItemsRv.addItemDecoration(itemDecoration)
                            //(adapter as WoItemsAdapter).notifyDataSetChanged()
                        }



                        stackFragment = StackFragment(2, workOrder!!.woID, this)

                        val ft = childFragmentManager.beginTransaction()
                        ft.add(R.id.work_order_cl, stackFragment, "stackFrag")
                        ft.commitAllowingStateLoss()

                        binding.statusBtn.setOnClickListener {
                            println("status btn clicked")
                            showStatusMenu()
                        }

                        binding.customerBtn.setOnClickListener {
                            println("customer btn clicked")
                            if (GlobalVars.permissions!!.customers == "1") {
                                val customer = Customer(workOrder!!.customer!!)
                                val directions = WorkOrderFragmentDirections.navigateWorkOrderToCustomer(customer.ID)
                                myView.findNavController().navigate(directions)
                            }
                            else {
                                com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers))
                            }
                        }

                        //customerBtn.text = "${workOrder!!.custName} ${workOrder!!.custAddress}"
                        binding.customerTv.text = getString(R.string.customer_button, workOrder!!.custName, workOrder!!.custAddress)

                        binding.directionsBtn.setOnClickListener {
                            val lng: String
                            val lat: String
                            if(workOrder!!.lng != null && workOrder!!.lat != null){
                                lng = workOrder!!.lng!!
                                lat = workOrder!!.lat!!
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q="+lat+","+lng+" (" + workOrder!!.custName + ")")
                                )
                                startActivity(intent)
                            }
                        }

                        binding.callBtn.setOnClickListener {

                            if (GlobalVars.permissions!!.customers == "1") {
                                if (!workOrder!!.mainPhone.isNullOrBlank()) {
                                    val popUp = PopupMenu(myView.context, binding.callBtn)
                                    popUp.inflate(R.menu.task_status_menu)
                                    popUp.menu.add(0, 1, 1, getString(R.string.call_x, workOrder!!.mainPhone!!))
                                    popUp.menu.add(0, 2, 1, getString(R.string.text_x, workOrder!!.mainPhone!!))
                                    popUp.menu.add(0, 3, 1, getString(R.string.all_contacts))

                                    popUp.setOnMenuItemClickListener { item: MenuItem? ->
                                        when (item?.itemId) {
                                            1 -> {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + workOrder?.mainPhone!!))
                                                com.example.AdminMatic.myView.context.startActivity(intent)
                                            }
                                            2 -> {
                                                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + workOrder?.mainPhone!!))
                                                smsIntent.putExtra("sms_body", "")
                                                com.example.AdminMatic.myView.context.startActivity(smsIntent)
                                            }
                                            3 -> {
                                                val tempCustomer = Customer(workOrder!!.customer!!)
                                                val directions = WorkOrderFragmentDirections.navigateWorkOrderToContacts(tempCustomer)
                                                myView.findNavController().navigate(directions)
                                            }
                                        }
                                        true
                                    }
                                    popUp.gravity = Gravity.START
                                    popUp.show()

                                }
                                else {
                                    com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.no_phone_found))
                                }
                            }
                            else {
                                com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers))
                            }



                        }


                        println("lat: ${workOrder!!.lat}")
                        println("lng: ${workOrder!!.lng}")
                        //scheduleTxt.text = workOrder!!.dateNice
                        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
                            getString(R.string.work_order_number, workOrder!!.woID)

                        workOrder!!.setEmps()

                        if (GlobalVars.permissions!!.scheduleMoney == "0") {
                            binding.workOrderFooterCl.visibility = View.GONE
                        }

                        if (listIndex >= 0) {
                            println("Setting this work order to list index $listIndex in the global list")
                            GlobalVars.globalWorkOrdersList?.set(listIndex, workOrder!!)
                        }

                        binding.addNoteBtn.setOnClickListener {
                            val builder = AlertDialog.Builder(myView.context)
                            builder.setTitle(getString(R.string.note_label))

                            val input = EditText(myView.context)


                            val container = LinearLayout(myView.context)
                            container.orientation = LinearLayout.VERTICAL
                            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                            lp.setMargins(globalVars.dpToPx(24), globalVars.dpToPx(12), globalVars.dpToPx(24), 0)
                            input.layoutParams = lp
                            input.setBackgroundResource(R.drawable.text_view_layout)
                            //input.gravity = Gravity.TOP or Gravity.LEFT
                            input.inputType = InputType.TYPE_CLASS_TEXT
                            //input.setLines(1)
                            //input.maxLines = 1
                            container.addView(input, lp)

                            builder.setView(container)

                            builder.setPositiveButton(getString(R.string.submit)) { _, _ ->
                                addNote(input.text.toString())
                            }

                            builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                                dialog.cancel()
                            }

                            builder.show()
                        }

                    }

                    hideProgressView()

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
                params["woID"] = woID
                params["view"] = "main"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "workOrder"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun addNote(newNote:String) {
        println("getWorkOrder")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrderNote.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Update Note Response $response")
                try {

                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().setLenient().create()
                        //val newString = gson.fromJson(parentObject["notes"].toString(), String::class.java)
                        println("New Notes: ${parentObject["notes"]}")
                        workOrder?.notes = parentObject["notes"].toString()
                        globalVars.playSaveSound(com.example.AdminMatic.myView.context)

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
                params["woID"] = workOrder!!.woID
                params["note"] = newNote
                return params
            }
        }
        postRequest1.tag = "workOrder"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun showStatusMenu() {
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        if (workOrder!!.skipped == null || workOrder!!.skipped != "1") {
            popUp.menu.add(0, 4, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.skip_visit)))
        }

        if (workOrder!!.invoiceID != "0") {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
        }
        else {

            popUp.setOnMenuItemClickListener { item: MenuItem? ->


                if (item!!.itemId == 4) { //skipped

                    val builder: AlertDialog.Builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.dialogue_wo_reason_for_skip))


                    //Todo: make this use the background resource with correct margins
                    val et = EditText(myView.context)
                    //et.setBackgroundResource(R.drawable.text_view_layout)
                    //et.setPadding(5,0,5,0)

                    et.inputType = InputType.TYPE_CLASS_TEXT
                    builder.setView(et)

                    // Set up the buttons
                    builder.setPositiveButton("OK") { _, _ ->
                        // Here you get get input text from the Edittext
                        workOrder!!.status = item.itemId.toString()
                        workOrder!!.notes = et.text.toString()
                        workOrder!!.skipped = "1"

                        if (listIndex >= 0) {
                            GlobalVars.globalWorkOrdersList?.set(listIndex, workOrder!!)
                        }

                        setStatusIcon(workOrder!!.status)
                        workOrder!!.skipped = "1"
                        updateStatus(false)

                    }
                    builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

                    builder.show()

                } else {
                    workOrder!!.status = item.itemId.toString()

                    if (listIndex >= 0) {
                        GlobalVars.globalWorkOrdersList?.set(listIndex, workOrder!!)
                    }

                    setStatusIcon(workOrder!!.status)
                    updateStatus(false)
                }





                true
            }
            popUp.gravity = Gravity.START
            popUp.show()
        }
    }


    private fun updateStatus(refreshWO: Boolean) {
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrderStatus.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(com.example.AdminMatic.myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        globalVars.playSaveSound(com.example.AdminMatic.myView.context)
                    }

                    //setFragmentResult("refresh", bundleOf("refresh" to true))

                    if (refreshWO) {
                        getWorkOrder()
                    }
                    else {
                        hideProgressView()
                        var foundUsage = false
                        workOrder!!.items?.forEach {
                            if (it.usageQty != "" && it.usageQty != "0.00") {
                                foundUsage = true
                            }
                        }

                        if (!foundUsage && workOrder!!.status == "3") { // finished
                            println("DID NOT FIND USAGE")

                            val dateNow = LocalDate.now()
                            val newDateValue = LocalDateTime.of(dateNow.year, dateNow.month, dateNow.dayOfMonth, 0, 0)
                            setEndDate(newDateValue.format(GlobalVars.dateFormatterPHP))

                            /*
                            val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                            builder.setTitle(getString(R.string.work_order_set_end_to_today))

                            val dateNow = LocalDate.now()
                            val newDateValue = LocalDateTime.of(dateNow.year, dateNow.month, dateNow.dayOfMonth, 0, 0)

                            builder.setPositiveButton(android.R.string.ok) { _, _ ->


                                setEndDate(newDateValue.format(GlobalVars.dateFormatterPHP))

                            }

                            builder.setNegativeButton(getString(R.string.select_a_different_day)) { _, _ ->
                                val datePicker = DatePickerHelper(com.example.AdminMatic.myView.context, true)
                                datePicker.showDialog(newDateValue.year, newDateValue.monthValue-1, newDateValue.dayOfMonth, object : DatePickerHelper.Callback {
                                    override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                                        val selectedDate = LocalDateTime.of(year, month+1, dayOfMonth, 0, 0)
                                        setEndDate(selectedDate.format(GlobalVars.dateFormatterPHP))
                                    }
                                })
                            }

                            builder.show()

                             */

                        }

                    }




                    globalVars.updateGlobalMySchedule(workOrder!!.woID, MyScheduleEntryType.workOrder, workOrder!!.status)



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
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["status"] = workOrder!!.status
                params["woID"] = workOrder!!.woID
                params["empID"] = GlobalVars.loggedInEmployee!!.ID
                params["empName"] = GlobalVars.loggedInEmployee!!.name
                params["skipped"] = workOrder!!.skipped.toString()
                params["notes"] = workOrder!!.notes.toString()
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    private fun setEndDate(dateString: String) {
        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/workOrderEnd.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(com.example.AdminMatic.myView.context)


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {
                        com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.work_order_end_date_title),getString(R.string.work_order_end_date_body))
                    }

                    hideProgressView()

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
                params["woID"] = workOrder!!.woID
                params["endTime"] = dateString
                println("params = $params")
                return params
            }
        }
        queue.add(postRequest1)
    }

    private fun createInvoice() {
        if (workOrder!!.status != "3") {
            com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.invoice_not_done))
            return
        }
        else if (workOrder!!.invoiceType != "1") {
            com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.invoice_type_wrong))
            return
        }
        else if (workOrder!!.total == "0" || workOrder!!.total == "0.00") {
            com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.invoice_no_total))
            return
        }


        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/new/invoice.php"

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
                        val newInvoiceID: String = gson.fromJson(parentObject["invoiceID"].toString(), String::class.java)
                        globalVars.playSaveSound(myView.context)
                        val directions = WorkOrderFragmentDirections.navigateWorkOrderToInvoice(null)
                        directions.invoiceID = newInvoiceID
                        myView.findNavController().navigate(directions)
                    }
                    hideProgressView()


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

                params["woID"] = workOrder!!.woID
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "workOrder"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)


    }



    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")

        val directions = WorkOrderFragmentDirections.navigateWorkOrderToLead(_lead.ID)
        myView.findNavController().navigate(directions)
    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
        val directions = WorkOrderFragmentDirections.navigateWorkOrderToContract(_contract.ID)
        myView.findNavController().navigate(directions)
    }

    override fun newWorkOrderView(_workOrder: WorkOrder) {
        println("newWorkOrderView $_workOrder")
        workOrder = _workOrder
        getWorkOrder()
    }

    override fun newInvoiceView(_invoice: Invoice) {
        println("newInvoiceView ${_invoice.ID}")
        val directions = WorkOrderFragmentDirections.navigateWorkOrderToInvoice(_invoice)
        myView.findNavController().navigate(directions)
    }

    override fun onWoItemCellClickListener(data:WoItem) {

        println("Cell clicked with woItem: ${data.item}")



        data.let {

            //var woItemFragment: WoItemFragment
            //val SIMPLE_FRAGMENT_TAG = "myfragmenttag"

            //myView.findNavController().navigate()
            // ft.add(R.id.container_all, frag as Fragment).commit()


            val directions = WorkOrderFragmentDirections.navigateToWoItem(it, workOrder!!)
            directions.listIndex = listIndex
            myView.findNavController().navigate(directions)


        }
    }

    override fun onAddNewItemClickListener() {
        println("Add New Woitem")

        if (GlobalVars.permissions!!.scheduleEdit == "1") {
            val directions = WorkOrderFragmentDirections.navigateToWoItem(null, workOrder!!)
            directions.listIndex = listIndex
            myView.findNavController().navigate(directions)
        }
        else {
            com.example.AdminMatic.globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule_edit))
        }

    }

    override fun onWoItemQuickComplete(data:WoItem) {
        println("onWoItemQuickComplete")
        println("woitem type: ${data.type}")

        if (data.est == "0.0") {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.no_estimated_value_title), getString(R.string.no_estimated_value_title))
            return
        }

        if (data.type == "1" && workOrder!!.emps.isEmpty()) {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.no_emps_on_wo_title), getString(R.string.no_emps_on_wo_body))
            return
        }

        if (workOrder!!.invoiceID != "0") {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.invoiced_wo_cant_edit))
            return
        }

        if (data.status == "3" || data.status == "4") {
            globalVars.simpleAlert(com.example.AdminMatic.myView.context, getString(R.string.dialogue_error), getString(R.string.quick_complete_already_complete))
            return
        }

        var alertStringEmps = ""
        var dividedUsage = 0.0
        var alertString = ""

        if (data.type == "1") { // labor type
            workOrder!!.emps.forEachIndexed{ i, emp ->
                if (i == 0) {
                    alertStringEmps = alertStringEmps.plus(emp.name)
                }
                else {
                    alertStringEmps = alertStringEmps.plus(", ${emp.name}")
                }
            }
            dividedUsage = data.est.toDouble() / workOrder!!.emps.size
            alertString = getString(R.string.quick_complete_item_labor, dividedUsage.toString(), alertStringEmps)
        }
        else {
            dividedUsage = data.est.toDouble()
            alertString = getString(R.string.quick_complete_item_material, dividedUsage.toString())
        }

        val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
        builder.setTitle(getString(R.string.quick_complete_item_title))
        builder.setMessage(alertString)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val usageToLog:MutableList<Usage> = mutableListOf()
            val dateNow = LocalDate.now()
            val newDateValue = LocalDateTime.of(dateNow.year, dateNow.month, dateNow.dayOfMonth, 0, 0)



            if (data.type == "1") {
                println("labor type")
                workOrder!!.emps.forEach {
                    println("divided usage to string: $dividedUsage")
                    val newUsage = Usage(
                        ID = "0",
                        woID = data.woID,
                        itemID = data.ID,
                        type = "1",
                        addedBy = GlobalVars.loggedInEmployee!!.ID,
                        qty = dividedUsage.toString(),
                        total_only = "1",
                        empID = it.ID,
                        depID = it.depID,
                        start = GlobalVars.dateFormatterPHP.format(newDateValue),
                        stop = GlobalVars.dateFormatterPHP.format(newDateValue),
                        lunch = null,
                        empName = it.name,
                        unitPrice = data.price,
                        totalPrice = data.total,
                        vendor = "",
                        unitCost = "",
                        totalCost = "",
                        usageCharge = data.charge,
                        override = "1",
                        pic = it.pic,
                        del = "",
                        custName = null,
                        woStatus = null,
                        hasReceipt = "0",
                        addedByName = GlobalVars.loggedInEmployee!!.name,
                        addedNice = "",
                        locked = false,
                        receipt = null,
                        editsMade = false,
                        startDateTime = null,
                        stopDateTime = null,
                        progressViewVisible = false
                    )
                    newUsage.auto_usage = "1"
                    usageToLog.add(newUsage)
                }
            }
            else { // material
                println("material type")
                println("divided usage to string: $dividedUsage")
                val newUsage = Usage(
                    ID = "0",
                    woID = data.woID,
                    itemID = data.ID,
                    type = "2",
                    addedBy = GlobalVars.loggedInEmployee!!.ID,
                    qty = data.est,
                    total_only = "1",
                    empID = null,
                    depID = null,
                    start = GlobalVars.dateFormatterPHP.format(newDateValue),
                    stop = GlobalVars.dateFormatterPHP.format(newDateValue),
                    lunch = null,
                    empName = null,
                    unitPrice = data.price,
                    totalPrice = data.total,
                    vendor = "",
                    unitCost = "",
                    totalCost = "",
                    usageCharge = data.charge,
                    override = "1",
                    pic = null,
                    del = "",
                    custName = null,
                    woStatus = null,
                    hasReceipt = "0",
                    addedByName = GlobalVars.loggedInEmployee!!.name,
                    addedNice = "",
                    locked = false,
                    receipt = null,
                    editsMade = false,
                    startDateTime = null,
                    stopDateTime = null,
                    progressViewVisible = false
                )
                newUsage.auto_usage = "1"


                if (!data.vendors.isNullOrEmpty()) {
                    data.vendors!!.forEach {
                        if (it.preferred == "1") {
                            newUsage.vendor = it.ID
                            if (it.cost != null) {
                                newUsage.unitCost = it.cost
                                val totalCost = it.cost!!.toDouble() * data.est.toDouble()
                                newUsage.totalCost = totalCost.toString()
                                println("total cost: ${newUsage.totalCost}")
                            }
                            else {
                                newUsage.unitCost = null
                                newUsage.totalCost = null
                            }
                        }
                    }
                }

                usageToLog.add(newUsage)

            }

            println(usageToLog)

            quickCompleteCallDB(data, usageToLog)

        }

        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            println("canceled quick complete")
        }

        builder.show()

    }

    private fun quickCompleteCallDB(data:WoItem, usageToLog:MutableList<Usage>) {

        val usageToLogJSONMutableList = mutableListOf<String>()
        val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()

        usageToLog.forEach {
            val jsonUsagePretty: String = gsonPretty.toJson(it)
            usageToLogJSONMutableList.add(jsonUsagePretty)
            println("jsonUsagePretty = $jsonUsagePretty")
        }

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/usage.php"
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
                        updateWoItemComplete(data)
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
                params["usageToLog"] = usageToLogJSONMutableList.toString()
                params["override"] = "0"
                println("update usage params = $params")
                return params
            }
        }
        postRequest1.tag = "usageEntry"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }

    private fun updateWoItemComplete(data:WoItem) {

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

                        data.status = "3"
                        binding.workOrderItemsRv.adapter?.notifyDataSetChanged()

                        val gson = GsonBuilder().create()
                        val newWoStatus: String = gson.fromJson(parentObject["newWoStatus"].toString(), String::class.java)

                        if (newWoStatus != workOrder!!.status) {
                            workOrder!!.status = newWoStatus
                            updateStatus(true)
                        }
                        else {
                            getWorkOrder()
                        }
                    }


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
                params["status"] = "3"
                params["taskStatus"] = "3"
                params["woID"] = workOrder!!.woID
                params["woItemID"] = data.ID
                params["empID"] = GlobalVars.loggedInEmployee!!.ID
                println("params = $params")
                return params

            }
        }
        postRequest1.tag = "woItem"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
        true
    }






    private fun setStatusIcon(status: String) {
        println("setStatus")
        when(status) {
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
                if ((workOrder!!.skipped ?: "0") != "1") {
                    Picasso.with(context)
                        .load(R.drawable.ic_canceled)
                        .into(binding.statusIv)
                    binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusRed))
                    binding.statusTv.text = getString(R.string.canceled)
                }
                else {
                    Picasso.with(context)
                        .load(R.drawable.ic_skipped)
                        .into(binding.statusIv)
                    binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusRed))
                    binding.statusTv.text = getString(R.string.skipped)
                }
            }
            "5" -> {
                println("5")
                Picasso.with(context)
                    .load(R.drawable.ic_waiting)
                    .into(binding.statusIv)
                binding.statusBtn.setBackgroundColor(resources.getColor(R.color.statusBlue))
                binding.statusTv.text = getString(R.string.waiting)
            }
        }
    }

    fun showProgressView() {
        println("showProgressView")
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}