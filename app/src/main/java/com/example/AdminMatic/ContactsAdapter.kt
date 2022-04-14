package com.example.AdminMatic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import kotlinx.android.synthetic.main.employee_list_item.view.*
import java.util.*


class ContactsAdapter(private val list: MutableList<Contact>, private val context: Context,private val cellClickListener: ContactCellClickListener)

    : RecyclerView.Adapter<ContactViewHolder>(){


    var filterList:MutableList<Contact> = emptyList<Contact>().toMutableList()


    //var queryText = ""

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContactViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {




        val contact: Contact = filterList[position]
        holder.bind(contact)

        holder.itemView.list_name.text = filterList[position].value!!



        val contactIconView:ImageView = holder.itemView.findViewById<ImageView>(R.id.contact_icon_view)




        val typeTxt:TextView = holder.itemView.findViewById<TextView>(R.id.contact_type_tv)
        when (filterList[position].type) {


            //Main Phone
            "1" -> {
                contactIconView.setImageResource(R.drawable.ic_phone);
                typeTxt.text = "Main Phone"
            }
            //Main Email
            "2" -> {
                contactIconView.setImageResource(R.drawable.ic_email);
                typeTxt.text = "Main Email"



            }
            //Billing Address
            "3" -> {
                contactIconView.setImageResource(R.drawable.ic_map);
                typeTxt.text = "Billing Address"
            }
            //Jobsite Address
            "4" -> {
                contactIconView.setImageResource(R.drawable.ic_map);
                typeTxt.text = "Jobsite Address"
            }
            //Website
            "5" -> {
                contactIconView.setImageResource(R.drawable.ic_web);
                typeTxt.text = "Website"
            }
            //Alt Contact
            "6" -> {
                contactIconView.setImageResource(R.drawable.ic_person);
                typeTxt.text = "Alt Contact"
            }
            //Fax
            "7" -> {
                contactIconView.setImageResource(R.drawable.ic_web);
                typeTxt.text = "Fax"
            }
            //Alt Phone
            "8" -> {
                contactIconView.setImageResource(R.drawable.ic_phone);
                typeTxt.text = "Alt Phone"
            }
            //Alt Email
            "9" -> {
                contactIconView.setImageResource(R.drawable.ic_email);
                typeTxt.text = "Alt Email"
            }
            //Mobile
            "10" -> {
                contactIconView.setImageResource(R.drawable.ic_phone);
                typeTxt.text = "Mobile"
            }
            //Alt Mobile
            "11" -> {
                contactIconView.setImageResource(R.drawable.ic_phone);
                typeTxt.text = "Alt Mobile"
            }
            //Home
            "12" -> {
                contactIconView.setImageResource(R.drawable.ic_phone);
                typeTxt.text = "Home Phone"
            }
            //Alt Email
            "13" -> {
                contactIconView.setImageResource(R.drawable.ic_email);
                typeTxt.text = "Alt Email"
            }
            //Invoice Address
            "14" -> {
                contactIconView.setImageResource(R.drawable.ic_map);
                typeTxt.text = "Invoice Address"
            }

        }




        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onContactCellClickListener(data)
        }







    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class ContactViewHolder(inflater: LayoutInflater, parent: ViewGroup, var contact:Contact? = null) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.contact_list_item, parent, false)) {
    private var mNameView: TextView? = null



    init {
        mNameView = itemView.findViewById(R.id.list_name)

    }

    fun bind(contact: Contact) {
        mNameView?.text = contact.value

    }



}