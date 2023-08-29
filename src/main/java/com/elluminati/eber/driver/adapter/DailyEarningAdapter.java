package com.elluminati.eber.driver.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.models.datamodels.DailyEarning;

import java.util.ArrayList;

/**
 * Created by elluminati on 21-Aug-17.
 */

public abstract class DailyEarningAdapter extends RecyclerView.Adapter<DailyEarningAdapter.MyViewHolder> {

    private ArrayList<DailyEarning> listDailyEarning;


    public DailyEarningAdapter(ArrayList<DailyEarning> listDailyEarning){
        this.listDailyEarning = listDailyEarning;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_earning
                , parent , false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final DailyEarning dailyEarning = listDailyEarning.get(position);

        holder.tvDailyItemDate.setText(dailyEarning.getDate());
        holder.tvDailyItemAmount.setText(dailyEarning.getCurrency()+""+dailyEarning.getAmount());

        holder.llDailyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDailyItemClicked(dailyEarning.getDate());
            }
        });

    }

    @Override
    public int getItemCount() {
        return listDailyEarning.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private MyFontTextView tvDailyItemDate , tvDailyItemAmount;
        private LinearLayout llDailyItem;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvDailyItemAmount = (MyFontTextView)itemView.findViewById(R.id.tvDailyItemAmount);
            tvDailyItemDate = (MyFontTextView)itemView.findViewById(R.id.tvDailyItemDate);
            llDailyItem = (LinearLayout)itemView.findViewById(R.id.llDailyItem);
        }
    }


    public abstract void onDailyItemClicked(String date);
}
