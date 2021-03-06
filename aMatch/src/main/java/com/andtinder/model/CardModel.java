/**
 * AndTinder v0.1 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 *
 * TAndTinder is a native library for Android that provide a
 * Tinder card like effect. A card can be constructed using an
 * image and displayed with animation effects, dismiss-to-like
 * and dismiss-to-unlike, and use different sorting mechanisms.
 *
 * AndTinder is compatible with API Level 13 and upwards
 *
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */

package com.andtinder.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.Serializable;

public class CardModel implements Serializable {

    private final static long serialVersionUID = 1;

	private String   title;
	private String   description;
	private String   companyImgUrl;
    private String   pageNumb;
    private String   dateAndLocation;
    private String   id;
	private int      descriptionLineCount;

	private Drawable cardImageDrawable;
	private Drawable cardLikeImageDrawable;
	private Drawable cardDislikeImageDrawable;

	private boolean isFavorite;

    private OnCardDimissedListener mOnCardDimissedListener = null;

    private OnClickListener mOnClickListener = null;

    /**
     * Serialization implementation
     * @param out
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
        // write 'this' to 'out'...
        out.writeObject(title);
        out.writeObject(description);
        out.writeObject(companyImgUrl);
        out.writeObject(pageNumb);
        out.writeObject(dateAndLocation);
        out.writeObject(id);
        out.writeInt(descriptionLineCount);
    }

    /**
     * Serialization implementation
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
   private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
     // populate the fields of 'this' from the data in 'in'...
       this.title = (String) in.readObject();
       this.description = (String) in.readObject();
       this.companyImgUrl = (String) in.readObject();
       this.pageNumb = (String) in.readObject();
       this.dateAndLocation = (String) in.readObject();
       this.id = (String) in.readObject();
       this.descriptionLineCount = in.readInt();
       this.isFavorite = true;
   }

    public interface OnCardDimissedListener {
        void onLike();
        void onDislike();
    }

    public interface OnClickListener {
        void OnClickListener();
    }

	public CardModel() {
		this(null, null, (Drawable)null);
	}

	public CardModel(String title, String description, Drawable cardImage) {
		this.title = title;
		this.description = description;
		this.cardImageDrawable = cardImage;
	}

	public CardModel(String title, String description, Bitmap cardImage) {
		this.title = title;
		this.description = description;
		this.cardImageDrawable = new BitmapDrawable(null, cardImage);
	}

    public CardModel(String title, String description, String companyImgUrl,
                     String pageNumb, String dateAndLocation,
                     String id, int descriptionLineCount) {
        this.title = title;
        this.description = description;
        this.companyImgUrl = companyImgUrl;
        this.pageNumb = pageNumb;
        this.dateAndLocation = dateAndLocation;
        this.id = id;
		this.descriptionLineCount = descriptionLineCount;
		this.isFavorite = false;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public String getCompanyImgUrl() {
        return companyImgUrl;
    }

    public void setCompanyImgUrl(String companyImgUrl) {
        this.companyImgUrl = companyImgUrl;
    }

	public int getDescriptionLineCount() {
		return descriptionLineCount;
	}

	public void setDescriptionLineCount(int descriptionLineCount) {
		this.descriptionLineCount = descriptionLineCount;
	}

    public String getPage() {
        return pageNumb;
    }

    public String getDateAndLocation() {
        return dateAndLocation;
    }

    public String getId() {
        return id;
    }

	public Drawable getCardImageDrawable() {
		return cardImageDrawable;
	}

	public void setCardImageDrawable(Drawable cardImageDrawable) {
		this.cardImageDrawable = cardImageDrawable;
	}

	public Drawable getCardLikeImageDrawable() {
		return cardLikeImageDrawable;
	}

	public void setCardLikeImageDrawable(Drawable cardLikeImageDrawable) {
		this.cardLikeImageDrawable = cardLikeImageDrawable;
	}

	public Drawable getCardDislikeImageDrawable() {
		return cardDislikeImageDrawable;
	}

	public void setCardDislikeImageDrawable(Drawable cardDislikeImageDrawable) {
		this.cardDislikeImageDrawable = cardDislikeImageDrawable;
	}

    public void setOnCardDimissedListener( OnCardDimissedListener listener ) {
        this.mOnCardDimissedListener = listener;
    }

    public OnCardDimissedListener getOnCardDimissedListener() {
       return this.mOnCardDimissedListener;
    }


    public void setOnClickListener( OnClickListener listener ) {
        this.mOnClickListener = listener;
    }

    public OnClickListener getOnClickListener() {
        return this.mOnClickListener;
    }

	public boolean isFavorite() {
		return isFavorite;
	}

	public void toggleFavorite() {
		this.isFavorite = !isFavorite;
	}

    public void setFavorite(boolean isFav) {
        this.isFavorite = isFav;
    }
}