package ca.itinerum.android.survey;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.utilities.SystemUtils;

public class SurveySelectionFragment extends NamedFragment {

	@BindView(R.id.select_survey_edittext) EditText mSelectSurveyEdittext;
	@BindView(R.id.fragment_continue_button) Button mContinueButton;

	private OnFragmentInteractionListener mListener;

	public static SurveySelectionFragment newInstance(String name) {
		SurveySelectionFragment fragment = new SurveySelectionFragment();
		Bundle args = new Bundle();
		args.putString(NAME, name);
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_survey_selection, container, false);
		ButterKnife.bind(this, v);


		mSelectSurveyEdittext.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				checkButtonLock();
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		mSelectSurveyEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					SystemUtils.hideKeyboardFrom(SurveySelectionFragment.this.getActivity(), v);
				}
			}
		});


		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonPressed();
			}
		});
		checkButtonLock();

		return v;
	}

	private void checkButtonLock() {
		toggleContinueButton(StringUtils.isNotEmpty(mSelectSurveyEdittext.getText()));
	}

	private void toggleContinueButton(boolean enabled) {
		if (enabled) {
			mContinueButton.setEnabled(true);
			mContinueButton.setClickable(true);
			mContinueButton.setAlpha(1f);
		} else {
			mContinueButton.setEnabled(false);
			mContinueButton.setClickable(false);
			mContinueButton.setAlpha(0.5f);
		}
	}

	public void onButtonPressed() {
		if (mListener != null) {

			Map<String, Object> results = new HashMap<>();
			results.put(getName(), mSelectSurveyEdittext.getText().toString());
			mListener.onFragmentInteraction(getName(), results);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
}
