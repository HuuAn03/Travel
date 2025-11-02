package fpt.edu.vn.assigment_travelapp.ui.paymentcompleted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentPaymentCompletedBinding;

public class PaymentCompletedFragment extends Fragment {

    private FragmentPaymentCompletedBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPaymentCompletedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupClickListeners();

        return root;
    }

    private void setupClickListeners() {
        binding.btnContinue.setOnClickListener(v -> {
            // Navigate to My Trips to see the booking
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_paymentCompletedFragment_to_navigation_my_trip);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

