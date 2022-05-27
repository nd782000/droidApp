package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.solver.state.State
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.example.AdminMatic.GlobalVars.Companion.dateFormatterYYYYMMDD
import com.example.AdminMatic.GlobalVars.Companion.globalWorkOrdersList
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.example.AdminMatic.GlobalVars.Companion.scheduleSpinnerPosition
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_work_order_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.*
import java.time.temporal.WeekFields
import java.util.*


interface WorkOrderCellClickListener {
    fun onWorkOrderCellClickListener(data:WorkOrder, listIndex:Int)
}


class WorkOrderListFragment : Fragment(), WorkOrderCellClickListener, AdapterView.OnItemSelectedListener {


    lateinit var myView:View
    private lateinit var  pgsBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView:androidx.appcompat.widget.SearchView
    private lateinit var  swipeRefresh:SwipeRefreshLayout
    private lateinit var adapter:WorkOrdersAdapter
    private lateinit var scheduleSpinner:Spinner
    private lateinit var crewBtn:Button
    private lateinit var mapBtn:Button
    private lateinit var countTextView: TextView
    private lateinit var allCL: ConstraintLayout

    //update eq
    private var datesArray:Array<String> = arrayOf(
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        myView = inflater.inflate(R.layout.fragment_work_order_list, container, false)

        ((activity as AppCompatActivity).supportActionBar?.customView!!
            .findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.woList)

        if (globalWorkOrdersList == null) {
            val emptyList: MutableList<WorkOrder> = mutableListOf()
            adapter = WorkOrdersAdapter(emptyList, this.myView.context,this)
        }
        else {
            adapter = WorkOrdersAdapter(globalWorkOrdersList!!, this.myView.context,this)
            (adapter).notifyDataSetChanged()
        }



        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")




        (activity as MainActivity?)!!.setWorkOrderList(this)

        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.work_orders_search)
        swipeRefresh = view.findViewById(R.id.customerSwipeContainer)
        scheduleSpinner = view.findViewById(R.id.schedule_spinner)
        crewBtn = view.findViewById(R.id.crew_btn)
        mapBtn = view.findViewById(R.id.map_btn)
        countTextView = view.findViewById(R.id.work_order_count_textview)
        allCL = view.findViewById(R.id.all_cl)

        crewBtn.setOnClickListener {
            println("Crew Click")

            var directions = WorkOrderListFragmentDirections.navigateToDepartments(false)
            //val spinnerPosition:Int = scheduleSpinner.getTag(R.id.pos) as Int

            // Even spinner positions are the personal ones
            if (scheduleSpinnerPosition % 2 == 0) {
                directions = WorkOrderListFragmentDirections.navigateToDepartments(true)
            }

            myView.findNavController().navigate(directions)
        }

        mapBtn.setOnClickListener{
            println("Map button clicked!")
            val directions = WorkOrderListFragmentDirections.navigateToMap(0)
            myView.findNavController().navigate(directions)
        }

