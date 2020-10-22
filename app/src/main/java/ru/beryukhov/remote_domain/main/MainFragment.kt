package ru.beryukhov.remote_domain.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import ru.beryukhov.remote_domain.R

const val ENTITY_ARGUMENT = "entity"
const val ENTITY_USER = "Users"
const val ENTITY_POST = "Posts"

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
        val viewPager2: ViewPager2 = view.findViewById(R.id.viewPager2)
        val tabs: TabLayout = view.findViewById(R.id.tabs)
        viewPager2.adapter = ViewPagerFragmentStateAdapter(this, entities)

        TabLayoutMediator(tabs, viewPager2,
            TabConfigurationStrategy { tab, position ->
                tab.text = entities[position]
            }).attach()
    }

}

class ViewPagerFragmentStateAdapter(fragment: Fragment, private val entities: Array<String>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = entities.size
    override fun createFragment(position: Int): Fragment = ListFragment().apply {
        arguments = bundleOf(
            ENTITY_ARGUMENT to entities[position]
        )
    }
}

