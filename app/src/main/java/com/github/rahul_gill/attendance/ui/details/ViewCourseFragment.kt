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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentViewCourseBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.ExtraClassDetails
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.ui.common.calendar.MonthsPagerAdapter
import com.github.rahul_gill.attendance.util.animateDropdown
import com.github.rahul_gill.attendance.util.animateRotation
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.textChangeWithBetterIndication
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


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

        val viewPagerAdapter = MonthsPagerAdapter(
            onDayOfMonthClick = {},
            onGoToIndexMonth = { index ->
                binding.calendarViewPager.setCurrentItem(index, true)
            }
        )
        binding.calendarViewPager.adapter = viewPagerAdapter
        binding.calendarViewPager.setCurrentItem(
            viewPagerAdapter.getMonthPosition(LocalDate.now()),
            false
        )

        binding.weeklyScheduleDropdown.setOnClickListener {
            val isVisibleAlready = binding.weeklyScheduleList.isVisible
            binding.weeklyScheduleDropdown.animateRotation(
                toDegree = if (isVisibleAlready) 0f else 90f
            )
            binding.weeklyScheduleList.animateDropdown(requireActivity(), !isVisibleAlready)
        }
        binding.extraClassDropdown.setOnClickListener {
            val isVisibleAlready = binding.extraClassesList.isVisible
            binding.extraClassDropdown.animateRotation(
                toDegree = if (isVisibleAlready) 0f else 90f
            )
            binding.extraClassesList.animateDropdown(requireActivity(), !isVisibleAlready)
        }

        binding.toolbar.title = args.courseItem.courseName
        binding.requiredAttendance.text = "%.2f%%".format(args.courseItem.requiredAttendance)
        binding.currentAttendance.text = "%.2f%%".format(args.courseItem.currentAttendancePercentage)
        binding.presentsText.text = "Presents: ${args.courseItem.presents}"
        binding.absentsText.text = "Absents: ${args.courseItem.absents}"
        binding.cancelsText.text = "Cancelled classes: ${args.courseItem.cancels}"
        binding.currentAttendance.setTextColor(
            requireContext().getThemeColor(
                if (args.courseItem.run { requiredAttendance <= currentAttendancePercentage })
                    R.attr.colorSuccess
                else
                    com.google.android.material.R.attr.colorError
            )
        )

        val scheduleAdapter = ScheduleClassesAdapter()
        binding.weeklyScheduleList.adapter = scheduleAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getScheduleClassesForCourse(args.courseItem.courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    scheduleAdapter.submitList(items)
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getCourseAttendancePercentage(args.courseItem.courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { (percent,presents, absents, cancels) ->
                    binding.currentAttendance.text = "%.2f%%".format(percent)
                    binding.presentsText.text = "Presents: $presents"
                    binding.absentsText.text = "Absents: $absents"
                    binding.cancelsText.text = "Cancelled classes: $cancels"
                    binding.currentAttendance.setTextColor(
                        requireContext().getThemeColor(
                            if (args.courseItem.requiredAttendance <= percent )
                                R.attr.colorSuccess
                            else
                                com.google.android.material.R.attr.colorError
                        )
                    )
                    binding.currentAttendance.textChangeWithBetterIndication()
                }
        }


        val extraClassesAdapter = ExtraClassesAdapter(onClick = { extraClassDetails ->
            findNavController().navigate(ViewCourseFragmentDirections.toClassStatusSetterBottomSheetFromViewCourse(
                TodayCourseItem(
                    scheduleIdOrExtraClassId = extraClassDetails.extraClassId,
                    startTime = extraClassDetails.startTime,
                    endTime = extraClassDetails.endTime,
                    classStatus = extraClassDetails.classStatus,
                    courseName = args.courseItem.courseName,
                    isExtraClass = true,
                    date = extraClassDetails.date
                )
            ))
        })

        //swipe to delete extra classes
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder,target: RecyclerView.ViewHolder)
                = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val toDelete: ExtraClassDetails = (viewHolder as ExtraClassesAdapter.VH).getItem()
                val position = viewHolder.adapterPosition
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_App_MaterialAlertDialogError
                )
                    .setTitle("Delete extra class?")
                    .setMessage("Do you really want to delete the extra class on ${toDelete.date.format(
                        dateFormatter)} from ${toDelete.startTime.format(timeFormatter)} to ${toDelete.endTime.format(timeFormatter)}")
                    .setIcon(R.drawable.baseline_warning_24)
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                        extraClassesAdapter.notifyItemChanged(position)
                    }.setPositiveButton(R.string.ok) { dialog, _ ->
                        dbOps.deleteExtraClass(toDelete.extraClassId)
                        dialog.dismiss()
                        extraClassesAdapter.notifyItemChanged(position)
                    }
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.extraClassesList)

        binding.extraClassesList.adapter = extraClassesAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getExtraClassesListForCourse(args.courseItem.courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    val emptyList = items.isEmpty()
                    binding.extraClassesContainer.isVisible = !emptyList
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
                            dbOps.deleteCourse(args.courseItem.courseId)
                            dialog.dismiss()
                            findNavController().navigateUp()
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
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ExtraClassTimings>("extra_class_key")
            ?.observe(viewLifecycleOwner) { extraClassesDetails ->
                dbOps.createExtraClasses(args.courseItem.courseId, extraClassesDetails)
            }
    }

}