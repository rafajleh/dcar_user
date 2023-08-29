package com.elluminati.eber.driver.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.adapter.CircularProgressViewAdapter;
import com.elluminati.eber.driver.adapter.InvoiceAdapter;
import com.elluminati.eber.driver.components.CustomCircularProgressView;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.MyFontTextViewMedium;
import com.elluminati.eber.driver.models.datamodels.CityType;
import com.elluminati.eber.driver.models.datamodels.Trip;
import com.elluminati.eber.driver.models.responsemodels.InvoiceResponse;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by elluminati on 26-07-2016.
 */
public class InvoiceFragment extends BaseFragments {


    private TextView tvPaymentWith, tvInvoiceNumber, tvInvoiceDistance, tvInvoiceTripTime,
            tvInvoiceTotal, tvTotalText;
    private String unit;
    private ImageView ivPaymentImage;
    private MyFontTextViewMedium tvInvoiceTripType;
    private MyFontTextView tvInvoiceMinFareApplied;
    private Dialog dialogProgress;
    private CustomCircularProgressView ivProgressBar;
    private RecyclerView rcvInvoice;
    private NumberFormat currencyFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable
                                     Bundle savedInstanceState) {
        View invoiceFrag = inflater.inflate(R.layout.fragment_invoice, container, false);
        getProviderInvoice();
        ivPaymentImage = (ImageView) invoiceFrag.findViewById(R.id.ivPaymentImage);
        tvPaymentWith = invoiceFrag.findViewById(R.id.tvPaymentWith);
        tvInvoiceNumber = invoiceFrag.findViewById(R.id.tvInvoiceNumber);

        tvInvoiceTripType = (MyFontTextViewMedium) invoiceFrag.findViewById(R.id.tvInvoiceTripType);
        tvInvoiceMinFareApplied = (MyFontTextView) invoiceFrag.findViewById(R.id
                .tvInvoiceMinFareApplied);
        tvInvoiceDistance = invoiceFrag.findViewById(R.id.tvInvoiceDistance);
        tvInvoiceTripTime = invoiceFrag.findViewById(R.id.tvInvoiceTripTime);
        tvInvoiceTotal = invoiceFrag.findViewById(R.id.tvInvoiceTotal);
        tvTotalText = invoiceFrag.findViewById(R.id.tvTotalText);
        rcvInvoice = invoiceFrag.findViewById(R.id.rcvInvoice);
        return invoiceFrag;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currencyFormat =
                drawerActivity.currencyHelper.getCurrencyFormat(drawerActivity.preferenceHelper.getCurrencyCode());
        tvTotalText.setVisibility(View.GONE);
        tvInvoiceTotal.setVisibility(View.GONE);
        drawerActivity.setToolbarBackgroundAndElevation(false, R.color.color_white, 0);
        drawerActivity.setScheduleListener(null);
        drawerActivity.preferenceHelper.putIsHaveTrip(false);
        drawerActivity.preferenceHelper.putLocationUniqueId(0);
        drawerActivity.setTitleOnToolbar(drawerActivity.getResources().getString(R.string.text_invoice));
        drawerActivity.setToolbarIcon(AppCompatResources.getDrawable(drawerActivity, R
                        .drawable.ic_done_black_24dp),
                new View
                        .OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        providerSubmitInvoice();
                    }
                });
        rcvInvoice.setLayoutManager(new LinearLayoutManager(drawerActivity));
        rcvInvoice.setNestedScrollingEnabled(false);
    }

    private void getProviderInvoice() {
        Utils.hideCustomProgressDialog();
        showCustomProgressDialog(drawerActivity);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());

            Call<InvoiceResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getInvoice(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<InvoiceResponse>() {
                @Override
                public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse>
                        response) {
                    if (ParseContent.getInstance().isSuccessful(response)) {
                        hideCustomProgressDialog();
                        if (response.body().isSuccess()) {
                            setInvoiceData(response.body().getTrip(), response.body()
                                    .getTripService());
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), drawerActivity);
                        }

                    }
                }

                @Override
                public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                    AppLog.handleThrowable(InvoiceFragment.class.getSimpleName(), t);

                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.INVOICE_FRAGMENT, e);
        }
    }


    @Override
    public void onClick(View v) {

    }

    public File getPathOfPdfFile() {
        File storageDir = new File(drawerActivity.imageHelper.getAlbumDir().getAbsolutePath(),
                drawerActivity.getResources().getString(R.string.text_invoice));

        if (storageDir != null) {
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d("PdfSample", "failed to create directory");
                    return null;
                }
            }
        }
        Date date = new Date();
        String timeStamp = drawerActivity.parseContent.dateFormat.format(date);
        timeStamp = timeStamp + "_" + drawerActivity.parseContent.timeFormat.format(date);
        String imageFileName = "Invoice_" + timeStamp + ".pdf";
        File imageF = new File(storageDir.getAbsolutePath(), imageFileName);
        return imageF;
    }

    private void createPdfFile(View view) {

        PdfDocument document = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(),
                    view.getHeight(), 1)
                    .create();
            PdfDocument.Page page = document.startPage(pageInfo);
            view.draw(page.getCanvas());
            document.finishPage(page);
            try {
                document.writeTo(new FileOutputStream(getPathOfPdfFile()));
            } catch (IOException e) {
                AppLog.handleException(Const.Tag.INVOICE_FRAGMENT, e);
            }
            document.close();
        }
    }


    private void providerSubmitInvoice() {
        Utils.showCustomProgressDialog(drawerActivity, drawerActivity.getResources()
                        .getString(R.string.msg_waiting_for_invoice), false,
                null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.TRIP_ID, drawerActivity.preferenceHelper.getTripId
                    ());
            jsonObject.put(Const.Params.TOKEN, drawerActivity.preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, drawerActivity.preferenceHelper
                    .getProviderId());
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .submitInvoice(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {

                    if (ParseContent.getInstance().isSuccessful(response)) {
                        Utils.hideCustomProgressDialog();
                        if (response.body().isSuccess()) {
                            drawerActivity.goToFeedBackFragment();
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(), drawerActivity);
                        }
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(InvoiceFragment.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.INVOICE_FRAGMENT, e);
        }
    }


    public void showCustomProgressDialog(Context context) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) context;
        if (!appCompatActivity.isFinishing()) {
            if (dialogProgress != null && dialogProgress.isShowing()) {
                return;
            }
            dialogProgress = new Dialog(context);
            dialogProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogProgress.setContentView(R.layout.circuler_progerss_bar_two);
            dialogProgress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ivProgressBar = (CustomCircularProgressView) dialogProgress.findViewById(R.id
                    .ivProgressBarTwo);
            ivProgressBar.addListener(new CircularProgressViewAdapter() {
                @Override
                public void onProgressUpdate(float currentProgress) {
                    Log.d("CPV", "onProgressUpdate: " + currentProgress);
                }

                @Override
                public void onProgressUpdateEnd(float currentProgress) {
                    Log.d("CPV", "onProgressUpdateEnd: " + currentProgress);
                }

                @Override
                public void onAnimationReset() {
                    Log.d("CPV", "onAnimationReset");
                }

                @Override
                public void onModeChanged(boolean isIndeterminate) {
                    Log.d("CPV", "onModeChanged: " + (isIndeterminate ? "indeterminate" :
                            "determinate"));
                }
            });
            ivProgressBar.startAnimation();
            dialogProgress.setCancelable(false);
            WindowManager.LayoutParams params = dialogProgress.getWindow().getAttributes();
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialogProgress.getWindow().setAttributes(params);
            dialogProgress.getWindow().setDimAmount(0);
            dialogProgress.show();
        }
    }

    public void hideCustomProgressDialog() {
        try {
            if (dialogProgress != null && ivProgressBar != null) {

                dialogProgress.dismiss();

            }
        } catch (Exception e) {
            AppLog.handleException(TAG, e);
        }
    }

    private void setInvoiceData(Trip trip, CityType tripService) {
        unit = Utils.showUnit(drawerActivity, trip.getUnit());
        if (trip.getIsMinFareUsed() == Const.TRUE && trip.getTripType() == Const.TripType
                .NORMAL) {
            tvInvoiceMinFareApplied.setVisibility(View.VISIBLE);
            tvInvoiceMinFareApplied.setText(String.valueOf(drawerActivity.getResources().getString(R.string
                    .start_min_fare) + " " + currencyFormat.format(tripService.getMinFare()) + " " +
                    drawerActivity.getResources
                            ().getString(R.string.text_applied)));
        }

        if (trip.getPaymentMode() == Const.CASH) {
            ivPaymentImage.setImageDrawable(AppCompatResources.getDrawable(drawerActivity,
                    R.drawable.cash));
            tvPaymentWith.setText(drawerActivity.getResources().getString(R.string
                    .text_payment_with_cash));
        } else {
            ivPaymentImage.setImageDrawable(AppCompatResources.getDrawable(drawerActivity,
                    R.drawable.card));
            tvPaymentWith.setText(drawerActivity.getResources().getString(R.string
                    .text_payment_with_card));
        }

        tvInvoiceNumber.setText(trip.getInvoiceNumber());

        tvInvoiceDistance.setText(ParseContent.getInstance().twoDigitDecimalFormat.format(trip
                .getTotalDistance()) + " " + unit);
        tvInvoiceTripTime.setText(trip.getTotalTime() + " " + drawerActivity.getResources().getString(R
                .string.text_unit_mins));
        tvInvoiceTotal.setText(currencyFormat
                .format(trip
                        .getTotal()));
        tvInvoiceTotal.setVisibility(View.VISIBLE);
        tvTotalText.setVisibility(View.VISIBLE);
        CurrentTrip.getInstance().setTime(trip.getTotalTime());
        CurrentTrip.getInstance().setDistance(trip.getTotalDistance());
        CurrentTrip.getInstance().setUnit(trip.getUnit());
        switch (trip.getTripType()) {
            case Const.TripType.AIRPORT:
                tvInvoiceTripType.setVisibility(View.VISIBLE);
                tvInvoiceTripType.setText(drawerActivity.getResources().getString(R.string
                        .text_airport_trip));
                break;
            case Const.TripType.ZONE:
                tvInvoiceTripType.setVisibility(View.VISIBLE);
                tvInvoiceTripType.setText(drawerActivity.getResources().getString(R.string
                        .text_zone_trip));
                break;
            case Const.TripType.CITY:
                tvInvoiceTripType.setVisibility(View.VISIBLE);
                tvInvoiceTripType.setText(drawerActivity.getResources().getString(R.string
                        .text_city_trip));
                break;
            default:
                //Default case here..
                if (trip.isFixedFare()) {
                    tvInvoiceTripType.setVisibility(View.VISIBLE);
                    tvInvoiceTripType.setText(drawerActivity.getResources().getString(R.string
                            .text_fixed_price));
                } else {
                    tvInvoiceTripType.setVisibility(View.GONE);
                }
                break;
        }

        if (rcvInvoice != null) {
            rcvInvoice.setAdapter(new InvoiceAdapter(drawerActivity.parseContent.parseInvoice
                    (drawerActivity, trip, tripService, currencyFormat)));
        }


    }

}