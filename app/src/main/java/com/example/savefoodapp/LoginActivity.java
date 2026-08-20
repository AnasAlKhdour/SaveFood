package com.example.savefoodapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.savefoodapp.utils.SessionManager;

import com.example.savefoodapp.security.PasswordUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private SessionManager sessionManager;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        LoginActivity.this,
                        RegisterActivity.class
                );

                startActivity(intent);
            }
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        // Open database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT id, name, email, role, password_hash, password_salt " +
                "FROM users " +
                "WHERE email = ?";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{email}
        );

        if (cursor.moveToFirst()) {

            int userId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            String role = cursor.getString(
                    cursor.getColumnIndexOrThrow("role")
            );

            String storedHash = cursor.getString(
                    cursor.getColumnIndexOrThrow("password_hash")
            );

            String storedSalt = cursor.getString(
                    cursor.getColumnIndexOrThrow("password_salt")
            );

            boolean passwordCorrect = PasswordUtils.verifyPassword(
                    password,
                    storedSalt,
                    storedHash
            );

            if (!passwordCorrect) {

                cursor.close();
                db.close();

                Toast.makeText(
                        LoginActivity.this,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            cursor.close();
            db.close();

            sessionManager.createSession(
                    userId,
                    name,
                    email,
                    role
            );

            Toast.makeText(
                    LoginActivity.this,
                    "Welcome " + name,
                    Toast.LENGTH_SHORT
            ).show();


            // Role-based navigation

            if (role.equals("Food Institution")) {

                Intent intent = new Intent(
                        LoginActivity.this,
                        FoodHomeActivity.class
                );

                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", name);
                intent.putExtra("USER_EMAIL", email);
                intent.putExtra("USER_ROLE", role);

                startActivity(intent);

            } else if (role.equals("Charity Organization")) {

                Intent intent = new Intent(
                        LoginActivity.this,
                        CharityHomeActivity.class
                );

                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", name);
                intent.putExtra("USER_EMAIL", email);
                intent.putExtra("USER_ROLE", role);

                startActivity(intent);

            } else {

                Toast.makeText(
                        LoginActivity.this,
                        "Invalid user role",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            finish();


        } else {

            cursor.close();
            db.close();

            Toast.makeText(
                    LoginActivity.this,
                    "Invalid email or password",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}