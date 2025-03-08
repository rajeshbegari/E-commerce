package com.example.e_commerce.models;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_commerce.R;

public class ManageOrdersActivity extends AppCompatActivity {

    private TextView comingSoonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);


        comingSoonTextView = findViewById(R.id.tvComingSoon);


        comingSoonTextView.setText("Orders Coming Soon!");
    }
}
