package edu.pitt.cs.exposure;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

public class SplashActivity extends BaseActivity {

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		animate();
	}

	/**
     * Helper function to set up the animation.
     */
	private void animate() {
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fadein_zoom);
		LayoutAnimationController controller = new LayoutAnimationController(anim);
		ViewGroup viewGroup = (ViewGroup)findViewById(R.id.splash_layout);
		viewGroup.setLayoutAnimation(controller);

		anim.setAnimationListener(new AnimationListener() {
			private int stopCount = 0;

			@Override
			public void onAnimationEnd(Animation animation) {
				stopCount++;
				if (stopCount == 4) {
					try {
						Thread.sleep(1500, 0);
					} catch (InterruptedException e) {
					}
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					SplashActivity.this.finish();
				}
			}
			@Override public void onAnimationRepeat(Animation arg0) {}
			@Override public void onAnimationStart(Animation arg0) {}
		});
	}

	/**
     * 
     */
	@Override
	protected void onPause() {
		super.onPause();

		// stop animations
		LinearLayout layout = (LinearLayout) findViewById(R.id.splash_layout);
		for (int i = 0; i < layout.getChildCount(); i++) {
			layout.getChildAt(i).clearAnimation();
		}
	}

	/**
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		animate();
	}

}