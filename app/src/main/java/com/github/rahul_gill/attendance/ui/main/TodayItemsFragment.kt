package com.github.rahul_gill.attendance.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentTodayItemsBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.viewBinding
import kotlinx.coroutines.launch

class TodayItemsFragment : Fragment(R.layout.fragment_today_items) {
    private val binding by viewBinding(FragmentTodayItemsBinding::bind)
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TodayItemsRecyclerViewAdapter(
            onClick = { todayItem ->
                findNavController().navigate(
                    TodayOverallPagerFragmentDirections.toClassStatusSetterBottomSheet(todayItem)
                )
            }
        )
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getScheduleAndExtraClassesForToday()
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