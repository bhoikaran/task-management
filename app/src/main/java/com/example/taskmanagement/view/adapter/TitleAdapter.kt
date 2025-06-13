package com.example.taskmanagement.view.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.model.TitleModel
import com.example.taskmanagement.databinding.ItemTitleBinding

class TitleAdapter(
    private val generalItemListener: GeneralItemListener
) : RecyclerView.Adapter<TitleAdapter.ViewHolder>() {
    var titles: List<TitleModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemTitleBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_title, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = titles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = titles[position]
        // find matching UserModel by UID

        holder.bind(task, position, position == itemCount - 1)
    }

    inner class ViewHolder(private val binding: ItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            titleModel: TitleModel,
            position: Int,
            isLastItem: Boolean
        ) {
            // these variable names must match those in your task_item.xml <data> block
            binding.title = titleModel
            binding.currentPosition = position
            binding.isLastItem = isLastItem
            binding.handler = generalItemListener

            binding.executePendingBindings()
        }
    }
}