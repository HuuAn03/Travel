package fpt.edu.vn.assigment_travelapp.ui.addcard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentAddCardBinding;

public class AddCardFragment extends Fragment {

    private FragmentAddCardBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddCardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupCardNumberFormatter();
        setupExpiryDateFormatter();
        setupClickListeners();

        return root;
    }

    private void setupCardNumberFormatter() {
        binding.etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Format card number with spaces (e.g., 1234 5678 9012 3456)
                String input = s.toString().replaceAll(" ", "");
                if (input.length() > 0 && input.length() % 4 == 0 && before > 0) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i += 4) {
                        if (i > 0) formatted.append(" ");
                        formatted.append(input.substring(i, Math.min(i + 4, input.length())));
                    }
                    binding.etCardNumber.removeTextChangedListener(this);
                    binding.etCardNumber.setText(formatted);
                    binding.etCardNumber.setSelection(formatted.length());
                    binding.etCardNumber.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupExpiryDateFormatter() {
        binding.etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().replaceAll("/", "");
                if (input.length() == 2 && before < 2) {
                    binding.etExpiryDate.removeTextChangedListener(this);
                    binding.etExpiryDate.setText(input + "/");
                    binding.etExpiryDate.setSelection(input.length() + 1);
                    binding.etExpiryDate.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        binding.btnAddCard.setOnClickListener(v -> {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // TODO: Save card to database/repository
            // For now, just go back
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (binding.etCardNumber.getText().toString().trim().length() < 16) {
            binding.etCardNumber.setError("Please enter a valid card number");
            isValid = false;
        }

        if (binding.etCardHolderName.getText().toString().trim().isEmpty()) {
            binding.etCardHolderName.setError("Please enter card holder name");
            isValid = false;
        }

        if (binding.etEmail.getText().toString().trim().isEmpty() ||
            !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.getText().toString()).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (binding.etExpiryDate.getText().toString().trim().length() != 5) {
            binding.etExpiryDate.setError("Please enter valid expiry date (MM/YY)");
            isValid = false;
        }

        if (binding.etCvv.getText().toString().trim().length() < 3) {
            binding.etCvv.setError("Please enter valid CVV");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

