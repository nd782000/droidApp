package com.example.AdminMatic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentPlannedDatesBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.util.Timer
import kotlin.concurrent.schedule


interface PlannedDateDelegate {
    fun flagEditsMade(_value: Boolean)
    fun reloadRecycler()
}


class PlannedDateSection(_workOrderID:String?, _plannedDate: String?, _firm: String?) {
    var workOrderID: String? = _workOrderID
    var plannedDate: String? = _plannedDate
    var firm: String? = _firm
    var rows: MutableList<PlannedDate> = mutableListOf()
}


class PlannedDatesFragment : Fragment(), PlannedDateDelegate {

    private var workOrder: WorkOrder? = null
    private val plannedDateSectionsList = mutableListOf<PlannedDateSection>()
    private val plannedDatesListFiltered = mutableListOf<PlannedDate>()
    private val plannedDatesListOld = mutableListOf<PlannedDate>()

    private var editsMade = false
    private var sectionAdapter:PlannedDateSectionAdapter? = null


    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workOrder = it.getParcelable("workOrder")
        }
    }

    private var _binding: FragmentPlannedDatesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlannedDatesBinding.inflate(inflater, container, false)
        myView = binding.root

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade){
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

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.xs_planned_dates, workOrder!!.woID)

        return myView
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("plannedDates")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Flag edits made false after all the views have time to set their states
        Timer("PlannedDatesEditsMade", false).schedule(500) {
            editsMade = false
        }

        binding.addDateBtn.setOnClickListener {

            var newDate = LocalDate.now()
            if (plannedDateSectionsList.isNotEmpty()) {
                newDate = LocalDate.parse(plannedDateSectionsList.last().plannedDate, GlobalVars.dateFormatterYYYYMMDD)
            }


            var newDateString = ""
            var foundDate = true

            while (foundDate) {
                foundDate = false
                newDate = newDate.plusDays(1)
                newDateString = newDate.format(GlobalVars.dateFormatterYYYYMMDD)
                println(newDateString)
                plannedDateSectionsList.forEach {
                    if (it.plannedDate == newDateString) {
                        foundDate = true
                    }
                }
            }

            val newSection = PlannedDateSection(workOrder!!.woID, newDateString, "0")
            val newRow = PlannedDate(newSection.workOrderID, "0", newSection.plannedDate, newSection.firm, "", "", "0", "0")
            newSection.rows.add(newRow)
            plannedDateSectionsList.add(newSection)
            //binding.noPlannedDatesTv.visibility = View.INVISIBLE
            sectionAdapter!!.notifyDataSetChanged()
        }

        binding.submitBtn.setOnClickListener {
            updatePlannedDates()
        }

        getPlannedDates()

    }

    private fun getPlannedDates(){
        println("getPlannedDates")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/plannedDates.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val plannedDates: JSONArray = parentObject.getJSONArray("plannedDates")
                        println("plannedDates = $plannedDates")
                        println("plannedDates count = ${plannedDates.length()}")

                        val gson = GsonBuilder().create()
                        val plannedDatesList =
                            gson.fromJson(plannedDates.toString(), Array<PlannedDate>::class.java)
                                .toMutableList()

                        plannedDatesList.forEach {

                            if (it.startTime == null) {
                                it.startTime = ""
                            }
                            if (it.endTime == null) {
                                it.endTime = ""
                            }

                            if (LocalDate.parse(it.plannedDate, GlobalVars.dateFormatterYYYYMMDD).dayOfYear < LocalDate.now().dayOfYear) {
                                plannedDatesListOld.add(it)
                            }
                            else {
                                plannedDatesListFiltered.add(it)
                            }
                        }


                        plannedDatesListFiltered.forEach { pd ->
                            var foundDate = false
                            plannedDateSectionsList.forEach { pds ->
                                if (pds.plannedDate == pd.plannedDate) {
                                    foundDate = true
                                    pds.rows.add(pd)
                                    println("Found the section, adding the row")
                                }
                            }
                            if (!foundDate) {
                                val newPlannedDateSection = PlannedDateSection(pd.workOrderID, pd.plannedDate, pd.firm)
                                newPlannedDateSection.rows.add(pd)
                                plannedDateSectionsList.add(newPlannedDateSection)
                                println("Didn't find the section, creating it and adding the row")
                            }
                        }

                        sectionAdapter = PlannedDateSectionAdapter(plannedDateSectionsList, myView.context, this@PlannedDatesFragment)
                        binding.recyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = sectionAdapter

                            //(adapter as WoItemsAdapter).notifyDataSetChanged()
                        }


                        /*
                        if (plannedDateSectionsList.isEmpty()) {
                            binding.noPlannedDatesTv.visibility = View.VISIBLE
                        }

                         */



                        binding.oldDatesTv.text = getString(R.string.planned_dates_x_old, plannedDatesListOld.count())

                        hideProgressView()

                    }




                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }


                // var intent:Intent = Intent(applicationContext,MainActivity2::class.java)
                // startActivity(intent)
            },
            Response.ErrorListener { // error


                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
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
        postRequest1.tag = "plannedDates"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    private fun updatePlannedDates() {
        println("getPlannedDates")



        // Build the list of planned dates to send back to the server, starting with the old ones
        val plannedDatesData: MutableList<PlannedDate> = plannedDatesListOld
        plannedDateSectionsList.forEach { pds ->
            pds.rows.forEach {
                plannedDatesData.add(it)
            }
        }

        plannedDatesData.forEach {
            if (it.crewID == "0") {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_error),getString(R.string.dialogue_planned_date_no_crew))
                return
            }
        }

        showProgressView()

        val gsonPretty = GsonBuilder().serializeNulls().setPrettyPrinting().create()
        val jsonUsagePretty: String = gsonPretty.toJson(plannedDatesData)

        println("Planned dates data to string: $jsonUsagePretty")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/plannedDates.php"

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
                        globalVars.playSaveSound(myView.context)
                        myView.findNavController().navigateUp()
                    }

                    /* Here 'response' is a String containing the response you received from the website... */
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }


                // var intent:Intent = Intent(applicationContext,MainActivity2::class.java)
                // startActivity(intent)
            },
            Response.ErrorListener { // error


                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["woID"] = workOrder!!.woID
                params["plannedDates"] = jsonUsagePretty
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "plannedDates"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

    override fun flagEditsMade(_value: Boolean) {
        editsMade = _value
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reloadRecycler() {
        for (i in plannedDateSectionsList.indices.reversed()) {
            if (plannedDateSectionsList[i].rows.isEmpty()) {
                plannedDateSectionsList.removeAt(i)
            }
        }

        sectionAdapter!!.notifyDataSetChanged()

        if (plannedDateSectionsList.size > 0) {
            binding.noPlannedDatesTv.visibility = View.INVISIBLE
        }
        else {
            binding.noPlannedDatesTv.visibility = View.VISIBLE
        }
    }


}
