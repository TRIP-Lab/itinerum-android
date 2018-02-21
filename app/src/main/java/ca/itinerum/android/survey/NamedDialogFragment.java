package ca.itinerum.android.survey;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Created by stewjacks on 16-01-18.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class NamedDialogFragment extends DialogFragment {
	protected static final String NAME = "name";
	private String mName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mName = getArguments().getString(NAME);
		} else {
			throw new NotImplementedException("name a NamedFragment buddy");
		}
	}

	public String getName() {
		return mName;
	}
}
