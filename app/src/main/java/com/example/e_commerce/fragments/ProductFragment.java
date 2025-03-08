package com.example.e_commerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_commerce.R;
import com.example.e_commerce.adapters.ProductAdapter;
import com.example.e_commerce.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private String category;

    public static ProductFragment newInstance(String category) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category");
        }
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        setupRecyclerView(view);
        loadProducts();
        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.productsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(productList, this, requireContext());
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        db.collection("products")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                productList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    product.setId(document.getId());
                    productList.add(product);
                }
                adapter.notifyDataSetChanged();
            });
    }

    @Override
    public void onProductClick(Product product) {
        // Handle product click if needed
    }

    private void addToCart(Product product) {
        // TODO: Implement add to cart functionality
        Toast.makeText(getContext(), "Added to cart: " + product.getName(),
                Toast.LENGTH_SHORT).show();
    }
} 