package ca.itinerum.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import ca.itinerum.android.R;

/**
 * Created by stewart on 2018-04-27.
 */

class RepeatButton extends AppCompatImageButton {

	private Timer mTimer;

	private OnRepeatButtonListener mListener;
	private boolean mTaskHasTriggered = false;

	public RepeatButton(Context context) {
		super(context);
	}

	public RepeatButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RepeatButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		setBackgroundResource(R.drawable.background_circle_button);

		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						v.setPressed(true);
						mTaskHasTriggered = false;
						if (mTimer == null) {
							mTimer = new Timer();
							mTimer.schedule(getTimerTask(), 750, 350);
						}

						return true;
					}

					case MotionEvent.ACTION_MOVE:
						return mTaskHasTriggered;

					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL: {
						mTimer.cancel();
						mTimer = null;
						v.setPressed(false);
						if (!mTaskHasTriggered && mListener != null) mListener.onClick();
						mListener.onTouchUp();
						return true;
					}
				}

				return false;
			}
		});

	}

	private TimerTask getTimerTask() {
		mTaskHasTriggered = false;
		return new TimerTask() {
			@Override
			public void run() {
				mTaskHasTriggered = true;
				if (mListener != null) {
					// this seems like a potentially unsafe way to do this.
					((Activity) getContext()).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (mListener.onButtonRepeat()) {
								Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
									v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
								} else {
									//deprecated in API 26
									v.vibrate(50);
								}
							}
						}
					});


				}
			}
		};
	}

	public void setOnRepeatListener(OnRepeatButtonListener listener) {
		mListener = listener;
	}

	public interface OnRepeatButtonListener {
		boolean onClick();
		boolean onButtonRepeat();
		void onTouchUp();

	}

}