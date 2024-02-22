package com.example.walletwizard;

// AndroidX imports for compatibility with older versions of Android
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

// Importing fragments for the application
import com.example.walletwizard.Fragments.ConverterFragment;
import com.example.walletwizard.Fragments.CreditsFragment;
import com.example.walletwizard.Fragments.HomeFragment;
import com.example.walletwizard.Fragments.MapFragment;

// Main activity class
public class MainActivity extends AppCompatActivity {

    // Fragment manager to manage fragments in this activity
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    // Instances of all fragments used in the app
    public final MapFragment mapFragment = new MapFragment();
    public final HomeFragment homeFragment = new HomeFragment();
    public final ConverterFragment converterFragment = new ConverterFragment();
    public final CreditsFragment creditsFragment = new CreditsFragment();

    // Variables to keep track of current UI state
    private RelativeLayout currentContainer;
    private ImageButton currentButton;

    // UI elements for fragment navigation
    public RelativeLayout homeButtonContainer;
    public ImageButton homeButton;
    public RelativeLayout mapButtonContainer;
    public ImageButton mapButton;
    public RelativeLayout converterButtonContainer;
    public ImageButton converterButton;

    // Animation resources for fragment transitions
    public final int toLeftAnimation = R.anim.fragment_to_left_animation;
    public final int toRightAnimation = R.anim.fragment_to_right_animation;

    // Current API version
    private int currentApiVersion;

    // Flags for achieving full-screen mode
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


    // Called when the activity is starting
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the current API version and set the app to full-screen mode
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        getFullScreenApp();

        // Set the content view to activity_main.xml and load the home fragment
        setContentView(R.layout.activity_main);
        loadFragment(homeFragment, 0);

        // Initialize UI fields and setup fragment buttons
        setFields();
        setFragmentButtons();
    }

    // Called when the window gains or loses focus
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Ensure full-screen mode is retained when the window regains focus
        if (hasFocus) getFullScreenApp();
    }

    // Method to change fragment and change button background
    public void changeFragment(RelativeLayout container, ImageButton button, Fragment fragment, int enterAnimation) {
        // Change the current Fragment Button
        if (currentContainer != null & currentButton != null) resetCurrentButtonBackground();
        setCurrentButton(container, button);
        if (currentContainer != null & currentButton != null) setCurrentButtonBackground();

        loadFragment(fragment, enterAnimation);
    }

    // Method to set UI fields
    protected void setFields() {
        // Initialize UI elements for fragment navigation
        homeButtonContainer = findViewById(R.id.home_btn_layout);
        homeButton = findViewById(R.id.home_button);

        converterButton = findViewById(R.id.converter_button);
        converterButtonContainer = findViewById(R.id.converter_btn_layout);

        mapButton = findViewById(R.id.map_button);
        mapButtonContainer = findViewById(R.id.map_btn_layout);
    }

    // Method to setup fragment navigation buttons
    protected void setFragmentButtons() {
        // Set listeners for fragment navigation buttons
        setFragmentButtonListener(homeButton, homeButtonContainer, homeFragment, 0);
        setFragmentButtonListener(mapButton, mapButtonContainer, mapFragment, toRightAnimation);
        setFragmentButtonListener(converterButton, converterButtonContainer, converterFragment, toLeftAnimation);

        // Set current button and background
        setCurrentButton(homeButtonContainer, homeButton);
        setCurrentButtonBackground();
    }

    // Method to set listener for a fragment navigation button
    protected void setFragmentButtonListener(ImageButton button, RelativeLayout container, final Fragment fragment, int enterAnimation) {
        button.setOnClickListener((view) -> changeFragment(container, button, fragment, enterAnimation));
    }

    // Method to set the current button and container
    private void setCurrentButton(RelativeLayout container, ImageButton button) {
        currentContainer = container;
        currentButton = button;
    }

    // Method to set the background for the current button
    private void setCurrentButtonBackground() {
        currentButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
        currentContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.background_rounded_secondary_color));
    }

    // Method to reset the background for the current button
    private void resetCurrentButtonBackground() {
        currentButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_color));
        currentContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.background_rounded_transparent));
    }

    // Method to load a fragment into the fragment container
    private void loadFragment(Fragment fragment, int enterAnimation) {
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

    // Method to set the app to full-screen mode
    private void getFullScreenApp() {
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
}
