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




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new HomeFragment());
        setFragmentButtonListeners();
    }

    protected void setFragmentButtonListeners() {
        setFragmentButtonListener(findViewById(R.id.map_button), mapFragment);
        setFragmentButtonListener(findViewById(R.id.home_button), homeFragment);
        setFragmentButtonListener(findViewById(R.id.convert_button), convertFragment);
    }


    protected void setFragmentButtonListener(ImageButton button, final Fragment fragment) {
        button.setOnClickListener((view) -> loadFragment(fragment));
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

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