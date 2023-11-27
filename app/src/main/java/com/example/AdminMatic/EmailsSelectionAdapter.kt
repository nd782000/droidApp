package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso


class EmailsSelectionAdapter(_emailList: MutableList<String>, _checkList: MutableList<Boolean>, private val ccl: EmailCheckClickListener) : RecyclerView.Adapter<EmailSelectionViewHolder>() {

    lateinit var globalVars:GlobalVars

    var emailList = _emailList
    var checkList = _checkList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailSelectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        globalVars = GlobalVars()
        return EmailSelectionViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EmailSelectionViewHolder, position: Int) {

        val context = myView.context

        val mainCL:ConstraintLayout = holder.itemView.findViewById(R.id.main_cl)
        val addCL:ConstraintLayout = holder.itemView.findViewById(R.id.add_cl)

        if (position == emailList.size) {
            mainCL.visibility = View.INVISIBLE
            addCL.visibility = View.VISIBLE

            val btn:Button = holder.itemView.findViewById(R.id.new_email_btn)
            val et:EditText = holder.itemView.findViewById(R.id.new_email_et)
            et.setText("")

            btn.setOnClickListener {
                ccl.onAddEmailListener(et.text.toString())
            }
        }
        else {

            mainCL.visibility = View.VISIBLE
            addCL.visibility = View.INVISIBLE

            val email = emailList[position]
            holder.bind(email)

            val checkImageView: ImageView = holder.itemView.findViewById(R.id.check_iv)

            if (checkList[position]) {
                Picasso.with(context).load(R.drawable.ic_check_enabled).into(checkImageView)
            } else {
                Picasso.with(context).load(R.drawable.ic_check_disabled).into(checkImageView)
            }

            checkImageView.setOnClickListener {
                ccl.onEmailCheckClickListener(position, checkImageView)
            }
        }


    }

    override fun getItemCount(): Int {
        return emailList.size + 1
    }
}

class EmailSelectionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.send_email_list_item, parent, false)) {
    private var mNameView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.email_tv)
    }

    fun bind(email: String) {
        mNameView?.text = email
    }

}