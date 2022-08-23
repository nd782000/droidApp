package com.example.AdminMatic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.AdminMatic.R
import com.AdminMatic.databinding.FragmentCustomerNotesBinding


//great resource fo recyclerView inf
//https://guides.codepath.com/android/using-the-recyclerview



class CustomerNotesFragment : Fragment(){

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    private var _binding: FragmentCustomerNotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        globalVars = GlobalVars()
        _binding = FragmentCustomerNotesBinding.inflate(inflater, container, false)
        myView = binding.root


        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.customer_notes)

        // Inflate the layout for this fragment
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //need to wait for this function to initialize views
        println("onViewCreated")
    }
}