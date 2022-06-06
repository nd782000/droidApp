package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.example.AdminMatic.GlobalVars.Companion.globalWorkOrdersList
import com.example.AdminMatic.GlobalVars.Companion.globalLeadList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View
    lateinit var  pgsBar: ProgressBar
    private lateinit var refreshBtn: Button
    private val markerList = mutableListOf<Marker>()
    private var mapFragment : SupportMapFragment?=null
    private val pinMapWorkOrder = HashMap<Marker?, WorkOrder>()
    private val pinMapLead = HashMap<Marker?, Lead>()
    private lateinit var googleMapGlobal:GoogleMap

    //Todo: change to an enum maybe, currently 0 = work orders and 1 = leads
    var mode:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mode = it.getInt("mode")
        }
        //activity!!.setContentView(R.layout.fragment_map)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_map, container, false)


        if (mode == 0){ // Work orders
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())
        }
        else { // Leads
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.lead_map)
        }

        mapFragment = childFragmentManager.findFragmentById(R.id.map_support_map_fragment) as SupportMapFragment?


        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //need to wait for this function to initialize views
        println("onViewCreated")

        (activity as MainActivity?)!!.setMap(this)


        pgsBar = view.findViewById(R.id.progressBar)
        refreshBtn = view.findViewById(R.id.map_refresh_btn)

        mapFragment!!.getMapAsync(this)

        hideProgressView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        println("map ready")
        googleMapGlobal = googleMap
        updateMap()

    }


    fun updateMap(){
        println("updateMap")

        var newMarker: Marker?
        var bitmapDescriptor: BitmapDescriptor?

        googleMapGlobal.clear()

        if (mode == 0) { // Work orders

            if (globalWorkOrdersList.isNullOrEmpty()) {
                return
            }

            globalWorkOrdersList!!.forEach {
                println("Marker: $it: ${it.lat}, ${it.lng}")
                bitmapDescriptor = when (it.status) {
                    "0" -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_skip)
                    }
                    "1" -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_not_started)
                    }
                    "2" -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_in_progress)
                    }
                    else -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_done)
                    }
                }

                newMarker = googleMapGlobal.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat!!.toDouble(), it.lng!!.toDouble()))
                        .title(it.custName + " - " + it.title)
                        .snippet(it.custAddress)
                        .icon(bitmapDescriptor)

                )
                newMarker?.let { it1 -> markerList.add(it1) }
                pinMapWorkOrder[newMarker] = it
            }

            googleMapGlobal.setOnInfoWindowClickListener { marker ->
                val targetWorkOrder = pinMapWorkOrder[marker]
                val directions = WorkOrderListFragmentDirections.navigateToWorkOrder(targetWorkOrder)
                directions.listIndex = globalWorkOrdersList!!.indexOf(targetWorkOrder)
                myView.findNavController().navigate(directions)
            }

            refreshBtn.setOnClickListener {
                (activity as MainActivity?)!!.refreshWorkOrders()
            }
        }
        else { // Leads

            if (globalLeadList.isNullOrEmpty()) {
                return
            }


            googleMapGlobal.setOnInfoWindowClickListener { marker ->
                val directions =
                    LeadListFragmentDirections.navigateToLead(pinMapLead[marker])
                myView.findNavController().navigate(directions)
            }

            globalLeadList!!.forEach {

                bitmapDescriptor = when (it.statusID) {
                    "0" -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_skip)
                    }
                    "1" -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_not_started)
                    }
                    "2" -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_in_progress)
                    }
                    else -> {
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_done)
                    }
                }

                newMarker = googleMapGlobal.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat!!.toDouble(), it.lng!!.toDouble()))
                        .title(it.custName)
                        .snippet(it.description)
                        .icon(bitmapDescriptor)

                )
                newMarker?.let { it1 -> markerList.add(it1) }
                pinMapLead[newMarker] = it
            }

            refreshBtn.setOnClickListener {
                (activity as MainActivity?)!!.refreshWorkOrders()
            }
        }

        val builder = LatLngBounds.Builder()
        println("Marker List Size: ${markerList.size}")
        for (marker in markerList) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)

        googleMapGlobal.moveCamera(cu)

    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }

}