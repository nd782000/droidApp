package com.example.AdminMatic

import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EquipmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


interface ServiceCellClickListener {
    fun onServiceCellClickListener(data:EquipmentService)
}


class EquipmentFragment : Fragment(), ServiceCellClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private  var equipment: Equipment? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    lateinit var currentRecyclerView: RecyclerView
    lateinit var historyRecyclerView: RecyclerView

    lateinit var equipmentImageView: ImageView
    lateinit var nameTxt:TextView
    lateinit var typeTxt:TextView
    lateinit var crewTxt:TextView
    lateinit var detailsBtn:Button

    lateinit var tabLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable<Equipment?>("equipment")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_equipment, container, false)
        myView = inflater.inflate(R.layout.fragment_equipment, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Equipment"



        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("equipment = ${equipment!!.name}")


        pgsBar = view.findViewById(R.id.progress_bar)
        currentRecyclerView = view.findViewById(R.id.service_recycler_view)
        historyRecyclerView = view.findViewById(R.id.service_history_recycler_view)
        nameTxt = myView.findViewById(R.id.equipment_name_txt)
        typeTxt = myView.findViewById(R.id.equipment_type_txt)
        crewTxt = myView.findViewById(R.id.equipment_crew_txt)
        equipmentImageView = myView.findViewById(R.id.equipment_pic_iv)
        detailsBtn = view.findViewById(R.id.equipment_details_btn)
        tabLayout = myView.findViewById(R.id.equipment_table_tl)

        Picasso.with(context)
            .load("${GlobalVars.thumbBase + equipment!!.image!!.fileName}")
            .placeholder(R.drawable.ic_images) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(equipmentImageView)                       //Your image view object.

        if(equipment!!.name != null){
            nameTxt.text = equipment!!.name
        }

        if(equipment!!.typeName != null){
            typeTxt.text = equipment!!.typeName
        }

        if(equipment!!.crewName != null){
            crewTxt.text = "Crew: " + equipment!!.crewName
        }

        detailsBtn.setOnClickListener{
            println("details btn clicked")
            val directions = EquipmentFragmentDirections.navigateToEquipmentDetails(equipment)
            myView.findNavController().navigate(directions)
        }




        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        //tableMode = "LEADS"
                        // Toast.makeText(com.example.AdminMatic.myView.context, "Leads", Toast.LENGTH_SHORT).show()
                        currentRecyclerView.visibility = View.VISIBLE
                        historyRecyclerView.visibility = View.GONE
                    }
                    1 -> {
                        //tableMode = "CONTRACTS"
                        //Toast.makeText(com.example.AdminMatic.myView.context, "Contracts", Toast.LENGTH_SHORT).show()
                        currentRecyclerView.visibility = View.GONE
                        historyRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        getServicesCurrent()

    }


    fun getServicesCurrent() {

        println("getServicesCurrent")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/equipment.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "${"$urlString?cb=$currentTimestamp"}"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                hideProgressView()
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = ${parentObject.toString()}")
                    var services:JSONArray = parentObject.getJSONArray("services")
                    println("services = ${services.toString()}")
                    println("services count = ${services.length()}")

                    val gson = GsonBuilder().create()
                    val servicesList = gson.fromJson(services.toString() , Array<EquipmentService>::class.java).toMutableList()
                    println("ServiceCount = ${servicesList.count()}")

                    val currentServicesAdapter = ServiceAdapter(servicesList,this.myView.context,this)

                    currentRecyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                    currentRecyclerView.adapter = currentServicesAdapter

                    val itemDecoration: RecyclerView.ItemDecoration =
                        DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                    currentRecyclerView.addItemDecoration(itemDecoration)

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

                    getServicesHistory()


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
                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)

    }

    fun getServicesHistory() {

        println("getServicesHistory")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/equipment.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "${"$urlString?cb=$currentTimestamp"}"
        val queue = Volley.newRequestQueue(myView.context)

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                hideProgressView()
                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = ${parentObject.toString()}")
                    var services:JSONArray = parentObject.getJSONArray("serviceHistory")
                    println("services = ${services.toString()}")
                    println("services count = ${services.length()}")

                    val gson = GsonBuilder().create()
                    val servicesList = gson.fromJson(services.toString() , Array<EquipmentService>::class.java).toMutableList()
                    println("ServiceCount = ${servicesList.count()}")

                    val historyServicesAdapter = ServiceHistoryAdapter(servicesList,this.myView.context,this)

                    historyRecyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                    historyRecyclerView.adapter = historyServicesAdapter

                    val itemDecoration: RecyclerView.ItemDecoration =
                        DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                    historyRecyclerView.addItemDecoration(itemDecoration)

                    /* Here 'response' is a String containing the response you received from the website... */
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
                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)
    }


    override fun onServiceCellClickListener(data:EquipmentService){
        println("onServiceCellClickListener ${data.ID}")
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        currentRecyclerView.visibility = View.INVISIBLE
        historyRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        currentRecyclerView.visibility = View.VISIBLE
        historyRecyclerView.visibility = View.VISIBLE
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EquipmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EquipmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}