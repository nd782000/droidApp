package com.example.AdminMatic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import kotlinx.android.synthetic.main.service_inspection_list_item.view.*


class ServiceInspectionAdapter(list: MutableList<InspectionQuestion>, private val cellClickListener: ServiceInspectionCellClickListener) : RecyclerView.Adapter<ServiceInspectionViewHolder>() {

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
        holder.bind(question)
        holder.itemView.service_inspection_item_name_txt.text = question.questionText
        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onServiceInspectionCellClickListener(data)
        }
    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }
}

class ServiceInspectionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.service_inspection_list_item, parent, false)) {
    private var mNameView:TextView? = null
    private var mRadioGroup:RadioGroup
    private var mRadioButtonGood:RadioButton
    private var mRadioButtonBad:RadioButton
    private var mRadioButtonNa:RadioButton
    init {
        mNameView = itemView.findViewById(R.id.service_inspection_item_name_txt)
        mRadioGroup = itemView.findViewById(R.id.service_inspection_radio_group)
        mRadioButtonGood = itemView.findViewById(R.id.service_inspection_good_radio)
        mRadioButtonBad = itemView.findViewById(R.id.service_inspection_bad_radio)
        mRadioButtonNa = itemView.findViewById(R.id.service_inspection_na_radio)


    }

    fun bind(question: InspectionQuestion) {
        mNameView?.text = question.questionText

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


}