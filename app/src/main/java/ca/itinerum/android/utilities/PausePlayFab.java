package ca.itinerum.android.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by stewjacks on 2016-09-24.
 */

public class PausePlayFab extends FloatingActionButton {
	private AnimatorSet mAnimatorSet;
	private PausePlayDrawable mDrawable;

	public PausePlayFab(Context context) {
		this(context, null);
	}

	public PausePlayFab(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PausePlayFab(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mDrawable = new PausePlayDrawable(context, getSize());
		mDrawable.setCallback(this);
		setImageDrawable(mDrawable);
	}

	public void setPaused() {
		if (!mDrawable.isPlay()) return;

		if (mAnimatorSet != null) {
			mAnimatorSet.cancel();
		}

		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.setDuration(0);
		mAnimatorSet.play(mDrawable.getPausePlayAnimator());
		mAnimatorSet.start();
	}

	public void setPlay() {
		if (mDrawable.isPlay()) return;

		if (mAnimatorSet != null) {
			mAnimatorSet.cancel();
		}

		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.setDuration(0);
		mAnimatorSet.play(mDrawable.getPausePlayAnimator());
		mAnimatorSet.start();
	}

	public void toggle() {
		if (mAnimatorSet != null) {
			mAnimatorSet.cancel();
		}

		mAnimatorSet = new AnimatorSet();
		final Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
		mAnimatorSet.setInterpolator(new DecelerateInterpolator());
		mAnimatorSet.setDuration(200);
		mAnimatorSet.play(pausePlayAnim);
		mAnimatorSet.start();
	}
}
