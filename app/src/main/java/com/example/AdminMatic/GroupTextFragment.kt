package com.example.AdminMatic

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentGroupTextBinding
import com.example.AdminMatic.MainActivity.DoneOnEditorActionListener
import com.squareup.picasso.Picasso

interface EmployeeCheckClickListener {
    fun onEmployeeCheckClickListener(index: Int, checkImageView: ImageView)
}

class GroupTextFragment : Fragment(), EmployeeCheckClickListener {


    lateinit var globalVars:GlobalVars
    lateinit var myView:View

    private lateinit var employeeList:Array<Employee>
    private lateinit var employeeCheckList:Array<Boolean>
    private var messageString = ""
    private var batchNumber = 0
    private var totalBatches = 0
    private var numbersThisBatch = 0
    private var batchSize = 7 // How many messages are sent at once while batching

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employeeList = (it.getParcelableArray("employeeList") as Array<Employee>?)!!
        }
        employeeCheckList = Array(employeeList.size) { true }
    }

    private var _binding: FragmentGroupTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGroupTextBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.group_text)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.with(context).load(R.drawable.ic_check_enabled).fetch()
        Picasso.with(context).load(R.drawable.ic_check_disabled).fetch()


        binding.groupTextEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                messageString = s.toString()
            }
        })
        binding.groupTextEt.setOnEditorActionListener(DoneOnEditorActionListener())

        binding.groupTextSelectNoneBtn.setOnClickListener {
            if (batchNumber == 0) {
                for (i in employeeCheckList.indices) {
                    employeeCheckList[i] = false
                }
                binding.groupTextRv.adapter?.notifyDataSetChanged()
                updateButtonText()
            }
            else {
                val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                builder.setTitle(getString(R.string.dialogue_group_text_stop_batch_title))
                builder.setMessage(getString(R.string.dialogue_group_text_stop_batch_body))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    batchNumber = 0
                    for (i in employeeCheckList.indices) {
                        employeeCheckList[i] = false
                    }
                    binding.groupTextRv.adapter?.notifyDataSetChanged()
                    batchNumber = 0
                    updateButtonText()
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                }
                builder.show()
            }
        }

        binding.groupTextSelectAllBtn.setOnClickListener {
            if (batchNumber == 0) {
                for (i in employeeCheckList.indices) {
                    if (employeeList[i].phone != "" && employeeList[i].phone != "No Phone Number")
                        employeeCheckList[i] = true
                }
                binding.groupTextRv.adapter?.notifyDataSetChanged()
                updateButtonText()
            }
            else {
                val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                builder.setTitle(getString(R.string.dialogue_group_text_stop_batch_title))
                builder.setMessage(getString(R.string.dialogue_group_text_stop_batch_body))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    for (i in employeeCheckList.indices) {
                        if (employeeList[i].phone != "" && employeeList[i].phone != "No Phone Number")
                            employeeCheckList[i] = true
                    }
                    binding.groupTextRv.adapter?.notifyDataSetChanged()
                    batchNumber = 0
                    updateButtonText()
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                }
                builder.show()
            }
        }

        binding.groupTextSendBtn.setOnClickListener {
            var atLeastOneChecked = false
            for (i in employeeCheckList.indices) {
                if (employeeCheckList[i]) {
                    atLeastOneChecked = true
                    break
                }
            }

            if (atLeastOneChecked) {
                if (messageString.isEmpty()) {
                    globalVars.simpleAlert(myView.context, getString(R.string.dialogue_group_text_message_empty_title), getString(R.string.dialogue_group_text_message_empty_body))
                }
                else {

                    // Create the list of phone numbers
                    val numberList = mutableListOf<String>()
                    for (i in employeeCheckList.indices) {
                        if (employeeCheckList[i]) {
                            numberList.add(employeeList[i].phone)
                        }
                    }

                    println("Number List Size: ${numberList.size}")

                    // If more than the batch size are checked and we're not currently in the middle of a batch...
                    if (numberList.size > batchSize && batchNumber == 0) {
                        // This not being 0 tells the code we're in the middle of a batch send
                        batchNumber = 1
                        // Get the total number of batches via integer division and a modulus
                        totalBatches = numberList.size / batchSize
                        if (numberList.size % batchSize != 0) {
                            totalBatches++
                        }
                        println("TOTAL BATCHES: $totalBatches")
                    }



                    var numberString = "smsto:"

                    // If not batching, just send all
                    if (batchNumber == 0) {
                        for (i in numberList.indices) {
                            if (i > 0) {
                                numberString += ","
                            }
                            numberString += numberList[i]
                        }
                    }
                    else {
                        val batchStart = (batchNumber-1) * batchSize
                        var batchEnd = batchStart + batchSize -1
                        if (batchNumber == totalBatches) {
                            println("FUCK")
                            batchEnd = numberList.size - 1
                        }

                        numbersThisBatch = batchEnd - batchStart + 1

                        println("Batch #$batchNumber of $totalBatches")
                        println("Batch Start: $batchStart")
                        println("Batch End: $batchEnd")

                        // Add the batch of numbers
                        for (i in batchStart..batchEnd) {
                            numberString += numberList[i]
                            if (i != batchEnd) {
                                numberString += ","
                            }
                        }

                    }


                    val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
                    //builder.setTitle(getString(R.string.dialogue_delete_wo_item_title))
                    if (batchNumber == 0) {
                        builder.setMessage(getString(R.string.dialogue_group_text_send, numberList.size.toString()))
                    }
                    else {
                        builder.setMessage(getString(R.string.dialogue_group_text_send_batch, numbersThisBatch, batchNumber, totalBatches))
                    }
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(numberString))
                        println("Numbers: $numberString")
                        smsIntent.putExtra("sms_body", getString(R.string.group_text_body, messageString, GlobalVars.loggedInEmployee!!.name))
                        startActivity(smsIntent)

                        if (batchNumber != 0 && batchNumber == totalBatches) {
                            batchNumber = 0 // done batching
                        }
                        else {
                            batchNumber++
                        }

                        updateButtonText()

                    }
                    builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                        // If this was the first text in the batch, cancel the batching
                        if (batchNumber == 1) {
                            batchNumber = 0
                        }
                    }
                    builder.show()


                }

            }
            else {
                globalVars.simpleAlert(myView.context, getString(R.string.dialogue_group_text_none_selected_title), getString(R.string.dialogue_group_text_none_selected_body))
            }

        }

        binding.groupTextRv.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = EmployeesTextAdapter(employeeList, employeeCheckList, this@GroupTextFragment)

            val itemDecoration: RecyclerView.ItemDecoration =
                DividerItemDecoration(myView.context, DividerItemDecoration.VERTICAL)
            binding.groupTextRv.addItemDecoration(itemDecoration)
        }

        updateButtonText()



    }

    fun updateButtonText() {
        var amountChecked = 0
        for (i in employeeCheckList.indices) {
            if (employeeCheckList[i]) {
                amountChecked++
            }
        }

        if (amountChecked <= batchSize) {
            binding.groupTextSendBtn.text = getString(R.string.group_text_send)
        }
        else {
            var totalBatches = amountChecked / batchSize
            if (amountChecked % batchSize != 0) {
                totalBatches++
            }

            var currentBatch = batchNumber
            if (currentBatch == 0) {
                currentBatch = 1
            }

            binding.groupTextSendBtn.text = getString(R.string.group_text_send_batch, currentBatch, totalBatches)

        }
    }

    override fun onEmployeeCheckClickListener(index: Int, checkImageView: ImageView) {

        if (batchNumber == 0) {
            if (employeeCheckList[index]) {
                employeeCheckList[index] = false
                Picasso.with(context).load(R.drawable.ic_check_disabled).into(checkImageView)
            }
            else {
                if (employeeList[index].phone != "" && employeeList[index].phone != "No Phone Number") {
                    employeeCheckList[index] = true
                    Picasso.with(context).load(R.drawable.ic_check_enabled).into(checkImageView)
                }
                else {
                    globalVars.simpleAlert(com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_group_text_no_number_title), com.example.AdminMatic.myView.context.getString(R.string.dialogue_group_text_no_number_body, employeeList[index].fname))
                }
            }
        }
        else {
            val builder = AlertDialog.Builder(com.example.AdminMatic.myView.context)
            builder.setTitle(getString(R.string.dialogue_group_text_stop_batch_title))
            builder.setMessage(getString(R.string.dialogue_group_text_stop_batch_body))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                batchNumber = 0
                if (employeeCheckList[index]) {
                    employeeCheckList[index] = false
                    Picasso.with(context).load(R.drawable.ic_check_disabled).into(checkImageView)
                }
                else {
                    if (employeeList[index].phone != "" && employeeList[index].phone != "No Phone Number") {
                        employeeCheckList[index] = true
                        Picasso.with(context).load(R.drawable.ic_check_enabled).into(checkImageView)

                    }
                    else {
                        globalVars.simpleAlert(com.example.AdminMatic.myView.context, com.example.AdminMatic.myView.context.getString(R.string.dialogue_group_text_no_number_title), com.example.AdminMatic.myView.context.getString(R.string.dialogue_group_text_no_number_body, employeeList[index].fname))
                    }
                }
                updateButtonText()
            }
            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }
            builder.show()
        }
        updateButtonText()

    }

}
