package ca.itinerum.android.settings;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.AboutDialog;
import ca.itinerum.android.DMApplication;
import ca.itinerum.android.MainActivity;
import ca.itinerum.android.TwoButtonDialog;
import ca.itinerum.android.onboarding.OnboardActivity;
import ca.itinerum.android.sync.DataSender;
import ca.itinerum.android.sync.DataSyncJob;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SettingsFragment extends PreferenceFragmentCompat {

	private int mBottomPadding;
	private List<Disposable> mDisposables = new ArrayList<>();

	public static SettingsFragment newInstance(int bottomPadding) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		args.putInt("PADDING", bottomPadding);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBottomPadding = getArguments().getInt("PADDING");

		findPreference("PAUSE_RECORDING").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				//Resume has no warning
				if (!(boolean) newValue) {
					EventBus.getDefault().post(new LocationLoggingEvent.PauseResume(false));
					return true;
				}

				// Here we throw a dialog, which will control toggling based on user input

				TwoButtonDialog dialog = TwoButtonDialog.newInstance("pause_recording", getString(R.string.should_pause_message), getString(android.R.string.ok), getString(android.R.string.cancel));
				dialog.show(getActivity().getSupportFragmentManager(), "pause_recording");

				return false;

//				EventBus.getDefault().post(new LocationLoggingEvent.PauseResume((boolean) newValue));
//				return true;
			}
		});

		findPreference("sync").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				DataSyncJob.scheduleImmediateJob();
				return true;
			}
		});

		mDisposables.add(SharedPreferenceManager.getInstance(getContext()).getLastSyncDateObservable().subscribe(new Consumer<DateTime>() {
			@Override
			public void accept(DateTime dateTime) throws Exception {
				DateTime lastSync = SharedPreferenceManager.getInstance(getContext()).getLastSyncDate();
				findPreference("sync").setSummary(getString(R.string.pref_summary_sync) + " " + (lastSync.getMillis() > 0 ? lastSync.toString(DateTimeFormat.shortDateTime()) : " "));
			}
		}));

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
				bundle.putInt("local_image", R.drawable.logo_icon);
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

		findPreference("tutorial").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getContext(), OnboardActivity.class);
				startActivity(intent);
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

		if (BuildConfig.SHOW_DEBUG) {
			findPreference("crash").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Crashlytics.getInstance().crash(); // Force a crash
					return false;
				}
			});

			findPreference("log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {

//					FirebaseStorage storage = FirebaseStorage.getInstance();
//					StorageReference remoteLogFile = storage.getReference().child("logs/" + SharedPreferenceManager.getInstance(getContext()).getUUID() + "-" + DateTime.now().toString() + ".log");
//
//					File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "itinerum");
//					final File logFile = new File(dir, "logfile.txt");
//
//					try {
//						InputStream stream = new FileInputStream(logFile);
//						UploadTask uploadTask = remoteLogFile.putStream(stream);
////						((SettingsActivity) getActivity()).mProgressBar.setVisibility(View.VISIBLE);
//						uploadTask.addOnFailureListener(new OnFailureListener() {
//							@Override
//							public void onFailure(@NonNull Exception e) {
////								((SettingsActivity) getActivity()).mProgressBar.setVisibility(View.GONE);
//								Toast.makeText(getContext(), "Failed to upload log", Toast.LENGTH_LONG).show();
//							}
//						}).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//							@Override
//							public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
////								((SettingsActivity) getActivity()).mProgressBar.setVisibility(View.GONE);
//								Toast.makeText(getContext(), "Upload complete", Toast.LENGTH_LONG).show();
//								logFile.delete();
//							}
//						});
//					} catch (FileNotFoundException e) {
//						Logger.l.e(e.toString());
////						((SettingsActivity) getActivity()).mProgressBar.setVisibility(View.GONE);
//					}

//						Intent intent = new Intent(getContext(), LogActivity.class);
//						startActivity(intent);

//						Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
//								Uri.fromParts("mailto", "dev@iogistics.com", null));
//						emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
//								"log report " + DateTime.now().toString(DateTimeFormat.fullDateTime()));
//						emailIntent.putExtra(Intent.EXTRA_TEXT, SystemUtils.getPhoneDetails());
////						emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////						emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//
////
//						File file = new File(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "itinerum"), "logfile.txt");
//						Uri uri = FileProvider.getUriForFile(getContext(), "ca.itinerum.android.fileprovider", file);
//						emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
//						startActivity(emailIntent);

					return false;
				}
			});

		} else {
			getPreferenceScreen().removePreference(findPreference("debug"));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		for (Disposable disposable: mDisposables) {
			disposable.dispose();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		getListView().setPadding(0, 0, 0, mBottomPadding);
		getListView().setClipToPadding(false);

		return v;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.pref_app);
	}
}
