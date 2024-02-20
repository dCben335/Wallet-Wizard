package com.example.walletwizard.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.walletwizard.MainActivity;
import com.example.walletwizard.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CreditsFragment extends Fragment {
    private View rootView;
    private Context context;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        return rootView = inflater.inflate(R.layout.fragment_credits, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        setGoBackButton();
    }

    private void setGoBackButton() {
        rootView.findViewById(R.id.credits_go_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                MainActivity mainActivity = ((MainActivity) getActivity());
                mainActivity.changeFragment(mainActivity.homeButtonContainer, mainActivity.homeButton, new CreditsFragment(), 0);
                requireActivity().onBackPressed();
            }
        });
    }
}