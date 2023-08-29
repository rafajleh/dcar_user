package com.elluminati.eber.driver.adapter;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.TripHistoryActivity;
import com.elluminati.eber.driver.TripHistoryDetailActivity;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.models.datamodels.TripHistory;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.CurrencyHelper;
import com.elluminati.eber.driver.utils.GlideApp;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.elluminati.eber.driver.utils.Utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

import static com.elluminati.eber.driver.utils.Const.IMAGE_BASE_URL;

/**
 * Created by elluminati on 02-07-2016.
 */
public class TripHistoryAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "TripHistoryAdaptor";
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private TripHistoryActivity historyActivity;
    private ArrayList<TripHistory> tripHistoryList;
    private ParseContent parseContent;
    private TreeSet<Integer> separatorsSet;
    private SimpleDateFormat dateFormat;
    private NumberFormat currencyFormat;

    public TripHistoryAdaptor(TripHistoryActivity historyActivity, ArrayList<TripHistory>
            tripHistoryList, TreeSet<Integer> separatorsSet) {
        this.historyActivity = historyActivity;
        this.tripHistoryList = tripHistoryList;
        this.separatorsSet = separatorsSet;
        parseContent = ParseContent.getInstance();
        parseContent.getContext(this.historyActivity);
        dateFormat = parseContent.dateFormat;
        currencyFormat =
                CurrencyHelper.getInstance(historyActivity).getCurrencyFormat(PreferenceHelper.getInstance(historyActivity).getCurrencyCode());

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.itam_trip_history, parent, false);
            return new ViewHolderHistory(v);
        } else if (viewType == TYPE_SEPARATOR) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_history_date, parent, false);
            return new ViewHolderSeparator(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // set data here
        TripHistory history = tripHistoryList.get(position);
        if (holder instanceof ViewHolderHistory) {
            ViewHolderHistory viewHolder = (ViewHolderHistory) holder;

            viewHolder.tvHistoryClientName.setText(history.getFirstName() + " " + history
                    .getLastName());
            viewHolder.tvHistoryTotalCost.setText(currencyFormat.format(history
                            .getTotal()));
            viewHolder.tvProviderEarning.setText(currencyFormat.format(history
                            .getProviderServiceFees()));
            try {
                Date date = ParseContent.getInstance().webFormat.parse(history.getUserCreateTime());
                viewHolder.tvHistoryTripTime.setText(ParseContent.getInstance().timeFormat_am
                        .format(date));
            } catch (ParseException e) {
                AppLog.handleException(TripHistoryAdaptor.class.getSimpleName(), e);
            }
            if (tripHistoryList.get(position).getIsTripCancelledByProvider() == Const.TRUE) {
                viewHolder.tvCanceledBy.setText(historyActivity.getResources().getString(R.string
                        .text_you_canceled_a_trip));
                viewHolder.tvCanceledBy.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvCanceledBy.setText("");
                viewHolder.tvCanceledBy.setVisibility(View.GONE);
            }

            GlideApp.with(historyActivity).load(IMAGE_BASE_URL + history.getPicture())
                    .fallback(R.drawable.ellipse).placeholder(R
                    .drawable.ellipse).override(200, 200)
                    .into(viewHolder.ivClientPhotoDialog);

        } else {
            ViewHolderSeparator viewHolderSeparator = (ViewHolderSeparator) holder;
            Date currentDate = new Date();
            String date = dateFormat.format(currentDate);
            String historyDate = "";
            Date parseHistoryDate = new Date();
            try {
                parseHistoryDate = ParseContent.getInstance().webFormat.parse(history
                        .getUserCreateTime());
                historyDate = dateFormat.format(parseHistoryDate);
            } catch (ParseException e) {
                AppLog.handleException(TripHistoryAdaptor.class.getSimpleName(), e);
            }
            if (historyDate.equals(date)) {
                viewHolderSeparator.tvDateSeparator.setText(historyActivity
                        .getString(R.string.text_today));
            } else if (historyDate
                    .equals(getYesterdayDateString())) {
                viewHolderSeparator.tvDateSeparator.setText(historyActivity
                        .getString(R.string.text_yesterday));
            } else {
                String daySuffix = Utils.getDayOfMonthSuffix(Integer.valueOf(parseContent.day
                        .format(parseHistoryDate)));
                viewHolderSeparator.tvDateSeparator.setText(daySuffix + " " + parseContent
                        .dateFormatMonth.format(parseHistoryDate));

            }
        }

    }


    @Override
    public int getItemCount() {
        return tripHistoryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return separatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    private String getYesterdayDateString() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime());
    }

    private void goToTripHistoryDetail(String tripId, int unit, String currency) {

        Intent tripDetailIntent = new Intent(historyActivity, TripHistoryDetailActivity
                .class);
        tripDetailIntent.putExtra(Const.Params.TRIP_ID, tripId);
        tripDetailIntent.putExtra(Const.Params.UNIT, unit);
        tripDetailIntent.putExtra(Const.Params.CURRENCY, currency);
        historyActivity.startActivity(tripDetailIntent);
        historyActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected class ViewHolderHistory extends RecyclerView.ViewHolder implements View
            .OnClickListener {

        TextView tvHistoryClientName, tvHistoryTripTime, tvHistoryTotalCost, tvCanceledBy,
                tvProviderEarning;
        LinearLayout llHistory;
        ImageView ivClientPhotoDialog;

        public ViewHolderHistory(View itemView) {
            super(itemView);
            tvHistoryClientName = itemView.findViewById(R.id.tvHistoryClientName);
            tvHistoryTotalCost = itemView.findViewById(R.id.tvHistoryTripCost);
            tvHistoryTripTime = itemView.findViewById(R.id.tvHistoryTripTime);
            ivClientPhotoDialog = (ImageView) itemView.findViewById(R.id.ivClientPhotoDialog);
            tvCanceledBy = itemView.findViewById(R.id.tvCanceledBy);
            llHistory = (LinearLayout) itemView.findViewById(R.id.llHistory);
            llHistory.setOnClickListener(this);
            tvProviderEarning = itemView.findViewById(R.id.tvProviderEarning);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.llHistory:
                    int position = getAdapterPosition();
                    if (tripHistoryList.get(position).getIsTripCompleted() == Const.TRUE) {
                        goToTripHistoryDetail(tripHistoryList.get(position).getTripId(),
                                tripHistoryList.get(position).getUnit(),
                                tripHistoryList.get(position).getCurrency());
                    }
                    break;
                default:
                    // do with default
                    break;
            }

        }

    }

    protected class ViewHolderSeparator extends RecyclerView.ViewHolder {

        MyFontTextView tvDateSeparator;

        public ViewHolderSeparator(View itemView) {
            super(itemView);
            tvDateSeparator = (MyFontTextView) itemView.findViewById(R.id.tvDateSeparator);
        }
    }

}

