package com.example.AdminMatic

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentDepartmentsBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface DepartmentCellClickListener {
    fun onDepartmentCellClickListener(data:Department)
}

class DepartmentsFragment : Fragment(), DepartmentCellClickListener {

    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var departmentAdapter:DepartmentAdapter

    private var departmentCount:Int = 0


    private var _binding: FragmentDepartmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_equipment, container, false)
        _binding = FragmentDepartmentsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.departments)

        return myView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newDepartmentBtn.setOnClickListener {
            val directions = DepartmentsFragmentDirections.navigateToNewEditDepartment(null)
            myView.findNavController().navigate(directions)
        }

        getDepartments()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("departments")
    }

    private fun getDepartments(){
        println("getDepartments")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/departments.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response
                println("Response $response")
                try {
                    val gson = GsonBuilder().create()


                    val parentObject = JSONObject(response)
                    println("parentObject = $parentObject")
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {


                        val departments: JSONArray = parentObject.getJSONArray("departments")
                        println("departments = $departments")
                        println("departments count = ${departments.length()}")

                        val departmentsList =
                            gson.fromJson(departments.toString(), Array<Department>::class.java)
                                .toMutableList()
                        println("DepartmentsCount = ${departmentsList.count()}")

                        departmentAdapter = DepartmentAdapter(
                            departmentsList,
                            this.myView.context,
                            this@DepartmentsFragment
                        )

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(
                                myView.context,
                                DividerItemDecoration.VERTICAL
                            )
                        binding.recyclerView.addItemDecoration(itemDecoration)

                        println(departmentAdapter.itemCount)
                        binding.recyclerView.layoutManager = LinearLayoutManager(this.myView.context, RecyclerView.VERTICAL, false)
                        binding.recyclerView.adapter = departmentAdapter

                        departmentCount = departmentsList.size

                        binding.departmentFooterTv.text = getString(R.string.x_active_departments, departmentCount)



                        hideProgressView()
                    }




                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { // error
                //Log.e("VOLLEY", error.toString())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["active"] = "1"
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "departments"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onDepartmentCellClickListener(data: Department) {
        println("department ${data.name} tapped")

        val directions = DepartmentsFragmentDirections.navigateToNewEditDepartment(data)
        myView.findNavController().navigate(directions)
    }

    fun showProgressView() {
        binding.allCl.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.allCl.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

}
