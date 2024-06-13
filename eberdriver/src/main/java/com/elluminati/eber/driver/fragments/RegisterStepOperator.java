package com.elluminati.eber.driver.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.Register_Step_Input;
import com.elluminati.eber.driver.components.MyFontEdittextView;

public class RegisterStepOperator extends Fragment {
    private EditText editText;
    private ImageView slider_edt_operator_img;

    Register_Step_Input registerStepInput;

    Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.activity_register_step_input, container, false);
        View view = inflater.inflate(R.layout.activity_register_step_operator, container, false);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        editText = view.findViewById(R.id.slider_edt_operator);
        slider_edt_operator_img = view.findViewById(R.id.slider_edt_operator_img);
        slider_edt_operator_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setAlpha(1.0f);
                        break;
                }
                return false;
            }
        });

        //spinner
        Spinner mySpinner = view.findViewById(R.id.slider_spinner_operator);

        if (getContext() != null) {
            if (getContext() != null) {
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.register_operator_spinner_items, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Apply the adapter to the spinner
                mySpinner.setAdapter(adapter);

                // Set a listener to handle selection events
                mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item
                        String selectedItem = (String) parentView.getItemAtPosition(position);

                        // Display the selected item in a Toast
                        editText.setText(selectedItem.toString());
                        Toast.makeText(getActivity(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();

                        // You can set the selected data to another view or variable here
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing
                    }
                });
            } else {
                System.out.println("Context is null. Cannot set adapter.");
            }
        }


    }

    public String getText() {
        if (editText != null) {
            return editText.getText().toString();
        } else {
            return "";
        }
    }

}