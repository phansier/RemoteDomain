package ru.beryukhov.remote_domain.recycler

import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.post_item.view.*
import kotlinx.android.synthetic.main.user_item.view.*
import ru.beryukhov.remote_domain.baserecyclerview.SimpleListAdapter
import ru.beryukhov.remote_domain.R
import ru.beryukhov.remote_domain.baserecyclerview.IBaseListItem
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.main.MainFragmentDirections


/**
 * Created by Andrey Beryukhov
 */
class DomainListAdapter : SimpleListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val context = parent.context

        val holder = when (viewType) {
            R.layout.user_item -> UserViewHolder(inflateByViewType(context, viewType, parent))
            R.layout.post_item -> PostViewHolder(inflateByViewType(context, viewType, parent))
            else -> throw IllegalStateException("There is no match with current layoutId")
        }
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(position, holder.itemView)
            }
        }

        return holder
    }

    fun onItemClick(adapterPosition: Int, view: View) {

        val item = items[adapterPosition]
        when (item) {
            is PostItem -> findNavController(view).navigate(
                MainFragmentDirections.actionMainToPost(
                    post = item.post
                )
            )
            is UserItem -> findNavController(view).navigate(
                MainFragmentDirections.actionMainToUser(
                    user = item.user
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is PostViewHolder -> {
                val postItem = items[position] as PostItem
                holder.postTitle.text = "${postItem.user?.id} : ${postItem.user?.userName}"
                holder.postMessage.text = "${postItem.post.id} : ${postItem.post.message}"
            }
            is UserViewHolder -> {
                val userItem = items[position] as UserItem
                holder.userName.text = "${userItem.user.id} : ${userItem.user.userName}"

            }

            else -> throw IllegalStateException("There is no match with current holder instance")
        }
    }
}

data class PostItem(val post: Post, val user: User?) : IBaseListItem {
    override fun getLayoutId() = R.layout.post_item
}

data class UserItem(val user: User) : IBaseListItem {
    override fun getLayoutId() = R.layout.user_item
}

class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val postMessage = view.postMessage
    val postTitle = view.postTitle
}

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val userName = view.userName
}