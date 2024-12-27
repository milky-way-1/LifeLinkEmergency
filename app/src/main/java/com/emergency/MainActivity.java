package com.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_main);

        TextView titleText = findViewById(R.id.titleText);
        LottieAnimationView ambulanceAnimation = findViewById(R.id.ambulanceAnimation);

        ambulanceAnimation.setAnimation(R.raw.ambulance);
        ambulanceAnimation.setRepeatCount(LottieDrawable.INFINITE);
        ambulanceAnimation.playAnimation();

        titleText.setAlpha(0f);
        titleText.animate()
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, 4000);
    }
}