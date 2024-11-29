package com.pxy.visaz.ui.applyvisa

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.firebase.util.nextAlphanumericString
import com.pxy.visaz.R
import com.pxy.visaz.core.AppConstants
import com.pxy.visaz.core.PopBackFragment
import com.pxy.visaz.core.customview.ViewYesNoDocUpload
import com.pxy.visaz.core.extension.copyFileToAppStorage
import com.pxy.visaz.core.extension.showDatePicker
import com.pxy.visaz.core.extension.uriToFilePath
import com.pxy.visaz.core.model.visa.Country
import com.pxy.visaz.core.model.visa.VisaApplicationModel
import com.pxy.visaz.core.utils.Validator
import com.pxy.visaz.databinding.FragmentVisaSubmitFormBinding
import com.pxy.visaz.ui.home.VisaViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random


class VisaSubmitFormFragment : PopBackFragment() {

    private lateinit var binding: FragmentVisaSubmitFormBinding
    private var visaApplicationModel: VisaApplicationModel? = null
    private var selectedDate: String? = null
    private var selectedViewYesNoDocUpload: ViewYesNoDocUpload? = null

    private val validator by lazy { Validator() }

    private val visaViewModel: VisaViewModel by viewModel()

    // List of Country objects
    private val countries = listOf(
        Country("India", "+91"),
        Country("United States", "+1"),
        Country("United Kingdom", "+44"),
        Country("Canada", "+1"),
        Country("Australia", "+61"),
        Country("Germany", "+49"),
        Country("France", "+33"),
        Country("Japan", "+81"),
        Country("China", "+86")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initViews()
        initListeners()
    }

    private fun initListeners() {
        with(binding) {
            inputSelectedDate.addOnClickListener {
                showSelectedDatePicker()
            }
            inputDOB.addOnClickListener {
                showDOBDatePicker()
            }
            docViewProfile.addDocImageClickListener {
                selectedViewYesNoDocUpload = docViewProfile
                pickImage.launch("image/*")
            }
            docViewPassport.addDocImageClickListener {
                selectedViewYesNoDocUpload = docViewPassport
                pickImage.launch("image/*")
            }
        }
    }

    private fun showSelectedDatePicker() {
        showDatePicker {
            binding.inputSelectedDate.setText(it)
        }
    }

    private fun showDOBDatePicker() {
        // Set constraints to disable past dates
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        showDatePicker(constraintsBuilder) {
            binding.inputDOB.setText(it)
        }
    }

    private fun initViews() {
        initToolbar()
        visaApplicationModel?.let {
            with(binding) {
                tvContactInCountry.text = getString(R.string.contact_in_country, it.name)

                docViewProfile.setData(
                    getString(R.string.doc_previous_passport_exhibiting),
                    getString(R.string.upload_doc)
                )

                docViewPassport.setData(
                    getString(R.string.doc_long_term_fd),
                    getString(R.string.upload_doc)
                )
            }
        }
    }

    private fun initCountryDropDown() {
        // Adapter for dropdown displaying country names
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            countries.map { it.name } // Display only the name
        )
        //binding.etCountry.setAdapter(adapter)
    }

    private fun initToolbar() {
        with(binding.layoutHeader.toolbar) {
            setNavigationOnClickListener {
                goBack()
            }
            title = getString(R.string.label_submit_application)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntentData()
    }

    private fun readIntentData() {
        arguments?.let {
            visaApplicationModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(
                    AppConstants.EXTRA_VISA_DETAILS,
                    VisaApplicationModel::class.java
                )
            } else {
                arguments?.getParcelable(AppConstants.EXTRA_VISA_DETAILS)
            }
            selectedDate = arguments?.getString(AppConstants.EXTRA_SELECTED_DATE)
        }
    }

    private fun initObservers() {

    }


    // Register for activity result to get an image from gallery
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val randomImageName = Random.nextAlphanumericString(10)
                val profileImageUri = uriToFilePath(imageUri)?.let {
                    copyFileToAppStorage(
                        requireContext(),
                        it,
                        randomImageName
                    )
                }
                selectedViewYesNoDocUpload?.setImageUri(profileImageUri, randomImageName)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisaSubmitFormBinding.inflate(inflater, container, false)
        return binding.root
    }
}