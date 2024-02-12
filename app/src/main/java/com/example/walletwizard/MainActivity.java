package com.example.walletwizard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new HomeFragment());
        setFragmentButtonListeners();
    }

    protected void setFragmentButtonListeners() {
        setFragmentButtonListener(findViewById(R.id.map_button), new MapFragment());
        setFragmentButtonListener(findViewById(R.id.home_button), new HomeFragment());
        setFragmentButtonListener(findViewById(R.id.convert_button), new ConvertFragment());
    }


    protected void setFragmentButtonListener(ImageButton button, final Fragment fragment) {
        button.setOnClickListener((view) -> loadFragment(fragment));
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}