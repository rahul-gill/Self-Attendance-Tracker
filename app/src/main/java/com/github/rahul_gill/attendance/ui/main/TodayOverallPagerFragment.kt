package com.github.rahul_gill.attendance.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentTodayOverallPagerBinding
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.util.enableSystemBarsInsetsCallback
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.tabs.TabLayoutMediator

class TodayOverallPagerFragment : Fragment(R.layout.fragment_today_overall_pager) {
    private val binding by viewBinding(FragmentTodayOverallPagerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableSharedZAxisTransition()
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.apply {
            toolbar.setOnMenuItemClickListener {
                findNavController().navigate(TodayOverallPagerFragmentDirections.toSettingsFragment())
                true
            }

            pager.adapter = FragmentPagerAdapter(
                fragmentList = listOf(
                    TodayItemsFragment(),
                    OverallItemsFragment()
                ),
                fm = childFragmentManager,
                lifecycle = lifecycle
            )
            TabLayoutMediator(tabLayout, pager) { tab, position ->
                tab.text = if (position == 0) "Today" else "Overall"
            }.attach()

            tabLayout.selectTab(tabLayout.getTabAt(PreferenceManager.defaultHomeTabPref.value.toInt()))

            createCourseButton.enableSystemBarsInsetsCallback(originalRightMarginDp = 16, originalBottomMarginDp = 16)
            createCourseButton.setOnClickListener {
                findNavController().navigate(TodayOverallPagerFragmentDirections.toCreateCourseScreen())
            }
        }
    }
}