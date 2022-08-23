package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class ServiceInspectionAdapter(list: MutableList<InspectionQuestion>, private val historyMode:Boolean) : RecyclerView.Adapter<ServiceInspectionViewHolder>() {

    var filterList:MutableList<InspectionQuestion> = emptyList<InspectionQuestion>().toMutableList()

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceInspectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ServiceInspectionViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ServiceInspectionViewHolder, position: Int) {
        val question: InspectionQuestion = filterList[position]
        val mNameView: TextView = holder.itemView.findViewById(R.id.service_inspection_item_name_txt)
        val mRadioGroup: RadioGroup = holder.itemView.findViewById(R.id.service_inspection_radio_group)
        val mRadioButtonGood: RadioButton = holder.itemView.findViewById(R.id.service_inspection_good_radio)
        val mRadioButtonBad: RadioButton = holder.itemView.findViewById(R.id.service_inspection_bad_radio)
        val mRadioButtonNa: RadioButton = holder.itemView.findViewById(R.id.service_inspection_na_radio)

        mNameView.text = question.questionText
        mRadioGroup.tag = position

        when (question.answer) {
            "0" -> {
                mRadioGroup.clearCheck()
            }
            "1" -> {
                mRadioGroup.check(mRadioButtonGood.id)
            }
            "2" -> {
                mRadioGroup.check(mRadioButtonBad.id)
            }
            "3" -> {
                mRadioGroup.check(mRadioButtonNa.id)
            }
        }

        mRadioGroup.setOnCheckedChangeListener(null)
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                mRadioButtonGood.id -> {
                    question.answer = "1"
                }
                mRadioButtonBad.id -> {
                    question.answer = "2"
                }
                mRadioButtonNa.id -> {
                    question.answer = "3"
                }
            }
        }
    }

    override fun getItemCount(): Int{
        //print("getItemCount = ${filterList.size}")
        return filterList.size
    }

}

class ServiceInspectionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.service_inspection_list_item, parent, false)) {
    init {

    }

    fun bind(question: InspectionQuestion) {

    }

}