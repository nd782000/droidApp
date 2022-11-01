package com.example.AdminMatic

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.BuildConfig
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentMainMenuBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.example.AdminMatic.GlobalVars.Companion.thumbBase
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject


// Todo: Look into refactoring this, it warns about memory leaks but nearly every other file relies on this in the global space
lateinit var myView:View

//menu[menu.size()-1].title = getString(R.string.version, BuildConfig.VERSION_NAME)
class MainMenuFragment : Fragment() {


    lateinit  var globalVars:GlobalVars

    //private var globalVars: GlobalVars? = null

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     */


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu_menu, menu)
        menu.findItem(R.id.version_item).title = getString(R.string.version, BuildConfig.VERSION_NAME, GlobalVars.phpVersion)
        super.onCreateOptionsMenu(menu, inflater)
        ((activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false))
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.

        when (item.itemId) {
            R.id.privacy_policy_item -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.adminmatic.com/app/privacy"))
                startActivity(intent)
            }
            R.id.support_item -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.adminmatic.com/support"))
                startActivity(intent)
            }
            R.id.reload_company_data_item -> {
                getFields()
            }
        }

        return super.onOptionsItemSelected(item)

    }

    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!this::globalVars.isInitialized) {
            globalVars = GlobalVars()
        }

        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        myView = binding.root

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.app_name)
        val constraintLayout: ConstraintLayout = myView.findViewById(R.id.logged_in_employee_constraint_layout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        //val loggedInEmployeeImageView:ImageView = myView.findViewById(R.id.logged_in_employee_image_view)

        Picasso.with(context)
            .load(thumbBase + loggedInEmployee!!.pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into( binding.loggedInEmployeeImageView)                       //Your image view object.

        binding.loggedInEmployee.text = getString(R.string.welcome_name, loggedInEmployee!!.fname)

        println("loggedInEmployeeTextView.id = ${binding.loggedInEmployee.id}")
        println("loggedInEmployeeImageView.id = ${binding.loggedInEmployeeImageView.id}")

        binding.loggedInEmployeeConstraintLayout.setOnClickListener(({

            println("Go To Logged in Employee")
            // Toast.makeText(activity,"Go To Logged in Employee",Toast.LENGTH_SHORT).show()


            val directions = MainMenuFragmentDirections.navigateToLoggedInEmployee(loggedInEmployee)
            myView.findNavController().navigate(directions)

            println("Cell clicked with employee: ${loggedInEmployee!!.name}")

            //val action = SpecifyAmountFragmentDirections.confirmationAction(amount)
            //v.findNavController().navigate(action)//var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            //startActivity(clickintent)
        }))


        binding.btnEmployees.setOnClickListener(({

            println("Go To Employees")
            // Toast.makeText(activity,"Go To Employees",Toast.LENGTH_SHORT).show()
            if (GlobalVars.permissions!!.employees == "1") {
                myView.findNavController().navigate(R.id.navigateToEmployeeList)//var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_employees))
            }

            //startActivity(clickintent)
        }))

        binding.btnCustomers.setOnClickListener(({
            println("Go To Customers")
            //Toast.makeText(activity,"Go To Customers",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)

            if (GlobalVars.permissions!!.customers == "1") {
                myView.findNavController().navigate(R.id.navigateToCustomerList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_customers))
            }
            //var clickintent = Intent(this@MainMenu, EmployeesList::class.java)
            // startActivity(clickintent)
        }))

        binding.btnVendors.setOnClickListener(({
            println("Go To Vendors")
            //Toast.makeText(activity,"Go To Vendors",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.vendors == "1") {
                myView.findNavController().navigate(R.id.navigateToVendorList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_vendors))
            }

            //var clickintent = Intent(this@MainMenu, VendorList::class.java)
            //startActivity(clickintent)
        }))

        binding.btnItems.setOnClickListener(({
            println("Go To Items")
            //Toast.makeText(activity,"Go To Items",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.items == "1") {
                myView.findNavController().navigate(R.id.navigateToItemList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_items))
            }

            //var clickintent = Intent(this@MainMenu, ItemsList::class.java)
            //startActivity(clickintent)
        }))

        binding.btnLeads.setOnClickListener(({
            println("Go To Leads")
            // Toast.makeText(activity,"Go To Leads",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)

            if (GlobalVars.permissions!!.leads == "1") {
                myView.findNavController().navigate(R.id.navigateToLeadList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_leads))
            }
            //var clickintent = Intent(this@MainMenu, ScheduleList::class.java)
            //startActivity(clickintent)
        }))
        binding.btnContracts.setOnClickListener(({
            println("Go To Contracts")
            // Toast.makeText(activity,"Go To Contracts",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.contracts == "1") {
                myView.findNavController().navigate(R.id.navigateToContractList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_contracts))
            }

            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))
        binding.btnSchedule.setOnClickListener(({
            println("Go To Work Orders")
            //Toast.makeText(activity,"Go To Work Orders",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.schedule == "1") {
                myView.findNavController().navigate(R.id.navigateToWorkOrderList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_schedule))
            }
            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))

        binding.btnInvoices.setOnClickListener(({
            println("Go To Invoices")
            //Toast.makeText(activity,"Go To Invoices",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.invoices == "1") {
                myView.findNavController().navigate(R.id.navigateToInvoiceList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_invoices))
            }

            //var clickintent = Intent(this@MainMenu, PerformanceList::class.java)
            //startActivity(clickintent)
        }))

        binding.btnImages.setOnClickListener(({
            println("Go To Images")
            //Toast.makeText(activity,"Go To Images",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.files == "1") {
                myView.findNavController().navigate(R.id.navigateToImageList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_images))
            }

            //var clickintent = Intent(this@MainMenu, ImagesList::class.java)
            //startActivity(clickintent)
        }))
        binding.btnEquipment.setOnClickListener(({
            println("Go To Equipment")
            // Toast.makeText(activity,"Go To Equipment",Toast.LENGTH_SHORT).show()            //var clickintent = Intent(this@MainMenu, CustomersList::class.java)
            if (GlobalVars.permissions!!.equipment == "1") {
                myView.findNavController().navigate(R.id.navigateToEquipmentList)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_equipment))
            }
            // var clickintent = Intent(this@MainMenu, EquipmentList::class.java)
            //startActivity(clickintent)
        }))

        return myView
    }




    private fun getFields(){
        showProgressView()
        println("getFields")

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/fields.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")



                try {
                    val parentObject = JSONObject(response)
                    println("parentObject get fields = $parentObject")
                    globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)


                    globalVars.populateFields(context, parentObject)
                    hideProgressView()



                    /* Here 'response' is a String containing the response you received from the website... */
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
                params["companyUnique"] = loggedInEmployee!!.companyUnique
                params["sessionKey"] = loggedInEmployee!!.sessionKey
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "logIn"
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