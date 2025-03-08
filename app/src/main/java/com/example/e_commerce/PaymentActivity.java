package com.example.e_commerce;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_payment); // Make sure to replace this with your actual layout file name

        // Show a Toast message saying "Payment coming soon"
        Toast.makeText(this, "Payment coming soon", Toast.LENGTH_SHORT).show();
    }
}
