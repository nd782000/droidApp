package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentImageLikesBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate


class ImageLikesFragment : Fragment(), EmployeeCellClickListener {

    private var image: Image? = null

    lateinit var globalVars: GlobalVars
    lateinit var myView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getParcelable("image")
        }
    }

    private var _binding: FragmentImageLikesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_item, container, false)
        _binding = FragmentImageLikesBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.image_likes_header)



        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        println("image = ${image!!.ID}")
        println("image path = ${GlobalVars.mediumBase + image!!.fileName}")
        Picasso.with(context).load(GlobalVars.mediumBase + image!!.fileName).into(binding.imageLikesIv)

        binding.imageLikesTitleTv.text = image!!.name
        binding.imageLikesByTv.text = getString(R.string.image_likes_by, image!!.createdByName)
        binding.imageLikesOnTv.text = getString(R.string.image_likes_on, LocalDate.parse(image!!.dateAdded, GlobalVars.dateFormatterPHP).format(GlobalVars.dateFormatterShort))
        binding.imageLikesFooterTv.text = getString(R.string.image_likes_count, image!!.likes)

        getLikes()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("imageLikes")
    }


    private fun getLikes() {
        println("getLikes")

        showProgressView()

        var urlString = "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/get/imageLikes.php"

        val currentTimestamp = System.currentTimeMillis()
        println("urlString = ${"$urlString?cb=$currentTimestamp"}")
        urlString = "$urlString?cb=$currentTimestamp"

        val postRequest1: StringRequest = object : StringRequest(
            Method.POST, urlString,
            Response.Listener { response -> // response

                println("Response $response")

                hideProgressView()

                try {
                    val parentObject = JSONObject(response)
                    if (globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)) {

                    val employees: JSONArray = parentObject.getJSONArray("employees")

                    val gson = GsonBuilder().create()
                    val employeesList =
                        gson.fromJson(employees.toString(), Array<Employee>::class.java)
                            .toMutableList()



                    binding.imageLikesRecyclerView.apply {
                        layoutManager = LinearLayoutManager(activity)

                        adapter = activity?.let {
                            EmployeesAdapter(
                                employeesList, true, it, this@ImageLikesFragment
                            )
                        }

                        val itemDecoration: RecyclerView.ItemDecoration =
                            DividerItemDecoration(
                                myView.context,
                                DividerItemDecoration.VERTICAL
                            )
                        binding.imageLikesRecyclerView.addItemDecoration(itemDecoration)
                    }


                        //(adapter as EmployeesAdapter).notifyDataSetChanged()

                    }
                } catch (e: JSONException) {
                    println("JSONException")
                    e.printStackTrace()
                }


                // var intent:Intent = Intent(applicationContext,MainActivity2::class.java)
                // startActivity(intent)
            },
            Response.ErrorListener { // error

                // Log.e("VOLLEY", error.toString())
                // Log.d("Error.Response", error())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique
                params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                params["imageID"] = image!!.ID
                println("params = $params")
                return params
            }
        }
        postRequest1.tag = "imageLikes"
        VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
    }

    override fun onEmployeeCellClickListener(data:Employee) {
        data.let {
            val directions = ImageLikesFragmentDirections.navigateImageLikesToEmployee(it)
            myView.findNavController().navigate(directions)
        }
        println("Cell clicked with employee: ${data.fname}")
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