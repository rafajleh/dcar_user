package com.elluminati.eber.driver;

import android.os.Bundle;
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
import androidx.viewpager2.widget.ViewPager2;

import com.elluminati.eber.driver.adapter.MyPagerAdapter;
import com.elluminati.eber.driver.interfaces.OTPListener;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Register_Step_Input extends BaseAppCompatActivity {
    ViewPager2 viewPager;
    LinearLayout dotShowLayout;

    int currentViewPagerPos;
    FloatingActionButton buttonNext,btnPrevScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step_input);
        CurrentTrip.getInstance().clear();
//tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager2);
        dotShowLayout = findViewById(R.id.dotShowLayout);
        buttonNext = findViewById(R.id.buttonNext);
        btnPrevScreen = findViewById(R.id.btnPrevScreen);



        viewPager.setAdapter(new MyPagerAdapter(6)); // Set the number of pages
        viewPager.setUserInputEnabled(false);

        currentViewPagerPos = viewPager.getCurrentItem();


        addDotImageInHeader();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentViewPagerPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });


        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentViewPagerPos >= 6){
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