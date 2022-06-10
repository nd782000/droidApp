package com.example.AdminMatic


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


class VendorFragment : Fragment() {
    private  var vendor: Vendor? = null
    private  var vendorID: String? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var  pgsBar: ProgressBar


    private lateinit var vendorNameTextView:TextView
    private lateinit var vendorPhoneBtn: ConstraintLayout
    private lateinit var vendorWebBtn: ConstraintLayout
    private lateinit var vendorAddressBtn: ConstraintLayout
    private lateinit var vendorPhoneBtnTxt:TextView
    private lateinit var vendorWebBtnTxt:TextView
    private lateinit var vendorAddressBtnTxt:TextView
    private lateinit var vendorBodyCl:ConstraintLayout




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable("vendor")
            vendorID = it.getString("vendorString")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_vendor, container, false)
        myView = inflater.inflate(R.layout.fragment_vendor, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.vendor)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pgsBar = view.findViewById(R.id.progress_bar)
        vendorBodyCl = view.findViewById(R.id.vendor_body_cl)
        hideProgressView()

        getVendor()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("vendor")
    }

    private fun populateVendorView() {
        vendorNameTextView = myView.findViewById(R.id.vendor_name_txt)
        vendorNameTextView.text = vendor!!.name

        vendorPhoneBtn = myView.findViewById(R.id.vendor_phone_btn_cl)
        vendorPhoneBtnTxt = myView.findViewById(R.id.vendor_phone_btn_tv)
        vendorPhoneBtnTxt.text = getString(R.string.no_phone_found)



        if(vendor!!.mainPhone != null){
            vendorPhoneBtnTxt.text = vendor!!.mainPhone!!

            vendorPhoneBtnTxt.setOnClickListener {

                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + vendor!!.mainPhone!!))
                com.example.AdminMatic.myView.context.startActivity(intent)
            }
        }

        vendorWebBtn = myView.findViewById(R.id.vendor_web_btn_cl)
        vendorWebBtnTxt = myView.findViewById(R.id.vendor_web_btn_tv)
        vendorWebBtnTxt.text = getString(R.string.no_website_found)
        if(vendor!!.website != null){
            vendorWebBtnTxt.text = vendor!!.website!!

            vendorWebBtnTxt.setOnClickListener {

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(vendor!!.website!!)
                startActivity(intent)
            }


        }

        vendorAddressBtn = myView.findViewById(R.id.vendor_address_btn_cl)
        vendorAddressBtn.setOnClickListener {
            println("map btn clicked ${vendor!!.mainAddr}")

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

        vendorAddressBtnTxt = myView.findViewById(R.id.vendor_address_btn_tv)
        vendorAddressBtnTxt.text = getString(R.string.no_address_found)

        if (vendor!!.mainAddr != ""){
            vendorAddressBtnTxt.text = vendor!!.mainAddr!!
        }





        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_frg) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync { mMap ->
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            mMap.clear() //clear old markers
            println("BBBBBBBBBB ${vendor!!.lat}")

            if(vendor!!.lat != null && vendor!!.lng != null && vendor!!.lat != "0" && vendor!!.lng != "0"){

                // println("vendor lat = ${vendor!!.lat!!.toDouble()}")
                // println("vendor lng = ${vendor!!.lng!!.toDouble()}")

                println("vendor lat = ${vendor!!.lat!!.toDouble()}")
                println("vendor lng = ${vendor!!.lng!!.toDouble()}")
                val googlePlex = CameraPosition.builder()
                    .target(LatLng(vendor!!.lat!!.toDouble(), vendor!!.lng!!.toDouble()))
                    .zoom(10f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 100, null)


/*
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(37.4219999, -122.0862462))
                    .title("Spider Man")
                    .icon(bitmapDescriptorFromVector(activity, R.drawable.ic_done))
            )
*/


                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(vendor!!.lat!!.toDouble(), vendor!!.lng!!.toDouble()))
                        .title(vendor!!.name)
                    // .snippet("His Talent : Plenty of money")
                )
            }
        }
    }

    private fun getVendor(){
        // println("getCustomer = ${customer!!.ID}")
        showProgressView()


        if (vendor == null) {
            var urlString = "https://www.adminmatic.com/cp/app/functions/get/vendor.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            println(vendorID)
            urlString = "$urlString?cb=$currentTimestamp"


            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    hideProgressView()

                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")


                        val gson = GsonBuilder().create()

                        val vendorArray = gson.fromJson(parentObject.toString() ,VendorArray::class.java)

                        vendor = vendorArray.vendors[0]
                        if (vendor!!.lat == null) {
                            vendor!!.lat = "0"
                        }
                        if (vendor!!.lng == null) {
                            vendor!!.lng = "0"
                        }

                        populateVendorView()


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
                    params["id"] = vendorID.toString()

                    println("params = $params")
                    return params
                }
            }
            postRequest1.tag = "vendor"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)

        }
        else {
            hideProgressView()
            populateVendorView()
        }
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        vendorBodyCl.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        vendorBodyCl.visibility = View.VISIBLE
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