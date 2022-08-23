package com.example.AdminMatic

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentServiceBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.AdminMatic.GlobalVars.Companion.dateFormatterPHP
import com.example.AdminMatic.GlobalVars.Companion.dateFormatterShort
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime


class ServiceFragment : Fragment() {

    private var service: EquipmentService? = null
    private var historyMode = false

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var adapter:EquipmentDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable("service")
            historyMode = it.getBoolean("historyMode")
        }
    }

    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentServiceBinding.inflate(inflater, container, false)
        myView = binding.root

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.service)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        println("onViewCreated")

        binding.serviceNotesEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                service!!.completionNotes = s.toString()
                println("${service!!.completionNotes}")
            }
        })

        binding.serviceStatusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        //Date stuff

        println(service!!.createDate)
        val createDate = LocalDateTime.parse(service!!.createDate, dateFormatterPHP)
        val currentDate = LocalDateTime.now()
        val nextDate = createDate.plusDays(service!!.frequency!!.toLong())


        binding.serviceNameTxt.text = service!!.name
        binding.serviceTypeTxt.text = activity!!.getString(R.string.service_type, service!!.typeName)
        if(service!!.addedBy != null){
            binding.serviceAddedByTxt.text = activity!!.getString(R.string.service_by, service!!.addedBy, createDate.format(dateFormatterShort))
        }
        if(service!!.instruction != null){
            binding.serviceInstructionsTxt.text = service!!.instruction
        }


        binding.currentEditTxt.setText(dateFormatterShort.format(currentDate))

        //TODO: make due text red if it's overdue

        when (service!!.type) {
            "0" -> { //one time
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_one_time))
                binding.nextEditTxt.visibility = View.GONE
                if(service!!.completionDate != null){
                    binding.serviceDueTxt.text = activity!!.getString(R.string.service_due, activity!!.getString(R.string.now), "")
                }
                binding.serviceFrequencyTxt.text = activity!!.getString(R.string.service_frequency, getString(R.string.na), "")
            }
            "1" -> { //date based
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_date_based))
                binding.nextEditTxt.setText(dateFormatterShort.format(nextDate))
                binding.serviceDueTxt.text = activity!!.getString(R.string.service_due, nextDate.format(dateFormatterShort), "")
                if (service!!.frequency != null) {
                    binding.serviceFrequencyTxt.text = activity!!.getString(R.string.service_frequency, service!!.frequency, activity!!.getString(R.string.days))
                }
            }
            "2" -> { //mile/km. based
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_mile_km_based))
                binding.serviceFrequencyTxt.text = getString(R.string.service_frequency, service!!.frequency, getString(R.string.mi_km))
                val nextValue = service!!.nextValue!!.toInt() + service!!.frequency!!.toInt()
                binding.currentEditTxt.text = null
                binding.nextEditTxt.setText(nextValue.toString())
                binding.serviceDueTxt.text = activity!!.getString(R.string.service_due, nextValue.toString(), getString(R.string.mi_km))
            }
            "3" -> { //engine hour based
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_engine_hour_based))
            }
            "4" -> { //inspection
                binding.serviceTypeTxt.text = getString(R.string.service_type, getString(R.string.service_type_inspection))
            }

        }


        setStatus(service!!.status.toString())

        hideProgressView()

    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("service")
    }

    private fun showStatusMenu(){
        println("showStatusMenu")

        val popUp = PopupMenu(myView.context, binding.serviceStatusBtn)
        popUp.inflate(R.menu.task_status_menu)
        popUp.menu.add(0, 0, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_not_started)!!,myView.context), myView.context.getString(R.string.not_started)))
        popUp.menu.add(0, 1, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_in_progress)!!,myView.context), myView.context.getString(R.string.in_progress)))
        popUp.menu.add(0, 2, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_done)!!,myView.context), myView.context.getString(R.string.finished)))
        popUp.menu.add(0, 3, 1, globalVars.menuIconWithText(globalVars.resize(ContextCompat.getDrawable(myView.context, R.drawable.ic_canceled)!!,myView.context), myView.context.getString(R.string.canceled)))
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            service!!.status = item!!.itemId.toString()

            setStatus(service!!.status.toString())
            Toast.makeText(com.example.AdminMatic.myView.context, item.title, Toast.LENGTH_SHORT)
                .show()


            showProgressView()

            var urlString =
                "https://www.adminmatic.com/cp/app/" + GlobalVars.phpVersion + "/functions/update/equipmentServiceComplete.php"

            val currentTimestamp = System.currentTimeMillis()
            println("urlString = ${"$urlString?cb=$currentTimestamp"}")
            urlString = "$urlString?cb=$currentTimestamp"

            val postRequest1: StringRequest = object : StringRequest(
                Method.POST, urlString,
                Response.Listener { response -> // response

                    println("Response $response")

                    try {
                        val parentObject = JSONObject(response)
                        println("parentObject = $parentObject")
                        globalVars.checkPHPWarningsAndErrors(parentObject, myView.context, myView)

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
                    val params: MutableMap<String, String> = java.util.HashMap()
                    params["ID"] = service!!.ID
                    params["completeValue"] = service!!.completionMileage.toString()
                    params["completedBy"] = GlobalVars.loggedInEmployee!!.ID
                    params["completionNotes"] = service!!.completionNotes.toString()
                    params["nextValue"] = service!!.nextValue.toString()
                    params["status"] = service!!.status.toString()
                    params["type"] = service!!.type
                    params["sessionKey"] = GlobalVars.loggedInEmployee!!.sessionKey
                    params["companyUnique"] = GlobalVars.loggedInEmployee!!.companyUnique

                    println("params = $params")
                    return params
                }
            }
            postRequest1.tag = "service"
            VolleyRequestQueue.getInstance(requireActivity().application).addToRequestQueue(postRequest1)
            true
        }

        //TODO: add toast here for new service alerts. swift code:
        /*if self.statusValueToUpdate == "2"{
                    switch (self.equipmentService.type) {
                    case "0":
                        print("no alert necessary")
                        break
                    case "1":
                        self.layoutVars.simpleAlert(_vc: self.layoutVars.getTopController(), _title: "New Service Added", _message: "A new \(self.equipmentService.typeName) service has been added to be done on \(self.layoutVars.determineUpcomingDate(_equipmentService: self.equipmentService)).")
                        break
                    case "2":
                        self.layoutVars.simpleAlert(_vc: self.layoutVars.getTopController(), _title: "New Service Added", _message: "A new \(self.equipmentService.typeName) service has been added to be done at \(self.equipmentService.nextValue!) Miles/Km..")
                        break
                    case "3":
                        self.layoutVars.simpleAlert(_vc: self.layoutVars.getTopController(), _title: "New Service Added", _message: "A new \(self.equipmentService.typeName) service has been added to be done at \(self.equipmentService.nextValue!) Engine Hours.")
                        break
                    default:
                        print("no alert necessary")
                        break
                    }
                }
         */

        popUp.gravity = Gravity.START
        popUp.show()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "0" -> {
                println("0")
                binding.serviceStatusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "1" -> {
                println("1")
                binding.serviceStatusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "2" -> {
                println("2")
                binding.serviceStatusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "3" -> {
                println("3")
                binding.serviceStatusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
        }
    }

}