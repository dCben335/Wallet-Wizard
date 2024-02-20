package com.example.walletwizard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.example.walletwizard.Fragments.ConverterFragment;
import com.example.walletwizard.Fragments.HomeFragment;
import com.example.walletwizard.Fragments.MapFragment;

public class MainActivity extends AppCompatActivity {
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    public final MapFragment mapFragment = new MapFragment();
    public final HomeFragment homeFragment = new HomeFragment();
    public final ConverterFragment converterFragment = new ConverterFragment();

    private RelativeLayout currentContainer;
    private ImageButton currentButton;

    public RelativeLayout homeButtonContainer;
    public ImageButton homeButton;
    public RelativeLayout mapButtonContainer;
    public ImageButton mapButton;
    public RelativeLayout converterButtonContainer;
    public ImageButton converterButton;

    public final int toLeftAnimation = R.anim.fragment_enter_animation;
    public final int toRightAnimation = R.anim.fragment_exit_animation;

    private int currentApiVersion;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        getFullScreenApp();

        setContentView(R.layout.activity_main);
        loadFragment(homeFragment, 0);

        setFields();
        setFragmentButtons();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) getFullScreenApp();
    }

    public void changeFragment(RelativeLayout container, ImageButton button, Fragment fragment, int enterAnimation) {
        if (currentContainer != null & currentButton != null) resetCurrentButtonBackground();
        setCurrentButton(container, button);
        if (currentContainer != null & currentButton != null) setCurrentButtonBackground();

        loadFragment(fragment, enterAnimation);
    }

    protected void setFields() {
        homeButtonContainer = findViewById(R.id.home_btn_layout);
        homeButton = findViewById(R.id.home_button);

        converterButton = findViewById(R.id.converter_button);
        converterButtonContainer = findViewById(R.id.converter_btn_layout);

        mapButton = findViewById(R.id.map_button);
        mapButtonContainer = findViewById(R.id.map_btn_layout);
    }

    protected void setFragmentButtons() {
        setFragmentButtonListener(homeButton, homeButtonContainer, homeFragment, 0);
        setFragmentButtonListener(mapButton, mapButtonContainer, mapFragment, toLeftAnimation);
        setFragmentButtonListener(converterButton, converterButtonContainer, converterFragment, toRightAnimation);

        setCurrentButton(homeButtonContainer, homeButton);
        setCurrentButtonBackground();
    }


    protected void setFragmentButtonListener(ImageButton button, RelativeLayout container, final Fragment fragment, int enterAnimation) {
        button.setOnClickListener((view) -> changeFragment(container, button, fragment, enterAnimation));
    }


    private void setCurrentButton(RelativeLayout container, ImageButton button) {
        currentContainer = container;
        currentButton = button;
    }

    private void setCurrentButtonBackground() {
        currentButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_900));
        currentContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.background_rounded_100));
    }

    private void resetCurrentButtonBackground() {
        currentButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_100));
        currentContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.background_rounded_transparent));
    }

    private void loadFragment(Fragment fragment,int enterAnimation) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(enterAnimation, 0);
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (existingFragment != null && existingFragment.getClass().equals(fragment.getClass())) {
            // If the fragment is already added, just show it
            fragmentTransaction.show(existingFragment);
        } else {
            fragmentTransaction.replace(R.id.fragment_container, fragment);
        }

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private void getFullScreenApp() {
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
}