package com.elluminati.eber.driver.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.adapter.CountryAdapter;
import com.elluminati.eber.driver.interfaces.ClickListener;
import com.elluminati.eber.driver.interfaces.RecyclerTouchListener;
import com.elluminati.eber.driver.models.datamodels.Country;

import java.util.ArrayList;

/**
 * Created by elluminati on 04-08-2016.
 */
public abstract class CustomCountryDialog extends Dialog {

    private RecyclerView rcvCountryCode;
    private MyFontEdittextView etCountrySearch;
    private CountryAdapter countryAdapter;
    private SearchView searchView;
    private TextView tvDialogTitle;
    private ArrayList<Country> codeArrayList;
    private Context context;

    public CustomCountryDialog(Context context, ArrayList<Country> countryList) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_country_code);
        codeArrayList = countryList;
        rcvCountryCode = (RecyclerView) findViewById(R.id.rcvCountryCode);
        etCountrySearch = (MyFontEdittextView) findViewById(R.id.etCountrySearch);
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText(context.getResources().getString(R.string.text_country_codes));
        rcvCountryCode.setLayoutManager(new LinearLayoutManager(context));
        countryAdapter = new CountryAdapter(countryList);
        rcvCountryCode.setAdapter(countryAdapter);
        this.context = context;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etCountrySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (countryAdapter != null) {
                    countryAdapter.getFilter().filter(s);
                } else {
                    Log.d("filter", "no filter availible");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rcvCountryCode.addOnItemTouchListener(new RecyclerTouchListener(context, rcvCountryCode,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        onSelect(position, countryAdapter.getFilterResult());
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));
    }

    public abstract void onSelect(int position, ArrayList<Country> filterList);

}
