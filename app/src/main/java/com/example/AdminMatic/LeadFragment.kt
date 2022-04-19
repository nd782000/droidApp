package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_work_order.*
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


interface LeadTaskCellClickListener {
    fun onLeadTaskCellClickListener(data:Task)
    fun uploadImage(_task:Task)
    fun showProgressView()
    fun getLead()
}



class LeadFragment : Fragment(), StackDelegate, LeadTaskCellClickListener {
    //private var param1: String? = null
    private var param2: String? = null

    private  var lead: Lead? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    private lateinit var  stackFragment: StackFragment


    private lateinit var statusBtn: ImageButton
    private lateinit var customerBtn: Button

    private lateinit var statusCustCL: ConstraintLayout
    private lateinit var dataCL: ConstraintLayout
    lateinit var scheduleTxt:TextView
    lateinit var deadlineTxt:TextView
    lateinit var salesRepTxt:TextView
    lateinit var requestedByTxt:TextView
    lateinit var descriptionTxt:TextView

    lateinit var taskRecyclerView: RecyclerView

    private lateinit var addTasksBtn: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lead = it.getParcelable("lead")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_lead, container, false)
        myView = inflater.inflate(R.layout.fragment_lead, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.lead_number, lead!!.ID)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("lead = ${lead!!.ID}")


        pgsBar = view.findViewById(R.id.progress_bar)

        stackFragment = StackFragment(0,lead!!.ID,this)

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.lead_cl, stackFragment, "stackFrag")
        ft.commitAllowingStateLoss()


        statusBtn = myView.findViewById(R.id.lead_status_btn)
        statusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()

        }

        customerBtn = myView.findViewById(R.id.lead_customer_btn)
        customerBtn.text = getString(R.string.lead_customer_button_text, lead!!.custName, lead!!.address)
        customerBtn.setOnClickListener{
            println("customer btn clicked")


            val customer = Customer(lead!!.customer!!)
            val directions = LeadFragmentDirections.navigateLeadToCustomer(customer.ID)
            myView.findNavController().navigate(directions)


        }

        scheduleTxt = myView.findViewById(R.id.lead_schedule_val_tv)
        deadlineTxt = myView.findViewById(R.id.lead_deadline_val_tv)
        salesRepTxt = myView.findViewById(R.id.lead_sales_rep_val_tv)
        requestedByTxt = myView.findViewById(R.id.lead_requested_val_tv)
        descriptionTxt = myView.findViewById(R.id.lead_description_val_tv)
        statusCustCL = myView.findViewById(R.id.lead_status_cust_cl)
        dataCL = myView.findViewById(R.id.lead_data_cl)
        addTasksBtn = myView.findViewById(R.id.lead_add_task_btn)
        taskRecyclerView = myView.findViewById(R.id.lead_task_rv)

        addTasksBtn.setOnClickListener {


                val directions = LeadFragmentDirections.navigateLeadToImageUpload("LEADTASK",
                    arrayOf(),lead!!.customer,lead!!.custName,"","",lead!!.ID,"0","","","")

                myView.findNavController().navigate(directions)


        }

        getLead()
    }


   override fun getLead(){
        println("getLead")

        showProgressView()
        var urlString = "https://www.adminmatic.com/cp/app/functions/get/lead.php"
        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"
        val queue = Volley.newRequestQueue(myView.context)
        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")

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
                        scheduleTxt.text = lead!!.dateNice!!
                    }
                    if(lead!!.deadlineNice != null){
                        deadlineTxt.text = lead!!.deadlineNice!!
                    }
                    if(lead!!.repName != null){
                        salesRepTxt.text = lead!!.repName!!
                    }
                    if(lead!!.requestedByCust != null){
                        requestedByTxt.text = lead!!.requestedByCust!!
                    }
                    if(lead!!.description != null){
                        descriptionTxt.text = lead!!.description!!
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


                    taskRecyclerView.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            LeadTasksAdapter(
                                lead!!.tasks!!.toMutableList(),
                                it,
                                this@LeadFragment,
                                lead as Lead
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        taskRecyclerView.addItemDecoration(itemDecoration)



                        (adapter as LeadTasksAdapter).notifyDataSetChanged()
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
        queue.add(postRequest1)


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

        val popUp = PopupMenu(myView.context,statusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 1, 1,globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_not_started)!!,myView.context)!!, myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_in_progress)!!,myView.context)!!, myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(myView.context.getDrawable(R.drawable.ic_done)!!,myView.context)!!, myView.context.getString(R.string.finished)))
        popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            lead!!.statusID = item!!.itemId.toString()

            setStatus(lead!!.statusID)
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()

            showProgressView()

            var urlString = "https://www.adminmatic.com/cp/app/functions/update/workOrderStatus.php"

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
            queue.add(postRequest1)
            true
        })
        popUp.gravity = Gravity.START
        popUp.show()
    }


    override fun uploadImage(_task:Task){

        val images:Array<Image> = if(_task.images == null){
            arrayOf()
        }else{
            _task.images!!
        }


        val directions = WoItemFragmentDirections.navigateWoItemToImageUpload("TASK",images,"","","","",lead!!.ID, _task.ID,"${_task.task}","","")
        myView.findNavController().navigate(directions)
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
            "5" -> {
                println("5")
                statusBtn.setBackgroundResource(R.drawable.ic_waiting)
            }
        }
    }



    override fun showProgressView() {

        println("showProgressView")
        pgsBar.visibility = View.VISIBLE
        statusCustCL.visibility = View.INVISIBLE
        dataCL.visibility = View.INVISIBLE
        taskRecyclerView.visibility = View.INVISIBLE
        addTasksBtn.visibility = View.INVISIBLE

    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        statusCustCL.visibility = View.VISIBLE
        dataCL.visibility = View.VISIBLE
        taskRecyclerView.visibility = View.VISIBLE
        addTasksBtn.visibility = View.VISIBLE
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