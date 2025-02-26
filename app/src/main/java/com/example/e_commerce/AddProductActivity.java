package com.example.e_commerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {
    private ImageView productImage;
    private TextInputEditText productNameInput, priceInput, descriptionInput, stockInput;
    private AutoCompleteTextView categoryDropdown;
    private MaterialButton selectImageButton, addProductButton;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Toolbar toolbar;
    
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    productImage.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        setupToolbar();
        initializeViews();
        setupCategoryDropdown();
        setupClickListeners();
        
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        productImage = findViewById(R.id.productImage);
        productNameInput = findViewById(R.id.productNameInput);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        stockInput = findViewById(R.id.stockInput);
        selectImageButton = findViewById(R.id.selectImageButton);
        addProductButton = findViewById(R.id.addProductButton);
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Men's Clothing", "Women's Clothing", "Kids' Clothing", 
                             "Accessories", "Footwear"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(adapter);
    }

    private void setupClickListeners() {
        selectImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        addProductButton.setOnClickListener(v -> validateAndAddProduct());
    }

    private void validateAndAddProduct() {
        String name = productNameInput.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String stockStr = stockInput.getText().toString().trim();

        if (selectedImageUri == null || name.isEmpty() || category.isEmpty() || 
            priceStr.isEmpty() || description.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or stock value", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndProduct(name, category, price, description, stock);
    }

    private void uploadImageAndProduct(String name, String category, double price, 
                                     String description, int stock) {
        String imageFileName = "products/" + UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child(imageFileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("name", name);
                        product.put("category", category);
                        product.put("price", price);
                        product.put("description", description);
                        product.put("stock", stock);
                        product.put("imageUrl", uri.toString());

                        db.collection("products")
                                .add(product)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(AddProductActivity.this, 
                                            "Product added successfully", 
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(AddProductActivity.this,
                                        "Failed to add product: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(AddProductActivity.this,
                        "Failed to upload image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }
} 