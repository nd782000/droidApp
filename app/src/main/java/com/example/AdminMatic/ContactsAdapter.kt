package com.example.AdminMatic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class ContactsAdapter(list: MutableList<Contact>, context_: Context, private val cellClickListener: ContactCellClickListener, private val editListener: ContactEditListener, private val deleteListener: ContactDeleteListener): RecyclerView.Adapter<ContactViewHolder>(){

    public var filterList:MutableList<Contact> = emptyList<Contact>().toMutableList()
    val context = context_

    init {

        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContactViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {




        val contact: Contact = filterList[position]
        holder.bind(contact, context)

        holder.itemView.findViewById<TextView>(R.id.list_name).text = filterList[position].value!!



        val contactIconView:ImageView = holder.itemView.findViewById(R.id.contact_icon_view)




        val typeTxt:TextView = holder.itemView.findViewById(R.id.contact_type_tv)
        when (filterList[position].type) {


            //todo: use the contactTypes from getfields instead of hardcoding

            //Main Phone
            "1" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text = context.getString(R.string.contacts_main_phone)
            }
            //Main Email
            "2" -> {
                contactIconView.setImageResource(R.drawable.ic_email)
                typeTxt.text =  context.getString(R.string.contacts_main_email)



            }
            //Billing Address
            "3" -> {
                contactIconView.setImageResource(R.drawable.ic_map)
                typeTxt.text =  context.getString(R.string.contacts_billing_address)
            }
            //Jobsite Address
            "4" -> {
                contactIconView.setImageResource(R.drawable.ic_map)
                typeTxt.text =  context.getString(R.string.contacts_jobsite_address)
            }
            //Website
            "5" -> {
                contactIconView.setImageResource(R.drawable.ic_web)
                typeTxt.text =  context.getString(R.string.contacts_website)
            }
            //Alt Contact
            "6" -> {
                contactIconView.setImageResource(R.drawable.ic_person)
                typeTxt.text =  context.getString(R.string.contacts_alt_contact)
            }
            //Fax
            "7" -> {
                contactIconView.setImageResource(R.drawable.ic_web)
                typeTxt.text =  context.getString(R.string.contacts_fax)
            }
            //Alt Phone
            "8" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text =  context.getString(R.string.contacts_alt_phone)
            }
            //Alt Email
            "9" -> {
                contactIconView.setImageResource(R.drawable.ic_email)
                typeTxt.text =  context.getString(R.string.contacts_alt_email)
            }
            //Mobile
            "10" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text =  context.getString(R.string.contacts_mobile)
            }
            //Alt Mobile
            "11" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text =  context.getString(R.string.contacts_alt_mobile)
            }
            //Home
            "12" -> {
                contactIconView.setImageResource(R.drawable.ic_phone)
                typeTxt.text =  context.getString(R.string.contacts_home_phone)
            }
            //Alt Email
            "13" -> {
                contactIconView.setImageResource(R.drawable.ic_email)
                typeTxt.text =  context.getString(R.string.contacts_alt_email)
            }
            //Invoice Address
            "14" -> {
                contactIconView.setImageResource(R.drawable.ic_map)
                typeTxt.text =  context.getString(R.string.contacts_invoice_address)
            }

        }




        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onContactCellClickListener(data)
        }




        //options btn click
        holder.itemView.findViewById<TextView>(R.id.textViewOptions).setOnClickListener {
            println("menu click")

            val popUp = PopupMenu(myView.context,holder.itemView.findViewById<TextView>(R.id.textViewOptions))
            popUp.inflate(R.menu.contact_options_menu)
            popUp.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.edit -> {
                        editListener.onContactEditListener(contact)
                    }
                    R.id.delete -> {
                        deleteListener.onContactDeleteListener(contact, position)
                    }
                }

                true
            }
            popUp.show()
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
    private var mCl: ConstraintLayout? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mCl = itemView.findViewById(R.id.contact_list_item)
    }

    fun bind(contact: Contact, context: Context) {
        mNameView?.text = contact.value
        if (contact.preferred == "1") {
            mCl!!.background = context.getColor(R.color.backgroundHighlight).toDrawable()
        }
        else {
            mCl!!.background = context.getColor(R.color.white).toDrawable()
        }
    }

}