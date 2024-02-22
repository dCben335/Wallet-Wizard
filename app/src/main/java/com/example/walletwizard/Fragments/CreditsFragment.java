package com.example.walletwizard.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.walletwizard.MainActivity;
import com.example.walletwizard.R;

// Fragment to display credits information
public class CreditsFragment extends Fragment {
    private View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return rootView = inflater.inflate(R.layout.fragment_credits, container, false);
    }

    // Called immediately after onCreateView()
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Setup UI elements and listeners
        setGoBackButton();
    }

    // Method to setup the go back button
    private void setGoBackButton() {
        rootView.findViewById(R.id.credits_go_back).setOnClickListener(v -> {
            // Check if activity is not null
            if (getActivity() == null) return;

            // Get reference to MainActivity
            MainActivity mainActivity = ((MainActivity) getActivity());

            // Change fragment to home fragment
            mainActivity.changeFragment(
                    mainActivity.homeButtonContainer,
                    mainActivity.homeButton,
                    mainActivity.homeFragment,
                    0
            );
        });
    }
}
