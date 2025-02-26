package com.example.e_commerce;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_commerce.adapters.UserAdapter;
import com.example.e_commerce.models.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private TextInputEditText searchInput;
    private UserAdapter adapter;
    private FirebaseFirestore db;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(userList, this::deleteUser);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        db.collection("users")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    userList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        if (!user.isAdmin()) { // Don't show admin users
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.updateList(filteredList);
    }

    private void deleteUser(User user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete user from Authentication
                    FirebaseAuth.getInstance().getCurrentUser().delete()
                            .addOnSuccessListener(aVoid -> {
                                // Delete user from Firestore
                                db.collection("users").document(user.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid1 -> 
                                            Toast.makeText(this, "User deleted successfully",
                                                    Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> 
                                            Toast.makeText(this, "Failed to delete user data",
                                                    Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this,
                                    "Failed to delete user account",
                                    Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 