package com.example.AdminMatic

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentWorkOrderListBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.dateFormatterYYYYMMDD
import com.example.AdminMatic.GlobalVars.Companion.globalWorkOrdersList
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.*
import java.time.temporal.WeekFields
import java.util.*


interface WorkOrderCellClickListener {
    fun onWorkOrderCellClickListener(data:WorkOrder, listIndex:Int)
}


class WorkOrderListFragment : Fragment(), WorkOrderCellClickListener {

    lateinit var myView:View
    private lateinit var adapter:WorkOrdersAdapter

    private var _binding: FragmentWorkOrderListBinding? = null
    private val binding get() = _binding!!

    // Filter Variables
    private var startDate = ""
    private var endDate = ""
    private var lockedDates = ""
    private var department = ""
    private var crew = ""
    private var status = ""
    private var sort = ""

    /*
    "employeeID":"",
    "custID":"",
    "startDate":startDateDB,
    "endDate":endDateDB,
    "deptID":department,
    "crewID":crew,
    "status":status,
    "locked":locked,
    "order":sort,
    "sessionKey": self.appDelegate.defaults.string(forKey: loggedInKeys.sessionKey)!,
    "companyUnique": self.appDelegate.defaults.string(forKey: loggedInKeys.companyUnique)!]
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("WorkOrderListSettings") { _, bundle ->
            val newStartDate = bundle.getString("startDate")
            val newEndDate = bundle.getString("endDate")
            val newLockedDates = bundle.getString("lockedDates")
            val newDepartment = bundle.getString("department")
            val newCrew = bundle.getString("crew")
            val newStatus = bundle.getString("status")
            val newSort = bundle.getString("sort")



            if (newStartDate != startDate ||
                newEndDate != endDate ||
                newLockedDates != lockedDates ||
                newDepartment != department ||
                newCrew != crew ||
                newStatus != status ||
                newSort != sort
                ) {

                startDate = newStartDate!!
                endDate = newEndDate!!
                lockedDates = newLockedDates!!
                department = newDepartment!!
                crew = newCrew!!
                status = newStatus!!
                sort = newSort!!

                if (newStartDate != "" ||
                    newEndDate != "" ||
                    newLockedDates != "" ||
                    newDepartment != "" ||
                    newCrew != "" ||
                    newStatus != "" ||
                    newSort != ""
                ) {
                    println("setting button color yellow")
                    ImageViewCompat.setImageTintList(binding.settingsIv, ColorStateList.valueOf(ContextCompat.getColor(myView.context, R.color.settingsActive)))
                }
                else {
                    println("setting button color default")
                    ImageViewCompat.setImageTintList(binding.settingsIv, null)
                }

                getWorkOrders()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")

        globalVars = GlobalVars()
        _binding = FragmentWorkOrderListBinding.inflate(inflater, container, false)
        myView = binding.root

        ((activity as AppCompatActivity).supportActionBar?.customView!!
            .findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.woList)

        adapter = if (globalWorkOrdersList == null) {
            val emptyList: MutableList<WorkOrder> = mutableListOf()
            WorkOrdersAdapter(emptyList, this.myView.context,this)
        } else {
            WorkOrdersAdapter(globalWorkOrdersList!!, this.myView.context,this)
            //(adapter).notifyDataSetChanged()
        }



        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")


        (activity as MainActivity?)!!.setWorkOrderList(this)


        binding.addWorkOrderBtn.setOnClickListener{
            if (GlobalVars.permissions!!.scheduleEdit == "1") {
                val directions = WorkOrderListFragmentDirections.navigateToNewEditWorkOrder(null)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule_edit))
            }
        }

        binding.mapBtn.setOnClickListener {
            println("Map button clicked!")
            val directions = WorkOrderListFragmentDirections.navigateToMap(0)
            myView.findNavController().navigate(directions)
        }

        binding.settingsBtn.setOnClickListener {
            val directions = WorkOrderListFragmentDirections.navigateToWorkOrderListSettings(startDate, endDate, lockedDates, department, crew, status, sort)
            myView.findNavController().navigate(directions)
        }

        if (globalWorkOrdersList.isNullOrEmpty()) {

            getWorkOrders()

        }
        else {

            //skip get workOrders and go directly to layoutViews
            //layoutViews()
            if (this.isVisible){
                layoutViews()
            }

        }

    }

    override fun onAttach(context: Context) {

        println("onAttach")
        super.onAttach(context)
    }



    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("woList")
    }


     fun getWorkOrders(){
        println("getWorkOrders")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/workOrders.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val workOrders: JSONArray = parentObject.getJSONArray("workOrders")
                        println("workOrders = $workOrders")
                        println("workOrders count = ${workOrders.length()}")

                        if (globalWorkOrdersList != null) {
                            globalWorkOrdersList!!.clear()
                        }

                        val gson = GsonBuilder().create()

                        val temp = gson.fromJson(parentObject.toString(), WorkOrderArray::class.java)

                        globalWorkOrdersList = temp.workOrders!!.toMutableList()
                        //globalDayNote = temp.note

                        binding.workOrderCountTextview.text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())

                        if (this.isVisible) {
                            layoutViews()
                        }

                    }
                    else {
                        hideProgressView()
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
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                params["employeeID"] = ""
                params["startDate"] = ""
                params["endDate"] = ""
                params["active"] = "1"
                params["custID"] = ""


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "woList"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

    }



    fun layoutViews(){
        println("layoutViews")
        println(activity)

        hideProgressView()

        binding.listRecyclerView.apply {

        layoutManager = LinearLayoutManager((activity as MainActivity?)!!)




            //workOrdersList.clear()
            adapter = activity?.let {
                WorkOrdersAdapter(
                    globalWorkOrdersList!!, context,this@WorkOrderListFragment
                )
            }


            if (binding.listRecyclerView.itemDecorationCount == 0) {
                val itemDecoration: ItemDecoration =
                    DividerItemDecoration(
                        myView.context,
                        DividerItemDecoration.VERTICAL
                    )
                binding.listRecyclerView.addItemDecoration(itemDecoration)
            }

            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
            // Setup refresh listener which triggers new data loading
            // Setup refresh listener which triggers new data loading
            binding.customerSwipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0)
                binding.workOrdersSearch.setQuery("", false)
                binding.workOrdersSearch.clearFocus()
                getWorkOrders()
            }
            // Configure the refreshing colors
            // Configure the refreshing colors
            binding.customerSwipeContainer.setColorSchemeResources(
                R.color.button,
                R.color.black,
                R.color.colorAccent,
                R.color.colorPrimaryDark
            )



            //(adapter as WorkOrdersAdapter).notifyDataSetChanged()

            // Remember to CLEAR OUT old items before appending in the new ones

            // ...the data has come back, add new items to your adapter...

            // Now we call setRefreshing(false) to signal refresh has finished
            binding.customerSwipeContainer.isRefreshing = false




            //search listener
            binding.workOrdersSearch.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                override fun onQueryTextSubmit(query: String?): Boolean {
                    myView.hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    println("onQueryTextChange = $newText")
                    (adapter as WorkOrdersAdapter).filter.filter(newText)
                    return false
                }

            })


        }

       //scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment


    }

    override fun onWorkOrderCellClickListener(data:WorkOrder, listIndex:Int) {
        println("Cell clicked with workOrder: ${data.woID}")

        data.let {
            val directions = WorkOrderListFragmentDirections.navigateToWorkOrder(it)
            directions.listIndex = listIndex
            myView.findNavController().navigate(directions)

        }
    }



    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        println("hideProgressView")
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }


    override fun onDetach() {
        println("onDetach")
        super.onDetach()
    }




    override fun onDestroyView() {
        println("onDestroyView")
        binding.workOrdersSearch.setOnQueryTextListener(null)

        super.onDestroyView()
    }

    override fun onDestroy() {
        println("onDestroy")
        super.onDestroy()
    }


}