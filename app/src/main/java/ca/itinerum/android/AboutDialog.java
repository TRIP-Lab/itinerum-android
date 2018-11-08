package ca.itinerum.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;

/**
 * Created by stewjacks on 2016-08-23.
 */

public class AboutDialog extends DialogFragment {

	@BindView(R.id.message) AppCompatTextView mMessage;
	@BindView(R.id.version) AppCompatTextView mVersion;
	@BindView(R.id.brand_logo) ImageView mBrandLogo;
	@BindView(R.id.avatar) SimpleDraweeView mAvatar;
	private String mMessageText;

	@SuppressLint("StringFormatInvalid")

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dialog_about, container, false);
		ButterKnife.bind(this, v);

		Bundle bundle = getArguments();

		if (bundle.containsKey("local_image")) {
			mAvatar.setController(Fresco.newDraweeControllerBuilder()
					.setImageRequest(
							ImageRequestBuilder.newBuilderWithResourceId(bundle.getInt("local_image"))
									.build())
					.build());
		}

		else mAvatar.setImageURI(bundle.getString("remote_image"));

		mBrandLogo.setVisibility(bundle.getBoolean("show_brand", false) ? View.VISIBLE : View.GONE);

		mMessageText = bundle.getString("message");
		mMessage.setText(mMessageText);

		mVersion.setText(String.format(getString(R.string.about_version), BuildConfig.VERSION_NAME));

		return v;
	}
}