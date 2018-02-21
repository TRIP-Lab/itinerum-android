package ca.itinerum.android.settings;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.AboutDialog;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.DMApplication;
import ca.itinerum.android.MainActivity;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.DataSender;
import ca.itinerum.android.sync.DataSyncJob;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SystemUtils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

public class SettingsActivity extends AppCompatActivity {

	@BindView(R.id.toolbar) Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		Fragment preferenceFragment = new GeneralPreferenceFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.container, preferenceFragment);
		ft.commit();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				supportFinishAfterTransition();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * This method stops fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName) || GeneralPreferenceFragment.class.getName().equals(fragmentName);
	}

	public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			findPreference("PAUSE_RECORDING").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					EventBus.getDefault().post(new LocationLoggingEvent.PauseResume((boolean) newValue));
					return true;
				}
			});

			findPreference("sync").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					DataSyncJob.scheduleImmediateJob();
					return true;
				}
			});


			findPreference("leave_survey").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new AlertDialog.Builder(getContext())
							.setTitle(R.string.title_warning_generic)
							.setMessage(R.string.message_warning_leave_survey)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									DataSender dataSender = new DataSender(getContext());

									try {
										dataSender.sync();
									} catch (Exception e) {
										Logger.l.e(e.toString());
									}

									SystemUtils.leaveCurrentSurvey(getContext());

									((DMApplication) getActivity().getApplicationContext()).stopLoggingService();

									Intent intent = new Intent(getContext(), MainActivity.class);
									getActivity().startActivity(intent);
									getActivity().finish();
								}
							}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
							.show();
					return false;
				}
			});

			findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					AboutDialog dialog = new AboutDialog();
					Bundle bundle = new Bundle();
					bundle.putString("message", getString(R.string.about_details));
					bundle.putInt("local_image", R.drawable.itinerum_logo_icon);
					bundle.putBoolean("show_brand", true);
					dialog.setArguments(bundle);
					dialog.show(getActivity().getSupportFragmentManager(), "about");

					return false;
				}
			});

			findPreference("rate_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					//https://stackoverflow.com/questions/10816757/rate-this-app-link-in-google-play-store-app-on-the-phone
					Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					// To count with Play market backstack, After pressing back button,
					// to taken back to our application, we need to add following flags to intent.
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
							Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
							Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
					}
					return false;
				}
			});

			findPreference("contact_us").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
							Uri.fromParts("mailto", getString(R.string.feedback_email), null));
					emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_title));
					emailIntent.putExtra(Intent.EXTRA_TEXT, SystemUtils.getPhoneDetails());
					startActivity(Intent.createChooser(emailIntent, ""));
					return false;
				}
			});

			findPreference("open_source").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new AlertDialog.Builder(getContext())
							.setPositiveButton(android.R.string.ok, null)
							.setView(R.layout.view_open_source)
							.show();
					return false;
				}
			});

			if (BuildConfig.DEBUG) {
				findPreference("crash").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						return false;
					}
				});
			} else {
				getPreferenceScreen().removePreference(findPreference("debug"));
			}
		}

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			addPreferencesFromResource(R.xml.pref_app);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
