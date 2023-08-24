package com.example.AdminMatic


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentVendorBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


class VendorFragment : Fragment(), OnMapReadyCallback {
    private  var vendor: Vendor? = null
    private  var vendorID: String? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    private var mapFragment : SupportMapFragment? = null
    private lateinit var googleMapGlobal:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable("vendor")
            vendorID = it.getString("vendorString")
        }
        if (vendor != null) {
            vendorID = vendor!!.ID
        }
    }

    private var _binding: FragmentVendorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.vendor)
        mapFragment = childFragmentManager.findFragmentById(R.id.map_support_map_fragment) as SupportMapFragment?
        setHasOptionsMenu(true)
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressView()

        //binding.mapFrg.getMapAsync(this)
        mapFragment!!.getMapAsync(this)

        //hideProgressView()

        //getVendor()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("vendor")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.vendor_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_vendor_item) {
            if (GlobalVars.permissions!!.vendorsEdit == "1") {
                val directions = VendorFragmentDirections.navigateToNewEditVendor(vendor)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_vendors_edit))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        println("map ready")
        googleMapGlobal = googleMap

        getVendor()
    }

    private fun updateMap(){
        println("updateMap")

        googleMapGlobal.clear()

        println(vendor!!.mainAddr)

        if (vendor!!.lat == "0" || vendor!!.lng == "0") {
            hideProgressView()
            return
        }



        val newMarker: Marker? = googleMapGlobal.addMarker(
            MarkerOptions()
                .position(LatLng(vendor!!.lat!!.toDouble(), vendor!!.lng!!.toDouble()))
                .title(vendor!!.name)
        )



        val builder = LatLngBounds.Builder()
        builder.include(newMarker!!.position)
        val cu = CameraUpdateFactory.newLatLngZoom(newMarker.position, 16f)
        googleMapGlobal.moveCamera(cu)


        hideProgressView()

    }

    private fun populateVendorView() {
        println("Vendor name: ${vendor!!.name}")
        println("Vendor balance: ${vendor!!.balance}")
        binding.vendorNameTxt.text = vendor!!.name
        if (vendor!!.balance == null) {
            vendor!!.balance = "0"
        }
        binding.vendorBalanceTxt.text = getString(R.string.vendor_balance, vendor!!.balance)
        if (GlobalVars.permissions!!.vendorsMoney == "0") {
            binding.vendorBalanceTxt.visibility = View.INVISIBLE
        }

        binding.vendorPhoneBtnTv.text = getString(R.string.no_phone_found)



        if(!vendor!!.mainPhone.isNullOrEmpty()){
            binding.vendorPhoneBtnTv.text = vendor!!.mainPhone!!

            binding.vendorPhoneBtnTv.setOnClickListener {

                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + vendor!!.mainPhone!!))
                com.example.AdminMatic.myView.context.startActivity(intent)
            }
        }


        binding.vendorWebBtnTv.text = getString(R.string.no_website_found)
        if(vendor!!.website != null){
            binding.vendorWebBtnTv.text = vendor!!.website!!

            binding.vendorWebBtnTv.setOnClickListener {

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(vendor!!.website!!)

                if (!vendor!!.website!!.startsWith("http://")) {
                    intent.data = Uri.parse("http://" + vendor!!.website!!)
                }

                startActivity(intent)
            }


        }

        binding.vendorAddressBtnCl.setOnClickListener {
            println("map btn clicked ${vendor!!.addr1}")

            var lng = "0"
            var lat = "-"

            if (vendor!!.lng != null) {
                lng = vendor!!.lng.toString()
            }
            if (vendor!!.lat != null) {
                lat = vendor!!.lat.toString()
            }

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q="+lng+","+lat+" (" + vendor!!.name + ")")
            )
            startActivity(intent)

        }

        //binding.vendorAddressBtnTv.text = getString(R.string.no_address_found)

        println("Main Address: ${vendor!!.addr1}")

        if (!vendor!!.addr1.isNullOrBlank()) {


            if (vendor!!.city.isNullOrBlank()) {
                binding.vendorAddressBtnTv.text = vendor!!.addr1!!
            }
            else {
                binding.vendorAddressBtnTv.text = getString(R.string.comma, vendor!!.addr1!!, vendor!!.city!!)
            }


        }

        updateMap()

    }

    private fun getVendor(){
        // println("getCustomer = ${customer!!.ID}")


        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/vendor.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        println(vendorID)
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val gson = GsonBuilder().create()
                        val vendorObject = parentObject.getJSONObject("vendor")
                        vendor = gson.fromJson(vendorObject.toString(), Vendor::class.java)

                        if (vendor!!.lat == null) {
                            vendor!!.lat = "0"
                        }
                        if (vendor!!.lng == null) {
                            vendor!!.lng = "0"
                        }

                        populateVendorView()
                    }


                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { // error
                println("ERROR")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["ID"] = vendorID.toString()

                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "vendor"
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

    /*
    private fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

     */



}