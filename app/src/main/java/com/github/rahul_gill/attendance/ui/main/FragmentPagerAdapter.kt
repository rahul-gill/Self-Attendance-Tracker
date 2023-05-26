package com.github.rahul_gill.attendance.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentPagerAdapter (
    private  val fragmentList: List<Fragment>,
    fm: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int) = fragmentList[position]
}