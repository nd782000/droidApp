package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.example.AdminMatic.GlobalVars.Companion.globalWorkOrdersList
import com.example.AdminMatic.GlobalVars.Companion.scheduleSpinnerPosition

import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_work_order_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset



interface WorkOrderCellClickListener {
    fun onWorkOrderCellClickListener(data:WorkOrder)
}


class WorkOrderListFragment : Fragment(), WorkOrderCellClickListener, AdapterView.OnItemSelectedListener {


    lateinit var myView:View
    lateinit var  pgsBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var searchView:androidx.appcompat.widget.SearchView
    lateinit var  swipeRefresh:SwipeRefreshLayout
    lateinit var adapter:WorkOrdersAdapter
    lateinit var scheduleSpinner:Spinner
    lateinit var crewBtn:Button
    lateinit var mapBtn:Button

    var datesArray:Array<String> = arrayOf(
        "All Dates (${loggedInEmployee!!.fName})",
        "All Dates (Everyone)",
        "Today (${loggedInEmployee!!.fName})",
        "Today (Everyone)",
        "Tomorrow (${loggedInEmployee!!.fName})",
        "Tomorrow (Everyone)",
        "This Week (${loggedInEmployee!!.fName})",
        "This Week (Everyone)",
        "Next Week (${loggedInEmployee!!.fName})",
        "Next Week (Everyone)",
        "Next 14 Days (${loggedInEmployee!!.fName})",
        "Next 14 Days (Everyone)",
        "Next 30 Days (${loggedInEmployee!!.fName})",
        "Next 30 Days (Everyone)",
        "This Year (${loggedInEmployee!!.fName})",
        "This Year (Everyone)")

        var startDateDB:String = ""
        var endDateDB:String = ""
        var empID:String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        println("onCreateView")
        myView = inflater.inflate(R.layout.fragment_work_order_list, container, false)


