package com.andtinder.view;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andtinder.model.CardModel;
import com.facebook.drawee.view.SimpleDraweeView;
import com.konradjanica.amatch.R;

public final class SimpleCardStackAdapter extends CardStackAdapter {

	public SimpleCardStackAdapter(Context mContext) {
		super(mContext);
	}

	@Override
	public View getCardView(int position, CardModel model, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.std_card_inner, parent, false);
			assert convertView != null;
		}

//		((ImageView) convertView.findViewById(R.id.image)).setImageDrawable(model.getCardImageDrawable());
		Uri uri = Uri.parse(model.getCompanyImgUrl());
        ((SimpleDraweeView) convertView.findViewById(R.id.image)).setImageURI(uri);
        ((TextView) convertView.findViewById(R.id.title)).setText(model.getTitle());
		((me.grantland.widget.AutofitTextView) convertView.findViewById(R.id.description)).setText(model.getDescription());

		return convertView;
	}
}
