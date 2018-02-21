package ca.itinerum.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.sync.PromptAnswerGroup;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.LocationDatabase;

public class PromptListActivity extends AppCompatActivity {

	@BindView(R.id.toolbar) Toolbar mToolbar;
	@BindView(R.id.prompts_recycler_view) PromptsRecyclerView mPromptsRecyclerView;
	@BindView(R.id.fab) FloatingActionButton mFab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prompt_list);
		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);

		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(PromptListActivity.this, PromptDetailsActivity.class);
				intent.putExtra("new_prompt", true);
				startActivity(intent);
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

	}

	@Override
	protected void onResume() {
		super.onResume();

		List<PromptAnswer> promptAnswers = LocationDatabase.getInstance(this).promptDao().getAllRegisteredPromptAnswers();
		List<PromptAnswerGroup> promptAnswerGroups = PromptAnswerGroup.sortPrompts(promptAnswers, SharedPreferenceManager.getInstance(this).getNumberOfPrompts());

		mPromptsRecyclerView.setPromptData(promptAnswerGroups);

		mPromptsRecyclerView.setOnPromptItemClickListener(new PromptsRecyclerView.OnPromptItemClickedListener() {
			@Override
			public void onPromptItemClick(View view, int position) {

				Intent intent = new Intent(PromptListActivity.this, PromptDetailsActivity.class);
				intent.putExtra("position", position);

				List<Pair<View, String>> pairs = new ArrayList<>();

				pairs.add(Pair.create(view.findViewById(R.id.textview_time), "time"));
//				pairs.add(Pair.create(view.findViewById(R.id.textview_date), "date"));
				pairs.add(Pair.create((View) mToolbar, "toolbar"));
                
                // These are fixes for flickering nav and status bars
				View statusBar = findViewById(android.R.id.statusBarBackground);
				View navigationBar = findViewById(android.R.id.navigationBarBackground);

				if (statusBar != null) {
					pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
				}
				if (navigationBar != null) {
					pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
				}

				ActivityOptionsCompat options = ActivityOptionsCompat.
						makeSceneTransitionAnimation(PromptListActivity.this, pairs.toArray(new Pair[pairs.size()]));

				startActivity(intent, options.toBundle());
			}
		});
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
}
