package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import com.example.AdminMatic.GlobalVars.Companion.scheduleSpinnerPosition
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


class WorkOrderListFragment : Fragment(), WorkOrderCellClickListener, AdapterView.OnItemSelectedListener {

    lateinit var myView:View
    private lateinit var adapter:WorkOrdersAdapter

    //update eq
    private var datesArray:Array<String> = arrayOf(
        "All Dates (${loggedInEmployee!!.fname})",
        "All Dates (Everyone)",
        "Today (${loggedInEmployee!!.fname})",
        "Today (Everyone)",
        "Tomorrow (${loggedInEmployee!!.fname})",
        "Tomorrow (Everyone)",
        "This Week (${loggedInEmployee!!.fname})",
        "This Week (Everyone)",
        "Next Week (${loggedInEmployee!!.fname})",
        "Next Week (Everyone)",
        "Next 14 Days (${loggedInEmployee!!.fname})",
        "Next 14 Days (Everyone)",
        "Next 30 Days (${loggedInEmployee!!.fname})",
        "Next 30 Days (Everyone)",
        "This Year (${loggedInEmployee!!.fname})",
        "This Year (Everyone)")

    var startDateDB:String = ""
    var endDateDB:String = ""
    var empID:String = ""

    private var _binding: FragmentWorkOrderListBinding? = null
    private val binding get() = _binding!!

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

        /*
        pgsBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        searchView = view.findViewById(R.id.work_orders_search)
        swipeRefresh = view.findViewById(R.id.customerSwipeContainer)
        scheduleSpinner = view.findViewById(R.id.schedule_spinner)
        crewBtn = view.findViewById(R.id.crew_btn)
        mapBtn = view.findViewById(R.id.map_btn)
        countTextView = view.findViewById(R.id.work_order_count_textview)
        allCL = view.findViewById(R.id.all_cl)

         */

        binding.crewBtn.setOnClickListener {
            println("Crew Click")

            var directions = WorkOrderListFragmentDirections.navigateToDepartments(null)
            //val spinnerPosition:Int = scheduleSpinner.getTag(R.id.pos) as Int

            // Even spinner positions are the personal ones
            if (scheduleSpinnerPosition % 2 == 0) {
                directions = WorkOrderListFragmentDirections.navigateToDepartments(loggedInEmployee)
            }

            myView.findNavController().navigate(directions)
        }

        binding.addWorkOrderBtn.setOnClickListener{
            if (GlobalVars.permissions!!.scheduleEdit == "1") {
                val directions = WorkOrderListFragmentDirections.navigateToNewEditWorkOrder(null)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule_edit))
            }
        }

        binding.mapBtn.setOnClickListener{
            println("Map button clicked!")
            val directions = WorkOrderListFragmentDirections.navigateToMap(0)
            myView.findNavController().navigate(directions)
        }

        if (globalWorkOrdersList == null) {

            println("globalWorkOrdersList = null")
            scheduleSpinnerPosition = 2

            binding.scheduleSpinner.setBackgroundResource(R.drawable.text_view_layout)


            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, datesArray
            )
            adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

            binding.scheduleSpinner.adapter = adapter

            //triggers first get workOrders
            binding.scheduleSpinner.setTag(R.id.pos, scheduleSpinnerPosition)
            binding.scheduleSpinner.setSelection(scheduleSpinnerPosition, false)

            binding.scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment

            println("today personal")
            val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
            val odtToday: OffsetDateTime = today.atTime(OffsetTime.MIN)


            startDateDB = odtToday.format(dateFormatterYYYYMMDD)
            endDateDB = startDateDB
            empID = loggedInEmployee!!.ID



            getWorkOrders()

        }else{

            println("globalWorkOrdersList != null")

            binding.scheduleSpinner.setBackgroundResource(R.drawable.text_view_layout)

            binding.scheduleSpinner.onItemSelectedListener = null

            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                myView.context,
                android.R.layout.simple_spinner_dropdown_item, datesArray

            )
            adapter.setDropDownViewResource(R.layout.spinner_right_aligned)

            binding.scheduleSpinner.adapter = adapter



            //skip get workOrders and go directly to layoutViews
            //layoutViews()
            if (this.isVisible){
                layoutViews()
            }

            //if(activity != null){

            //}
            binding.scheduleSpinner.setTag(R.id.pos, scheduleSpinnerPosition)
            binding.scheduleSpinner.setSelection(scheduleSpinnerPosition, false)

            binding.scheduleSpinner.onItemSelectedListener = this@WorkOrderListFragment

            updateScheduleInfo(scheduleSpinnerPosition)
            binding.workOrderCountTextview.text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())

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

                        globalWorkOrdersList =
                            gson.fromJson(workOrders.toString(), Array<WorkOrder>::class.java)
                                .toMutableList()

                        //(activity as MainActivity?)!!.updateMap()

                        binding.workOrderCountTextview.text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())

                        if (this.isVisible) {
                            layoutViews()
                        }

                        updateScheduleInfo(scheduleSpinnerPosition)
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
                params["employeeID"] = empID
                params["startDate"] = startDateDB
                params["endDate"] = endDateDB
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
                println("Start date: $startDateDB")
                println("End date: $endDateDB")
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
        println("scheduleSpinner.getTag(R.id.pos) = ${binding.scheduleSpinner.getTag(R.id.pos)}")



        if(binding.scheduleSpinner.getTag(R.id.pos) != null && binding.scheduleSpinner.getTag(R.id.pos) != position){

            println("tag != pos")

            updateScheduleInfo(position)

            getWorkOrders()
        }

        scheduleSpinnerPosition = position
        binding.scheduleSpinner.setTag(R.id.pos, position)



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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
            }
            1 -> {println("all dates everyone")
                startDateDB = ""
                endDateDB = ""

                empID = ""
                binding.crewBtn.text = getString(R.string.crews_everyone)
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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
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
                binding.crewBtn.text = getString(R.string.crews_everyone)
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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
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
                binding.crewBtn.text = getString(R.string.crews_everyone)
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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
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
                binding.crewBtn.text = getString(R.string.crews_everyone)
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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
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
                binding.crewBtn.text = getString(R.string.crews_everyone)
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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
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
                binding.crewBtn.text = getString(R.string.crews_everyone)
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
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
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
                binding.crewBtn.text = getString(R.string.crews_everyone)
            }
            14 -> {println("this year personal")
                val odtStart: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year, 1).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year+1, 1).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = loggedInEmployee!!.ID
                binding.crewBtn.text = getString(R.string.crews_name, loggedInEmployee!!.fname)
            }
            15 -> {println("this year everyone")
                val odtStart: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year, 1).atTime(OffsetTime.MIN)
                val odtStop: OffsetDateTime = LocalDate.ofYearDay(LocalDate.now().year+1, 1).atTime(OffsetTime.MIN)
                print("odtStart = $odtStart")
                print("odtStop = $odtStop")
                startDateDB = odtStart.format(dateFormatterYYYYMMDD)
                endDateDB = odtStop.format(dateFormatterYYYYMMDD)
                empID = ""
                binding.crewBtn.text = getString(R.string.crews_everyone)
            }
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
        binding.scheduleSpinner.onItemSelectedListener = null

        super.onDestroyView()
    }

    override fun onDestroy() {
        println("onDestroy")
        super.onDestroy()
    }


}