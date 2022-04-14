package com.example.AdminMatic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.AdminMatic.R



import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VendorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */






class VendorFragment : Fragment() {
    private  var vendor: Vendor? = null
    private  var vendorID: String? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar


    lateinit var vendorNameTextView:TextView
    lateinit var vendorPhoneBtn: ConstraintLayout
    lateinit var vendorWebBtn: ConstraintLayout
    lateinit var vendorAddressBtn: ConstraintLayout
    lateinit var vendorPhoneBtnTxt:TextView
    lateinit var vendorWebBtnTxt:TextView
    lateinit var vendorAddressBtnTxt:TextView
    lateinit var vendorBodyCl:ConstraintLayout




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable<Vendor?>("vendor")
            vendorID = it.getString("vendorString")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_vendor, container, false)
        myView = inflater.inflate(R.layout.fragment_vendor, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Vendor"

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pgsBar = view.findViewById(R.id.progress_bar)
        vendorBodyCl = view.findViewById(R.id.vendor_body_cl)
        hideProgressView()

        getVendor()



    }

    private fun populateVendorView() {
        vendorNameTextView = myView.findViewById(R.id.vendor_name_txt)
        vendorNameTextView.text = vendor!!.name

        vendorPhoneBtn = myView.findViewById(R.id.vendor_phone_btn_cl)
        vendorPhoneBtnTxt = myView.findViewById(R.id.vendor_phone_btn_tv)
        vendorPhoneBtnTxt.text = "No Phone Found"



        if(vendor!!.mainPhone != null){
            vendorPhoneBtnTxt.text = vendor!!.mainPhone!!

            vendorPhoneBtnTxt.setOnClickListener {

                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + vendor!!.mainPhone!!))
                com.example.AdminMatic.myView.context.startActivity(intent)
            }
        }

        vendorWebBtn = myView.findViewById(R.id.vendor_web_btn_cl)
        vendorWebBtnTxt = myView.findViewById(R.id.vendor_web_btn_tv)
        vendorWebBtnTxt.text = "No Website Found"
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

            var lng:String = ""
            var lat:String = ""
            if(vendor!!.lng != null && vendor!!.lat != null){
                lng = vendor!!.lng!!
                lat = vendor!!.lat!!
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q="+lng+","+lat+" (" + vendor!!.name + ")")
                )
                startActivity(intent)
            }
        }

        vendorAddressBtnTxt = myView.findViewById(R.id.vendor_address_btn_tv)
        vendorAddressBtnTxt.text = "No Address Found"

        if (vendor!!.mainAddr != ""){
            vendorAddressBtnTxt.text = vendor!!.mainAddr!!
        }





        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_frg) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync { mMap ->
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            mMap.clear() //clear old markers


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
                        .title(vendor!!.name!!)
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
            val queue = Volley.newRequestQueue(myView.context)


            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    hideProgressView()


                    //Todo: figure out why the vendor is coming in null
                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = ${parentObject.toString()}")


                        val gson = GsonBuilder().create()

                        val vendorArray = gson.fromJson(parentObject.toString() ,VendorArray::class.java)

                        vendor = vendorArray.vendors[0]

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

                    println("params = ${params.toString()}")
                    return params
                }
            }
            queue.add(postRequest1)

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

    private fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }






        companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VendorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VendorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}