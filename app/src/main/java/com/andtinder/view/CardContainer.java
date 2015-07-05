package com.andtinder.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations.Orientation;
import com.facebook.drawee.view.SimpleDraweeView;
import com.konradjanica.amatch.R;

import java.util.Random;

public class CardContainer extends AdapterView<ListAdapter> {
    public static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private static final double DISORDERED_MAX_ROTATION_RADIANS = Math.PI / 64;
    private int mNumberOfCards = -1;
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            clearStack();
            ensureFull();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            clearStack();
        }
    };
    private final Random mRandom = new Random();
    private final Rect boundsRect = new Rect();
    private final Rect childRect = new Rect();
    private final Matrix mMatrix = new Matrix();


    //TODO: determine max dynamically based on device speed
    private int mMaxVisible = 5;
    private GestureDetector mGestureDetector;
    private int mFlingSlop;
    private Orientation mOrientation;
    private ListAdapter mListAdapter;
    private float mLastTouchX;
    private float mLastTouchY;
    private View mTopCard;
    private int mTouchSlop;
    private int mGravity;
    private int mNextAdapterPosition;
    private boolean mDragging;

    private boolean mIsFlingAnimating;
    private boolean mIsUrlPressedDown;

    public View getTopCardView() {
        return mTopCard;
    }

    public CardContainer(Context context) {
        super(context);

        setOrientation(Orientation.Disordered);
        setGravity(Gravity.CENTER);
        init();
    }

    public CardContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromXml(attrs);
        init();
    }


    public CardContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFromXml(attrs);
        init();
    }

    private void init() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mFlingSlop = viewConfiguration.getScaledMinimumFlingVelocity();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        mIsFlingAnimating = false;
        mIsUrlPressedDown = false;
    }

    private void initFromXml(AttributeSet attr) {
        TypedArray a = getContext().obtainStyledAttributes(attr,
                R.styleable.CardContainer);

        setGravity(a.getInteger(R.styleable.CardContainer_android_gravity, Gravity.CENTER));
        int orientation = a.getInteger(R.styleable.CardContainer_orientation, 1);
        setOrientation(Orientation.fromIndex(orientation));

        a.recycle();
    }

    @Override
    public ListAdapter getAdapter() {
        return mListAdapter;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mListAdapter != null)
            mListAdapter.unregisterDataSetObserver(mDataSetObserver);

        clearStack();
        mTopCard = null;
        mListAdapter = adapter;
        mNextAdapterPosition = 0;
        adapter.registerDataSetObserver(mDataSetObserver);

        refreshTopCard();

        requestLayout();
    }

    private void ensureFull() {
        while (mNextAdapterPosition < mListAdapter.getCount() && getChildCount() < mMaxVisible) {
            View view = mListAdapter.getView(mNextAdapterPosition, null, this);
            view.setLayerType(LAYER_TYPE_SOFTWARE, null);
            if (mOrientation == Orientation.Disordered) {
                if (getChildCount() != 0) {
                    view.setRotation(getDisorderedRotation());
                } else {
                    addUrlListener(view);
                }
            }
            addViewInLayout(view, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    mListAdapter.getItemViewType(mNextAdapterPosition)), false);

            requestLayout();

            mNextAdapterPosition += 1;
        }
    }

    public void clearStack() {
        removeAllViewsInLayout();
        mNextAdapterPosition = 0;
        mTopCard = null;
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null)
            throw new NullPointerException("Orientation may not be null");
        if (mOrientation != orientation) {
            this.mOrientation = orientation;
            if (orientation == Orientation.Disordered) {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.setRotation(getDisorderedRotation());
                }
            } else {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.setRotation(0);
                }
            }
            requestLayout();
        }

    }

    private float getDisorderedRotation() {
        return (float) Math.toDegrees(mRandom.nextGaussian() * DISORDERED_MAX_ROTATION_RADIANS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int requestedWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int requestedHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int childWidth, childHeight;

        if (mOrientation == Orientation.Disordered) {
            int R1, R2;
            if (requestedWidth >= requestedHeight) {
                R1 = requestedHeight;
                R2 = requestedWidth;
            } else {
                R1 = requestedWidth;
                R2 = requestedHeight;
            }
            childWidth = (int) ((R1 * Math.cos(DISORDERED_MAX_ROTATION_RADIANS) - R2 * Math.sin(DISORDERED_MAX_ROTATION_RADIANS)) / Math.cos(2 * DISORDERED_MAX_ROTATION_RADIANS));
            childHeight = (int) ((R2 * Math.cos(DISORDERED_MAX_ROTATION_RADIANS) - R1 * Math.sin(DISORDERED_MAX_ROTATION_RADIANS)) / Math.cos(2 * DISORDERED_MAX_ROTATION_RADIANS));
        } else {
            childWidth = requestedWidth;
            childHeight = requestedHeight;
        }

        int childWidthMeasureSpec, childHeightMeasureSpec;
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            assert child != null;
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        for (int i = 0; i < getChildCount(); i++) {
            boundsRect.set(0, 0, getWidth(), getHeight());

            View view = getChildAt(i);
            int w, h;
            w = view.getMeasuredWidth();
            h = view.getMeasuredHeight();

            Gravity.apply(mGravity, w, h, boundsRect, childRect);
            view.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTopCard == null) {
            return false;
        }
        if (mIsUrlPressedDown) {
            return false;
        }
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        Log.d("Touch Event", MotionEvent.actionToString(event.getActionMasked()) + " ");
        final int pointerIndex;
        final float x, y;
        final float dx, dy;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:


                mTopCard.getHitRect(childRect);

                pointerIndex = event.getActionIndex();
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                if (!childRect.contains((int) x, (int) y)) {
                    return false;
                }
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = event.getPointerId(pointerIndex);

                float[] points = new float[]{x - mTopCard.getLeft(), y - mTopCard.getTop()};
                mTopCard.getMatrix().invert(mMatrix);
                mMatrix.mapPoints(points);
                mTopCard.setPivotX(points[0]);
                mTopCard.setPivotY(points[1]);

                break;
            case MotionEvent.ACTION_MOVE:

                pointerIndex = event.findPointerIndex(mActivePointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                dx = x - mLastTouchX;
                dy = y - mLastTouchY;

                if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                }

                if (!mDragging) {
                    return true;
                }

                mTopCard.setTranslationX(mTopCard.getTranslationX() + dx);
                mTopCard.setTranslationY(mTopCard.getTranslationY() + dy);

                mTopCard.setRotation(40 * mTopCard.getTranslationX() / (getWidth() / 2.f));

                mLastTouchX = x;
                mLastTouchY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                removeTopCardRotation();
                if (!mDragging) {
                    return true;
                }
                mDragging = false;
                mActivePointerId = INVALID_POINTER_ID;
                ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mTopCard,
                        PropertyValuesHolder.ofFloat("translationX", 0),
                        PropertyValuesHolder.ofFloat("translationY", 0),
//                        PropertyValuesHolder.ofFloat("rotation", (float) Math.toDegrees(mRandom.nextGaussian() * DISORDERED_MAX_ROTATION_RADIANS)),
                        PropertyValuesHolder.ofFloat("rotation", 0),
                        PropertyValuesHolder.ofFloat("pivotX", mTopCard.getWidth() / 2.f),
                        PropertyValuesHolder.ofFloat("pivotY", mTopCard.getHeight() / 2.f)
                ).setDuration(250);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                removeTopCardRotation();

                pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);

                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mTopCard == null) {
            return false;
        }
        if (mIsUrlPressedDown) {
            return false;
        }
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        final int pointerIndex;
        final float x, y;
        final float dx, dy;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTopCard.getHitRect(childRect);

                CardModel cardModel = (CardModel) getAdapter().getItem(getChildCount() - 1);

                if (cardModel.getOnClickListener() != null) {
                    cardModel.getOnClickListener().OnClickListener();
                }
                pointerIndex = event.getActionIndex();
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                if (!childRect.contains((int) x, (int) y)) {
                    return false;
                }

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = event.getPointerId(pointerIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(mActivePointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);
                if (Math.abs(x - mLastTouchX) > mTouchSlop || Math.abs(y - mLastTouchY) > mTouchSlop) {
                    float[] points = new float[]{x - mTopCard.getLeft(), y - mTopCard.getTop()};
                    mTopCard.getMatrix().invert(mMatrix);
                    mMatrix.mapPoints(points);
                    mTopCard.setPivotX(points[0]);
                    mTopCard.setPivotY(points[1]);
                    return true;
                }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeTopCardRotation();
                return true;
        }

        return false;
    }

    private void removeTopCardRotation() {
        if (Math.abs(mTopCard.getRotation()) > 30) {
            removeTopCard();
        }
    }

    @Override
    public View getSelectedView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSelection(int position) {
        throw new UnsupportedOperationException();
    }

    public int getGravity() {
        return mGravity;
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }
    }

    private class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d("Fling", "Fling with " + velocityX + ", " + velocityY);
            if (Math.abs(velocityX) > mFlingSlop) {

                removeTopCard();

                return true;
            } else
                return false;
        }
    }

    private void removeTopCard() {
        if (!mIsFlingAnimating) {
            // Lock animator incase fling again while animating
            mIsFlingAnimating = true;

            float targetX = mTopCard.getX();

            // Choose animation side
            if (targetX <= 0) {
                // Left
                targetX = -getWidth() / 4;
            } else {
                // Right
                targetX = getWidth() / 4;
            }
            targetX *= 3;
            // Animation Durations
            final long duration = 500;

            // Layout removal requires final
            final float finalTargetX = targetX;
            final View topCard = mTopCard;

            topCard.animate()
                    .setDuration(duration)
                    .alpha(.00f)
                    .setInterpolator(new LinearInterpolator())
                    .x(targetX)
                    .rotationBy(Math.copySign(25, targetX))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Call listener on past top card
                            CardModel cardModel = (CardModel) getAdapter().getItem(getChildCount() - 1);
                            if (cardModel.getOnCardDimissedListener() != null) {
                                if (finalTargetX > 0) {
                                    cardModel.getOnCardDimissedListener().onLike();
                                } else {
                                    cardModel.getOnCardDimissedListener().onDislike();
                                }
                            }
                            // Remove top card
                            removeViewInLayout(topCard);
                            // Repopulate adapter
                            ensureFull();
                            // Unlock animator
                            mIsFlingAnimating = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            onAnimationEnd(animation);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            // Reference top card
                            mTopCard = getChildAt(getChildCount() - 2);
                            if (mTopCard != null) {
                                mTopCard.setLayerType(LAYER_TYPE_HARDWARE, null);
                            }
                            // Straighten next card
                            mTopCard.animate()
                                    .rotation(0)
                                    .setDuration(duration);

                            // Add url listener to top card
                            addUrlListener(mTopCard);
                        }
                    });
        }
    }

    public void refreshTopCard() {
        ensureFull();
        if (getChildCount() != 0) {
            mTopCard = getChildAt(getChildCount() - 1);
            if (mTopCard != null)
                mTopCard.setLayerType(LAYER_TYPE_HARDWARE, null);
            mNumberOfCards = getAdapter().getCount();
        }
    }

    public void addUrlListener(View view) {
        final SimpleDraweeView companyImage = ((SimpleDraweeView) view.findViewById(R.id.image));
        companyImage.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        companyImage.setColorFilter(Color.argb(50, 0, 0, 0));
                        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        mIsUrlPressedDown = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        companyImage.setColorFilter(Color.argb(0, 0, 0, 50));
                        mIsUrlPressedDown = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                            companyImage.setColorFilter(Color.argb(0, 0, 0, 0));
                            mIsUrlPressedDown = false;
                        }
                }
                return true;
            }
        });
    }
}
