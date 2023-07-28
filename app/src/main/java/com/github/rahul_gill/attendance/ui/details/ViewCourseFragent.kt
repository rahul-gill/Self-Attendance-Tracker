package com.github.rahul_gill.attendance.ui.details

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentViewCoursePagedBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform


class ViewCourseFragment : Fragment(R.layout.fragment_view_course_paged) {
    private val binding by viewBinding(FragmentViewCoursePagedBinding::bind)
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
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = args.courseItem.courseName
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_course_details -> {
                    findNavController().navigate(
                        ViewCourseFragmentDirections.toEditCourseFragment(args.courseItem)
                    )
                    true
                }

                R.id.delete_course_details -> {
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.ThemeOverlay_App_MaterialAlertDialogError
                    )
                        .setTitle(
                            getString(
                                R.string.delete_course_dialog_title,
                                args.courseItem.courseName
                            )
                        )
                        .setMessage(getString(R.string.delete_course_dialog_description))
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
        binding.viewPager.adapter = object : FragmentStateAdapter(requireActivity()) {

            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> CourseInfoFragment.create(args.courseItem.courseId)

                    1 -> CourseExtraClassesInfoFragment.create(
                        args.courseItem.courseId,
                        args.courseItem.courseName
                    )

                    else -> {
                        val fragment = AttendanceRecordFragment.create(
                            args.courseItem.courseId,
                            args.courseItem.courseName
                        )
                        (fragment as OnAttendanceRecordClickPropagator).doThis { todayCourseItem ->
                            findNavController().navigate(
                                ViewCourseFragmentDirections.toClassStatusSetterBottomSheetFromViewCourse(
                                    todayCourseItem
                                )
                            )
                        }
                        fragment
                    }

                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getStringArray(R.array.view_course_tabs)[position]
        }.attach()
    }
}