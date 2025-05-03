package com.example.taskmanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagement.R
import com.example.taskmanagement.databinding.TaskItemBinding
import com.example.taskmanagement.interactors.GeneralItemListener
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.model.UserModel

/*
class TaskAdapter(var users: List<User>, generalItemListener: GeneralItemListener) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    var status = Status.entries
    var tasks = listOf<Task>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var generalItemListener: GeneralItemListener = generalItemListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.task_item, parent, false)
//        return ViewHolder(view)

        val binding: TaskItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.task_item, parent, false
        )
//        binding.setHandler(generalItemListener)
//        binding.setIsDetailView(isDetailView)
        return ViewHolder(binding)


    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        val user = users.find { it.id == task.assignPersonId }
        holder.bind(task, user, position, position == (itemCount - 1))
    }

    */
/* inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        *//*
*/
/* fun bind(task: Task, user: User?) {
          *//*
*/
/**//*
*/
/*  itemView.apply {
                findViewById<TextView>(R.id.tvDetail).text = task.taskDetail
                findViewById<TextView>(R.id.tvAssignPerson).text = user?.name ?: "-"
                findViewById<TextView>(R.id.tvAssignDate).text =
                    ((task.assignDate.toString() == "") ?: "-").toString()
                findViewById<TextView>(R.id.tvCompletionDate).text =
                    ((task.completionDate.toString() == "") ?: "-").toString()

                findViewById<TextView>(R.id.tvRemark).text = task.remark ?: "-"
                // Bind other views similarly
            }*//*
*/
/**//*
*/
/*
        }*//*
*/
/*

    }*//*


    inner class ViewHolder(mBinding: TaskItemBinding) :
        RecyclerView.ViewHolder(mBinding.getRoot()) {
        private val mBinding: TaskItemBinding = mBinding

        fun bind(pojoTask: Task?, pojoUser: User?, position: Int, isLastItem: Boolean) {
            mBinding.task = pojoTask
            mBinding.user = pojoUser
            mBinding.setCurrentPosition(position)
            mBinding.setIsLastItem(isLastItem)
            mBinding.executePendingBindings()
            mBinding.setHandler(generalItemListener)
        }
    }
}*/






class TaskAdapter(
    var users: List<UserModel>,
    private val generalItemListener: GeneralItemListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

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
            binding.currentPosition = position
            binding.isLastItem = isLastItem
            binding.handler = generalItemListener

            binding.executePendingBindings()
        }
    }
}
