package com.example.AdminMatic

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewServiceBinding
import com.example.AdminMatic.GlobalVars.Companion.loggedInEmployee
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class NewServiceFragment : Fragment() {

    private var equipment: Equipment? = null
    private var newService: EquipmentService? = null


    private var currentDate = LocalDateTime.now()
    private var nextDate = currentDate

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            equipment = it.getParcelable("equipment")
        }
    }

    private var _binding: FragmentNewServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("onCreateView")
        globalVars = GlobalVars()
        _binding = FragmentNewServiceBinding.inflate(inflater, container, false)
        myView = binding.root

        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_service)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //need to wait for this function to initialize views
        println("onViewCreated")

        /** Using these makes it so typeEditText only requires one tap, but it won't
         * close currently opened keyboards which could create bugs when the type changes
         * and a different input type is required
        **/
        //typeEditTxt.isFocusable = false
        //typeEditTxt.isFocusableInTouchMode = false

        binding.newServiceSubmitBtn.setOnClickListener{
            println("status btn clicked")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentTime = Date()

        newService = EquipmentService(
            "0", //temp
            "Untitled",
            "0",
            getString(R.string.service_type_one_time), //typeName
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

        binding.newServiceNameEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                newService!!.name = s.toString()
            }
        })
        binding.newServiceFrequencyEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (s.isNotBlank()) {
                    newService!!.frequency = s.toString()
                    when (newService!!.type) {
                        "1" -> { // date based
                            if (newService!!.frequency != null) {
                                nextDate = currentDate.plusDays(newService!!.frequency!!.toLong())
                            }
                            binding.newServiceNextEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                            newService!!.nextValue = GlobalVars.dateFormatterPHP.format(nextDate)
                        }
                        "2" -> { // mile/km based
                            val tempNext =
                                newService!!.currentValue!!.toInt() + newService!!.frequency!!.toInt()
                            newService!!.nextValue = tempNext.toString()
                            binding.newServiceNextEditTxt.setText(tempNext.toString())
                        }
                        "3" -> { // engine hour based
                            nextDate = currentDate.plusDays(newService!!.frequency!!.toLong())
                            val tempNext = newService!!.currentValue!!.toInt() + newService!!.frequency!!.toInt()
                            newService!!.nextValue = tempNext.toString()
                            binding.newServiceNextEditTxt.setText(tempNext.toString())
                        }
                    }
                }
            }

        })
        binding.newServiceCurrentEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (newService!!.type != "1") { // Only change if not in date mode, in date mode it changes itself
                    newService!!.currentValue = s.toString()
                }
            }
        })
        binding.newServiceNextEditTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (newService!!.type != "1") {
                    newService!!.nextValue = s.toString()
                }
            }
        })
        binding.newServiceInstructionsEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                newService!!.instruction = s.toString()
            }
        })

        binding.newServiceTypeTxt.setOnClickListener{
            showTypeMenu()
            hideKeyboard()
        }

        val dateSetListenerCurrent =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                currentDate = LocalDateTime.of(year, monthOfYear, dayOfMonth, 0, 0)
                binding.newServiceCurrentEditTxt.setText(GlobalVars.dateFormatterShort.format(currentDate))
                newService!!.currentValue = GlobalVars.dateFormatterPHP.format(currentDate)
            }

        // Todo: Finish this (editing next date currently just applies to current date)
        val dateSetListenerNext =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                nextDate = LocalDateTime.of(year, monthOfYear, dayOfMonth, 0, 0)
                binding.newServiceNextEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                newService!!.nextValue = GlobalVars.dateFormatterPHP.format(nextDate)
            }

        binding.newServiceCurrentEditTxt.setOnClickListener {
            context?.let { it1 ->
                if(newService!!.type == "1") {
                    DatePickerDialog(
                        it1,
                        dateSetListenerCurrent,
                        // set DatePickerDialog to point to today's date when it loads up
                        nextDate.year,
                        nextDate.monthValue,
                        nextDate.dayOfYear
                    ).show()
                }
            }
        }

        binding.newServiceNextEditTxt.setOnClickListener {
            context?.let { it1 ->
                if(newService!!.type == "1") {
                    DatePickerDialog(
                        it1,
                        dateSetListenerCurrent,
                        // set DatePickerDialog to point to today's date when it loads up
                        currentDate.year,
                        currentDate.monthValue,
                        currentDate.dayOfYear
                    ).show()
                }
            }
        }

        //Set up the elements to start in "one time" mode
        //nameEditTxt.setText(R.string.new_service_name)
        binding.newServiceTypeTxt.setText(R.string.service_type_one_time)
        binding.newServiceFrequencyEditTxt.isFocusable = false
        binding.newServiceFrequencyEditTxt.isFocusableInTouchMode = false
        binding.newServiceCurrentEditTxt.isFocusable = false
        binding.newServiceCurrentEditTxt.isFocusableInTouchMode = false
        binding.newServiceNextEditTxt.isFocusable = false
        binding.newServiceNextEditTxt.isFocusableInTouchMode = false

        hideProgressView()

    }

    private fun showTypeMenu(){
        println("showTypeMenu")

        val popUp = PopupMenu(com.example.AdminMatic.myView.context,binding.newServiceTypeTxt)
        popUp.inflate(R.menu.new_service_type_menu)
        popUp.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.new_service_type_menu_one_time -> {
                    newService!!.type = "0"
                    newService!!.typeName = getString(R.string.service_type_one_time)
                    binding.newServiceTypeTxt.setText(R.string.service_type_one_time)
                    binding.newServiceFrequencyTitleTxt.setText(R.string.new_service_frequency_days)
                    binding.newServiceCurrentTitleTxt.setText(R.string.new_service_current)
                    binding.newServiceNextTitleTxt.setText(R.string.new_service_next)
                    binding.newServiceFrequencyEditTxt.isFocusable = false
                    binding.newServiceCurrentEditTxt.isFocusable = false
                    binding.newServiceNextEditTxt.isFocusable = false
                    binding.newServiceFrequencyEditTxt.isFocusableInTouchMode = false
                    binding.newServiceCurrentEditTxt.isFocusableInTouchMode = false
                    binding.newServiceNextEditTxt.isFocusableInTouchMode = false
                    binding.newServiceFrequencyEditTxt.text = null
                    binding.newServiceCurrentEditTxt.text = null
                    binding.newServiceNextEditTxt.text = null
                    newService!!.frequency = null
                    newService!!.currentValue = null
                    newService!!.nextValue = null
                }
                R.id.new_service_type_menu_date_based -> {
                    newService!!.type = "1"
                    newService!!.typeName = getString(R.string.service_type_date_based)
                    binding.newServiceTypeTxt.setText(R.string.service_type_date_based)
                    binding.newServiceFrequencyTitleTxt.setText(R.string.new_service_frequency_days)
                    binding.newServiceCurrentTitleTxt.setText(R.string.new_service_current)
                    binding.newServiceNextTitleTxt.setText(R.string.new_service_next)
                    binding.newServiceFrequencyEditTxt.isFocusable = true
                    binding.newServiceCurrentEditTxt.isFocusable = false
                    binding.newServiceNextEditTxt.isFocusable = false
                    binding.newServiceFrequencyEditTxt.isFocusableInTouchMode = true
                    binding.newServiceCurrentEditTxt.isFocusableInTouchMode = false
                    binding.newServiceNextEditTxt.isFocusableInTouchMode = false
                    binding.newServiceFrequencyEditTxt.text = null
                    binding.newServiceCurrentEditTxt.text = null
                    binding.newServiceNextEditTxt.text = null
                    binding.newServiceFrequencyEditTxt.inputType = InputType.TYPE_CLASS_DATETIME
                    binding.newServiceNextEditTxt.inputType = InputType.TYPE_CLASS_DATETIME
                    newService!!.frequency = "0"
                    binding.newServiceFrequencyEditTxt.setText(GlobalVars.dateFormatterShort.format(currentDate))
                    binding.newServiceNextEditTxt.setText(GlobalVars.dateFormatterShort.format(nextDate))
                    newService!!.currentValue = GlobalVars.dateFormatterPHP.format(currentDate)
                    newService!!.nextValue = GlobalVars.dateFormatterPHP.format(nextDate)
                }
                R.id.new_service_type_menu_mile_km_based -> {
                    newService!!.type = "2"
                    newService!!.typeName = getString(R.string.service_type_mile_km_based)
                    binding.newServiceTypeTxt.setText(R.string.service_type_mile_km_based)
                    binding.newServiceFrequencyTitleTxt.setText(R.string.new_service_frequency_miles_km)
                    binding.newServiceCurrentTitleTxt.setText(R.string.new_service_current_miles_km)
                    binding.newServiceNextTitleTxt.setText(R.string.new_service_next_miles_km)
                    binding.newServiceFrequencyEditTxt.isFocusable = true
                    binding.newServiceCurrentEditTxt.isFocusable = true
                    binding.newServiceNextEditTxt.isFocusable = true
                    binding.newServiceFrequencyEditTxt.isFocusableInTouchMode = true
                    binding.newServiceCurrentEditTxt.isFocusableInTouchMode = true
                    binding.newServiceNextEditTxt.isFocusableInTouchMode = true
                    binding.newServiceFrequencyEditTxt.text = null
                    binding.newServiceCurrentEditTxt.setText("0")
                    binding.newServiceNextEditTxt.setText("0")
                    binding.newServiceCurrentEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    binding.newServiceNextEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    newService!!.frequency = "0"
                    newService!!.currentValue = "0"
                    newService!!.nextValue = "0"
                }
                R.id.new_service_type_menu_engine_hour_based -> {
                    newService!!.type = "3"
                    newService!!.typeName = getString(R.string.service_type_engine_hour_based)
                    binding.newServiceTypeTxt.setText(R.string.service_type_engine_hour_based)
                    binding.newServiceFrequencyTitleTxt.setText(R.string.new_service_frequency_engine_hours)
                    binding.newServiceCurrentTitleTxt.setText(R.string.new_service_current_engine_hours)
                    binding.newServiceNextTitleTxt.setText(R.string.new_service_next_engine_hours)
                    binding.newServiceFrequencyEditTxt.isFocusable = true
                    binding.newServiceCurrentEditTxt.isFocusable = true
                    binding.newServiceNextEditTxt.isFocusable = true
                    binding.newServiceFrequencyEditTxt.isFocusableInTouchMode = true
                    binding.newServiceCurrentEditTxt.isFocusableInTouchMode = true
                    binding.newServiceNextEditTxt.isFocusableInTouchMode = true
                    binding.newServiceFrequencyEditTxt.text = null
                    binding.newServiceCurrentEditTxt.setText("0")
                    binding.newServiceNextEditTxt.setText("0")
                    binding.newServiceCurrentEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    binding.newServiceNextEditTxt.inputType = InputType.TYPE_CLASS_NUMBER
                    newService!!.frequency = "0"
                    newService!!.currentValue = "0"
                    newService!!.nextValue = "0"
                }
                R.id.new_service_type_menu_inspection -> {
                    newService!!.type = "4"
                    newService!!.typeName = getString(R.string.service_type_inspection)
                    binding.newServiceTypeTxt.setText(R.string.service_type_inspection)
                    binding.newServiceFrequencyTitleTxt.setText(R.string.new_service_frequency_days)
                    binding.newServiceCurrentTitleTxt.setText(R.string.new_service_current)
                    binding.newServiceNextTitleTxt.setText(R.string.new_service_next)
                    binding.newServiceFrequencyEditTxt.isFocusable = false
                    binding.newServiceCurrentEditTxt.isFocusable = false
                    binding.newServiceNextEditTxt.isFocusable = false
                    binding.newServiceFrequencyEditTxt.isFocusableInTouchMode = false
                    binding.newServiceCurrentEditTxt.isFocusableInTouchMode = false
                    binding.newServiceNextEditTxt.isFocusableInTouchMode = false
                    binding.newServiceFrequencyEditTxt.text = null
                    binding.newServiceCurrentEditTxt.text = null
                    binding.newServiceNextEditTxt.text = null
                    newService!!.frequency = null
                    newService!!.currentValue = null
                    newService!!.nextValue = null
                }
            }

            true
        }

        popUp.show()

        popUp.gravity = Gravity.START
        popUp.show()
    }

    fun showProgressView() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressView() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun hideKeyboard() {
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}