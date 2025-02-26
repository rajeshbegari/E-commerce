package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView forgotPasswordText;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginButton = view.findViewById(R.id.loginButton);
        forgotPasswordText = view.findViewById(R.id.forgotPasswordText);

        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());

        return view;
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Check if user is admin
                    if (email.equals("admin@ecommerce.com")) {
                        startActivity(new Intent(getContext(), AdminActivity.class));
                    } else {
                        startActivity(new Intent(getContext(), MainActivity.class));
                    }
                    getActivity().finish();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), 
                    "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showForgotPasswordDialog() {
        EditText emailInput = new EditText(getContext());
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Enter your email");
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        emailInput.setPadding(padding, padding, padding, padding);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Password")
                .setView(emailInput)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!email.isEmpty()) {
                        resetPassword(email);
                    } else {
                        Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Password reset email sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to send reset email: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }
} 