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
import com.example.AdminMatic.GlobalVars.Companion.globalMyScheduleSections
import com.example.AdminMatic.GlobalVars.Companion.globalWorkOrdersList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

enum class MapViewMode {
    workOrders, leads, mySchedule
}

class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View
    private val markerList = mutableListOf<Marker>()
    private val pinMapWorkOrder = HashMap<Marker?, WorkOrder>()
    private val pinMapLead = HashMap<Marker?, Lead>()
    private val pinMapMyScheduleEntry = HashMap<Marker?, MyScheduleEntry>()
    private lateinit var googleMapGlobal:GoogleMap
    private var fName = ""
    private var mapFragment : SupportMapFragment? = null
    private var showCompleted = false

    private var dataLoaded = false

    //Todo: change to an enum maybe; currently 0 = work orders, 1 = leads, 2 = my schedule
    var mode:MapViewMode = MapViewMode.workOrders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val modeInt = it.getInt("mode")

            mode = when (modeInt) {
                0 -> {
                    MapViewMode.workOrders
                }
                1 -> {
                    MapViewMode.leads
                }
                else -> {
                    MapViewMode.mySchedule
                }
            }

            fName = it.getString("name")!!
            showCompleted = it.getBoolean("showCompleted")
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

            when (mode) {
                MapViewMode.workOrders -> {
                    ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
                        getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())
                }
                MapViewMode.leads -> {
                    ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
                        getString(R.string.lead_map)
                }
                MapViewMode.mySchedule -> {
                    ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text =
                        getString(R.string.xs_schedule_map, fName)
                }
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
        else {
            updateMap()
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

        var unplottableCount = 0

        when (mode) {
            MapViewMode.workOrders -> {
                if (globalWorkOrdersList.isNullOrEmpty()) {
                    return
                }

                globalWorkOrdersList!!.forEach {

                    println(it.lat)
                    println(it.lng)

                    if (it.lat.isNullOrBlank() || it.lng.isNullOrBlank()) {
                        unplottableCount++
                    }
                    else {
                        println("Marker: $it: ${it.lat}, ${it.lng}")

                        val markerLayout: View = layoutInflater.inflate(R.layout.map_pin_layout, null)

                        val markerImage: ImageView =
                            markerLayout.findViewById<View>(R.id.marker_image) as ImageView
                        val markerNumber = markerLayout.findViewById<View>(R.id.marker_text) as TextView

                        when (it.status) {
                            "0" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                            }
                            "1" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                            }
                            "2" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_in_progress)
                            }
                            "3" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_done)
                            }
                            else -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_skip)
                            }
                        }

                        if (!it.daySort.isNullOrBlank()) {
                            if (it.daySort == "0") {
                                markerNumber.text = "-"
                            } else {
                                markerNumber.text = it.daySort
                                markerImage.setImageResource(R.drawable.ic_map_pin_numbered)
                            }

                        } else {
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

                if (!dataLoaded) {
                    if (unplottableCount == 1) {
                        globalVars.simpleAlert(myView.context,getString(R.string.unplottable_wos_title),getString(R.string.unplottable_wos_body_one))
                    }
                    else if (unplottableCount > 1) {
                        globalVars.simpleAlert(myView.context,getString(R.string.unplottable_wos_title),getString(R.string.unplottable_wos_body, unplottableCount))
                    }
                }

            }
            MapViewMode.leads -> {
                if (globalLeadList.isNullOrEmpty()) {
                    return
                }

                googleMapGlobal.setOnInfoWindowClickListener { marker ->
                    val directions =
                        LeadListFragmentDirections.navigateToLead(pinMapLead[marker]!!.ID)
                    myView.findNavController().navigate(directions)
                }

                globalLeadList!!.forEach {

                    println(it.lat)
                    println(it.lng)

                    if (it.lat == 0.0 || it.lng == 0.0) {
                        unplottableCount++
                    }
                    else {

                        val markerLayout: View = layoutInflater.inflate(R.layout.map_pin_layout, null)

                        val markerImage: ImageView =
                            markerLayout.findViewById<View>(R.id.marker_image) as ImageView
                        val markerNumber = markerLayout.findViewById<View>(R.id.marker_text) as TextView

                        when (it.statusID) {
                            "0" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                            }
                            "1" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                            }
                            "2" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_in_progress)
                            }
                            "3" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_done)
                            }
                            "4" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_skip)
                            }
                            else -> { // 5
                                markerImage.setImageResource(R.drawable.ic_map_pin_waiting)
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
                }

                binding.mapRefreshBtn.setOnClickListener {
                    (activity as MainActivity?)!!.refreshWorkOrders()
                }

                if (!dataLoaded) {
                    if (unplottableCount == 1) {
                        globalVars.simpleAlert(myView.context,getString(R.string.unplottable_leads_title),getString(R.string.unplottable_leads_body_one))
                    }
                    else if (unplottableCount > 1) {
                        globalVars.simpleAlert(myView.context,getString(R.string.unplottable_leads_title),getString(R.string.unplottable_leads_body, unplottableCount))
                    }
                }

            }
            MapViewMode.mySchedule -> {
                if (globalMyScheduleSections.isEmpty()) {
                    return
                }

                val entries = mutableListOf<MyScheduleEntry>()

                for (sec in globalMyScheduleSections) {
                    if (showCompleted) {
                        entries.addAll(sec.entries)
                    }
                    else {
                        for (entry in sec.entries) {
                            if (!entry.checkIfCompleted()) {
                                entries.add(entry)
                            }
                        }
                    }
                }

                googleMapGlobal.setOnInfoWindowClickListener { marker ->
                    when (pinMapMyScheduleEntry[marker]!!.entryType) {
                        MyScheduleEntryType.workOrder -> {
                            val directions = MapFragmentDirections.navigateToWorkOrder(null)
                            directions.workOrderID = pinMapMyScheduleEntry[marker]!!.refID
                            myView.findNavController().navigate(directions)
                        }
                        MyScheduleEntryType.lead -> {
                            val directions = MapFragmentDirections.navigateToLead(pinMapMyScheduleEntry[marker]!!.refID)
                            myView.findNavController().navigate(directions)
                        }
                        MyScheduleEntryType.service -> {

                            val data = pinMapMyScheduleEntry[marker]!!

                            val newEquipment = Equipment(
                                data.equipmentID!!,
                                data.name!!,
                                data.status!!,
                                "",
                                "1",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                data.usage,
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                data.usageType,
                                null
                            )

                            val historyMode = data.status == "2" || data.status == "3" || data.status == "4"

                            println("Type: ${data.type}")

                            if (data.serviceType == "4") {
                                val directions = MyScheduleFragmentDirections.navigateToServiceInspection(null, newEquipment, historyMode)
                                directions.serviceID = data.refID
                                directions.fromMySchedule = true
                                myView.findNavController().navigate(directions)
                            }
                            else {
                                val directions = MyScheduleFragmentDirections.navigateToService(null, newEquipment, historyMode)
                                directions.serviceID = data.refID
                                directions.fromMySchedule = true
                                myView.findNavController().navigate(directions)
                            }
                        }
                    }


                }

                entries.forEach {
                    println(it.lat)
                    println(it.lng)
                    if (it.lat.isNullOrBlank() || it.lng.isNullOrBlank() || it.lat == "0" || it.lng == "0") {
                        unplottableCount++
                    }
                    else {

                        val markerLayout: View = layoutInflater.inflate(R.layout.map_pin_layout, null)

                        val markerImage: ImageView =
                            markerLayout.findViewById<View>(R.id.marker_image) as ImageView
                        val markerNumber = markerLayout.findViewById<View>(R.id.marker_text) as TextView

                        when (it.status) {
                            "0" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                            }
                            "1" -> {
                                if (it.entryType == MyScheduleEntryType.service) {
                                    markerImage.setImageResource(R.drawable.ic_map_pin_in_progress)
                                } else {
                                    markerImage.setImageResource(R.drawable.ic_map_pin_not_started)
                                }
                            }
                            "2" -> {
                                if (it.entryType == MyScheduleEntryType.service) {
                                    markerImage.setImageResource(R.drawable.ic_map_pin_done)
                                } else {
                                    markerImage.setImageResource(R.drawable.ic_map_pin_in_progress)
                                }
                            }
                            "3" -> {
                                if (it.entryType == MyScheduleEntryType.service) {
                                    markerImage.setImageResource(R.drawable.ic_map_pin_skip)
                                } else {
                                    markerImage.setImageResource(R.drawable.ic_map_pin_done)
                                }
                            }
                            "4" -> {
                                markerImage.setImageResource(R.drawable.ic_map_pin_skip)
                            }
                            else -> { // 5
                                markerImage.setImageResource(R.drawable.ic_map_pin_waiting)
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
                                .title(it.title)
                                .snippet(it.name)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

                        )
                        newMarker?.let { it1 -> markerList.add(it1) }
                        pinMapMyScheduleEntry[newMarker] = it
                    }
                }

                binding.mapRefreshBtn.setOnClickListener {
                    //todo:implement
                }

                if (!dataLoaded) {
                    if (unplottableCount == 1) {
                        globalVars.simpleAlert(myView.context,getString(R.string.unplottable_my_schedule_title),getString(R.string.unplottable_my_schedule_body_one, fName))
                    }
                    else if (unplottableCount > 1) {
                        globalVars.simpleAlert(myView.context,getString(R.string.unplottable_my_schedule_title),getString(R.string.unplottable_my_schedule_body, unplottableCount, fName))
                    }
                }

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