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
import com.AdminMatic.databinding.FragmentEquipmentBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface ServiceCellClickListener {
    fun onServiceCellClickListener(data:EquipmentService)
}


class EquipmentFragment : Fragment(), ServiceCellClickListener {

    private var equipment: Equipment? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var currentServicesAdapter:ServiceAdapter
    lateinit var historyServicesAdapter:ServiceAdapter

    lateinit var tableMode:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable("equipment")
        }
    }

    private var _binding: FragmentEquipmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.equipment)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("equipment = ${equipment!!.name}")

        binding.equipmentStatusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        binding.addServiceBtn.setOnClickListener{
            println("status btn clicked")

            if (GlobalVars.permissions!!.equipmentEdit == "1") {
                val directions = EquipmentFragmentDirections.navigateToNewService(equipment)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment_edit))
            }
        }

        if (equipment!!.image != null) {
            Picasso.with(context)
                .load(GlobalVars.thumbBase + equipment!!.image!!.fileName)
                .placeholder(R.drawable.ic_images) //optional
                //.resize(imgWidth, imgHeight)         //optional
                //.centerCrop()                        //optional
                .into(binding.equipmentPicIv)                       //Your image view object.
        }




        binding.equipmentNameTxt.text = equipment!!.name

        if(equipment!!.typeName != null){
            binding.equipmentTypeTxt.text = equipment!!.typeName
        }

        if(equipment!!.crewName != null){
            binding.equipmentCrewTxt.text = getString(R.string.equipment_crew_display, equipment!!.crewName)
        }

        binding.equipmentDetailsBtn.setOnClickListener{
            println("details btn clicked")
            val directions = EquipmentFragmentDirections.navigateToEquipmentDetails(equipment)
            myView.findNavController().navigate(directions)
        }

        //setStatus(equipment!!.status)




        binding.equipmentTableTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = "CURRENT"

                        binding.serviceRecyclerView.adapter = currentServicesAdapter
                    }
                    1 -> {

                        binding.serviceRecyclerView.adapter = historyServicesAdapter
                        tableMode = "HISTORY"
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


        tableMode = "CURRENT"
        getServiceInfo()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("equipment")
    }



    private fun getServiceInfo(){
        println("getServiceInfo")


        showProgressView()


        //val vendor

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/equipment.php"

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
                if (equipment != null){
                    params["equipmentID"] = equipment!!.ID
                }
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "equipment"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onServiceCellClickListener(data:EquipmentService){
        println("onServiceCellClickListener ${data.ID}")

        var historyMode = false
        if (tableMode == "HISTORY") {
            historyMode = true
        }

        if (data.type == "4") { // Go to the special inspection fragment if the service type is inspection
            val directions = EquipmentFragmentDirections.navigateToServiceInspection(data, equipment, historyMode)
            myView.findNavController().navigate(directions)
        }
        else {
            val directions = EquipmentFragmentDirections.navigateToService(data, historyMode)
            myView.findNavController().navigate(directions)
        }




        /*
        if (tableMode == "CURRENT"){
            println("Show service in current mode")
            val directions = EquipmentFragmentDirections.navigateToService(data)
            myView.findNavController().navigate(directions)
        }else{
            println("Show service in history mode")

        }
        */
    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.equipmentStatusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 0, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_online)!!,myView.context), myView.context.getString(R.string.equipment_status_online)))
        popUp.menu.add(0, 1, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_needs_repair)!!,myView.context), myView.context.getString(R.string.equipment_status_needs_repair)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_broken)!!,myView.context), myView.context.getString(R.string.equipment_status_broken)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_winterized)!!,myView.context), myView.context.getString(R.string.equipment_status_winterized)))
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            equipment!!.status = item!!.itemId.toString()

            setStatus(equipment!!.status)
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT)
                .show()


            showProgressView()

            var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentStatus.php"

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
                        }

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
                    val params: MutableMap<String, String> = java.util.HashMap()
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["status"] = equipment!!.status
                    params["equipmentID"] = equipment!!.ID
                    println("params = $params")
                    return params
                }
            }
            postRequest1.tag = "equipment"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)


            true


        }

        popUp.gravity = Gravity.START
        popUp.show()
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "0" -> {
                println("0")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_online)
            }
            "1" -> {
                println("1")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_needs_repair)
            }
            "2" -> {
                println("2")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_broken)
            }
            "3" -> {
                println("3")
                binding.equipmentStatusBtn.setBackgroundResource(R.drawable.ic_winterized)
            }
        }
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.serviceRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.serviceRecyclerView.visibility = View.VISIBLE
    }

}
