package com.example.ethereumwallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    private static final String CORRECT_PASSWORD = "password"; // Correct password

    private EditText etPassword;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // Initialize views
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Set up the button click listener
        btnSubmit.setOnClickListener(v -> validatePassword());
    }

    private void validatePassword() {
        String enteredPassword = etPassword.getText().toString().trim();

        if (enteredPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else if (enteredPassword.equals(CORRECT_PASSWORD)) {
            // If the password is correct, navigate to MainActivity
            Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the password screen
        } else {
            Toast.makeText(this, "Incorrect password, please try again", Toast.LENGTH_SHORT).show();
        }
    }
}
