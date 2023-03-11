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
import com.AdminMatic.databinding.FragmentEquipmentFieldsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


interface EquipmentFieldCellClickListener {
    fun onEquipmentFieldCellClickListener(data:EquipmentField)
}




class EquipmentFieldsFragment : Fragment(), EquipmentFieldCellClickListener {


    private lateinit var globalVars:GlobalVars
    private lateinit var myView:View

    private lateinit var typeAdapter:EquipmentFieldsAdapter
    private lateinit var fuelAdapter:EquipmentFieldsAdapter
    private lateinit var engineAdapter:EquipmentFieldsAdapter
    private lateinit var inspectionAdapter:EquipmentFieldsAdapter

    private var tableMode = 0


    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     */

    private var _binding: FragmentEquipmentFieldsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentFieldsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment_fields)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addFieldBtn.setOnClickListener{
            println("add field button clicked")
            val directions = EquipmentFieldsFragmentDirections.navigateToNewEditEquipmentField(null, tableMode)
            myView.findNavController().navigate(directions)
        }

        binding.tableLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tableMode = tab!!.position
                when (tab.position) {
                    0 -> {
                        binding.recyclerView.adapter = typeAdapter
                    }
                    1 -> {
                        binding.recyclerView.adapter = fuelAdapter
                    }
                    2 -> {
                        binding.recyclerView.adapter = engineAdapter
                    }
                    3 -> {
                        binding.recyclerView.adapter = inspectionAdapter
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        getEquipmentFields()
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("equipmentFields")
    }



    private fun getEquipmentFields(){
        println("getEquipmentFields")


        showProgressView()


        //val vendor

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipmentFields.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                hideProgressView()
                try {
                    val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val types: JSONArray = parentObject.getJSONArray("types")
                        val fuelTypes: JSONArray = parentObject.getJSONArray("fuelTypes")
                        val engineTypes: JSONArray = parentObject.getJSONArray("engineTypes")
                        val questions: JSONArray = parentObject.getJSONArray("questions")


                        val typesList = gson.fromJson(types.toString(), Array<EquipmentField>::class.java).toMutableList()
                        val fuelTypesList = gson.fromJson(fuelTypes.toString(), Array<EquipmentField>::class.java).toMutableList()
                        val engineTypesList = gson.fromJson(engineTypes.toString(), Array<EquipmentField>::class.java).toMutableList()
                        val questionsList = gson.fromJson(questions.toString(), Array<EquipmentField>::class.java).toMutableList()

                        println("Types size: ${typesList.size}")
                        println("Fuel size: ${fuelTypesList.size}")
                        println("Engine size: ${engineTypesList.size}")
                        println("Question size: ${questionsList.size}")
                        println(questions)

                        typeAdapter = EquipmentFieldsAdapter(typesList, requireActivity().application, "TYPE", this@EquipmentFieldsFragment)
                        fuelAdapter = EquipmentFieldsAdapter(fuelTypesList, requireActivity().application, "FUEL", this@EquipmentFieldsFragment)
                        engineAdapter = EquipmentFieldsAdapter(engineTypesList, requireActivity().application, "ENGINE", this@EquipmentFieldsFragment)
                        inspectionAdapter = EquipmentFieldsAdapter(questionsList, requireActivity().application, "inspection", this@EquipmentFieldsFragment)

                        /*
                        typeAdapter.notifyDataSetChanged()
                        fuelAdapter.notifyDataSetChanged()
                        engineAdapter.notifyDataSetChanged()
                        inspectionAdapter.notifyDataSetChanged()

                         */

                        binding.recyclerView.adapter = typeAdapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        binding.recyclerView.addItemDecoration(itemDecoration)

                        hideProgressView()
                        /*

                        //current adapter
                        val services: JSONArray = parentObject.getJSONArray("services")
                        println("services = $services")
                        println("services count = ${services.length()}")

                        val servicesListCurrent =
                            gson.fromJson(services.toString(), Array<EquipmentService>::class.java)
                                .toMutableList()
                        println("ServiceCount = ${servicesListCurrent.count()}")

                        currentServicesAdapter =
                            ServiceAdapter(servicesListCurrent, this.myView.context, false, this)

                        //history adapter
                        val servicesHistory: JSONArray = parentObject.getJSONArray("serviceHistory")
                        println("servicesHistory = $servicesHistory")
                        println("servicesHistory count = ${servicesHistory.length()}")

                        val servicesListHistory = gson.fromJson(
                            servicesHistory.toString(),
                            Array<EquipmentService>::class.java
                        ).toMutableList()
                        println("ServiceHistoryCount = ${servicesListHistory.count()}")

                        println(equipment!!.dealer)

                        historyServicesAdapter =
                            ServiceAdapter(servicesListHistory, this.myView.context, true, this)


                        binding.serviceRecyclerView.layoutManager =
                            LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)


                        //status
                        val equipmentJSON: JSONObject = parentObject.getJSONObject("equipment")

                        val equipmentNew =
                            gson.fromJson(equipmentJSON.toString(), Equipment::class.java)
                        equipment!!.status = equipmentNew.status
                        setStatus(equipment!!.status)

                        binding.serviceRecyclerView.adapter = currentServicesAdapter


                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        binding.serviceRecyclerView.addItemDecoration(itemDecoration)

                         */
                    }
                    /*
                    currentRecyclerView.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = activity?.let {
                            ServiceAdapter(servicesList,
                                it, this@EquipmentFragment)
                        }
                        (adapter as ServiceAdapter).notifyDataSetChanged();
                    }

                     */

                    /* Here 'response' is a String containing the response you received from the website... */

                    // getServicesHistory()


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
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipmentFields"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    override fun onEquipmentFieldCellClickListener(data:EquipmentField){
        println("onEquipmentFieldCellClickListener ${data.name}")

        val directions = EquipmentFieldsFragmentDirections.navigateToNewEditEquipmentField(data, tableMode)
        myView.findNavController().navigate(directions)

    }




    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}
