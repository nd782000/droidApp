package com.example.AdminMatic

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AdminMatic.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.fragment_equipment_list.list_recycler_view
import kotlinx.android.synthetic.main.fragment_equipment_list.customerSwipeContainer
import kotlinx.android.synthetic.main.fragment_equipment_list.*
import kotlinx.android.synthetic.main.fragment_new_service.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/*
interface EquipmentDetailCellClickListener {
    fun onEquipmentDetailCellClickListener(data:String)
}
*/

class NewServiceFragment : Fragment() {

    private  var equipment: Equipment? = null
    private  var newService: EquipmentService? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var pgsBar: ProgressBar

    lateinit var frequencyTxt:TextView
    lateinit var currentTxt:TextView
    lateinit var nextTxt:TextView

    lateinit var nameEditTxt:EditText
    lateinit var typeEditTxt:EditText
    lateinit var frequencyEditTxt:EditText
    lateinit var currentEditTxt:EditText
    lateinit var nextEditTxt:EditText
    lateinit var instructionsEditTxt:EditText
    lateinit var submitBtn:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable<Equipment?>("equipment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_new_service, container, false)


        ((activity as AppCompatActivity).supportActionBar?.getCustomView()!!.findViewById(R.id.app_title_tv) as TextView).text = "New Service"



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)

        frequencyTxt = view.findViewById(R.id.new_service_frequency_title_txt)
        currentTxt = view.findViewById(R.id.new_service_current_title_txt)
        nextTxt = view.findViewById(R.id.new_service_next_title_txt)

        nameEditTxt = view.findViewById(R.id.new_service_name_editTxt)
        typeEditTxt = view.findViewById(R.id.new_service_type_editTxt)
        typeEditTxt.setInputType(InputType.TYPE_NULL)

        //TODO: figure out why this requires two taps and fix, alternatively change to just TextView instead of EditText (TextView was only chosen for visual consistency)
        typeEditTxt.setOnClickListener{

            showTypeMenu()
        }
        /*
        typeEditTxt.setOnFocusChangeListener {

        }

         */

        frequencyEditTxt = view.findViewById(R.id.new_service_frequency_editTxt)
        currentEditTxt = view.findViewById(R.id.new_service_current_editTxt)
        nextEditTxt = view.findViewById(R.id.new_service_next_editTxt)
        instructionsEditTxt = view.findViewById(R.id.new_service_instructions_editTxt)

        submitBtn = view.findViewById(R.id.new_service_submit_btn)
        submitBtn.setOnClickListener{
            println("status btn clicked")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentTime = Date()

        newService = EquipmentService(  "0", //temp
            "tempName",
            "@string/",
            null,
            loggedInEmployee!!.ID, //added by
            "0", // 0 = not started
            equipment!!.ID, //equipment ID
            null,
            null,
            sdf.format(currentTime),
            null,
            null,
            null,
            null,
            null,
            null,
            false
        )

        hideProgressView()

    }

    private fun showTypeMenu(){
        println("showTypeMenu")

        var popUp = PopupMenu(com.example.AdminMatic.myView.context,new_service_type_editTxt)
        popUp.inflate(R.menu.new_service_type_menu)
        popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.new_service_type_menu_one_time -> {
                    Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()
                }
                R.id.new_service_type_menu_date_based -> {
                    Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()
                }
                R.id.new_service_type_menu_mile_km_based -> {
                    Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()
                }
                R.id.new_service_type_menu_engine_hour_based -> {
                    Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()
                }
                R.id.new_service_type_menu_inspection -> {
                    Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT).show()
                }
            }

            true
        })

        popUp.show()

        popUp.gravity = Gravity.LEFT
        popUp.show()
    }

    fun showProgressView() {
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EquipmentListFragment().apply {

            }
    }
}