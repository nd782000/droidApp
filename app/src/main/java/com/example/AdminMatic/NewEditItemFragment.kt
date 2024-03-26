package com.example.AdminMatic

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentNewEditItemBinding
import com.AdminMatic.databinding.FragmentNewEditWorkOrderBinding
import java.util.Timer
import kotlin.concurrent.schedule

class NewEditItemFragment : Fragment(), AdapterView.OnItemSelectedListener, CustomerCellClickListener, EmployeeCellClickListener, TemplateCellClickListener {

    private var editsMade = false
    private var editsMadeDelayPassed = false

    private var item:Item? = null

    private var editMode = false


    private lateinit var itemTypesArray: Array<String>
    private lateinit var itemTypesAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getParcelable("item")
        }
        if (item != null) {
            editMode = true
        }
        else {
            item = Item("0", "", "", "","", "", "", "", "", "0", "0")
        }


    }

    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("workOrderNewEdit")
    }

    private var _binding: FragmentNewEditItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewEditItemBinding.inflate(inflater, container, false)
        myView = binding.root


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                println("handleOnBackPressed")
                if(editsMade && editsMadeDelayPassed){
                    println("edits made")
                    val builder = AlertDialog.Builder(myView.context)
                    builder.setTitle(getString(R.string.dialogue_edits_made_title))
                    builder.setMessage(R.string.dialogue_edits_made_body)
                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        myView.findNavController().navigateUp()
                    }
                    builder.setNegativeButton(R.string.no) { _, _ ->
                    }
                    builder.show()
                }else{
                    myView.findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        globalVars = GlobalVars()
        if (editMode) {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.edit_item, item!!.ID)
        }
        else {
            ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.new_item)
        }

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("on view created")
        layoutViews()
    }

    private fun layoutViews() {

        Timer("ItemEditsMade", false).schedule(500) {
            editsMade = false
            editsMadeDelayPassed = true
        }

        // Type Spinner
        itemTypesArray = arrayOf(
            getString(R.string.item_type_service),
            getString(R.string.item_type_inventory_part),
            getString(R.string.item_type_non_inventory_part),
            getString(R.string.item_type_other_charge)
        )

        itemTypesAdapter = ArrayAdapter<String>(
            myView.context,
            android.R.layout.simple_spinner_dropdown_item,
            itemTypesArray
        )
        binding.typeSpinner.adapter = itemTypesAdapter
        binding.typeSpinner.onItemSelectedListener = this@NewEditItemFragment

        if (!item!!.type.isNullOrBlank()) {
            binding.typeSpinner.setSelection(item!!.type!!.toInt())
        }

        binding.yesNoSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!editsMadeDelayPassed) {
                return@setOnCheckedChangeListener
            }

            hideShowFields()
        }

        hideShowFields()

    }

    private fun hideShowFields() {

        println("when: ${binding.typeSpinner.selectedItemPosition}")

        when (binding.typeSpinner.selectedItemPosition) {
            0 -> { // None Selected
                println("None Selected")
                binding.yesNoSwitch.visibility = View.GONE
                binding.yesNoTv.visibility = View.GONE

                binding.mainInfoCl.visibility = View.GONE
                binding.purchaseInfoCl.visibility = View.GONE
                binding.salesInfoCl.visibility = View.GONE
                binding.inventoryInfoCl.visibility = View.GONE
                binding.estimateInfoCl.visibility = View.GONE
                binding.markupMarginCl.visibility = View.GONE



                binding.submitBtn.visibility = View.GONE
            }
            1 -> { // Service
                binding.mainInfoCl.visibility = View.VISIBLE
                binding.yesNoSwitch.visibility = View.VISIBLE
                binding.yesNoTv.visibility = View.VISIBLE
                binding.yesNoTv.text = getString(R.string.is_this_item_subcontractor)

                binding.partNumberTv.visibility = View.INVISIBLE
                binding.partNumberEt.visibility = View.INVISIBLE

                if (binding.yesNoSwitch.isChecked) {
                    println("Service Yes")
                    binding.purchaseInfoCl.visibility = View.VISIBLE
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.VISIBLE
                    binding.rateTv.text = getString(R.string.sales_price_label)
                }
                else {
                    println("Service No")
                    binding.purchaseInfoCl.visibility = View.GONE
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.GONE
                    binding.rateTv.text = getString(R.string.rate_label)
                }

                binding.submitBtn.visibility = View.VISIBLE

            }
            2 -> { // Inventory Part
                println("Inventory Part")
                binding.mainInfoCl.visibility = View.VISIBLE
                binding.yesNoSwitch.visibility = View.GONE
                binding.yesNoTv.visibility = View.GONE

                binding.partNumberTv.visibility = View.VISIBLE
                binding.partNumberEt.visibility = View.VISIBLE

                binding.purchaseInfoCl.visibility = View.VISIBLE
                binding.salesInfoCl.visibility = View.VISIBLE
                binding.inventoryInfoCl.visibility = View.VISIBLE
                binding.estimateInfoCl.visibility = View.VISIBLE
                binding.markupMarginCl.visibility = View.VISIBLE
                binding.rateTv.text = getString(R.string.sales_price_label)

                binding.submitBtn.visibility = View.VISIBLE

            }
            3 -> { // Non-inventory Part
                binding.mainInfoCl.visibility = View.VISIBLE
                binding.yesNoSwitch.visibility = View.VISIBLE
                binding.yesNoTv.visibility = View.VISIBLE
                binding.yesNoTv.text = getString(R.string.is_this_item_specific_customer_job)

                binding.partNumberTv.visibility = View.VISIBLE
                binding.partNumberEt.visibility = View.VISIBLE

                if (binding.yesNoSwitch.isChecked) {
                    println("Non-inventory Part Yes")
                    binding.purchaseInfoCl.visibility = View.VISIBLE
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.VISIBLE
                    binding.rateTv.text = getString(R.string.sales_price_label)
                }
                else {
                    println("Non-inventory Part No")
                    binding.purchaseInfoCl.visibility = View.GONE
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.GONE
                    binding.rateTv.text = getString(R.string.price_label)
                }

                binding.submitBtn.visibility = View.VISIBLE

            }
            4 -> { // Other Charge

                binding.mainInfoCl.visibility = View.VISIBLE
                binding.yesNoSwitch.visibility = View.VISIBLE
                binding.yesNoTv.visibility = View.VISIBLE
                binding.yesNoTv.text = getString(R.string.is_this_item_reimbursable)

                binding.partNumberTv.visibility = View.INVISIBLE
                binding.partNumberEt.visibility = View.INVISIBLE

                if (binding.yesNoSwitch.isChecked) {
                    println("Other Charge Yes")
                    binding.purchaseInfoCl.visibility = View.VISIBLE
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.VISIBLE
                    binding.rateTv.text = getString(R.string.sales_price_label)
                }
                else {
                    println("Other Charge No")
                    binding.purchaseInfoCl.visibility = View.GONE
                    binding.salesInfoCl.visibility = View.VISIBLE
                    binding.inventoryInfoCl.visibility = View.GONE
                    binding.estimateInfoCl.visibility = View.VISIBLE
                    binding.markupMarginCl.visibility = View.VISIBLE
                    binding.rateTv.text = getString(R.string.charge_amount_label)
                }

                binding.submitBtn.visibility = View.VISIBLE
            }

        }
    }

    //<string name="is_this_item_subcontractor">Is this service performed by a subcontractor or partner?</string>
    //<string name="is_this_item_specific_customer_job">Is this item purchased for a specific customer job?</string>
    //<string name="is_this_item_reimbursable">Is this a reimbursable charge?</string>

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        binding.yesNoSwitch.isChecked = false
        binding.yesNoSwitch.jumpDrawablesToCurrentState()

        when (parent!!.id) {
            R.id.type_spinner -> {
                item!!.type = position.toString()
                hideShowFields()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("todo")
    }

    override fun onCustomerCellClickListener(data: Customer) {
        println("todo")
    }

    override fun onEmployeeCellClickListener(data: Employee) {
        println("todo")
    }

    override fun onTemplateCellClickListener(data: Template) {
        println("todo")
    }

}