package com.example.AdminMatic

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
import com.AdminMatic.databinding.FragmentLeadBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


interface LeadTaskCellClickListener {
    fun onLeadTaskCellClickListener(data:Task)
    fun uploadImage(_task:Task)
    fun showProgressView()
    fun getLead()
}



class LeadFragment : Fragment(), StackDelegate, LeadTaskCellClickListener {

    private  var lead: Lead? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var  stackFragment: StackFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lead = it.getParcelable("lead")
        }
    }

    private var _binding: FragmentLeadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeadBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.lead_number, lead!!.ID)
        setHasOptionsMenu(true)
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("lead = ${lead!!.ID}")

        stackFragment = StackFragment(0,lead!!.ID,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.lead_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()


        binding.leadStatusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        binding.leadCustomerBtn.text = getString(R.string.lead_customer_button_text, lead!!.custName, lead!!.address)
        binding.leadCustomerBtn.setOnClickListener{
            println("customer btn clicked")


            val customer = Customer(lead!!.customer!!)
            val directions = LeadFragmentDirections.navigateLeadToCustomer(customer.ID)
            myView.findNavController().navigate(directions)

        }

        binding.leadAddTaskBtn.setOnClickListener {

                val directions = LeadFragmentDirections.navigateLeadToImageUpload("LEADTASK",
                    arrayOf(),lead!!.customer,lead!!.custName,"","",lead!!.ID,"0","","","", "")

                myView.findNavController().navigate(directions)

        }

        getLead()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.lead_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_lead_item) {
            if (GlobalVars.permissions!!.leadsEdit == "1") {
                val directions = LeadFragmentDirections.navigateLeadToNewEditLead(lead)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_leads_edit))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("lead")
    }


    override fun getLead(){
        println("getLead")

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/lead.php"
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
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    val gson = GsonBuilder().create()
                    //var leadJSONObject:JSONObject
                    //leadJSONObject = gson.fromJson(parentObject["leads"].toString() , JSONObject::class.java)

                    val leadsArray:Array<Lead> = gson.fromJson(parentObject["leads"].toString() , Array<Lead>::class.java)
                    //var leadsArray:Array<Lead> = leadJSONObject["leads"] as Array<Lead>
                    //leadJSONObject = gson.fromJson(parentObject.toString(), )
                    lead = leadsArray[0]
                    //lead.tasks =

                    setStatus(lead!!.statusID)

                    if(lead!!.dateNice != null){
                        binding.leadScheduleValTv.text = lead!!.dateNice!!
                    }
                    if(lead!!.deadlineNice != null){
                        binding.leadDeadlineValTv.text = lead!!.deadlineNice!!
                    }
                    if(lead!!.repName != null){
                        binding.leadSalesRepValTv.text = lead!!.repName!!
                    }
                    if(lead!!.requestedByCust != null){
                        if (lead!!.requestedByCust!! == "1") {
                            binding.leadRequestedValTv.text = getString(R.string.yes)
                        }
                        else {
                            binding.leadRequestedValTv.text = getString(R.string.no)
                        }
                    }
                    if(lead!!.description != null){
                        binding.leadDescriptionValTv.text = lead!!.description!!
                    }

                   // var leadObj:JSONObject = parentObject["leads"][0] as JSONObject
                   // var taskJSON: JSONArray = parentObject[0].getJSONArray("tasks")
                   // val taskList = gson.fromJson(taskJSON.toString() , Array<Task>::class.java).toMutableList()


                    //var leadTaskJSON: JSONArray = leadsArray[0].getJSONArray("tasks")
                    //val itemList = gson.fromJson(woItemJSON.toString() , Array<WoItem>::class.java).toMutableList()

                   // var woItemJSON: JSONArray = parentObject.getJSONArray("items")
                    //val itemList = gson.fromJson(woItemJSON.toString() , Array<WoItem>::class.java).toMutableList()


                   // var leadTaskJSON: JSONArray = parentObject.getJSONArray("tasks")
                   // val taskList = gson.fromJson(leadTaskJSON.toString() , Array<Task>::class.java).toMutableList()
                    println("lead.cust = ${lead!!.custName!!}")
                   // if(lead!!.tasks != null && ){
                       // println("tasks 1 = ${lead!!.tasks!![0].task}")
                   // }


                    binding.leadTaskRv.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            LeadTasksAdapter(
                                lead!!.tasks!!.toMutableList(),
                                it, requireActivity().application,
                                this@LeadFragment,
                                lead as Lead
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        binding.leadTaskRv.addItemDecoration(itemDecoration)



                        //(adapter as LeadTasksAdapter).notifyDataSetChanged()
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
                params["leadID"] = lead!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "lead"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }


    override fun onLeadTaskCellClickListener(data:Task) {

        println("Cell clicked with leadTask: ${data.task}")

        /*

        data?.let { data ->
            var images:Array<Image>
            if(data.images == null){
                images = arrayOf()
            }else{
                images = data.images!!
            }
            val directions = LeadFragmentDirections.navigateLeadToImageUpload("LEADTASK",
                images,"","","","",lead!!.ID,data.ID,"","")

            myView.findNavController().navigate(directions)

        }
*/



    }


    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.leadStatusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            lead!!.statusID = item!!.itemId.toString()

            setStatus(lead!!.statusID)
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT)
                .show()

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
                        globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

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
                    val params: MutableMap<String, String> = HashMap()
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["status"] = lead!!.statusID
                    params["leadID"] = lead!!.ID
                    params["empID"] = GlobalVars.loggedInEmployee!!.ID
                    println("params = $params")
                    return params
                }
            }
            postRequest1.tag = "lead"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
            true
        }
        popUp.gravity = Gravity.START
        popUp.show()
    }


    override fun uploadImage(_task:Task){

        val images:Array<Image> = if(_task.images == null){
            arrayOf()
        }else{
            _task.images!!
        }


        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,"","","","",lead!!.ID, _task.ID,"${_task.task}","","", "")
        myView.findNavController().navigate(directions)
    }




    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "1" -> {
                println("1")
                binding.leadStatusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "2" -> {
                println("2")
                binding.leadStatusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "3" -> {
                println("3")
                binding.leadStatusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "4" -> {
                println("4")
                binding.leadStatusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
            "5" -> {
                println("5")
                binding.leadStatusBtn.setBackgroundResource(R.drawable.ic_waiting)
            }
        }
    }



    override fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.leadStatusCustCl.visibility = View.INVISIBLE
        binding.leadDataCl.visibility = View.INVISIBLE
        binding.leadTaskRv.visibility = View.INVISIBLE
        binding.leadAddTaskBtn.visibility = View.INVISIBLE

    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.leadStatusCustCl.visibility = View.VISIBLE
        binding.leadDataCl.visibility = View.VISIBLE
        binding.leadTaskRv.visibility = View.VISIBLE
        binding.leadAddTaskBtn.visibility = View.VISIBLE
    }



    //Stack delegates
    override fun newLeadView(_lead: Lead) {
        println("newLeadView ${_lead.ID}")

        //val directions = LeadFragmentDirections.navigateInvoiceToLead(_lead)
        //myView.findNavController().navigate(directions)
        lead = _lead
    }

    override fun newContractView(_contract: Contract) {
        println("newContractView ${_contract.ID}")
        val directions = LeadFragmentDirections.navigateLeadToContract(_contract)
        myView.findNavController().navigate(directions)
    }

    override fun newWorkOrderView(_workOrder: WorkOrder) {
        println("newWorkOrderView $_workOrder")
        val directions = LeadFragmentDirections.navigateLeadToWorkOrder(_workOrder)
        myView.findNavController().navigate(directions)
    }

    override fun newInvoiceView(_invoice: Invoice) {
        println("newInvoiceView ${_invoice.ID}")

        val directions = LeadFragmentDirections.navigateLeadToInvoice(_invoice)
        myView.findNavController().navigate(directions)
    }


}