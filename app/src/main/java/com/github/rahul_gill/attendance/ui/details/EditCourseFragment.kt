package com.github.rahul_gill.attendance.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentEditCourseBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.ExtraClassDetails
import com.github.rahul_gill.attendance.ui.create.AddCourseBottomSheetResult
import com.github.rahul_gill.attendance.ui.create.ClassDetail
import com.github.rahul_gill.attendance.ui.create.CreateCourseFragmentDirections
import com.github.rahul_gill.attendance.util.BaseFragment
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.enableSystemBarsInsetsCallback
import com.github.rahul_gill.attendance.util.enableViewAboveKeyboardWithAnimationCallback
import com.github.rahul_gill.attendance.util.showSnackBarWithDismissButton
import com.github.rahul_gill.attendance.util.softKeyboardVisible
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.chip.Chip

class EditCourseFragment : BaseFragment(R.layout.fragment_edit_course) {
    private val binding by viewBinding(FragmentEditCourseBinding::bind)
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }
    private val args by navArgs<EditCourseFragmentArgs>()
    private var courseName = savedStateOf("course_name", "")
    private var requiredAttendancePercentage = savedStateOf("required_attendance", 75)
    private var classesForTheCourse = savedStateOf("class_details", listOf<ClassDetail>())
    private var extraClassesForTheCourse = savedStateOf("extra_class_details", listOf<ExtraClassDetails>())
    private var doneButtonBottom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableSharedZAxisTransition()

        args.courseItem.let {
            courseName.value = it.courseName
            requiredAttendancePercentage.value = it.requiredAttendance.toInt()
        }
        classesForTheCourse.value = args.scheduleClasses.toList()
        extraClassesForTheCourse.value = args.extraClasses.toList()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup initial values
        binding.toolbar.title = "Edit details for ${courseName.value}"
        binding.courseName.setText(courseName.value)
        binding.courseNameTextInputLayout.helperText = "Previous name: ${args.courseItem.courseName}"
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
        binding.requiredAttendanceOriginalText.text = "Previous value:: %d%%".format(args.courseItem.requiredAttendance.toInt())

        binding.doneButton.enableSystemBarsInsetsCallback(
            originalRightMarginDp = 16,
            originalBottomMarginDp = 16
        )
        enableViewAboveKeyboardWithAnimationCallback(binding.doneButton){ doneButtonBottom = it }
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
        requiredAttendancePercentage.observe(viewLifecycleOwner) {newValue ->
            binding.requiredAttendanceText.text = "Required Attendance updated: %d%%".format(newValue)
        }

        classesForTheCourse.observe(viewLifecycleOwner) { list ->
            binding.scheduleItemsGroup.removeAllViews()
            list.forEachIndexed { index, it ->
                val title = "${it.dayOfWeek.name}; ${it.startTime.format(timeFormatter)} to ${
                    it.endTime.format(timeFormatter)
                }"
                addChipToGroup(
                    title = title,
                    onCloseIconClick = {
                        classesForTheCourse.value = list.toMutableList().apply { removeAt(index) }
                    },
                )
            }
        }
        binding.resetScheduleButton.setOnClickListener {
            classesForTheCourse.value = args.scheduleClasses.toList()
        }
        binding.addClassButton.setOnClickListener {
            //Hacks
            if(softKeyboardVisible(it)){
                binding.doneButton.bottom = doneButtonBottom
            }
            findNavController().navigate(EditCourseFragmentDirections.toAddCourseClassBottomSheet())
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<AddCourseBottomSheetResult>("course_key")
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


    private fun saveCreatedCourseToDB(){
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
