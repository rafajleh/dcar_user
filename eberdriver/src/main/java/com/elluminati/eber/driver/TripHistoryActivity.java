package com.elluminati.eber.driver;

import android.app.DatePickerDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elluminati.eber.driver.adapter.TripHistoryAdaptor;
import com.elluminati.eber.driver.components.MyTitleFontTextView;
import com.elluminati.eber.driver.models.datamodels.TripHistory;
import com.elluminati.eber.driver.models.responsemodels.TripHistoryResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.ImageHelper;
import com.elluminati.eber.driver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripHistoryActivity extends BaseAppCompatActivity {

    public ImageHelper imageHelper;
    private DatePickerDialog.OnDateSetListener fromDateSet, toDateSet;
    private String userDate;
    private int day;
    private int month;
    private int year;
    private ImageView ivSearchHistory;
    private TextView tvFromDate, tvToDate;
    private Calendar calendar;
    private ArrayList<TripHistory> tripHistoryList, tripHistoryShortList;
    private TreeSet<Integer> separatorSet;
    private ArrayList<Date> dateList;
    private RecyclerView rcvHistory;
    private LinearLayout rlFromDate, rlToDate;
    private boolean isFromDateSet, isToDateSet;
    private long fromDateSetTime;
    private TripHistoryAdaptor tripHistoryAdaptor;
    private MyTitleFontTextView tvNoItemHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);
        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.text_trip_history));
        setToolbarIcon(new BitmapDrawable(getResources(), Utils.vectorToBitmap(this, R.drawable
                .refresh)), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFromDate.setText(getResources().getString
                        (R.string.text_from));
                tvToDate.setText(getResources().getString
                        (R.string.text_to));
                calendar.setTimeInMillis(System.currentTimeMillis());
                fromDateSetTime = 0;
                getTripHistory("", "");
            }
        });

        imageHelper = new ImageHelper(this);
        tvNoItemHistory = (MyTitleFontTextView) findViewById(R.id.tvNoItemHistory);

        rlFromDate = findViewById(R.id.rlFromDate);
        rlToDate = findViewById(R.id.rlToDate);
        rlFromDate.setOnClickListener(this);
        rlToDate.setOnClickListener(this);

        ivSearchHistory = (ImageView) findViewById(R.id.ivSearchHistory);
        ivSearchHistory.setOnClickListener(this);

        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);

        rcvHistory = (RecyclerView) findViewById(R.id.history_recycler_view);
        rcvHistory.setLayoutManager(new LinearLayoutManager(this));

        tripHistoryList = new ArrayList<>();
        tripHistoryShortList = new ArrayList<>();
        separatorSet = new TreeSet<>();
        dateList = new ArrayList<>();


        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);


        fromDateSet = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                userDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                calendar.clear();
                calendar.set(year, monthOfYear, dayOfMonth);
                tvFromDate.setText(userDate);
                fromDateSetTime = calendar.getTimeInMillis();
                isFromDateSet = true;

            }
        };
        toDateSet = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                userDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                calendar.clear();
                calendar.set(year, monthOfYear, dayOfMonth);
                tvToDate.setText(userDate);
                isToDateSet = true;

            }
        };

        getTripHistory("", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
        onBackPressed();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSearchHistory:
                if (!TextUtils.equals(tvFromDate.getText().toString(),
                        getResources().getString(R.string
                        .text_from)) && !TextUtils.equals(tvToDate.getText().toString(),
                        getResources().getString(R.string.text_to))) {
                    getTripHistory(tvFromDate.getText().toString(), tvToDate.getText().toString());
                } else {
                    if (TextUtils.equals(tvFromDate.getText().toString(), getResources().getString(R
                            .string.text_from)) && TextUtils.equals(tvToDate.getText().toString(),
                            getResources().getString(R.string.text_to))) {
                        Utils.showToast(getResources().getString(R.string.msg_plz_select_valid_date), this);
                    } else {
                        if (TextUtils.equals(tvFromDate.getText().toString(),
                                getResources().getString(R
                                .string.text_from))) {
                            Utils.showToast(getResources().getString(R.string.msg_plz_select_from_date),
                                    this);
                        } else {
                            Utils.showToast(getResources().getString(R.string.msg_plz_select_to_date),
                                    this);
                        }
                    }
                }
                break;
            case R.id.rlFromDate:
                openFromDatePicker();
                break;
            case R.id.rlToDate:
                openToDatePicker();
                break;
            default:
                // do with default
                break;
        }

    }

    private void getTripHistory(String startDate, String endDate) {
        JSONObject jsonObject = new JSONObject();
        Utils.showCustomProgressDialog(this, getResources().getString(R.string
                .msg_waiting_for_history), false, null);

        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.accumulate(Const.Params.START_DATE, startDate);
            jsonObject.accumulate(Const.Params.END_DATE, endDate);

            Call<TripHistoryResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getTripHistory(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<TripHistoryResponse>() {
                @Override
                public void onResponse(Call<TripHistoryResponse> call,
                                       Response<TripHistoryResponse> response) {

                    if (parseContent.isSuccessful(response)) {
                        tripHistoryList.clear();
                        if (response.body().isSuccess()) {
                            tripHistoryList.clear();
                            tripHistoryList.addAll(response.body().getTrips());
                            tripHistoryShortList = getShortHistoryList(tripHistoryList);
                            if (tripHistoryAdaptor == null) {
                                tripHistoryAdaptor =
                                        new TripHistoryAdaptor(TripHistoryActivity.this,
                                                tripHistoryShortList,
                                                separatorSet);
                                rcvHistory.setAdapter(tripHistoryAdaptor);
                            } else {
                                tripHistoryAdaptor.notifyDataSetChanged();
                            }
                            updateUi(tripHistoryList.size() > 0);
                            Utils.hideCustomProgressDialog();
                        } else {
                            updateUi(false);
                            Utils.hideCustomProgressDialog();
                        }
                    }


                }

                @Override
                public void onFailure(Call<TripHistoryResponse> call, Throwable t) {
                    AppLog.handleThrowable(TripHistoryActivity.class.getSimpleName(), t);

                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.TRIP_HISTORY_ACTIVITY, e);
        }

    }

    /**
     * This method is give arrayList which have unique date arrayList and is also add Date list
     * in treeSet
     *
     * @param tripHistoryList
     * @return ArrayList tripHistoryShortList
     */
    private ArrayList<TripHistory> getShortHistoryList(ArrayList<TripHistory> tripHistoryList) {
        tripHistoryShortList.clear();
        dateList.clear();
        separatorSet.clear();
        try {
            SimpleDateFormat sdf = parseContent.dateFormat;
            final Calendar cal = Calendar.getInstance();

            Collections.sort(tripHistoryList, new Comparator<TripHistory>() {
                @Override
                public int compare(TripHistory lhs, TripHistory rhs) {
                    return compareTwoDate(lhs.getUserCreateTime(), rhs.getUserCreateTime());
                }
            });

            HashSet<Date> listToSet = new HashSet<Date>();
            for (int i = 0; i < tripHistoryList.size(); i++) {

                if (listToSet.add(sdf.parse(tripHistoryList.get(i).getUserCreateTime()))) {
                    dateList.add(sdf.parse(tripHistoryList.get(i).getUserCreateTime()));
                }

            }

            for (int i = 0; i < dateList.size(); i++) {

                cal.setTime(dateList.get(i));
                TripHistory item = new TripHistory();
                item.setUserCreateTime(parseContent.webFormat.format(dateList.get(i)));
                tripHistoryShortList.add(item);

                separatorSet.add(tripHistoryShortList.size() - 1);
                for (int j = 0; j < tripHistoryList.size(); j++) {
                    Calendar messageTime = Calendar.getInstance();
                    messageTime.setTime(sdf.parse(tripHistoryList.get(j).getUserCreateTime()));
                    if (cal.getTime().compareTo(messageTime.getTime()) == 0) {
                        tripHistoryShortList.add(tripHistoryList.get(j));
                    }
                }
            }
        } catch (ParseException e) {
            AppLog.handleException(TripHistoryActivity.class.getSimpleName(), e);
        }
        updateUi(!tripHistoryShortList.isEmpty());
        return tripHistoryShortList;
    }

    private int compareTwoDate(String firstStrDate, String secondStrDate) {
        try {
            SimpleDateFormat dateFormat = parseContent.webFormat;
            Date date2 = dateFormat.parse(secondStrDate);
            Date date1 = dateFormat.parse(firstStrDate);
            return date2.compareTo(date1);
        } catch (ParseException e) {
            AppLog.handleException(TripHistoryActivity.class.getSimpleName(), e);
        }
        return 0;
    }


    /**
     * This method is give array list of between two date to set on adaptor
     *
     * @param fromDate
     * @param toDate
     * @return ArrayList particularDateHistoryList
     */

    private ArrayList<TripHistory> getParticularDateHistoryList(ArrayList<TripHistory>
                                                                        tripHistoryList, String
                                                                        fromDate, String toDate) {
        ArrayList<TripHistory> particularDateHistoryList = new ArrayList<>();
        try {
            Date d1 = null, createDate = null, dateStart = null, dateEnd = null;
            SimpleDateFormat f = parseContent.dateFormat;
            dateStart = f.parse(fromDate);
            dateEnd = f.parse(toDate);
            for (int i = 0; i < tripHistoryList.size(); i++) {
                String tripCreateDate = tripHistoryList.get(i).getUserCreateTime();
                d1 = parseContent.webFormat.parse(tripCreateDate);
                createDate = parseContent.dateFormat.parse(parseContent.dateFormat.format(d1));
                if (createDate.compareTo(dateStart) >= 0 && createDate.compareTo(dateEnd) <= 0) {
                    particularDateHistoryList.add(tripHistoryList.get(i));
                }
            }
        } catch (ParseException e) {
            AppLog.handleException(Const.Tag.TRIP_HISTORY_ACTIVITY, e);
        }
        return particularDateHistoryList;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void openFromDatePicker() {
        DatePickerDialog fromPiker = new DatePickerDialog(this, fromDateSet, year, month, day);
        if (isToDateSet) {
            fromPiker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        } else {
            fromPiker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        }
        fromPiker.show();
    }

    private void openToDatePicker() {

        DatePickerDialog toPiker = new DatePickerDialog(this, toDateSet, year, month, day);
        if (isFromDateSet) {
            toPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
            toPiker.getDatePicker().setMinDate(fromDateSetTime);
        } else {
            toPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        toPiker.show();
    }

    private void updateUi(boolean isUpdate) {
        if (isUpdate) {
            tvNoItemHistory.setVisibility(View.GONE);
            rcvHistory.setVisibility(View.VISIBLE);
        } else {
            tvNoItemHistory.setVisibility(View.VISIBLE);
            rcvHistory.setVisibility(View.GONE);
        }


    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogInternet();
        } else {
            openInternetDialog();
        }
    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogGps();
        } else {
            openGpsDialog();

        }
    }

    @Override
    public void onAdminApproved() {
        goWithAdminApproved();
    }

    @Override
    public void onAdminDeclined() {
        goWithAdminDecline();
    }

    @Override
    public void onprofileApprove() {

    }
}

