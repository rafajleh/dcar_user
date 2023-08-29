package com.elluminati.eber.driver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.models.datamodels.City;
import com.elluminati.eber.driver.models.datamodels.Oprators;

import java.util.ArrayList;

public class OpratorAdapter extends RecyclerView.Adapter<OpratorAdapter.CityViewHolder> implements
        Filterable {

    private ArrayList<Oprators> cityList;
    private Filter filter;

    public OpratorAdapter( ArrayList<Oprators> cityList) {
        this.cityList = cityList;
    }

    @Override
    public OpratorAdapter.CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_name_code,
                parent, false);
        OpratorAdapter.CityViewHolder countryViewHolder = new OpratorAdapter.CityViewHolder(view);

        return countryViewHolder;
    }

    @Override
    public void onBindViewHolder(OpratorAdapter.CityViewHolder holder, int position) {

        holder.tvCityName.setText(cityList.get(position).getFirstname());
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new OpratorAdapter.AppFilter(cityList);
        return filter;
    }

    protected class CityViewHolder extends RecyclerView.ViewHolder {
        MyFontTextView tvCityName;
        View viewDive;

        public CityViewHolder(View itemView) {
            super(itemView);

            tvCityName = (MyFontTextView) itemView.findViewById(R.id.tvItemCountryName);
            viewDive = itemView.findViewById(R.id.viewDiveCity);

        }
    }


    private class AppFilter extends Filter {

        private ArrayList<Oprators> sourceObjects;

        public AppFilter(ArrayList<Oprators> objects) {

            sourceObjects = new ArrayList<Oprators>();
            synchronized (this) {
                sourceObjects.addAll(objects);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString();
            FilterResults result = new FilterResults();
            if (filterSeq.length() > 0) {
                ArrayList<Oprators> filter = new ArrayList<Oprators>();
                for (Oprators city : sourceObjects) {
                    // the filtering itself:
                    if (city.getFirstname().toUpperCase().startsWith(filterSeq.toUpperCase()))
                        filter.add(city);
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                // add all objects
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.

            if (results.count != 0) {
                cityList = (ArrayList<Oprators>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    public ArrayList<Oprators> getFilterResult() {
        return cityList;
    }
}
