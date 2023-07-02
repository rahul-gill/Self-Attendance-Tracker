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
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }
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
                    binding.currentAttendance.text = "%.2f%%".format(percent)
                    binding.requiredAttendance.text = "%.2f%%".format(requiredPercentage)
                    binding.presentsText.text = "Presents: $presents"
                    binding.absentsText.text = "Absents: $absents"
                    binding.cancelsText.text = "Cancelled classes: $cancels"
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