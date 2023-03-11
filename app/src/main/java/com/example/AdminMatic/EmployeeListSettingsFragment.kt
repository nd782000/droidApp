package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentEmployeeListSettingsBinding


class EmployeeListSettingsFragment : Fragment() {

    private var showInactive = false


    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            showInactive = it.getBoolean("showInactive")
        }
    }

    private var _binding: FragmentEmployeeListSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEmployeeListSettingsBinding.inflate(inflater, container, false)
        myView = binding.root

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.employees_list_settings)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("handleOnBackPressed")
                setFragmentResult("_showInactive", bundleOf("_showInactive" to showInactive))
                myView.findNavController().navigateUp()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        return myView
    }

    /*
    override fun onStop() {
        super.onStop()
        VolleyRequestQueue.getInstance(requireActivity().application).requestQueue.cancelAll("documents")
    }

     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showInactiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            showInactive = isChecked
        }

        binding.clearAllFiltersBtn.setOnClickListener {
            setFragmentResult("_showInactive", bundleOf("_showInactive" to false))
            myView.findNavController().navigateUp()
        }

        binding.showInactiveSwitch.isChecked = showInactive
        binding.showInactiveSwitch.jumpDrawablesToCurrentState()
    }



}
