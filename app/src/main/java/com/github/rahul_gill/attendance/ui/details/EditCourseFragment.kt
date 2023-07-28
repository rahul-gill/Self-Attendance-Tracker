package com.github.rahul_gill.attendance.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentEditCourseBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.ui.create.AddCourseBottomSheetResult
import com.github.rahul_gill.attendance.ui.create.AddCourseClassBottomSheet
import com.github.rahul_gill.attendance.ui.create.ClassDetail
import com.github.rahul_gill.attendance.util.BaseFragment
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.enableSystemBarsInsetsCallback
import com.github.rahul_gill.attendance.util.enableViewAboveKeyboardWithAnimationCallback
import com.github.rahul_gill.attendance.util.showSnackBarWithDismissButton
import com.github.rahul_gill.attendance.util.softKeyboardVisible
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditCourseFragment : BaseFragment(R.layout.fragment_edit_course) {
    private val binding by viewBinding(FragmentEditCourseBinding::bind)
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }
    private val args by navArgs<EditCourseFragmentArgs>()
    private var courseName = savedStateOf("course_name", "")
    private var requiredAttendancePercentage = savedStateOf("required_attendance", 75)
    private var classesForTheCourse = savedStateOf("class_details", listOf<ClassDetail>())
    private var doneButtonBottom = 0

    private var defaultListSchedule = listOf<ClassDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableSharedZAxisTransition()

        args.courseItem.let {
            courseName.value = it.courseName
            requiredAttendancePercentage.value = it.requiredAttendance.toInt()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            appbarLayout.isVisible = false
            mainContent.isVisible = false
            progressCircular.isVisible = true
            doneButton.isVisible = false
        }
        viewLifecycleOwner.lifecycleScope.launch {
            defaultListSchedule =
                dbOps.getScheduleClassesForCourse(args.courseItem.courseId).first()
            classesForTheCourse.value = defaultListSchedule
            binding.apply {
                appbarLayout.isVisible = true
                mainContent.isVisible = true
                progressCircular.isVisible = false
                doneButton.isVisible = true
            }
        }
        //setup initial values
        binding.toolbar.title = getString(R.string.edit_course_screen_title, courseName.value)
        binding.courseName.setText(courseName.value)
        binding.courseNameTextInputLayout.helperText = getString(
            R.string.previous_course_name,
            args.courseItem.courseName
        )
        binding.requiredPercentageSlider.value = requiredAttendancePercentage.value.toFloat()



        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.courseName.doAfterTextChanged {
            courseName.value = it.toString()
        }
        binding.requiredPercentageSlider.addOnChangeListener { _, value, _ ->
            requiredAttendancePercentage.value = value.toInt()
        }
        binding.requiredAttendanceOriginalText.text = getString(
            R.string.previous_value_int_percentage,
            args.courseItem.requiredAttendance.toInt()
        )

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
        requiredAttendancePercentage.observe(viewLifecycleOwner) { newValue ->
            binding.requiredAttendanceText.text = getString(
                R.string.required_attendance_updated_int_percentage,
                newValue
            )
        }

        classesForTheCourse.observe(viewLifecycleOwner) { list ->
            binding.scheduleItemsGroup.removeAllViews()
            list.forEachIndexed { index, it ->
                val title = getString(
                    R.string.schedule_class_weekday_and_start_end_time,
                    it.dayOfWeek.name,
                    it.startTime.format(timeFormatter),
                    it.endTime.format(timeFormatter)
                )
                addChipToGroup(
                    title = title,
                    onCloseIconClick = {
                        classesForTheCourse.value = list.toMutableList().apply { removeAt(index) }
                    },
                )
            }
        }
        binding.resetScheduleButton.setOnClickListener {
            classesForTheCourse.value = defaultListSchedule
        }
        binding.addClassButton.setOnClickListener {
            //Hacks
            if (softKeyboardVisible(it)) {
                binding.doneButton.bottom = doneButtonBottom
            }
            findNavController().navigate(EditCourseFragmentDirections.toAddCourseClassBottomSheet())
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
    ) {
        val chip = Chip(context)
        chip.text = title
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = true
        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        binding.scheduleItemsGroup.addView(chip, binding.scheduleItemsGroup.size - 2)
        chip.setOnCloseIconClickListener {
            binding.scheduleItemsGroup.removeView(chip)
            onCloseIconClick()
        }
    }

    //TODO: notifications when changing schedule item
    //TODO: schedule item can't be changed, only removed or added
    //TODO: reset button for schedule classes and extra classes
    //TODO: show original values like: (Change course name: original name: This)
}
