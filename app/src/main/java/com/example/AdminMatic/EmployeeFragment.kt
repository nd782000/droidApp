package com.example.AdminMatic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEmployeeBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class EmployeeFragment : Fragment(), ImageCellClickListener {

    //private val args: Employee by navArgs()
    //private  var args:Employee? = null
    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View


    lateinit var adapter:ImagesAdapter

    lateinit var imageList: MutableList<Image>
    lateinit var loadMoreImageList: MutableList<Image>
    var refreshing = false

    private lateinit var logOutBtn:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }


    private var listener: LogOut? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is LogOut) {
            context
        } else {
            throw ClassCastException(
                "$context must implement LogOut"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("employee")
    }


    private var _binding: FragmentEmployeeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

       // employee = args
        _binding = FragmentEmployeeBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        imageList = mutableListOf()
        adapter = ImagesAdapter(imageList,myView.context, false, this)

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.employee)
        setHasOptionsMenu(true)

        return myView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.employee_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_employee_item) {
            if (GlobalVars.permissions!!.employeesEdit == "1") {
                val directions = EmployeeFragmentDirections.navigateToNewEditEmployee(employee!!)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.access_denied),getString(R.string.no_permission_employees_edit))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Employee View")

        println("employee = ${employee!!.fname}")


        Picasso.with(context)
            .load(GlobalVars.thumbBase + employee!!.pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(binding.empPicIv)                       //Your image view object.


        binding.empNameTxt.text = employee!!.name

        binding.empPhoneBtnCl.setOnClickListener {
            println("phone btn clicked ${employee!!.phone}")

            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + employee!!.phone))
            startActivity(intent)
        }
        binding.empPhoneBtnTv.text = employee!!.phone

        binding.empEmailBtnCl.setOnClickListener {
            println("email btn clicked ${employee!!.email}")
        }

        binding.empEmailBtnTv.text = employee!!.email

        binding.payrollBtn.setOnClickListener{
            println("payroll btn clicked")

            var i = 0
            var wasFound = false
            for (emp in GlobalVars.employeeList!!) {
                println(emp.name) // or your logic to catch the "B"
                if (emp.ID == employee!!.ID){
                    wasFound = true
                }
                i += 1
            }

            if (wasFound) {
                val directions = EmployeeFragmentDirections.navigateToPayroll(employee)
                myView.findNavController().navigate(directions)
            }
            else {
                globalVars.simpleAlert(myView.context,getString(R.string.dialogue_inactive_payroll_title),getString(R.string.dialogue_inactive_payroll_body))
            }

        }

        binding.usageBtn.setOnClickListener{
            println("usage btn clicked")
            val directions = EmployeeFragmentDirections.navigateEmployeeToUsage(employee)
            myView.findNavController().navigate(directions)
        }

        binding.shiftsBtn.setOnClickListener{
            println("shifts btn clicked")
            val directions = EmployeeFragmentDirections.navigateEmployeeToShifts(employee)
            myView.findNavController().navigate(directions)
        }

        binding.licensesBtn.setOnClickListener{
            println("shifts btn clicked")
            val directions = EmployeeFragmentDirections.navigateEmployeeToLicenses(employee)
            myView.findNavController().navigate(directions)
        }

        binding.deptsBtn.setOnClickListener{
            println("shifts btn clicked")
            val directions = EmployeeFragmentDirections.navigateEmployeeToCrews()
            myView.findNavController().navigate(directions)
        }

        // Only show the logout button if this it's you
        if (employee!!.ID == GlobalVars.loggedInEmployee!!.ID) {
            logOutBtn = view.findViewById(R.id.log_out_btn)
            logOutBtn.setOnClickListener{
                listener!!.logOut(myView)
            }
        }
        else {
            val footerCl:ConstraintLayout = view.findViewById(R.id.emp_footer_cl)
            footerCl.visibility = View.GONE
        }


        binding.listRecyclerView.onScrollToEnd {
            getImages()
        }

        binding.listRecyclerView.adapter = adapter

        // recyclerView.apply {
        // layoutManager = GridLayoutManager(myView.context, 2)


        // }

        binding.listRecyclerView.layoutManager = GridLayoutManager(myView.context, 3)


        binding.listRecyclerView.apply {

            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
            // Setup refresh listener which triggers new data loading
            // Setup refresh listener which triggers new data loading
            binding.customerSwipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0)

                //clear list
                imageList = mutableListOf()
                refreshing = true



                getImages()
            }
            // Configure the refreshing colors
            // Configure the refreshing colors
            binding.customerSwipeContainer.setColorSchemeResources(
                R.color.button,
                R.color.black,
                R.color.colorAccent,
                R.color.colorPrimaryDark
            )

        }

        getEmployee()

    }


    private fun getEmployee(){
        println("getEmployee")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/employees.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()

                try {
                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                        val employees: JSONArray = parentObject.getJSONArray("employees")
                        // println("employees = ${employees.toString()}")
                        // println("employees count = ${employees.length()}")


                        val gson = GsonBuilder().create()
                        val employeesList =
                            gson.fromJson(employees.toString(), Array<Employee>::class.java)
                                .toMutableList()

                        employee = employeesList[0]
                        getImages()
                    }

                    //adapter.notifyDataSetChanged();




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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["empID"] = employee!!.ID


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "employee"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }



    private fun getImages(){
        println("getImages")


        // println("pgsBar = $pgsBar")


        showProgressView()

        //loadMoreItemsCells = mutableListOf<Image?>()

        var offset = adapter.itemCount
        if (refreshing){
            offset = 0
        }
        refreshing = false

        val limit = 200

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/images.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"


        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                //Log.d("Response", response)

                println("Response $response")

                hideProgressView()


                try {
                    if (isResumed) {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                            val images: JSONArray = parentObject.getJSONArray("images")
                            println("images = $images")
                            println("images count = ${images.length()}")


                            val gson = GsonBuilder().create()
                            loadMoreImageList =
                                gson.fromJson(images.toString(), Array<Image>::class.java)
                                    .toMutableList()
                            println("loadMoreImageList count = ${loadMoreImageList.count()}")
                            imageList.addAll(loadMoreImageList)
                            println("imageList count = ${imageList.count()}")

                            // Now we call setRefreshing(false) to signal refresh has finished
                            binding.customerSwipeContainer.isRefreshing = false

                            Toast.makeText(
                                activity,
                                "${imageList.count()} Images Loaded",
                                Toast.LENGTH_SHORT
                            ).show()

                            //employee!!.fName = fName


                            adapter.filterList = imageList
                        }

                        //adapter.notifyDataSetChanged();
                    }



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
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["loginID"] = GlobalVars.loggedInEmployee!!.ID
                params["limit"] = limit.toString()
                params["offset"] = offset.toString()
                params["uploadedBy"] = employee!!.ID


                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "employee"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }


    private fun RecyclerView.onScrollToEnd(
        onScrollNearEnd: (Unit) -> Unit
    ) = addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!recyclerView.canScrollVertically(1)) {
                onScrollNearEnd(Unit)
            }
        }
    })


    override fun onImageCellClickListener(data:Image) {
        data.let {
            val directions = EmployeeFragmentDirections.navigateEmployeeToImage(imageList.toTypedArray(), imageList.indexOf(it))
            myView.findNavController().navigate(directions)
        }
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.listRecyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.listRecyclerView.visibility = View.VISIBLE
    }



    /*
    fun openCloseNavigationDrawer(view: View) {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }
*/



}