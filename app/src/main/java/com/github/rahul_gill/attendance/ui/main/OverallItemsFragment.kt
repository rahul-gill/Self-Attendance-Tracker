package com.github.rahul_gill.attendance.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentOverallItemsBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.viewBinding
import kotlinx.coroutines.launch

class OverallItemsFragment : Fragment(R.layout.fragment_overall_items) {
    private val binding by viewBinding(FragmentOverallItemsBinding::bind)
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = OverallItemsRecyclerViewAdapter(
            onClick = { v, item ->
                val cardTransitionName = getString(R.string.course_detail_page_transition)
                val extras = FragmentNavigatorExtras(v to cardTransitionName)
                findNavController().navigate(
                    TodayOverallPagerFragmentDirections.toViewCourseFragment(item), extras
                )
            }
        )
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getCoursesDetailsList()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    if (items.isEmpty()) {
                        binding.recyclerView.isVisible = false
                        binding.noItemMessage.isVisible = true
                    } else {
                        binding.recyclerView.isVisible = true
                        binding.noItemMessage.isVisible = false
                    }
                    adapter.submitList(items)
                }
        }
    }
}