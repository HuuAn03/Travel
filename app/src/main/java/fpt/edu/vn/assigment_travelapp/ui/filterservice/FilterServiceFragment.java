package fpt.edu.vn.assigment_travelapp.ui.filterservice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;

import java.util.Arrays;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentFilterServiceBinding;

public class FilterServiceFragment extends Fragment {

    private FragmentFilterServiceBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilterServiceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupFilterChips();
        setupPriceSlider();

        return root;
    }

    private void setupFilterChips() {
        List<String> filters = Arrays.asList(
                "Hotels (340)", "Swimming Pool (340)", "5 stars (100)",
                "Private Bathroom (200)", "Breakfast Included (115)", "Kitchen (10)"
        );

        for (int i = 0; i < filters.size(); i++) {
            Chip chip = new Chip(getContext());
            chip.setText(filters.get(i));
            chip.setCheckable(true);
            chip.setChecked(i == 1); // Swimming Pool is selected by default
            binding.flexboxFilters.addView(chip);
        }
    }

    private void setupPriceSlider() {
        binding.sliderPrice.setValues(40f, 120f);
        binding.sliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && slider.getValues().size() >= 2) {
                float min = slider.getValues().get(0);
                float max = slider.getValues().get(1);
                binding.tvPriceRange.setText(String.format("$%.0f - $%.0f", min, max));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

