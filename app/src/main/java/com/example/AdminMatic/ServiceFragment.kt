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

    private lateinit var pgsBar: ProgressBar
    private lateinit var nameTxt:TextView
    private lateinit var typeTxt:TextView
    private lateinit var dueTxt:TextView
    private lateinit var frequencyTxt:TextView
    private lateinit var addedByTxt:TextView
    private lateinit var instructionsTxt:TextView

    private lateinit var statusBtn: ImageButton

    private lateinit var currentEditTxt:EditText
    private lateinit var nextEditTxt:EditText
    private lateinit var notesEditText: EditText

    // lateinit var  btn: Button

    lateinit var adapter:EquipmentDetailAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service = it.getParcelable("service")
            historyMode = it.getBoolean("historyMode")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        println("onCreateView")
        globalVars = GlobalVars()
        myView = inflater.inflate(R.layout.fragment_service, container, false)


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.service)



        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")
        pgsBar = view.findViewById(R.id.progressBar)

        nameTxt = myView.findViewById(R.id.service_name_txt)
        typeTxt = myView.findViewById(R.id.service_type_txt)
        dueTxt = myView.findViewById(R.id.service_due_txt)
        frequencyTxt = myView.findViewById(R.id.service_frequency_txt)
        addedByTxt = myView.findViewById(R.id.service_added_by_txt)
        instructionsTxt = myView.findViewById(R.id.service_instructions_txt)

        currentEditTxt = myView.findViewById(R.id.current_editTxt)
        nextEditTxt = myView.findViewById(R.id.next_editTxt)
        notesEditText = myView.findViewById(R.id.service_notes_editTxt)

        notesEditText.addTextChangedListener(object : TextWatcher {

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

        statusBtn = myView.findViewById(R.id.service_status_btn)
        statusBtn.setOnClickListener{
            println("status btn clicked")
            showStatusMenu()
        }

        //Date stuff

        println(service!!.createDate)
        val createDate = LocalDateTime.parse(service!!.createDate, dateFormatterPHP)
        val currentDate = LocalDateTime.now()
        val nextDate = createDate.plusDays(service!!.frequency!!.toLong())


        nameTxt.text = service!!.name
        typeTxt.text = activity!!.getString(R.string.service_type, service!!.typeName)
        if(service!!.addedBy != null){
            addedByTxt.text = activity!!.getString(R.string.service_by, service!!.addedBy, createDate.format(dateFormatterShort))
        }
        if(service!!.instruction != null){
            instructionsTxt.text = service!!.instruction
        }


        currentEditTxt.setText(dateFormatterShort.format(currentDate))

        //TODO: make due text red if it's overdue

        when (service!!.type) {
            "0" -> { //one time
                typeTxt.text = getString(R.string.service_type, getString(R.string.service_type_one_time))
                nextEditTxt.visibility = View.GONE
                if(service!!.completionDate != null){
                    dueTxt.text = activity!!.getString(R.string.service_due, activity!!.getString(R.string.now), "")
                }
                frequencyTxt.text = activity!!.getString(R.string.service_frequency, getString(R.string.na), "")
            }
            "1" -> { //date based
                typeTxt.text = getString(R.string.service_type, getString(R.string.service_type_date_based))
                nextEditTxt.setText(dateFormatterShort.format(nextDate))
                dueTxt.text = activity!!.getString(R.string.service_due, nextDate.format(dateFormatterShort), "")
                if (service!!.frequency != null) {
                    frequencyTxt.text = activity!!.getString(R.string.service_frequency, service!!.frequency, activity!!.getString(R.string.days))
                }
            }
            "2" -> { //mile/km. based
                typeTxt.text = getString(R.string.service_type, getString(R.string.service_type_mile_km_based))
                frequencyTxt.text = getString(R.string.service_frequency, service!!.frequency, getString(R.string.mi_km))
                val nextValue = service!!.nextValue!!.toInt() + service!!.frequency!!.toInt()
                currentEditTxt.text = null
                nextEditTxt.setText(nextValue.toString())
                dueTxt.text = activity!!.getString(R.string.service_due, nextValue.toString(), getString(R.string.mi_km))
            }
            "3" -> { //engine hour based
                typeTxt.text = getString(R.string.service_type, getString(R.string.service_type_engine_hour_based))
            }
            "4" -> { //inspection
                typeTxt.text = getString(R.string.service_type, getString(R.string.service_type_inspection))
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

        val popUp = PopupMenu(myView.context,statusBtn)
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
        pgsBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        pgsBar.visibility = View.INVISIBLE
    }

    private fun setStatus(status: String) {
        println("setStatus")
        when(status) {
            "0" -> {
                println("0")
                statusBtn.setBackgroundResource(R.drawable.ic_not_started)
            }
            "1" -> {
                println("1")
                statusBtn.setBackgroundResource(R.drawable.ic_in_progress)
            }
            "2" -> {
                println("2")
                statusBtn.setBackgroundResource(R.drawable.ic_done)
            }
            "3" -> {
                println("3")
                statusBtn.setBackgroundResource(R.drawable.ic_canceled)
            }
        }
    }

}