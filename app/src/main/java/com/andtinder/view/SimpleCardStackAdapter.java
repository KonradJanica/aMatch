package com.andtinder.view;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andtinder.model.CardModel;
import com.facebook.drawee.view.SimpleDraweeView;
import com.konradjanica.amatch.R;

import me.grantland.widget.AutofitTextView;

public final class SimpleCardStackAdapter extends CardStackAdapter {

    public SimpleCardStackAdapter(Context mContext) {
        super(mContext);
    }

    @Override
    public View getCardView(int position, CardModel model, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.std_card_inner, parent, false);
            assert convertView != null;
        }

//		((ImageView) convertView.findViewById(R.id.image)).setImageDrawable(model.getCardImageDrawable());
        Uri uri = Uri.parse(model.getCompanyImgUrl());
        ((SimpleDraweeView) convertView.findViewById(R.id.image)).setImageURI(uri);

        ((AutofitTextView) convertView.findViewById(R.id.title)).setText(model.getTitle());

        ((AutofitTextView) convertView.findViewById(R.id.page_date)).setText("Page: " + model.getPage() + ",");

        AutofitTextView description = ((AutofitTextView) convertView.findViewById(R.id.description));
        description.setMaxLines(model.getDescriptionLineCount());
        description.setText(model.getDescription());
        description.setMaxHeight(description.getHeight());
//        description.setGravity(Gravity.CENTER);
        description.setEllipsize(TextUtils.TruncateAt.END);
        description.setMinTextSize(2);
        description.setHeightFitting();
        description.setSizeToFit();

        FrameLayout favoriteHeart = ((FrameLayout) convertView.findViewById(R.id.fav));
        if (model.isFavorite()) {
            favoriteHeart.setVisibility(View.VISIBLE);
        } else {
            favoriteHeart.setVisibility(View.INVISIBLE);
        }

        TextView favNumb = ((TextView) convertView.findViewById(R.id.fav_page));
        favNumb.setText(model.getPage());

        return convertView;
    }
}
