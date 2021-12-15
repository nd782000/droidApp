package com.example.AdminMatic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_work_order.*
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CustomerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CustomerFragment : Fragment(), ImageCellClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private  var customer: Customer? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    lateinit var custUI: ConstraintLayout


    lateinit var leadsRecyclerView: RecyclerView
    lateinit var  swipeRefresh: SwipeRefreshLayout



    lateinit var custNameTextView:TextView
    lateinit var custPhoneBtn: ConstraintLayout
    lateinit var custEmailBtn: ConstraintLayout
    lateinit var custAddressBtn:ConstraintLayout
    lateinit var custPhoneBtnTxt:TextView
    lateinit var custEmailBtnTxt:TextView
    lateinit var custAddressBtnTxt:TextView


    lateinit var contactsBtn: Button
    lateinit var settingsBtn: Button





    lateinit var adapter:ImagesAdapter

    lateinit var imageList: MutableList<Image>
    lateinit var loadMoreImageList: MutableList<Image>


    lateinit var addBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customer = it.getParcelable<Customer?>("customer")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_customer, container, false)
        // employee = args
        myView = inflater.inflate(R.layout.fragment_customer, container, false)

        globalVars = GlobalVars()


        imageList = mutableListOf()
        adapter = ImagesAdapter(imageList,myView.context, this)



        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "Customer"

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("customer = ${customer!!.ID}")


        pgsBar = view.findViewById(R.id.progress_bar)

        custUI = view.findViewById(R.id.customer_ui_cl)










        getCustomer()

    }

    fun getCustomer(){
        println("getCustomer = ${customer!!.ID}")
        showProgressView()


        var urlString = "https://www.adminmatic.com/cp/app/functions/get/customer.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "${"$urlString?cb=$currentTimestamp"}"
        val queue = Volley.newRequestQueue(myView.context)


        val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")




                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = ${parentObject.toString()}")


                        val gson = GsonBuilder().create()

                        val customerArray = gson.fromJson(parentObject.toString() ,CustomerArray::class.java)

                        customer = customerArray.customers[0]

                        layoutViews()




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
                params["ID"] = customer!!.ID

                println("params = ${params.toString()}")
                return params
            }
        }
        queue.add(postRequest1)






    }


    fun layoutViews(){
        println("layoutViews")

        hideProgressView()

        leadsRecyclerView = myView.findViewById(R.id.customer_leads_rv)
        // swipeRefresh= view.findViewById(R.id.swipeContainer)


        custNameTextView = myView.findViewById(R.id.customer_name_txt)
        custNameTextView.text = customer!!.sysname

        custPhoneBtn = myView.findViewById(R.id.customer_phone_btn_cl)
       // custPhoneBtn.setOnClickListener {
          //  println("phone btn clicked ${customer!!.phone}")

           // val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + customer!!.phone))
           // startActivity(intent)
        //}
        custPhoneBtnTxt = myView.findViewById(R.id.customer_phone_btn_tv)

       // custPhoneBtnTxt.text = customer!!.phone

        custPhoneBtnTxt.text = "No Phone Found"

        custEmailBtn = myView.findViewById(R.id.customer_email_btn_cl)


        custEmailBtnTxt = myView.findViewById(R.id.customer_email_btn_tv)

        //custEmailBtnTxt.text = customer!!.email

        custEmailBtnTxt.text = "No Email Found"

        custAddressBtn = myView.findViewById(R.id.customer_address_btn_cl)
        custAddressBtn.setOnClickListener {
            println("map btn clicked ${customer!!.mainAddr}")

            var lng:String = ""
            var lat:String = ""
            if(customer!!.lng != null && customer!!.lat != null){
                lng = customer!!.lng!!
                lat = customer!!.lat!!
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q="+lng+","+lat+" (" + customer!!.sysname + ")")
                )
                startActivity(intent)
            }


        }

        custAddressBtnTxt = myView.findViewById(R.id.customer_address_btn_tv)

        //custEmailBtnTxt.text = customer!!.email

        custAddressBtnTxt.text = "No Address Found"

        if (customer!!.mainAddr != ""){
            custAddressBtnTxt.text = customer!!.mainAddr
        }


        if (customer!!.contacts.count() > 0){
            for (contact in customer!!.contacts) {
                when(contact.type) {
                    "1" -> {
                        println("1")
                        custPhoneBtnTxt.text = contact.value!!
                        custEmailBtn.setOnClickListener {

                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.value!!))
                            com.example.AdminMatic.myView.context.startActivity(intent)
                        }

                    }
                    "2" -> {
                        println("2")
                        custEmailBtnTxt.text = contact.value!!
                        custEmailBtn.setOnClickListener {
                            println("email btn clicked ${contact.value!!}")
                            val intent = Intent(Intent.ACTION_SENDTO)
                            intent.data = Uri.parse("mailto:") // only email apps should handle this
                            val emailArray = arrayOf<String>(contact.value!!)
                            intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
                            // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
                            // intent.putExtra(Intent.EXTRA_TEXT, "Body Here")
                            com.example.AdminMatic.myView.context.startActivity(intent)
                        }

                    }



                }
            }
        }

        contactsBtn = myView.findViewById((R.id.contacts_btn))
        contactsBtn.setOnClickListener{
            println("contacts btn clicked")

            val directions = CustomerFragmentDirections.navigateToCustomerContacts(customer!!)


            myView.findNavController().navigate(directions)


        }

        settingsBtn = myView.findViewById((R.id.customer_notes_btn))
        settingsBtn.setOnClickListener{
            println("notes btn clicked")

            val directions = CustomerFragmentDirections.navigateToCustomerNotes(customer!!)

            myView.findNavController().navigate(directions)
            //val directions = EmployeeListFragmentDirections.navigateToEmployee(data)
            // val directions = EmployeeFragmentDirections.navigateToPayroll(employee)
            // myView.findNavController().navigate(directions)
        }


        addBtn = myView.findViewById((R.id.customer_add_btn))
        addBtn.setOnClickListener{
            println("add btn clicked")
            //val directions = EmployeeListFragmentDirections.navigateToEmployee(data)
            // val directions = EmployeeFragmentDirections.navigateToPayroll(employee)
            // myView.findNavController().navigate(directions)
        }


    }

    override fun onImageCellClickListener(data:Image) {
        //Toast.makeText(this,"Cell clicked", Toast.LENGTH_SHORT).show()
        Toast.makeText(activity,"${data.name} Clicked", Toast.LENGTH_SHORT).show()

        println("Cell clicked with image: ${data.name}")
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
        custUI.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        custUI.visibility = View.VISIBLE
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CustomerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}