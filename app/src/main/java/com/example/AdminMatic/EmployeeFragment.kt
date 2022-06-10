package com.example.AdminMatic

import android.content.Context
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_employee_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class EmployeeFragment : Fragment(), ImageCellClickListener {

    //private val args: Employee by navArgs()
    //private  var args:Employee? = null
    private  var employee: Employee? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar
    private lateinit var  empViewTop: ConstraintLayout
    //lateinit var  empViewMenu: ConstraintLayout
    //lateinit var  empViewContainer: ConstraintLayout
    //lateinit var  empViewFooter: ConstraintLayout
    lateinit var recyclerView: RecyclerView
    lateinit var  swipeRefresh: SwipeRefreshLayout


    private lateinit var empImageView:ImageView
    private lateinit var empNameTextView:TextView
    private lateinit var empPhoneBtn:ConstraintLayout
    private lateinit var empEmaileBtn:ConstraintLayout
    private lateinit var empPhoneBtnTxt:TextView
    private lateinit var empEmaileBtnTxt:TextView

    private lateinit var payrollBtn:Button
    private lateinit var usageBtn:Button
    private lateinit var shiftsBtn:Button


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




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

       // employee = args
        myView = inflater.inflate(R.layout.fragment_employee, container, false)

        globalVars = GlobalVars()



        imageList = mutableListOf()

        adapter = ImagesAdapter(imageList,myView.context, this)


        //(activity as AppCompatActivity).supportActionBar?.title = "Employee"

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.employee)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Employee View")

        println("employee = ${employee!!.name}")


        pgsBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        swipeRefresh= view.findViewById(R.id.customerSwipeContainer)
        empViewTop = view.findViewById(R.id.emp_top_cl)

        empImageView = view.findViewById(R.id.emp_pic_iv)
        Picasso.with(context)
            .load(GlobalVars.thumbBase + employee!!.pic)
            .placeholder(R.drawable.user_placeholder) //optional
            //.resize(imgWidth, imgHeight)         //optional
            //.centerCrop()                        //optional
            .into(empImageView)                       //Your image view object.


        empNameTextView = view.findViewById(R.id.emp_name_txt)
        empNameTextView.text = employee!!.name

        empPhoneBtn = view.findViewById(R.id.emp_phone_btn_cl)
        empPhoneBtn.setOnClickListener {
            println("phone btn clicked ${employee!!.phone}")

            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + employee!!.phone))
            startActivity(intent)
        }
        empPhoneBtnTxt = view.findViewById(R.id.emp_phone_btn_tv)
        empPhoneBtnTxt.text = employee!!.phone

        empEmaileBtn = view.findViewById(R.id.emp_email_btn_cl)
        empEmaileBtn.setOnClickListener {
            println("email btn clicked ${employee!!.email}")
        }

        empEmaileBtnTxt = view.findViewById(R.id.emp_email_btn_tv)
        empEmaileBtnTxt.text = employee!!.email

        payrollBtn = view.findViewById((R.id.payroll_btn))
        payrollBtn.setOnClickListener{
            println("payroll btn clicked")
            //val directions = EmployeeListFragmentDirections.navigateToEmployee(data)
                val directions = EmployeeFragmentDirections.navigateToPayroll(employee)
            myView.findNavController().navigate(directions)
        }

        usageBtn = view.findViewById((R.id.usage_btn))
        usageBtn.setOnClickListener{
            println("usage btn clicked")
            val directions = EmployeeFragmentDirections.navigateEmployeeToUsage(employee)
            myView.findNavController().navigate(directions)
        }

        shiftsBtn = view.findViewById((R.id.shifts_btn))
        shiftsBtn.setOnClickListener{
            println("shifts btn clicked")
            val directions = EmployeeFragmentDirections.navigateEmployeeToShifts(employee)
            myView.findNavController().navigate(directions)
        }


        logOutBtn = view.findViewById(R.id.log_out_btn)
        logOutBtn.setOnClickListener{


            listener!!.logOut(myView)


        }



        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(itemDecoration)

        recyclerView.onScrollToEnd {
            getImages()
        }

        recyclerView.adapter = adapter

        // recyclerView.apply {
        // layoutManager = GridLayoutManager(myView.context, 2)


        // }

        recyclerView.layoutManager = GridLayoutManager(myView.context, 2)


        recyclerView.apply {



            // var swipeContainer = myView.findViewById(R.id.swipeContainer) as SwipeRefreshLayout
            // Setup refresh listener which triggers new data loading
            // Setup refresh listener which triggers new data loading
            swipeRefresh.setOnRefreshListener { // Your code to refresh the list here.
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
            swipeRefresh.setColorSchemeResources(
                R.color.button,
                R.color.black,
                R.color.colorAccent,
                R.color.colorPrimaryDark
            )

        }

        getImages()

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

        var urlString = "https://www.adminmatic.com/cp/app/functions/get/images.php"

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
                        customerSwipeContainer.isRefreshing = false

                        Toast.makeText(
                            activity,
                            "${imageList.count()} Images Loaded",
                            Toast.LENGTH_SHORT
                        ).show()


                        adapter.filterList = imageList

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
        pgsBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
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