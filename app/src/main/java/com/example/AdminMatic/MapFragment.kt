package com.example.AdminMatic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentMapBinding
import com.example.AdminMatic.GlobalVars.Companion.globalLeadList
import com.example.AdminMatic.GlobalVars.Companion.globalWorkOrdersList
import com.google.android.gms.maps.*
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.*


class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View
    private val markerList = mutableListOf<Marker>()
    private val pinMapWorkOrder = HashMap<Marker?, WorkOrder>()
    private val pinMapLead = HashMap<Marker?, Lead>()
    private lateinit var googleMapGlobal:GoogleMap
    private var mapFragment : SupportMapFragment? = null

    private var dataLoaded = false

    //Todo: change to an enum maybe, currently 0 = work orders and 1 = leads
    var mode:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mode = it.getInt("mode")
        }
        //activity!!.setContentView(R.layout.fragment_map)
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        myView = binding.root

        if (!dataLoaded) {
            if (mode == 0) { // Work orders
                ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
                    getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())
            } else { // Leads
                ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
                    getString(R.string.lead_map)
            }

            mapFragment = childFragmentManager.findFragmentById(R.id.map_support_map_fragment) as SupportMapFragment?
        }

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //need to wait for this function to initialize views
        println("onViewCreated")

        (activity as MainActivity?)!!.setMap(this)

        //binding.mapFrg.getMapAsync(this)
        if (!dataLoaded) {
            mapFragment!!.getMapAsync(this)
        }

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

        googleMapGlobal.clear()

        if (mode == 0) { // Work orders

            if (globalWorkOrdersList.isNullOrEmpty()) {
                return
            }

            globalWorkOrdersList!!.forEach {
                println("Marker: $it: ${it.lat}, ${it.lng}")

                val markerLayout: View = layoutInflater.inflate(R.layout.map_pin_layout, null)

                val markerImage: ImageView =
                    markerLayout.findViewById<View>(R.id.marker_image) as ImageView
                val markerNumber = markerLayout.findViewById<View>(R.id.marker_text) as TextView

                when (it.status) {
                    "0" -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_skip)
                    }
                    "1" -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                    }
                    "2" -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_in_progress)
                    }
                    else -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_done)
                    }
                }

                if (!it.daySort.isNullOrBlank()) {
                    if (it.daySort == "0") {
                        markerNumber.text = "-"
                    }
                    else {
                        markerNumber.text = it.daySort
                        markerImage.setImageResource(R.drawable.ic_map_pin_numbered)
                    }

                }
                else {
                    markerNumber.text = "-"
                }

                markerLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                    ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                markerLayout.layout(0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight)


                val bitmap = Bitmap.createBitmap(
                    markerLayout.measuredWidth,
                    markerLayout.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                markerLayout.draw(canvas)


                newMarker = googleMapGlobal.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat!!.toDouble(), it.lng!!.toDouble()))
                        .title(it.custName + " - " + it.title)
                        .snippet(it.custAddress)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

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

            binding.mapRefreshBtn.setOnClickListener {
                //todo: implement
                //(activity as MainActivity?)!!.refreshWorkOrders()
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

                val markerLayout: View = layoutInflater.inflate(R.layout.map_pin_layout, null)

                val markerImage: ImageView =
                    markerLayout.findViewById<View>(R.id.marker_image) as ImageView
                val markerNumber = markerLayout.findViewById<View>(R.id.marker_text) as TextView

                when (it.statusID) {
                    "0" -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_skip)
                    }
                    "1" -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                    }
                    "2" -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_in_progress)
                    }
                    else -> {
                        markerImage.setImageResource(R.drawable.ic_map_pin_done)
                    }
                }

                markerNumber.text = "-"

                markerLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                    ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                markerLayout.layout(0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight)


                val bitmap = Bitmap.createBitmap(
                    markerLayout.measuredWidth,
                    markerLayout.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                markerLayout.draw(canvas)

                newMarker = googleMapGlobal.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat!!.toDouble(), it.lng!!.toDouble()))
                        .title(it.custName)
                        .snippet(it.description)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

                )
                newMarker?.let { it1 -> markerList.add(it1) }
                pinMapLead[newMarker] = it
            }

            binding.mapRefreshBtn.setOnClickListener {
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

        println("Marker list size: ${markerList.size}")

        dataLoaded = true



    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
    }

}