        if (globalWorkOrdersList == null) {

            println("globalWorkOrdersList = null")
            scheduleSpinnerPosition = 2

            scheduleSpinner.setBackgroundResource(R.drawable.text_view_layout)


            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, datesArray
            )
            adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

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

            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, datesArray

            )
            adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

            scheduleSpinner.adapter = adapter



            //skip get workOrders and go directly to layoutViews
            //layoutViews()
            if (this.isVisible){
                layoutViews()
            }

            //if(activity != null){

            //}
            scheduleSpinner.setTag(R.id.pos, scheduleSpinnerPosition)
            scheduleSpinner.setSelection(scheduleSpinnerPosition, false)

            scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment

            updateScheduleInfo(scheduleSpinnerPosition)
            countTextView.text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())

        }

    }

    override fun onAttach(context: Context) {

        println("onAttach")
        super.onAttach(context)
    }




     fun getWorkOrders(){
        println("getWorkOrders")


            showProgressView()


            var urlString = "https://www.adminmatic.com/cp/app/functions/get/workOrders.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"
            val queue = Volley.newRequestQueue(myView.context)

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response
                    //Log.d("Response", response)

                    println("Response $response")



                    try {
                        if (isResumed) {
                            val parentObject = JSONObject(response)
                            println("parentObject = $parentObject")
                            val workOrders: JSONArray = parentObject.getJSONArray("workOrders")
                            println("workOrders = $workOrders")
                            println("workOrders count = ${workOrders.length()}")

                            if (globalWorkOrdersList != null) {
                                globalWorkOrdersList!!.clear()
                            }


                            val gson = GsonBuilder().create()

                            globalWorkOrdersList =
                                gson.fromJson(workOrders.toString(), Array<WorkOrder>::class.java)
                                    .toMutableList()

                            (activity as MainActivity?)!!.updateMap()

                            countTextView.text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())

                            if (this.isVisible){
                                layoutViews()
                            }

                            updateScheduleInfo(scheduleSpinnerPosition)
                        }

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


                    println("params = $params")
                    return params
                }
            }
            queue.add(postRequest1)

    }



    fun layoutViews(){
        println("layoutViews")
        println(activity)

        hideProgressView()

        list_recycler_view.apply {

        layoutManager = LinearLayoutManager((activity as MainActivity?)!!)




            //workOrdersList.clear()
            adapter = activity?.let {
                WorkOrdersAdapter(
                    globalWorkOrdersList!!, context,this@WorkOrderListFragment
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
                searchView.setQuery("", false)
                searchView.clearFocus()
                println("Start date: $startDateDB")
                println("End date: $endDateDB")
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



            (adapter as WorkOrdersAdapter).notifyDataSetChanged()

            // Remember to CLEAR OUT old items before appending in the new ones

            // ...the data has come back, add new items to your adapter...

            // Now we call setRefreshing(false) to signal refresh has finished
            customerSwipeContainer.isRefreshing = false




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

    override fun onWorkOrderCellClickListener(data:WorkOrder, listIndex:Int) {
        println("Cell clicked with workOrder: ${data.woID}")

        data.let {
            val directions = WorkOrderListFragmentDirections.navigateToWorkOrder(it)
            directions.listIndex = listIndex
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

            updateScheduleInfo(position)

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

    private fun updateScheduleInfo(position:Int) {
        when(position) {
            0 -> {println("all dates personal")
                startDateDB = ""
                endDateDB = ""

                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            1 -> {println("all dates everyone")
                startDateDB = ""
                endDateDB = ""

                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            2 -> {println("today personal")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = odtStart
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            3 -> {println("today everyone")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = odtStart

                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            4 -> {println("tomorrow personal")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN).plusDays(1)
                val odtStop: OffsetDateTime = odtStart
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            5 -> {println("tomorrow everyone")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN).plusDays(1)
                val odtStop: OffsetDateTime = odtStart
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID =""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            6 -> {println("this week personal")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1L).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = odtStart.plusDays(6)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            7 -> {println("this week everyone")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1L).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = odtStart.plusDays(6)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            8 -> {println("next week personal")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1L).plusDays(7).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = odtStart.plusDays(6)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            9 -> {println("next week everyone")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1L).plusDays(7).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = odtStart.plusDays(6)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            10 -> {println("next 14 days personal")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = today.plusDays(14).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            11 -> {println("next 14 days everyone")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = today.plusDays(14).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            12 -> {println("next 30 days personal")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = today.plusDays(30).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            13 -> {println("next 30 days everyone")
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
                val odtStart: OffsetDateTime = today.atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = today.plusDays(30).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
            14 -> {println("this year personal")
                val odtStart: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year, 1).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year+1, 1).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fName)
            }
            15 -> {println("this year everyone")
                val odtStart: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year, 1).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year+1, 1).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                crewBtn.text = getString(R.string.crews_everyone)
            }
        }
    }


    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        allCL.visibility = View.INVISIBLE
        /*
        searchView.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
        scheduleSpinner.visibility = View.INVISIBLE
        crewBtn.visibility = View.INVISIBLE
        mapBtn.visibility = View.INVISIBLE
        countTextView.visibility = View.INVISIBLE

         */

    }

    fun hideProgressView() {
        println("hideProgressView")
        pgsBar.visibility = View.INVISIBLE
        allCL.visibility = View.VISIBLE
        /*
        searchView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
        scheduleSpinner.visibility = View.VISIBLE
        crewBtn.visibility = View.VISIBLE
        mapBtn.visibility = View.VISIBLE
        countTextView.visibility = View.VISIBLE

         */
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