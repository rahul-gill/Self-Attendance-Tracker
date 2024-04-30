package com.github.rahul_gill.attendance.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentCourseExtraClassesInfoBinding
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.ExtraClassDetails
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CourseExtraClassesInfoFragment: Fragment(R.layout.fragment_course_extra_classes_info) {
    private val binding by viewBinding(FragmentCourseExtraClassesInfoBinding::bind)
    private val dbOps by lazy { DBOps.instance }
    val courseId: Long by lazy {
        requireArguments().getLong("courseId")
    }
    val courseName: String by lazy {
        requireArguments().getString("courseName")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraClassesAdapter = ExtraClassesAdapter(onClick = { extraClassDetails ->
            findNavController().navigate(
                ViewCourseFragmentDirections.toClassStatusSetterBottomSheetFromViewCourse(
                    TodayCourseItem(
                        scheduleIdOrExtraClassId = extraClassDetails.extraClassId,
                        startTime = extraClassDetails.startTime,
                        endTime = extraClassDetails.endTime,
                        classStatus = extraClassDetails.classStatus,
                        courseName = courseName,
                        isExtraClass = true,
                        date = extraClassDetails.date
                    )
                )
            )
        })

        //swipe to delete extra classes
        setupSwipeToDeleteExtraClass(extraClassesAdapter)

        binding.extraClassesList.adapter = extraClassesAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            dbOps.getExtraClassesListForCourse(courseId)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { items ->
                    val emptyList = items.isEmpty()
                    binding.extraClassesList.isVisible = !emptyList
                    binding.noExtraHelperView1.isVisible = emptyList
                    binding.noExtraHelperView2.isVisible = emptyList
                    binding.noExtraClassMessage.isVisible = emptyList
                    binding.addExtraClassFab.isVisible = !emptyList
                    binding.addExtraClassZero.isVisible = emptyList
                    extraClassesAdapter.submitList(items)
                }
        }
        binding.addExtraClassFab.setOnClickListener {
            findNavController().navigate(ViewCourseFragmentDirections.toAddExtraClassBottomSheet())
        }
        binding.addExtraClassZero.setOnClickListener {
            findNavController().navigate(ViewCourseFragmentDirections.toAddExtraClassBottomSheet())
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ExtraClassTimings>(AddExtraClassBottomSheet.EXTRA_CLASS_KEY)
            ?.observe(viewLifecycleOwner) { extraClassesDetails ->
                dbOps.createExtraClasses(courseId, extraClassesDetails)
            }
    }

    private fun setupSwipeToDeleteExtraClass(extraClassesAdapter: ExtraClassesAdapter) {
        val onItemTouchHelperSwiped = {
                viewHolder: RecyclerView.ViewHolder,
                adapter: ExtraClassesAdapter ->
            val toDelete: ExtraClassDetails = (viewHolder as ExtraClassesAdapter.VH).getItem()
            val position = viewHolder.adapterPosition
            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_App_MaterialAlertDialogError
            )
                .setTitle(getString(R.string.delete_extra_class_dialog_title))
                .setMessage(
                    getString(R.string.delete_extra_class_dialog_description,
                        toDelete.date.format(dateFormatter),
                        toDelete.startTime.format(timeFormatter),
                        toDelete.endTime.format(timeFormatter)
                    )
                )
                .setIcon(R.drawable.baseline_warning_24)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton(R.string.ok) { dialog, _ ->
                    dbOps.deleteExtraClass(toDelete.extraClassId)
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    adapter.notifyItemChanged(position)
                }
                .show()
        }

        val itemTouchHelperRight =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    onItemTouchHelperSwiped(viewHolder, extraClassesAdapter)
                }
            })
        val itemTouchHelperLeft =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    onItemTouchHelperSwiped(viewHolder, extraClassesAdapter)
                }
            })
        itemTouchHelperRight.attachToRecyclerView(binding.extraClassesList)
        itemTouchHelperLeft.attachToRecyclerView(binding.extraClassesList)
    }



    companion object{
        fun create(courseId: Long, courseName: String): CourseExtraClassesInfoFragment {
            val argBundle = Bundle()
            argBundle.putLong("courseId", courseId)
            argBundle.putString("courseName", courseName)
            val fragment = CourseExtraClassesInfoFragment()
            fragment.arguments = argBundle
            return fragment
        }
    }
}