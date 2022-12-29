package com.example.trymapbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.trymapbox.databinding.FragmentSelectionExampleBinding;

public class SelectionExampleFragment extends Fragment {

    private FragmentSelectionExampleBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSelectionExampleBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonMyLocationExample.setOnClickListener(view1 ->
                Navigation.findNavController(view1).navigate(R.id.action_SelectionExampleFragment_to_ShowMyLocationFragment));
        binding.searchPlace.setOnClickListener(view1 ->
                Navigation.findNavController(view1).navigate(R.id.action_SelectionExampleFragment_to_searchPlaceFragment));
        binding.requestAndDrawRoute.setOnClickListener(view1 ->
                Navigation.findNavController(view1).navigate(R.id.action_SelectionExampleFragment_to_routeFragment));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}