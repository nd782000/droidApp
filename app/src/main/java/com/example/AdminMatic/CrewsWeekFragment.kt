package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentCrewsBinding
import com.AdminMatic.databinding.FragmentCrewsWeekBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.*



class CrewDaySection(_dateString:String) {
    var dateString = _dateString
    var entries: MutableList<CrewSection> = mutableListOf()
}

class CrewsWeekFragment : Fragment(), CrewCellClickListener, CrewEntryCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var crewDaysAdapter:CrewDaysAdapter

    private var employee:Employee? = null

    var currentIteratedDate:LocalDate? = null

    private var crewList:MutableList<Crew> = mutableListOf()
    private var crewDaySections:MutableList<CrewDaySection> = mutableListOf()

    private var departmentValue = "0"
    private var dateValueDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
    private var dateValue = ""

    private var dataLoaded = false
    private var viewsLaidOut = false

    private var _binding: FragmentCrewsWeekBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_equipment, container, false)
        _binding = FragmentCrewsWeekBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()


        if (dateValue == "this week") {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_crews_for_this_week, employee!!.fname)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_crews_for_next_week, employee!!.fname)
        }


        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("Data loaded in onViewCreate: $dataLoaded")

        if (!dataLoaded) {
            getEmployeeCrews()
        }
        else {
            layoutViews()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
            dateValue = it.getString("dateValue")!!
        }
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("crewsWeek")
    }


    private fun getEmployeeCrews(){
        println("getEmployeeCrews")

        showProgressView()

        if (currentIteratedDate == null) { // Starting a new get
            crewDaySections.clear()
            if (dateValue == "this week") {
                currentIteratedDate = LocalDate.now().with(TemporalAdjusters.previousOrSame( DayOfWeek.SUNDAY ))
            }
            else { // next week
                currentIteratedDate = LocalDate.now().with(TemporalAdjusters.next( DayOfWeek.SUNDAY ))
            }
        }

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/employeeCrews.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val crews: JSONArray = parentObject.getJSONArray("crews")
                        println("crews = $crews")
                        println("crews count = ${crews.length()}")

                        val crewArrayTemp = gson.fromJson(crews.toString(), Array<Crew>::class.java)
                        println("crewArray count = ${crewArrayTemp.count()}")

                        crewList.clear()

                        crewArrayTemp.forEach {
                            if (it.subcolor == "" || it.subcolor == null) {
                                it.subcolor = it.color
                            }

                            it.emps!!.forEach { emp ->
                                emp.crewName = it.name
                                emp.crewColor = it.color
                            }

                            it.equipment!!.forEach { equip ->
                                equip.crewName = it.name
                                equip.crewColor = it.color
                            }

                            crewList.add(it)
                        }

                        //dataLoaded = true

                        val newCrewDaySection = CrewDaySection(GlobalVars.dateFormatterShort.format(currentIteratedDate))

                        crewList.forEach{

                            if (it.subcolor == null) {
                                it.subcolor = it.color
                            }

                            val crewSection = CrewSection(it.name, it.ID, it.subcolor!!)

                            it.emps!!.forEach { emp ->
                                crewSection.entries.add(EmployeeOrEquipment(emp))
                            }

                            it.equipment!!.forEach { equip ->
                                crewSection.entries.add(EmployeeOrEquipment(equip))
                            }

                            newCrewDaySection.entries.add(crewSection)
                        }

                        crewDaySections.add(newCrewDaySection)

                        if (currentIteratedDate!!.dayOfWeek == DayOfWeek.SATURDAY) {
                            currentIteratedDate = null
                            dataLoaded = true
                            if (!viewsLaidOut) {
                                layoutViews()
                            }
                            else {
                                binding.recyclerView.adapter!!.notifyDataSetChanged()
                                hideProgressView()
                            }
                        }
                        else {
                            currentIteratedDate = currentIteratedDate!!.plusDays(1)
                            getEmployeeCrews()
                        }
                    }

                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error
                //Log.e("VOLLEY", error.toString())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["empID"] = employee!!.ID
                params["date"] = GlobalVars.dateFormatterYYYYMMDD.format(currentIteratedDate)
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "crewsWeek"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun layoutViews() {

        println("layout views")
        println("crew day sections: ${crewDaySections.count()}")

        crewDaysAdapter = CrewDaysAdapter(
            crewDaySections,
            this@CrewsWeekFragment,
            this@CrewsWeekFragment,
        )

        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(
                myView.context,
                DividerItemDecoration.VERTICAL
            )
        binding.recyclerView.addItemDecoration(itemDecoration)


        binding.recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = crewDaysAdapter


        viewsLaidOut = true

        hideProgressView()

    }


    override fun onCrewCellClickListener(data: CrewSection) {
        println("crew ${data.name} tapped")

        if (data.ID != "0") {
            val directions = CrewsFragmentDirections.navigateToNewEditCrew(data.ID, crewList.toTypedArray(), true)
            myView.findNavController().navigate(directions)
        }

    }

    override fun onCrewEntryCellClickListener(data: EmployeeOrEquipment) {
        if (data.isEquipment) {
            println("${data.equipment!!.name} tapped")
            val directions = CrewsFragmentDirections.navigateToEquipment(data.equipment!!)
            myView.findNavController().navigate(directions)
        }
        else {
            println("${data.employee!!.name} tapped")
            val directions = CrewsFragmentDirections.navigateToEmployee(data.employee)
            myView.findNavController().navigate(directions)
        }
    }

    fun showProgressView() {
        binding.allCl.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.allCl.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

}
