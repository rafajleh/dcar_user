package com.elluminati.eber.driver.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.elluminati.eber.driver.fragments.RegisterStepEmailAddress;
import com.elluminati.eber.driver.fragments.RegisterStepFirstName;
import com.elluminati.eber.driver.fragments.RegisterStepLastName;
import com.elluminati.eber.driver.fragments.RegisterStepOperator;
import com.elluminati.eber.driver.fragments.RegisterStepPCOLicence;
import com.elluminati.eber.driver.fragments.RegisterStepMobileNo;

import java.util.HashMap;
import java.util.Map;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                RegisterStepFirstName fragment = new RegisterStepFirstName();
                fragmentMap.put(position, fragment);
                return fragment;
                //return new RegisterStepFirstName();
            case 1:
                RegisterStepLastName fragment2 = new RegisterStepLastName();
                fragmentMap.put(position, fragment2);
                return fragment2;
               // return new RegisterStepLastName();
            case 2:
                RegisterStepEmailAddress fragment3 = new RegisterStepEmailAddress();
                fragmentMap.put(position, fragment3);
                return fragment3;
            case 3:
                RegisterStepMobileNo fragment4 = new RegisterStepMobileNo();
                fragmentMap.put(position, fragment4);
                return fragment4;
            case 4:
                RegisterStepOperator fragment5 = new RegisterStepOperator();
                fragmentMap.put(position, fragment5);
                return fragment5;
            case 5:
                RegisterStepPCOLicence fragment6 = new RegisterStepPCOLicence();
                fragmentMap.put(position, fragment6);
                return fragment6;
            default:
                RegisterStepFirstName fragment7 = new RegisterStepFirstName();
                fragmentMap.put(position, fragment7);
                return fragment7;
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
    public Fragment getFragmentAt(int position) {
        return fragmentMap.get(position);
    }
}
