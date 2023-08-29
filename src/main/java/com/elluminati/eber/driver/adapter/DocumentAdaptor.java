package com.elluminati.eber.driver.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elluminati.eber.driver.DocumentActivity;
import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.MyFontTextViewMedium;
import com.elluminati.eber.driver.models.datamodels.Document;
import com.elluminati.eber.driver.parse.ParseContent;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.GlideApp;

import java.text.ParseException;
import java.util.ArrayList;

import static com.elluminati.eber.driver.utils.Const.IMAGE_BASE_URL;

/**
 * Created by elluminati on 08-08-2016.
 */
public class DocumentAdaptor extends RecyclerView.Adapter<DocumentAdaptor
        .DocumentViewHolder> {

    private ArrayList<Document> docList;
    private Context context;

    public DocumentAdaptor(Context context, ArrayList<Document> docList) {
        this.docList = docList;
        this.context = context;
    }


    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_document, parent, false);

        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {

        Document document = docList.get(position);

        GlideApp.with(context).load(IMAGE_BASE_URL + document.getDocumentPicture()).dontAnimate()
                .fallback(R.drawable
                        .uploading)
                .override(200, 200).placeholder(R.drawable.uploading).into(holder.ivDocumentImage);

        if (document.isIsExpiredDate()) {
            holder.tvExpireDate.setVisibility(View.VISIBLE);
            String date = context.getResources().getString(R.string.text_expire_date);
            try {
                if (!TextUtils.isEmpty(document.getExpiredDate())) {
                    date = date + " " + ParseContent.getInstance().dateFormat.format(ParseContent
                            .getInstance()
                            .webFormatWithLocalTimeZone
                            .parse(document.getExpiredDate()));
                }
            } catch (ParseException e) {
                AppLog.handleException(DocumentActivity.class.getSimpleName(), e);
            }
            holder.tvExpireDate.setText(date);
        } else {
            holder.tvExpireDate.setVisibility(View.GONE);
        }
        if (document.isIsUniqueCode()) {
            holder.tvIdNumber.setVisibility(View.VISIBLE);
            String date = context.getResources().getString(R.string.text_id_number) + " " +
                    "" + document.getUniqueCode();
            holder.tvIdNumber.setText(date);
        } else {
            holder.tvIdNumber.setVisibility(View.GONE);
        }
        holder.tvDocumentTittle.setText(document.getName());
        if (document.getOption() == Const.TRUE) {
            holder.tvOption.setVisibility(View.VISIBLE);
        } else {
            holder.tvOption.setVisibility(View.GONE);
        }

        if(document.getDocumentapprove()==0)
        {
            holder.document_varify.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.document_varify.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return docList.size();
    }


    protected class DocumentViewHolder extends RecyclerView.ViewHolder {

        ImageView ivDocumentImage;
        MyFontTextView tvIdNumber, tvExpireDate, tvDocumentTittle, tvOption;
        MyFontTextViewMedium document_varify;
        LinearLayout llDocumentUpload;

        public DocumentViewHolder(View itemView) {


            super(itemView);
            ivDocumentImage = (ImageView) itemView.findViewById(R.id.ivDocumentImage);
            tvDocumentTittle = (MyFontTextView) itemView.findViewById(R.id.tvDocumentTittle);
            llDocumentUpload = (LinearLayout) itemView.findViewById(R.id.llDocumentUpload);
            tvIdNumber = (MyFontTextView) itemView.findViewById(R.id.tvIdNumber);
            tvExpireDate = (MyFontTextView) itemView.findViewById(R.id.tvExpireDate);
            tvOption = (MyFontTextView) itemView.findViewById(R.id.tvOption);
            document_varify =  itemView.findViewById(R.id.document_varify);


        }


    }

}
