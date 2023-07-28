package com.github.rahul_gill.attendance.ui.create

import android.os.Bundle
import android.view.View
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentCreateCourseBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.BaseFragment
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.enableSystemBarsInsetsCallback
import com.github.rahul_gill.attendance.util.enableViewAboveKeyboardWithAnimationCallback
import com.github.rahul_gill.attendance.util.showSnackBarWithDismissButton
import com.github.rahul_gill.attendance.util.softKeyboardVisible
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.chip.Chip


class CreateCourseFragment : BaseFragment(R.layout.fragment_create_course) {
    private val binding by viewBinding(FragmentCreateCourseBinding::bind)
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }
    private var courseName = savedStateOf("course_name", "")
    private var requiredAttendancePercentage = savedStateOf("required_attendance", 75)
    private var classesForTheCourse = savedStateOf("class_details", listOf<ClassDetail>())

    private var doneButtonBottom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableSharedZAxisTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.courseName.doAfterTextChanged {
            courseName.value = it.toString()
        }
        binding.requiredPercentageSlider.addOnChangeListener { _, value, _ ->
            requiredAttendancePercentage.value = value.toInt()
        }

        binding.doneButton.enableSystemBarsInsetsCallback(
            originalRightMarginDp = 16,
            originalBottomMarginDp = 16
        )
        enableViewAboveKeyboardWithAnimationCallback(binding.doneButton) { doneButtonBottom = it }
        binding.doneButton.setOnClickListener {
            if (courseName.value.isBlank()) {
                showSnackBarWithDismissButton(
                    binding.root,
                    getString(R.string.error_course_name_blank)
                )
                return@setOnClickListener
            }
            if (classesForTheCourse.value.isEmpty()) {
                showSnackBarWithDismissButton(
                    binding.root,
                    getString(R.string.error_no_classes_for_course)
                )
                return@setOnClickListener
            }
            saveCreatedCourseToDB()
            findNavController().navigateUp()
        }
        requiredAttendancePercentage.observe(viewLifecycleOwner) {
            binding.requiredAttendanceText.text =
                getString(R.string.required_percentage_detail, it)
        }
        classesForTheCourse.observe(viewLifecycleOwner) { list ->
            binding.scheduleItemsGroup.removeAllViews()
            list.forEachIndexed { index, it ->
                addChipToGroup(
                    title = getString(
                        R.string.schedule_class_weekday_and_start_end_time,
                        it.dayOfWeek.name,
                        it.startTime.format(timeFormatter),
                        it.endTime.format(timeFormatter)
                    ),
                    onCloseIconClick = {
                        classesForTheCourse.value = list.toMutableList().apply { removeAt(index) }
                    },
                    onClick = {
                        findNavController().navigate(
                            CreateCourseFragmentDirections.toAddCourseClassBottomSheet(
                                weekDay = it.dayOfWeek,
                                startTime = it.startTime,
                                endTime = it.endTime,
                                itemToUpdateIndex = index
                            )
                        )
                    }
                )
            }
        }
        binding.addClassButton.setOnClickListener {
            //Hacks
            if (softKeyboardVisible(it)) {
                binding.doneButton.bottom = doneButtonBottom
            }
            findNavController().navigate(CreateCourseFragmentDirections.toAddCourseClassBottomSheet())
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<AddCourseBottomSheetResult>(AddCourseClassBottomSheet.COURSE_KEY)
            ?.observe(viewLifecycleOwner) { (result, itemToUpdateIndex) ->
                if (itemToUpdateIndex != -1) {
                    classesForTheCourse.value = classesForTheCourse.value
                        .toMutableList()
                        .apply { set(itemToUpdateIndex, result) }
                } else {
                    classesForTheCourse.value = classesForTheCourse.value
                        .toMutableList()
                        .apply { add(result) }
                }
            }

    }


    private fun saveCreatedCourseToDB() {
        dbOps.createCourse(
            name = courseName.value,
            requiredAttendancePercentage = requiredAttendancePercentage.value.toDouble(),
            schedule = classesForTheCourse.value
        )
    }

    private fun addChipToGroup(
        title: String,
        onCloseIconClick: () -> Unit = {},
        onClick: () -> Unit
    ) {
        val chip = Chip(context)
        chip.text = title
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = true
        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        chip.setOnClickListener {
            onClick()
        }
        binding.scheduleItemsGroup.addView(chip, binding.scheduleItemsGroup.size - 2)
        chip.setOnCloseIconClickListener {
            binding.scheduleItemsGroup.removeView(chip)
            onCloseIconClick()
        }
    }
}
