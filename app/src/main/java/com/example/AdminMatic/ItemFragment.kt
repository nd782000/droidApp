package com.example.AdminMatic

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_item.*
import kotlinx.android.synthetic.main.fragment_work_order.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ItemFragment : Fragment(), OnMapReadyCallback, VendorCellClickListener, WorkOrderCellClickListener {

    private  var item: Item? = null

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var itemNameTv:TextView
    lateinit var itemPriceTv:TextView
    lateinit var itemDescriptionTv:TextView
    lateinit var itemTypeTv:TextView
    lateinit var itemTaxTv:TextView
    lateinit var footerTv:TextView
    lateinit var recyclerView: RecyclerView

    lateinit var vendorsAdapter: ItemVendorsAdapter
    lateinit var workOrdersAdapter: ItemWorkOrdersAdapter

    lateinit var pgsBar: ProgressBar
    lateinit var allCl: ConstraintLayout
    lateinit var mapCl: ConstraintLayout

    var remainingQty = 0.0

    private val markerList = mutableListOf<Marker>()
    private val pinMapVendor = HashMap<Marker?, Vendor>()
    private var mapFragment : SupportMapFragment? = null
    private lateinit var googleMapGlobal:GoogleMap


    private lateinit var tabLayout: TabLayout
    private var tableMode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_item, container, false)
        myView = inflater.inflate(R.layout.fragment_item, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.item)
        mapFragment = childFragmentManager.findFragmentById(R.id.map_support_map_fragment) as SupportMapFragment?

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("item name = ${item!!.name}")
        println("item id = ${item!!.ID}")
        println("item price = ${item!!.price}")

        pgsBar = view.findViewById(R.id.progress_bar)
        allCl = view.findViewById(R.id.all_cl)
        mapCl = view.findViewById(R.id.item_map_cl)

        itemNameTv = view.findViewById(R.id.item_name_tv)
        itemPriceTv = view.findViewById(R.id.item_price_tv)
        itemDescriptionTv = view.findViewById(R.id.item_description_tv)
        itemTypeTv = view.findViewById(R.id.item_type_tv)
        itemTaxTv = view.findViewById(R.id.item_tax_tv)
        footerTv = view.findViewById(R.id.item_footer_tv)
        recyclerView = view.findViewById(R.id.item_recycler_view)

        itemNameTv.text = item!!.name
        itemPriceTv.text = getString(R.string.item_price_each, item!!.price, item!!.unit)
        if (GlobalVars.permissions!!.itemsMoney == "0") {
            itemPriceTv.visibility = View.GONE
        }


        if (item!!.salesDescription.isNullOrEmpty()) {
            itemDescriptionTv.text = getString(R.string.no_description_provided)
        }
        else {
            itemDescriptionTv.text = item!!.salesDescription
        }

        when (item!!.typeID) {
            "1" -> {
                itemTypeTv.text = getString(R.string.item_labor_type)
            }
            "2" -> {
                itemTypeTv.text = getString(R.string.item_material_type)
            }
            else -> {
                itemTypeTv.text = getString(R.string.item_other_type)
            }
        }

        //itemTypeTv.text = item!!.type

        if (item!!.tax == "1") {
            itemTaxTv.text = getString(R.string.taxable_yes)
        }
        else {
            itemTaxTv.text = getString(R.string.taxable_no)
        }

        mapFragment!!.getMapAsync(this)


        tabLayout = view.findViewById(R.id.item_tab_layout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = 0
                        mapCl.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE

                        //serviceRecyclerView.adapter = currentServicesAdapter
                    }
                    1 -> {
                        tableMode = 1
                        mapCl.visibility = View.INVISIBLE
                        recyclerView.visibility = View.VISIBLE
                        footerTv.text = getString(R.string.item_x_vendors, item!!.vendors!!.size.toString(), item!!.name)
                        recyclerView.adapter = vendorsAdapter

                    }
                    2 -> {
                        tableMode = 2
                        mapCl.visibility = View.INVISIBLE
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.adapter = workOrdersAdapter

                        footerTv.text = getString(R.string.item_x_work_orders, item!!.workOrders!!.size.toString(), remainingQty.toString(), item!!.unit)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("item")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        println("map ready")
        googleMapGlobal = googleMap

        getItem()

    }

    private fun updateMap(){
        println("updateMap")

        var newMarker: Marker?

        googleMapGlobal.clear()

        if (item!!.vendors!!.isNullOrEmpty()) {
            return
        }


        item!!.vendors!!.forEach {
            println("Marker: $it: ${it.lat}, ${it.lng}")

            newMarker = googleMapGlobal.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat!!.toDouble(), it.lng!!.toDouble()))
                    .title(getString(R.string.item_price_each, it.cost, item!!.unit))
                    .snippet(it.name)
            )
            newMarker?.let { it1 -> markerList.add(it1) }
            pinMapVendor[newMarker] = it
        }

        /*
        googleMapGlobal.setOnInfoWindowClickListener { marker ->
            val targetWorkOrder = pinMapVendor[marker]
            val directions = WorkOrderListFragmentDirections.navigateToWorkOrder(targetWorkOrder)
            directions.listIndex = GlobalVars.globalWorkOrdersList!!.indexOf(targetWorkOrder)
            myView.findNavController().navigate(directions)
        }
         */


        val builder = LatLngBounds.Builder()
        println("Marker List Size: ${markerList.size}")
        for (marker in markerList) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        var cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
        if (markerList.size == 1) {
            // If there's only one pin, zoom out a little
            cu = CameraUpdateFactory.newLatLngZoom(markerList[0].position, 16f)
        }


        googleMapGlobal.moveCamera(cu)

        hideProgressView()

    }

    override fun onVendorCellClickListener(data:Vendor) {
        data.let {
            val directions = ItemFragmentDirections.navigateItemToVendor(null, it.ID)
            myView.findNavController().navigate(directions)
        }
    }

    override fun onWorkOrderCellClickListener(data:WorkOrder, listIndex:Int) {
        data.let {
            val directions = ItemFragmentDirections.navigateItemToWorkOrder(it)
            directions.listIndex = listIndex
            myView.findNavController().navigate(directions)
        }
    }

    private fun getItem(){
        showProgressView()

        // get/item.php is bugged and doesn't store the item ID, so this is a band aid fix for now
        val storedItemID = item!!.ID

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/item.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                   //val testWarnings =
                   //     "{\"warningArray\":[\"here's a warning\", \"here's another\"],\"errorArray\":[],\"items\":[{\"type\":\"2\",\"name\":\"1 Poly Elbow Connector\",\"purchaseDescription\":\"697\",\"salesDescription\":\"\",\"cost\":\"0.40\",\"price\":\"0.60\",\"tax\":\"1\",\"unit\":\"Piece\",\"remQty\":\"0\",\"description\":\"\",\"workOrders\":[],\"vendors\":[{\"ID\":\"1243\",\"cost\":\"0.40\",\"price\":\"0.60\",\"minQty\":\"0.00\",\"prefered\":\"1\",\"name\":\"Stateline Irrigation\",\"lat\":\"42.66918460\",\"lng\":\"-71.41806220\"}]}]}"

                    val parentObject = JSONObject(response)
                    //val parentObject = JSONObject(testWarnings)

                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

                    println("parentObject = $parentObject")
                    val items: JSONArray = parentObject.getJSONArray("items")
                    println("items = $items")
                    println("items count = ${items.length()}")

                    val gson = GsonBuilder().create()






                    val itemArray = gson.fromJson(items.toString(), Array<Item>::class.java).toMutableList()
                    item = itemArray[0]
                    item!!.ID = storedItemID

                    remainingQty = 0.0
                    item!!.workOrders!!.forEach {
                        remainingQty += it.remQty!!.toDouble()
                    }

                    vendorsAdapter = ItemVendorsAdapter(item!!.vendors!!.toMutableList(), this.myView.context, item!!.unit!!, this)
                    workOrdersAdapter = ItemWorkOrdersAdapter(item!!.workOrders!!.toMutableList(), this.myView.context, item!!.unit!!, this)
                    recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                    val itemDecoration: RecyclerView.ItemDecoration =
                        DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                    recyclerView.addItemDecoration(itemDecoration)

                    if (item!!.vendors!!.isEmpty()) {
                        globalVars.simpleAlert(myView.context,getString(R.string.dialogue_item_no_vendors_title),getString(R.string.dialogue_item_no_vendors_body))
                        tabLayout.selectTab(tabLayout.getTabAt(2))
                        tabLayout.getTabAt(0)?.view?.isClickable = false
                        tabLayout.getTabAt(1)?.view?.isClickable = false
                        hideProgressView()
                    }
                    else {
                        updateMap()
                    }





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
                params["itemID"] = item!!.ID

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "item"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        allCl.visibility = View.VISIBLE
    }

}