package com.example.e_commerce.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.e_commerce.fragments.ProductFragment;

public class CategoryPagerAdapter extends FragmentStateAdapter {
    private final String[] categories;

    public CategoryPagerAdapter(FragmentActivity fragmentActivity, String[] categories) {
        super(fragmentActivity);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ProductFragment.newInstance(categories[position]);
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }
} 