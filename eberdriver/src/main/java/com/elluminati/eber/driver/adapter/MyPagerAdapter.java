package com.elluminati.eber.driver.adapter;

// src/main/java/com/example/myapp/MyPagerAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elluminati.eber.driver.R;

public class MyPagerAdapter extends RecyclerView.Adapter<MyPagerAdapter.ViewHolder> {
    private final int itemCount;

    public MyPagerAdapter(int itemCount) {
        this.itemCount = itemCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind your data here
        holder.textViewPager.setText("Current view Pager " + position);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewPager;
        ViewHolder(View view) {
            super(view);
            textViewPager = view.findViewById(R.id.textViewPager);
        }
    }
}
