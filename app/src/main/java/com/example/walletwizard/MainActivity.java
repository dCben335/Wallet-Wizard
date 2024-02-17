package com.example.walletwizard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.walletwizard.Fragments.ConvertFragment;
import com.example.walletwizard.Fragments.HomeFragment;
import com.example.walletwizard.Fragments.MapFragment;

public class MainActivity extends AppCompatActivity {

    private final MapFragment mapFragment = new MapFragment();
    private final HomeFragment homeFragment = new HomeFragment();
    private final ConvertFragment convertFragment = new ConvertFragment();
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private final int toLeftAnimation = R.anim.fragment_enter_animation;
    private final int toRightAnimation = R.anim.fragment_exit_animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new HomeFragment(), 0);
        setFragmentButtonListeners();
    }

    protected void setFragmentButtonListeners() {
        setFragmentButtonListener(findViewById(R.id.map_button), mapFragment, toLeftAnimation);
        setFragmentButtonListener(findViewById(R.id.home_button), homeFragment, 0);
        setFragmentButtonListener(findViewById(R.id.convert_button), convertFragment, toRightAnimation);
    }


    protected void setFragmentButtonListener(ImageButton button, final Fragment fragment, int enterAnimation) {
        button.setOnClickListener((view) -> loadFragment(fragment, enterAnimation));
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