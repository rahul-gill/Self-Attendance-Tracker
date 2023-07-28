package com.github.rahul_gill.attendance.ui.create

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.AddClassBottomSheetBinding
import com.github.rahul_gill.attendance.ui.main.ZoomOutPageTransformer
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.parcelize.Parcelize
import java.time.LocalTime


class AddCourseClassBottomSheet : BottomSheetDialogFragment(R.layout.add_class_bottom_sheet) {
    private val binding by viewBinding(AddClassBottomSheetBinding::bind)
    private val args: AddCourseClassBottomSheetArgs by navArgs()
    private lateinit var result: ClassDetail
    private var itemToUpdateIndex = -1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemToUpdateIndex = args.itemToUpdateIndex
        result = ClassDetail(
            dayOfWeek = args.weekDay,
            startTime = args.startTime ?: LocalTime.now(),
            endTime = args.endTime ?: LocalTime.now().plusHours(1)
        )

        binding.apply {
            pager.adapter = AddClassBottomSheetPagerAdapter(
                initials = result,
                onSetWeekDay = { dayOfWeek ->
                    result = result.copy(dayOfWeek = dayOfWeek)
                },
                onSetStartTime = { time ->
                    result = result.copy(startTime = time)
                },
                onSetEndTime = { time ->
                    result = result.copy(endTime = time)
                }
            )
            pager.setPageTransformer(ZoomOutPageTransformer())
            TabLayoutMediator(tabLayout, pager) { tab, position ->
                tab.text =
                    resources.getStringArray(R.array.add_schedule_class_bottom_sheet_tabs)[position]
            }.attach()
            cancelButton.setOnClickListener {
                findNavController().navigateUp()
            }
            doneButton.setOnClickListener {
                findNavController()
                    .previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(COURSE_KEY, AddCourseBottomSheetResult(result, itemToUpdateIndex))
                findNavController().navigateUp()
            }
        }
    }

    companion object {
        const val COURSE_KEY = "course_key"
    }

}

@Parcelize
data class AddCourseBottomSheetResult(
    val classDetail: ClassDetail,
    val itemToUpdateIndex: Int = -1
) : Parcelable