package com.github.rahul_gill.attendance.ui.details

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentViewCourseBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.launch

class ViewCourseFragment : Fragment(R.layout.fragment_view_course) {
    private val binding by viewBinding(FragmentViewCourseBinding::bind)
    private val args by navArgs<ViewCourseFragmentArgs>()
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 500L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getThemeColor(com.google.android.material.R.attr.colorSurface))
        }
        enableSharedZAxisTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = args.courseItem.courseName
        binding.requiredAttendance.text = "%.2f%%".format(args.courseItem.requiredAttendance)
        binding.currentAttendance.text =
            "%.2f%%".format(args.courseItem.currentAttendancePercentage)
        binding.currentAttendance.setTextColor(requireContext().getThemeColor(
            if(args.courseItem.run { requiredAttendance <= currentAttendancePercentage })
                R.attr.colorSuccess
            else
                com.google.android.material.R.attr.colorError
        ))

        val scheduleAdapter = ScheduleClassesAdapter()
        binding.weeklyScheduleList.adapter = scheduleAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getScheduleClassesForCourse(args.courseItem.courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    scheduleAdapter.submitList(items)
                }
        }


        val extraClassesAdapter = ExtraClassesAdapter()
        binding.extraClassesList.adapter = extraClassesAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getExtraClassesListForCourse(args.courseItem.courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    val emptyList = items.isEmpty()
                    binding.extraClassesList.isVisible = !emptyList
                    binding.extraClassesHeader.isVisible = !emptyList
                    binding.addExtraClassZero.isVisible = emptyList
                    extraClassesAdapter.submitList(items)
                }
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_course_details -> {
                    findNavController().navigate(
                        ViewCourseFragmentDirections.toEditCourseFragment(
                            args.courseItem,
                            scheduleAdapter.currentList.toTypedArray(),
                            extraClassesAdapter.currentList.toTypedArray()
                        )
                    )
                    true
                }

                R.id.delete_course_details -> {
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.ThemeOverlay_App_MaterialAlertDialogError
                    )
                        .setTitle("Delete course: ${args.courseItem.courseName}?")
                        .setMessage("Do you really want to delete this course item? You'll lose all the data associated with this course.")
                        .setIcon(R.drawable.baseline_warning_24)
                        .setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }.setPositiveButton(R.string.ok) { dialog, _ ->
                            //TODO: the delete logic
                            dialog.dismiss()
                        }
                        .show()
                    true
                }

                else -> false
            }
        }

        binding.addExtraClass.setOnClickListener {
            findNavController().navigate(ViewCourseFragmentDirections.toAddExtraClassBottomSheet())
        }
        binding.addExtraClassZero.setOnClickListener {
            findNavController().navigate(ViewCourseFragmentDirections.toAddExtraClassBottomSheet())
        }


        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ExtraClassTimings>("extra_class_key")
            ?.observe(viewLifecycleOwner) { extraClassesDetails ->
                dbOps.createExtraClasses(args.courseItem.courseId, extraClassesDetails)
            }
    }
}