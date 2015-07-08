package com.andtinder.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

        ((AutofitTextView) convertView.findViewById(R.id.page_date)).setText(
                "Page: " + model.getPage() + ", " + model.getDateAndLocation());

        final String descriptionText = model.getDescription();
        AutofitTextView description = ((AutofitTextView) convertView.findViewById(R.id.description));
        description.setMaxLines(model.getDescriptionLineCount());
        description.setText(descriptionText);
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
        favNumb.setVisibility(View.GONE);

        // Share button listener
        convertView.findViewById(R.id.image_2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
//                sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, descriptionText);
                    getContext().startActivity(Intent.createChooser(sendIntent, getContext().getResources().getText(R.string.share_to)));
                } catch(Exception e) {
                    return; //do nothing
                }
            }
        });

        return convertView;
    }
}
