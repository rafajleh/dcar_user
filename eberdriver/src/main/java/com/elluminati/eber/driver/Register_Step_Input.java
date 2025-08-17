package com.elluminati.eber.driver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.elluminati.eber.driver.adapter.MyPagerAdapter;
import com.elluminati.eber.driver.adapter.ViewPagerAdapter;
import com.elluminati.eber.driver.fragments.RegisterStepAddress;
import com.elluminati.eber.driver.fragments.RegisterStepEmailAddress;
import com.elluminati.eber.driver.fragments.RegisterStepFirstName;
import com.elluminati.eber.driver.fragments.RegisterStepLastName;
import com.elluminati.eber.driver.fragments.RegisterStepMobileNo;
import com.elluminati.eber.driver.fragments.RegisterStepOperator;
import com.elluminati.eber.driver.fragments.RegisterStepPCOLicence;
import com.elluminati.eber.driver.interfaces.OTPListener;
import com.elluminati.eber.driver.models.datamodels.RegisterDataModel;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Register_Step_Input extends BaseAppCompatActivity {
    ViewPager2 viewPager;
    LinearLayout dotShowLayout;
    //RegisterDataModel registerDataModel;
    ViewPagerAdapter adapter;
    int currentViewPagerPos;
    RegisterDataModel values =new RegisterDataModel();
    FloatingActionButton buttonNext,btnPrevScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step_input);
        CurrentTrip.getInstance().clear();
        //registerDataModel = new RegisterDataModel();
//tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager2);
        dotShowLayout = findViewById(R.id.dotShowLayout);
        buttonNext = findViewById(R.id.buttonNext);
        btnPrevScreen = findViewById(R.id.btnPrevScreen);




        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);



//        viewPager.setAdapter(new MyPagerAdapter(6)); // Set the number of pages
//        viewPager.setUserInputEnabled(false);


        currentViewPagerPos = viewPager.getCurrentItem();


        addDotImageInHeader();

//        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                currentViewPagerPos = position;
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//            }
//        });





        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 getEditTextValues();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // Convert the ArrayList to JSON
                String json = gson.toJson(values);

                // Print the JSON string
                Log.d("", "EditTextValue: "+json);
                /*for (RegisterDataModel value : values) {
                    Log.d("EditTextValue", value.getFirstName());
                    //Log.d("EditTextValue", value.getLastName());
                }*/
                if(currentViewPagerPos >= 6){
                    Intent intent = new Intent(Register_Step_Input.this, RegisterActivity.class);
                    intent.putExtra("reg_model", values);
                    startActivity(intent);
                    return;
                }

                currentViewPagerPos = (currentViewPagerPos + 1);
                viewPager.setCurrentItem(currentViewPagerPos);
                addDotImageInHeader();

            }
        });

        btnPrevScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }



    public RegisterDataModel getEditTextValues() {

        for (int i = 0; i < adapter.getItemCount(); i++) {

            Fragment fragment = adapter.getFragmentAt(i);

            if (fragment instanceof RegisterStepFirstName) {
                String text = ((RegisterStepFirstName) fragment).getText();
                values.setFirstName(text);
                Log.d("EditTextValue", "firstname "+values.getFirstName());
            }
            else if (fragment instanceof RegisterStepLastName) {
                String text = ((RegisterStepLastName) fragment).getText();
                values.setLastName(text);
                Log.d("EditTextValue", "lastname "+values.getLastName());

            }
            else if (fragment instanceof RegisterStepEmailAddress) {
                String text = ((RegisterStepEmailAddress) fragment).getText();
                values.setEmailAddress(text);
                Log.d("EditTextValue", "email "+values.getEmailAddress());

            }
            else if (fragment instanceof RegisterStepMobileNo) {
                String text = ((RegisterStepMobileNo) fragment).getText();
                values.setMobileNumber(text);
                Log.d("EditTextValue", "mobile "+values.getMobileNumber());

            }
            /*else if (fragment instanceof RegisterStepOperator) {
                String text = ((RegisterStepOperator) fragment).getText();
                values.setOperator(text);
                Log.d("EditTextValue", "operator "+values.getOperator());

            }*/
            else if (fragment instanceof RegisterStepAddress) {
                String text = ((RegisterStepAddress) fragment).getText();
                values.setAddress(text);
                Log.d("EditTextValue", "email "+values.getAddress());

            }
            else if (fragment instanceof RegisterStepPCOLicence) {
                String text = ((RegisterStepPCOLicence) fragment).getText();
                values.setPcoLicence(text);
                Log.d("EditTextValue", "pco "+values.getPcoLicence());

            }
        }
        return values;
    }
    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {

    }

    public void addDotImageInHeader(){
        dotShowLayout.removeAllViews();


        for(int i = 0; i < 6; i++){
            ImageView imageView = new ImageView(this);


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(12, 12);
            layoutParams.setMargins(20,10,20,10);
            imageView.setLayoutParams(layoutParams);

            if(i <= currentViewPagerPos){
                imageView.setImageResource(R.drawable.dot_active);
            }
            else {
                imageView.setImageResource(R.drawable.dot_inactive);
            }


            dotShowLayout.addView(imageView);
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAdminApproved() {

    }

    @Override
    public void onAdminDeclined() {

    }

    @Override
    public void onprofileApprove() {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {

    }
}