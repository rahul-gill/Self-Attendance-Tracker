package com.github.rahul_gill.attendance.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.FragmentSettingsBinding
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.util.Constants
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition
import com.github.rahul_gill.attendance.util.observerWithLifecycle
import com.github.rahul_gill.attendance.util.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding(FragmentSettingsBinding::bind)
    private val dateFormatChoices: Array<String> by lazy {
        resources.getStringArray(R.array.date_format_choices)
    }
    private val timeFormatChoices: Array<String> by lazy {
        resources.getStringArray(R.array.time_format_choices)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableSharedZAxisTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            themeSettingCard.setOnClickListener {
                showListPreferenceDialog(
                    dialogTitle = getString(R.string.app_theme_title),
                    selectedItemIndex = PreferenceManager.themePref.value.toInt(),
                    choices = resources.getStringArray(R.array.theme_entries_values).toList()
                ) { index ->
                    PreferenceManager.themePref.setValue(index.toLong())
                    when (index) {
                        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
            defaultHomeTabSettingCard.setOnClickListener {
                showListPreferenceDialog(
                    dialogTitle = getString(R.string.default_main_page_tab_title),
                    selectedItemIndex = PreferenceManager.defaultHomeTabPref.value.toInt(),
                    choices = resources.getStringArray(R.array.default_main_pager_tab_entries_values)
                        .toList()
                ) { index ->
                    PreferenceManager.defaultHomeTabPref.setValue(index.toLong())
                }
            }
            timeFormatSettingCard.setOnClickListener {
                showListPreferenceDialog(
                    dialogTitle = getString(R.string.time_format),
                    selectedItemIndex = timeFormatChoices.indexOf(PreferenceManager.defaultTimeFormatPref.value),
                    choices = timeFormatChoices.map {
                        DateTimeFormatter.ofPattern(it).format(LocalTime.now())
                    }
                ) { index ->
                    PreferenceManager.defaultTimeFormatPref.setValue(timeFormatChoices[index])
                }
            }
            dateFormatSettingCard.setOnClickListener {
                showListPreferenceDialog(
                    dialogTitle = getString(R.string.date_format),
                    selectedItemIndex = dateFormatChoices.indexOf(PreferenceManager.defaultDateFormatPref.value).also {
                        println("dateFormatChoices: ${dateFormatChoices.toList()}")
                        println("PreferenceManager.defaultDateFormatPref.value: ${PreferenceManager.defaultDateFormatPref.value}")
                        println("index: $it")
                    },
                    choices = dateFormatChoices.map {
                        DateTimeFormatter.ofPattern(it).format(LocalDate.now())
                    }
                ) { index ->
                    PreferenceManager.defaultDateFormatPref.setValue(dateFormatChoices[index])
                }
            }
            githubLinkCard.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(Constants.GITHUB_APP_LINK)
                requireContext().startActivity(intent)
            }
            observerWithLifecycle(PreferenceManager.themePref){ themeType ->
                val array = resources.getStringArray(R.array.theme_entries_values)
                binding.themeSettingValue.text = array[themeType.toInt()]
            }
            observerWithLifecycle(PreferenceManager.defaultHomeTabPref){ tabType ->
                val array =
                    resources.getStringArray(R.array.default_main_pager_tab_entries_values)
                binding.defaultHomeTabSettingValue.text = array[tabType.toInt()]
            }
            observerWithLifecycle(PreferenceManager.defaultTimeFormatPref){
                binding.timeFormatSettingValue.text = it
            }
            observerWithLifecycle(PreferenceManager.defaultDateFormatPref){
                binding.dateFormatSettingValue.text = it
            }
        }
    }

    private fun showListPreferenceDialog(
        dialogTitle: String,
        selectedItemIndex: Int,
        choices: List<String>,
        onSelectIndex: (Int) -> Unit
    ) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogTitle)
            .setSingleChoiceItems(
                choices.toTypedArray(),
                selectedItemIndex
            ) { dialog, selectionIndex ->
                onSelectIndex(selectionIndex)
                dialog.dismiss()
            }.create()
        dialog.show()
    }
}
