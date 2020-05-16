package ru.beryukhov.remote_domain.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.remote_domain.R

const val ENTITY_ARGUMENT = "entity"
const val ENTITY_USER = "Users"
const val ENTITY_POST = "Posts"

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        return view
    }
    private val entities = arrayOf(
        ENTITY_POST, ENTITY_USER
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2.adapter = ViewPagerFragmentStateAdapter(this, entities)

        TabLayoutMediator(tabs, viewPager2,
            TabConfigurationStrategy { tab, position ->
                tab.text = entities[position]
            }).attach()
    }

}
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ViewPagerFragmentStateAdapter(fragment: Fragment, private val entities: Array<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = entities.size
    override fun createFragment(position: Int): Fragment = ListFragment().apply {
        arguments = bundleOf(
            ENTITY_ARGUMENT to entities[position]
        )
    }
}

