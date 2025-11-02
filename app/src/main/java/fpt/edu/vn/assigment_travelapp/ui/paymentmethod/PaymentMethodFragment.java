package fpt.edu.vn.assigment_travelapp.ui.paymentmethod;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentPaymentMethodBinding;

public class PaymentMethodFragment extends Fragment {

    private FragmentPaymentMethodBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPaymentMethodBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupClickListeners();
        setupRadioButtons();

        return root;
    }

    private void setupRadioButtons() {
        // Uncheck all when one is checked
        binding.cbPaypal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cbMastercard.setChecked(false);
            }
        });

        binding.cbMastercard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cbPaypal.setChecked(false);
            }
        });
    }

    private void setupClickListeners() {
        binding.cardAddPayment.setOnClickListener(v -> {
            // Navigate to add card screen
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_paymentMethodFragment_to_addCardFragment);
        });

        binding.btnConfirmPayment.setOnClickListener(v -> {
            // Go back to checkout with selected payment method
            // In real app, you would save the selected payment method
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

