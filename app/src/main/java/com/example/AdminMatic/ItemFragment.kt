package com.example.AdminMatic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentItemBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ItemFragment : Fragment(), OnMapReadyCallback, VendorCellClickListener, WorkOrderCellClickListener {

    private  var item: Item? = null

    private lateinit var globalVars:GlobalVars
    private lateinit var myView:View

    private lateinit var vendorsAdapter: ItemVendorsAdapter
    private lateinit var workOrdersAdapter: ItemWorkOrdersAdapter

    private var remainingQty = 0.0

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

    private var _binding: FragmentItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemBinding.inflate(inflater, container, false)
        myView = binding.root

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


        binding.itemNameTv.text = item!!.name
        binding.itemPriceTv.text = getString(R.string.item_price_each, item!!.price, item!!.unit)
        if (GlobalVars.permissions!!.itemsMoney == "0") {
            binding.itemPriceTv.visibility = View.GONE
        }


        if (item!!.salesDescription.isNullOrEmpty()) {
            binding.itemDescriptionTv.text = getString(R.string.no_description_provided)
        }
        else {
            binding.itemDescriptionTv.text = item!!.salesDescription
        }

        when (item!!.typeID) {
            "1" -> {
                binding.itemTypeTv.text = getString(R.string.item_labor_type)
            }
            "2" -> {
                binding.itemTypeTv.text = getString(R.string.item_material_type)
            }
            else -> {
                binding.itemTypeTv.text = getString(R.string.item_other_type)
            }
        }

        //itemTypeTv.text = item!!.type

        if (item!!.tax == "1") {
            binding.itemTaxTv.text = getString(R.string.taxable_yes)
        }
        else {
            binding.itemTaxTv.text = getString(R.string.taxable_no)
        }

        mapFragment!!.getMapAsync(this)


        tabLayout = view.findViewById(R.id.item_tab_layout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        tableMode = 0
                        binding.itemMapCl.visibility = View.VISIBLE
                        binding.itemRecyclerView.visibility = View.INVISIBLE

                        //serviceRecyclerView.adapter = currentServicesAdapter
                    }
                    1 -> {
                        tableMode = 1
                        binding.itemMapCl.visibility = View.INVISIBLE
                        binding.itemRecyclerView.visibility = View.VISIBLE
                        binding.itemFooterTv.text = getString(R.string.item_x_vendors, item!!.vendors!!.size.toString(), item!!.name)
                        binding.itemRecyclerView.adapter = vendorsAdapter

                    }
                    2 -> {
                        tableMode = 2
                        binding.itemMapCl.visibility = View.INVISIBLE
                        binding.itemRecyclerView.visibility = View.VISIBLE
                        binding.itemRecyclerView.adapter = workOrdersAdapter

                        binding.itemFooterTv.text = getString(R.string.item_x_work_orders, item!!.workOrders!!.size.toString(), remainingQty.toString(), item!!.unit)
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

        if (item!!.vendors!!.isEmpty()) {
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

                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        println("parentObject = $parentObject")
                        val items: JSONArray = parentObject.getJSONArray("items")
                        println("items = $items")
                        println("items count = ${items.length()}")

                        val gson = GsonBuilder().create()


                        val itemArray = gson.fromJson(items.toString(), Array<Item>::class.java).toMutableList()
                        item = itemArray[0]
                        item!!.ID = storedItemID

                        if (item!!.unit == null) {
                            item!!.unit = ""
                        }

                        remainingQty = 0.0
                        item!!.workOrders!!.forEach {
                            remainingQty += it.remQty!!.toDouble()
                        }
                        println("Vendors: ${item!!.unit!!}")
                        vendorsAdapter = ItemVendorsAdapter(item!!.vendors!!.toMutableList(), this.myView.context, item!!.unit!!, this)

                        workOrdersAdapter = ItemWorkOrdersAdapter(item!!.workOrders!!.toMutableList(), this.myView.context, item!!.unit!!, this)
                        binding.itemRecyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
                        binding.itemRecyclerView.addItemDecoration(itemDecoration)

                        if (item!!.vendors!!.isEmpty()) {
                            globalVars.simpleAlert(
                                myView.context,
                                getString(R.string.dialogue_item_no_vendors_title),
                                getString(R.string.dialogue_item_no_vendors_body)
                            )
                            tabLayout.selectTab(tabLayout.getTabAt(2))
                            tabLayout.getTabAt(0)?.view?.isClickable = false
                            tabLayout.getTabAt(1)?.view?.isClickable = false
                            hideProgressView()
                        } else {
                            updateMap()
                        }
                    }
                    else {
                        hideProgressView()
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
        binding.progressBar.visibility = View.VISIBLE
        binding.allCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.allCl.visibility = View.VISIBLE
    }

}