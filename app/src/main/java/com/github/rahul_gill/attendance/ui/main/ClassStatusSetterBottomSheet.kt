package com.github.rahul_gill.attendance.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.ClassStatusSetterBottomSheetBinding
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

class ClassStatusSetterBottomSheet :
    BottomSheetDialogFragment(R.layout.class_status_setter_bottom_sheet) {
    private val binding by viewBinding(ClassStatusSetterBottomSheetBinding::bind)
    private val dbOps by lazy { DBOps.getInstance(requireContext()) }
    private val args by navArgs<ClassStatusSetterBottomSheetArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.infoText.text = "Set Attendance Status for ${args.todayItem.courseName}\n" +
                "from ${args.todayItem.startTime.format(timeFormatter)} to ${args.todayItem.endTime.format(timeFormatter)}"+
                if(args.todayItem.isExtraClass) "(Extra class)" else "" +
                if(args.todayItem.date != null) " on " + args.todayItem.date!!.format(dateFormatter) else ""
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.radioGroup.check(
            when (args.todayItem.classStatus) {
                CourseClassStatus.Present -> R.id.present
                CourseClassStatus.Absent -> R.id.absent
                CourseClassStatus.Cancelled -> R.id.cancelled
                CourseClassStatus.Unset -> R.id.unset
            }
        )
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.doneButton.setOnClickListener {
            val classStatus  = when (binding.radioGroup.checkedRadioButtonId) {
                R.id.present -> CourseClassStatus.Present
                R.id.absent -> CourseClassStatus.Absent
                R.id.cancelled -> CourseClassStatus.Cancelled
                else -> CourseClassStatus.Unset
            }
            if(args.todayItem.isExtraClass)
                dbOps.markAttendanceForExtraClass(
                    args.todayItem.scheduleIdOrExtraClassId,
                    classStatus
                )
            else dbOps.markAttendanceForScheduleClass(
                scheduleId = args.todayItem.scheduleIdOrExtraClassId,
                date = LocalDate.now(),
                classStatus = classStatus
            )
            findNavController().navigateUp()
        }
    }
}