package com.example.walletwizard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.walletwizard.Fragments.ConverterFragment;
import com.example.walletwizard.Fragments.HomeFragment;
import com.example.walletwizard.Fragments.MapFragment;

public class MainActivity extends AppCompatActivity {

    private final MapFragment mapFragment = new MapFragment();
    private final HomeFragment homeFragment = new HomeFragment();
    private final ConverterFragment converterFragment = new ConverterFragment();
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private RelativeLayout currentContainer;
    private ImageButton currentButton;

    private final int toLeftAnimation = R.anim.fragment_enter_animation;
    private final int toRightAnimation = R.anim.fragment_exit_animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new HomeFragment(), 0);
        setFragmentButtons();

    }

    protected void setFragmentButtons() {
        RelativeLayout mapButtonContainer = findViewById(R.id.map_btn_layout);
        RelativeLayout homeButtonContainer = findViewById(R.id.home_btn_layout);
        RelativeLayout converterButtonContainer = findViewById(R.id.converter_btn_layout);

        ImageButton mapButton = findViewById(R.id.map_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton converterButton = findViewById(R.id.converter_button);

        setFragmentButtonListener(mapButton, mapButtonContainer, mapFragment, toLeftAnimation);
        setFragmentButtonListener(homeButton, homeButtonContainer, homeFragment, 0);
        setFragmentButtonListener(converterButton, converterButtonContainer, converterFragment, toRightAnimation);

        setCurrentButton(homeButtonContainer, homeButton);
        setCurrentButtonBackground();
    }


    private void setFragmentButtonListener(ImageButton button,RelativeLayout container, final Fragment fragment, int enterAnimation) {
        button.setOnClickListener((view) -> {
            loadFragment(fragment, enterAnimation);

            resetCurrentButtonBackground();
            setCurrentButton(container, button);
            setCurrentButtonBackground();
        });
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
}