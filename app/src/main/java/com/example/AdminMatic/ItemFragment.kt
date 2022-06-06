package com.example.AdminMatic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.AdminMatic.R


class ItemFragment : Fragment() {

    private  var item: Item? = null

    lateinit  var globalVars:GlobalVars
    lateinit var myView:View

    lateinit var  pgsBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_item, container, false)
        myView = inflater.inflate(R.layout.fragment_item, container, false)

        globalVars = GlobalVars()
        ((activity as AppCompatActivity).supportActionBar?.customView!!.findViewById(R.id.app_title_tv) as TextView).text = getString(R.string.item)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("item = ${item!!.name}")


        pgsBar = view.findViewById(R.id.progress_bar)

    }

}