package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class ContactsAdapter(list: MutableList<Contact>, private val cellClickListener: ContactCellClickListener): RecyclerView.Adapter<ContactViewHolder>(){

    var filterList:MutableList<Contact> = emptyList<Contact>().toMutableList()

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

        holder.itemView.findViewById<TextView>(R.id.list_name).text = filterList[position].value!!



        val contactIconView:ImageView = holder.itemView.findViewById(R.id.contact_icon_view)




        val typeTxt:TextView = holder.itemView.findViewById(R.id.contact_type_tv)
        when (filterList[position].type) {


            //Main Phone
            "1" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text = R.string.contacts_main_phone.toString()
            }
            //Main Email
            "2" -> {
                contactIconView.setImageResource(R.drawable.ic_email)
                typeTxt.text = R.string.contacts_main_email.toString()



            }
            //Billing Address
            "3" -> {
                contactIconView.setImageResource(R.drawable.ic_map)
                typeTxt.text = R.string.contacts_billing_address.toString()
            }
            //Jobsite Address
            "4" -> {
                contactIconView.setImageResource(R.drawable.ic_map)
                typeTxt.text = R.string.contacts_jobsite_address.toString()
            }
            //Website
            "5" -> {
                contactIconView.setImageResource(R.drawable.ic_web)
                typeTxt.text = R.string.contacts_website.toString()
            }
            //Alt Contact
            "6" -> {
                contactIconView.setImageResource(R.drawable.ic_person)
                typeTxt.text = R.string.contacts_alt_contact.toString()
            }
            //Fax
            "7" -> {
                contactIconView.setImageResource(R.drawable.ic_web)
                typeTxt.text = R.string.contacts_fax.toString()
            }
            //Alt Phone
            "8" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text = R.string.contacts_alt_phone.toString()
            }
            //Alt Email
            "9" -> {
                contactIconView.setImageResource(R.drawable.ic_email)
                typeTxt.text = R.string.contacts_alt_email.toString()
            }
            //Mobile
            "10" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text = R.string.contacts_mobile.toString()
            }
            //Alt Mobile
            "11" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text = R.string.contacts_alt_mobile.toString()
            }
            //Home
            "12" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text = R.string.contacts_home_phone.toString()
            }
            //Alt Email
            "13" -> {
                contactIconView.setImageResource(R.drawable.ic_email)
                typeTxt.text = R.string.contacts_alt_email.toString()
            }
            //Invoice Address
            "14" -> {
                contactIconView.setImageResource(R.drawable.ic_map)
                typeTxt.text = R.string.contacts_invoice_address.toString()
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