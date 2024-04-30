package com.github.rahul_gill.attendance.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentCourseInfoBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.textChangeWithBetterIndication
import com.github.rahul_gill.attendance.util.viewBinding
import kotlinx.coroutines.launch

class CourseInfoFragment : Fragment(R.layout.fragment_course_info) {
    private val binding by viewBinding(FragmentCourseInfoBinding::bind)
    private val dbOps by lazy { DBOps.instance }
    private val courseId by lazy {
        this.requireArguments().getLong("courseId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scheduleAdapter = ScheduleClassesAdapter()
        binding.weeklyScheduleList.adapter = scheduleAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getScheduleClassesForCourse(courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    scheduleAdapter.submitList(items)
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {

            dbOps.getCourseAttendancePercentage(courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { (percent, presents, absents, cancels, requiredPercentage) ->
                    binding.currentAttendance.text = getString(R.string.double_repr, percent)
                    binding.requiredAttendance.text = getString(R.string.double_repr, requiredPercentage)
                    binding.presentsText.text = getString(R.string.presents_count, presents)
                    binding.absentsText.text = getString(R.string.absents_count, absents)
                    binding.cancelsText.text = getString(R.string.cancelled_classes_count, cancels)
                    binding.currentAttendance.setTextColor(
                        requireContext().getThemeColor(
                            if (requiredPercentage <= percent)
                                R.attr.colorSuccess
                            else
                                com.google.android.material.R.attr.colorError
                        )
                    )
                    binding.currentAttendance.textChangeWithBetterIndication()
                }
        }

    }


    companion object{
        fun create(courseId: Long): CourseInfoFragment {
            val argBundle = Bundle()
            argBundle.putLong("courseId", courseId)
            val fragment = CourseInfoFragment()
            fragment.arguments = argBundle
            return fragment
        }
    }
}