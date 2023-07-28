package com.github.rahul_gill.attendance.ui.details

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.navigation.fragment.findNavController
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.AddExtraClassBottomSheetBinding
import com.github.rahul_gill.attendance.ui.main.ZoomOutPageTransformer
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

class AddExtraClassBottomSheet : BottomSheetDialogFragment(R.layout.add_extra_class_bottom_sheet) {
    private val binding by viewBinding(AddExtraClassBottomSheetBinding::bind)

    private var extraClass = ExtraClassTimings(
        date = LocalDate.now(),
        startTime = LocalTime.now(),
        endTime = LocalTime.now().plusHours(1)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            pager.adapter = AddExtraClassBottomSheetPagerAdapter(
                initials = extraClass,
                onSetDate = { date ->
                    extraClass = extraClass.copy(date = date)
                },
                onSetStartTime = { time ->

                    extraClass = extraClass.copy(startTime = time)
                },
                onSetEndTime = { time ->
                    extraClass = extraClass.copy(endTime = time)
                }
            )
            pager.setPageTransformer(ZoomOutPageTransformer())
            TabLayoutMediator(tabLayout, pager) { tab, position ->
                tab.text =
                    resources.getStringArray(R.array.add_extra_class_bottom_sheet_tabs)[position]
            }.attach()
            cancelButton.setOnClickListener {
                findNavController().navigateUp()
            }
            doneButton.setOnClickListener {
                findNavController()
                    .previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(EXTRA_CLASS_KEY, extraClass)
                findNavController().navigateUp()
            }
        }
    }

    companion object{
        const val EXTRA_CLASS_KEY = "extra_class_key"
    }
}


@Parcelize
data class ExtraClassTimings(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
) : Parcelable