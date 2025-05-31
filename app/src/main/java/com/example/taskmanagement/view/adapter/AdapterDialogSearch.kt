package com.example.taskmanagement.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.databinding.SelectSearchRowItemBinding

class AdapterDialogSearch(
    private val pojoSearchlist: MutableList<PojoDialogSearch>,
  
    private val mListener: GeneralItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastCheckedRB: RadioButton? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: SelectSearchRowItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.select_search_row_item,
            parent, false
        )
        return ViewHolderItem(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (pojoSearchlist[position].is_checked) {
            pojoSearchlist[position].is_checked = true
            (holder as ViewHolderItem).binding.audioRadiobutton.isChecked = true
            lastCheckedRB = holder.binding.audioRadiobutton
        } else {
            pojoSearchlist[position].is_checked =false
            (holder as ViewHolderItem).binding.audioRadiobutton.isChecked = false
        }
        holder.bind(pojoSearchlist[position], position)
    }

    override fun getItemCount(): Int {
        return pojoSearchlist.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolderItem internal constructor(internal val binding: SelectSearchRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: PojoDialogSearch?, position: Int) {
            binding.setPojo(data)
            binding.setCurrentPosition(position)
            binding.setListener(mListener)
            binding.executePendingBindings()


            binding.audioRadiobutton.setOnCheckedChangeListener(object :
                CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(compoundButton: CompoundButton?, ischecked: Boolean) {
                    if (lastCheckedRB != null) {
                        lastCheckedRB!!.isChecked = false
                        if (position < pojoSearchlist.size) {
                            pojoSearchlist[position].is_checked = false
                        }
                    } else {
                        if (position < pojoSearchlist.size) {
                            pojoSearchlist[position].is_checked = true
                        }
                    }
                    //store the clicked radiobutton
                    lastCheckedRB = binding.audioRadiobutton
                }
            })
        }
    }
}
