package com.github.rahul_gill.attendance.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentAttendanceRecordBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.ui.details.sticky.AttendanceRecordRecyclerViewAdapter
import com.github.rahul_gill.attendance.ui.details.sticky.StickyHeaderItemDecoration
import com.github.rahul_gill.attendance.util.viewBinding
import kotlinx.coroutines.launch

interface OnAttendanceRecordClickPropagator{
    fun doThis(call: (TodayCourseItem) -> Unit)
}

class AttendanceRecordFragment: Fragment(R.layout.fragment_attendance_record), OnAttendanceRecordClickPropagator {
    private val binding by viewBinding(FragmentAttendanceRecordBinding::bind)

    private val dbOps by lazy { DBOps.getInstance(requireContext()) }

    private val courseId by lazy {
        requireArguments().getLong("course_id", 0)
    }
    private val courseName by lazy {
        requireArguments().getString("course_name")!!
    }
    private lateinit var callback : (TodayCourseItem) -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AttendanceRecordRecyclerViewAdapter{ item ->
            callback( TodayCourseItem(
                item.entityId,
                courseName,
                item.startTime,
                item.endTime,
                item.classStatus,
                item.isExtraCLass == 1L,
                item.date
            ))
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(StickyHeaderItemDecoration(
            binding.recyclerView,
            shouldFadeOutHeader = true,
            isHeader = { index -> adapter.getItemViewType(index) == 1 }
        ))
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getMarkedAttendancesForCourse(courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    val emptyList = items.isEmpty()
                    binding.apply {
                        noAttendanceRecordMessage.isVisible = emptyList
                        recyclerView.isVisible = !emptyList
                    }
                    adapter.submitList(items)
                }
        }
    }

    companion object{
        fun create(courseId: Long, courseName: String): AttendanceRecordFragment {
            val bundle = Bundle()
            bundle.putLong("course_id", courseId)
            bundle.putString("course_name", courseName)
            val fragment = AttendanceRecordFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun doThis(call: (TodayCourseItem) -> Unit) {
        callback = call
    }
}