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


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.wo_count, globalWorkOrdersList!!.size.toString())

        mapFragment = childFragmentManager.findFragmentById(R.id.map_support_map_fragment) as SupportMapFragment?


        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)
        refreshBtn = view.findViewById(R.id.map_refresh_btn)

        mapFragment!!.getMapAsync(this)

        hideProgressView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        println("map ready")
        var newMarker: Marker?
        var bitmapDescriptor: BitmapDescriptor?

        googleMap.setOnInfoWindowClickListener { marker ->
            val directions =
                WorkOrderListFragmentDirections.navigateToWorkOrder(pinMapWorkOrder[marker])
            myView.findNavController().navigate(directions)
        }

        globalWorkOrdersList!!.forEach {

            bitmapDescriptor = when (it.status) {
                "0" -> {
                    //Todo: can't set it gray with official code, so maybe use our own bitmaps here
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
                "1" -> {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                }
                "2" -> {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                }
                else -> {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                }
            }

            newMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat!!.toDouble(), it.lng!!.toDouble()))
                    .title(it.custName +" - "+ it.title)
                    .snippet(it.custAddress)
                    .icon(bitmapDescriptor)
            )
            newMarker?.let { it1 -> markerList.add(it1) }
            pinMapWorkOrder[newMarker] = it
        }

        val builder = LatLngBounds.Builder()
        for (marker in markerList) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)

        googleMap.moveCamera(cu)

    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }

}