    if (globalWorkOrdersList == null) {
        var emptyList: MutableList<WorkOrder> = mutableListOf()

        adapter = WorkOrdersAdapter(emptyList, myView.context, this)


        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!
            .findViewById(R.id.app_title_tv) as TextView).text = "WorkOrder List"
    }

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")


        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.work_orders_search)
        swipeRefresh = view.findViewById(R.id.swipeContainer)
        scheduleSpinner = view.findViewById(R.id.schedule_spinner)
        crewBtn = view.findViewById(R.id.crew_btn)
        mapBtn = view.findViewById(R.id.map_btn)

        if (globalWorkOrdersList == null) {

            println("globalWorkOrdersList = null")


            crewBtn.setOnClickListener {
                println("Crew Click")
            }

            mapBtn.setOnClickListener{
                println("go to mapView")
            }


            scheduleSpinner.setBackgroundResource(R.drawable.text_view_layout)


            val adapter: ArrayAdapter<String>? = ArrayAdapter<String>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, datesArray

            )
            adapter!!.setDropDownViewResource(R.layout.spinner_right_aligned)

            scheduleSpinner.adapter = adapter




            //triggers first get workOrders
            scheduleSpinner.setTag(R.id.pos, scheduleSpinnerPosition)
            scheduleSpinner.setSelection(scheduleSpinnerPosition, false)

            scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment







            println("today personal")
            val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
            val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
            val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
            print("odtStart = $odtStart")
            print("odtStop = $odtStop")
            startDateDB = odtStart.toString()
            endDateDB = odtStop.toString()
            empID = loggedInEmployee!!.ID



           getWorkOrders()

        }else{

            println("globalWorkOrdersList != null")

            scheduleSpinner = view.findViewById(R.id.schedule_spinner)
            scheduleSpinner.setBackgroundResource(R.drawable.text_view_layout)

            scheduleSpinner.onItemSelectedListener = null

            val adapter: ArrayAdapter<String>? = ArrayAdapter<String>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, datesArray

            )
            adapter!!.setDropDownViewResource(R.layout.spinner_right_aligned)

            scheduleSpinner.adapter = adapter


            //skip get workOrders and go directly to layoutViews
            layoutViews()

            scheduleSpinner.setTag(R.id.pos, scheduleSpinnerPosition)
            scheduleSpinner.setSelection(scheduleSpinnerPosition, false)

            scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment
        }

    }

    override fun onAttach(context: Context) {

        println("onAttach")
        super.onAttach(context)
    }


    private fun getWorkOrders(){
        println("getWorkOrders")


            showProgressView()


            var urlString = "https://www.adminmatic.com/cp/app/functions/get/workOrders.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "${"$urlString?cb=$currentTimestamp"}"
            val queue = Volley.newRequestQueue(myView.context)

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response
                    //Log.d("Response", response)

                    println("Response $response")



                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = ${parentObject.toString()}")
                        var workOrders: JSONArray = parentObject.getJSONArray("workOrders")
                        println("workOrders = ${workOrders.toString()}")
                        println("workOrders count = ${workOrders.length()}")

                        if (globalWorkOrdersList != null) {
                            globalWorkOrdersList!!.clear()
                        }


                        val gson = GsonBuilder().create()

                        globalWorkOrdersList =
                            gson.fromJson(workOrders.toString(), Array<WorkOrder>::class.java)
                                .toMutableList()

                        layoutViews()

                        Toast.makeText(
                            activity,
                            "${globalWorkOrdersList!!.count()} WorkOrders Loaded",
                            Toast.LENGTH_SHORT
                        ).show()



                        /* Here 'response' is a String containing the response you received from the website... */
                    } catch (e: JSONException) {
                        println("JSONException")
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener { // error


                    // Log.e("VOLLEY", error.toString())
                    // Log.d("Error.Response", error())
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["companyUnique"] = loggedInEmployee!!.companyUnique
                    params["sessionKey"] = loggedInEmployee!!.sessionKey
                    params["employeeID"] = empID
                    params["startDate"] = startDateDB
                    params["endDate"] = endDateDB
                    params["active"] = "1"
                    params["custID"] = ""


                    println("params = ${params.toString()}")
                    return params
                }
            }
            queue.add(postRequest1)

    }

    fun layoutViews(){
        println("layoutViews")

        hideProgressView()

        list_recycler_view.apply {

                layoutManager = LinearLayoutManager(activity)


            //workOrdersList.clear()
            adapter = activity?.let {
                WorkOrdersAdapter(
                    globalWorkOrdersList!!,
                    it, this@WorkOrderListFragment
                )
            }

            val itemDecoration: ItemDecoration =
                DividerItemDecoration(
                    myView.context,
                    DividerItemDecoration.VERTICAL
                )
            recyclerView.addItemDecoration(itemDecoration)


            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
            // Setup refresh listener which triggers new data loading
            // Setup refresh listener which triggers new data loading
            swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0)
                searchView.setQuery("", false);
                searchView.clearFocus();
                getWorkOrders()
            }
            // Configure the refreshing colors
            // Configure the refreshing colors
            swipeRefresh.setColorSchemeResources(
                R.color.button,
                R.color.black,
                R.color.colorAccent,
                R.color.colorPrimaryDark
            )



            (adapter as WorkOrdersAdapter).notifyDataSetChanged();

            // Remember to CLEAR OUT old items before appending in the new ones

            // ...the data has come back, add new items to your adapter...

            // Now we call setRefreshing(false) to signal refresh has finished
            swipeContainer.isRefreshing = false;




            //search listener
            work_orders_search.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {


                override fun onQueryTextSubmit(query: String?): Boolean {
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

    override fun onWorkOrderCellClickListener(data:WorkOrder) {
        println("Cell clicked with workOrder: ${data.woID}")

        data?.let { data ->

            val directions = WorkOrderListFragmentDirections.navigateToWorkOrder(data)
            myView.findNavController().navigate(directions)
        }



    }

    //spinner delegates
    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("onNothingSelected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        println("onItemSelected position = $position")
        println("scheduleSpinner.getTag(R.id.pos) = ${scheduleSpinner.getTag(R.id.pos)}")

        if(scheduleSpinner.getTag(R.id.pos) != null && scheduleSpinner.getTag(R.id.pos) != position){

            println("tag != pos")


            when(position) {
                0 -> {println("all dates personal")
                    startDateDB = ""
                    endDateDB = ""

                    empID = loggedInEmployee!!.ID
                }
                1 -> {println("all dates everyone")
                    startDateDB = ""
                    endDateDB = ""

                    empID = ""
                }
                2 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                3 -> {println("today everyone")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = ""
                }
                4 -> {println("tomorrow personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN).plusDays(1)
                    val odtStop: OffsetDateTime = today.plusDays(2).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                5 -> {println("tomorrow everyone")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN).plusDays(1)
                    val odtStop: OffsetDateTime = today.plusDays(2).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID =""
                }
                6 -> {println("this week personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                7 -> {println("this week everyone")


                  // var calender:Calender


                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                8 -> {println("next week personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                9 -> {println("next week everyone")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                10 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                11 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                12 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                13 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                14 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }
                15 -> {println("today personal")
                    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                    val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                    val odtStop: OffsetDateTime = today.plusDays(1).atTime(OffsetTime.MIN)
                    print("odtStart = $odtStart")
                    print("odtStop = $odtStop")
                    startDateDB = odtStart.toString()
                    endDateDB = odtStop.toString()
                    empID = loggedInEmployee!!.ID
                }


            }
            getWorkOrders()
        }

        scheduleSpinnerPosition = position
        scheduleSpinner.setTag(R.id.pos, position)


        /*





           case 4:

            print("tomorrow personal")

            let tomorrow = calendar.startOfDay(for: Date()).addNumberOfDaysToDate(_numberOfDays: 1)
            print("tomorrow = \(tomorrow)")

            startDateDB = dateFormatterDB.string(from: tomorrow)
            endDateDB = dateFormatterDB.string(from: tomorrow)

            self.employeeID = appDelegate.loggedInEmployee!.ID

           break
           case 5:

            print("tomorrow everyone")

                       let tomorrow = calendar.startOfDay(for: Date()).addNumberOfDaysToDate(_numberOfDays: 1)
                       print("tomorrow = \(tomorrow)")

                       startDateDB = dateFormatterDB.string(from: tomorrow)
                       endDateDB = dateFormatterDB.string(from: tomorrow)





                  self.employeeID = ""


           break
           case 6:
            print("this week personal")
                                                    let first = calendar.startOfDay(for: Date().startOfWeek)
                                                    let last = calendar.startOfDay(for: Date().endOfWeek)
                                                    startDateDB = dateFormatterDB.string(from: first)
                                                    endDateDB = dateFormatterDB.string(from: last)

            self.employeeID = appDelegate.loggedInEmployee!.ID


           break
           case 7:

            print("this week everyone")
            let first = calendar.startOfDay(for: Date().startOfWeek)
            let last = calendar.startOfDay(for: Date().endOfWeek)
            startDateDB = dateFormatterDB.string(from: first)
            endDateDB = dateFormatterDB.string(from: last)

              self.employeeID = ""

           break
            case 8:

                print("next week personal")
                                         let first = calendar.startOfDay(for: Date().startOfNextWeek)
                                         let last = calendar.startOfDay(for: Date().endOfNextWeek)
                                         startDateDB = dateFormatterDB.string(from: first)
                                         endDateDB = dateFormatterDB.string(from: last)


                self.employeeID = appDelegate.loggedInEmployee!.ID

            break
            case 9:

                print("next week everyone")
                              let first = calendar.startOfDay(for: Date().startOfNextWeek)
                              let last = calendar.startOfDay(for: Date().endOfNextWeek)
                              startDateDB = dateFormatterDB.string(from: first)
                              endDateDB = dateFormatterDB.string(from: last)


                self.employeeID = ""


            break
            case 10:

               print("next 14 days personal")
                              let today = calendar.startOfDay(for: Date())
                              print("today = \(today)")
                              let last = calendar.startOfDay(for: Date()).addNumberOfDaysToDate(_numberOfDays: 14)
                              print("last = \(last)")
                              startDateDB = dateFormatterDB.string(from: today)
                              endDateDB = dateFormatterDB.string(from: last)


               self.employeeID = appDelegate.loggedInEmployee!.ID



            break
            case 11:
                 print("next 14 days everyone")
                               let today = calendar.startOfDay(for: Date())
                               print("today = \(today)")
                               let last = calendar.startOfDay(for: Date()).addNumberOfDaysToDate(_numberOfDays: 14)
                               print("last = \(last)")
                               startDateDB = dateFormatterDB.string(from: today)
                               endDateDB = dateFormatterDB.string(from: last)


                  self.employeeID = ""

            break
            case 12:

                 print("next 30 days personal")
                                                  let today = calendar.startOfDay(for: Date())
                                                  print("today = \(today)")
                                                  let last = calendar.startOfDay(for: Date()).addNumberOfDaysToDate(_numberOfDays: 30)
                                                  print("last = \(last)")
                                                  startDateDB = dateFormatterDB.string(from: today)
                                                  endDateDB = dateFormatterDB.string(from: last)

                 self.employeeID = appDelegate.loggedInEmployee!.ID

            break
            case 13:


                print("next 30 days everyone")
                                                 let today = calendar.startOfDay(for: Date())
                                                 print("today = \(today)")
                                                 let last = calendar.startOfDay(for: Date()).addNumberOfDaysToDate(_numberOfDays: 30)
                                                 print("last = \(last)")
                                                 startDateDB = dateFormatterDB.string(from: today)
                                                 endDateDB = dateFormatterDB.string(from: last)

                self.employeeID = ""


            break
            case 14:
                 print("this year personal")


                               let today = calendar.startOfDay(for: Date())
                               print("today = \(today)")
                               var last:Date

                               // Get the current year
                               let year = calendar.component(.year, from: Date())
                               // Get the first day of next year
                               if let firstOfNextYear = calendar.date(from: DateComponents(year: year + 1, month: 1, day: 1)) {
                                   // Get the last day of the current year
                                   last = calendar.date(byAdding: .day, value: -1, to: firstOfNextYear)!
                                   print("last = \(last)")
                                   endDateDB = dateFormatterDB.string(from: last)
                               }


                               startDateDB = dateFormatterDB.string(from: today)



                 self.employeeID = appDelegate.loggedInEmployee!.ID

            break
            case 15:
                print("this year everyone")


                let today = calendar.startOfDay(for: Date())
                print("today = \(today)")
                var last:Date

                // Get the current year
                let year = calendar.component(.year, from: Date())
                // Get the first day of next year
                if let firstOfNextYear = calendar.date(from: DateComponents(year: year + 1, month: 1, day: 1)) {
                    // Get the last day of the current year
                    last = calendar.date(byAdding: .day, value: -1, to: firstOfNextYear)!
                    print("last = \(last)")
                    endDateDB = dateFormatterDB.string(from: last)
                }


                startDateDB = dateFormatterDB.string(from: today)

                self.employeeID = ""

            break
           default:
               print("all dates")
               //all dates

               startDateDB = ""
               endDateDB = ""

            self.employeeID = ""

           }
         */




    }




    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        searchView.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
        scheduleSpinner.visibility = View.INVISIBLE
        crewBtn.visibility = View.INVISIBLE
        mapBtn.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        println("hideProgressView")
        pgsBar.visibility = View.INVISIBLE
        searchView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
        scheduleSpinner.visibility = View.VISIBLE
        crewBtn.visibility = View.VISIBLE
        mapBtn.visibility = View.VISIBLE
    }


    override fun onDetach() {
        println("onDetach")
        super.onDetach()
    }




    override fun onDestroyView() {
        println("onDestroyView")
        work_orders_search.setOnQueryTextListener(null)
        scheduleSpinner.onItemSelectedListener = null

        super.onDestroyView()
    }

    override fun onDestroy() {
        println("onDestroy")
        super.onDestroy()
    }






    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WorkOrderListFragment().apply {

            }
    }
}