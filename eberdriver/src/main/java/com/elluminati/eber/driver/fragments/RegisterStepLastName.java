package com.elluminati.eber.driver.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.components.MyFontEdittextView;

public class RegisterStepLastName extends Fragment {
    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.activity_register_step_input, container, false);
        View view = inflater.inflate(R.layout.activity_register_step_last_name, container, false);
        editText = view.findViewById(R.id.slider_edt_last_name);
        //editText = view.findViewById(R.id.slider_edt_1);
        return view;

    }

public String getText() {
        if (editText != null) {
            return editText.getText().toString();
        } else {
            return "";
        }
    }
}
