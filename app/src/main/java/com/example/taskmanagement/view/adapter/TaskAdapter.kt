package com.example.taskmanagement.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import com.example.taskmanagement.databinding.TaskItemBinding

class TaskAdapter(
    var users: List<UserModel>,
    private val generalItemListener: GeneralItemListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    var isAdmin = false
    var tasks: List<TaskModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: TaskItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.task_item, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        // find matching UserModel by UID
        val user = users.find { it.uid == task.assignPersonId }
        Log.d("user", "user : $user")
        Log.d("task", "task : $task")
        holder.bind(task, user, position, position == itemCount - 1)
    }

    inner class ViewHolder(private val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: TaskModel,
            user: UserModel?,
            position: Int,
            isLastItem: Boolean
        ) {
            // these variable names must match those in your task_item.xml <data> block
            binding.task = task
            binding.user = user
            binding.isAdmin = isAdmin
            binding.currentPosition = position
            binding.isLastItem = isLastItem
            binding.handler = generalItemListener

            binding.executePendingBindings()
        }
    }
}