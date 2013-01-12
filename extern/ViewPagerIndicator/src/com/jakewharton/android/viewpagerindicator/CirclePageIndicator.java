/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakewharton.android.viewpagerindicator;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Draws circles (one for each view). The current view position is filled and others are only stroked.
 */
public class CirclePageIndicator extends View implements PageIndicator {
	private float mRadius;
	private final Paint mPaintStroke;
	private final Paint mPaintFill;
	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mListener;
	private int mCurrentPage;
	private int mSnapPage;
	private int mCurrentOffset;
	private int mPageWidth;
	private boolean mCentered;
	private boolean mSnap;

	public CirclePageIndicator(Context context) {
		this(context, null);
	}

	public CirclePageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.circlePageIndicatorStyle);
	}

	public CirclePageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Load defaults from resources
		final Resources res = getResources();
		final int defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color);
		final int defaultStrokeColor = res.getColor(R.color.default_circle_indicator_stroke_color);
		final float defaultStrokeWidth = res.getDimension(R.dimen.default_circle_indicator_stroke_width);
		final float defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius);
		final boolean defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered);
		final boolean defaultSnap = res.getBoolean(R.bool.default_circle_indicator_snap);

		// Retrieve styles attributes
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, R.style.Widget_CirclePageIndicator);

		mCentered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered);
		mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		// mPaintStroke.setStyle(Style.STROKE);
		mPaintStroke.setStyle(Style.FILL);
		mPaintStroke.setColor(a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor));
		mPaintStroke.setStrokeWidth(a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth));
		mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFill.setStyle(Style.FILL);
		mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor));
		mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius);
		mSnap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap);

		a.recycle();
	}

	public void setCentered(boolean centered) {
		mCentered = centered;
		invalidate();
	}

	public boolean isCentered() {
		return mCentered;
	}

	public void setFillColor(int fillColor) {
		mPaintFill.setColor(fillColor);
		invalidate();
	}

	public int getFillColor() {
		return mPaintFill.getColor();
	}

	public void setStrokeColor(int strokeColor) {
		mPaintStroke.setColor(strokeColor);
		invalidate();
	}

	public int getStrokeColor() {
		return mPaintStroke.getColor();
	}

	public void setStrokeWidth(float strokeWidth) {
		mPaintStroke.setStrokeWidth(strokeWidth);
		invalidate();
	}

	public float getStrokeWidth() {
		return mPaintStroke.getStrokeWidth();
	}

	public void setRadius(float radius) {
		mRadius = radius;
		invalidate();
	}

	public float getRadius() {
		return mRadius;
	}

	public void setSnap(boolean snap) {
		mSnap = snap;
		invalidate();
	}

	public boolean isSnap() {
		return mSnap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final int count = mViewPager.getAdapter().getCount();
		final float threeRadius = mRadius * 3;
		final float topOffset = getPaddingTop() + mRadius;
		final int paddingLeft = getPaddingLeft();
		float leftOffset = paddingLeft + mRadius;
		if (mCentered) {
			leftOffset += ((getWidth() - paddingLeft - getPaddingRight()) / 2.0f) - ((count * threeRadius) / 2.0f);
		}

		// Draw stroked circles
		for (int iLoop = 0; iLoop < count; iLoop++) {
			canvas.drawCircle(leftOffset + (iLoop * threeRadius), topOffset, mRadius, mPaintStroke);
		}

		// Draw the filled circle according to the current scroll
		float cx = (mSnap ? mSnapPage : mCurrentPage) * threeRadius;
		if (!mSnap && (mPageWidth != 0)) {
			cx += (mCurrentOffset * 1.0f / mPageWidth) * threeRadius;
		}
		canvas.drawCircle(leftOffset + cx, topOffset, mRadius, mPaintFill);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			final int count = mViewPager.getAdapter().getCount();
			final float halfWidth = getWidth() / 2;
			final float halfCircleWidth = (count * 3 * mRadius) / 2;

			if ((mCurrentPage > 0) && (event.getX() < halfWidth - halfCircleWidth)) {
				setCurrentItem(mCurrentPage - 1);
				return true;
			} else if ((mCurrentPage < count - 1) && (event.getX() > halfWidth + halfCircleWidth)) {
				setCurrentItem(mCurrentPage + 1);
				return true;
			}
		}

		return super.onTouchEvent(event);
	}

	public void setViewPager(ViewPager view) {
		if (view.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		mViewPager = view;
		mViewPager.setOnPageChangeListener(this);
		mPageWidth = mViewPager.getWidth();
		invalidate();
	}

	public void setViewPager(ViewPager view, int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}

	public void setCurrentItem(int item) {
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		mViewPager.setCurrentItem(item);
		mCurrentPage = item;
		invalidate();
	}

	public void onPageScrollStateChanged(int state) {
		if (mListener != null) {
			mListener.onPageScrollStateChanged(state);
		}
	}

	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mCurrentPage = position;
		mCurrentOffset = positionOffsetPixels;
		mPageWidth = mViewPager.getWidth();
		invalidate();

		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	public void onPageSelected(int position) {
		mCurrentPage = position;
		mSnapPage = position;
		invalidate();

		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Calculate the width according the views count
			final int count = mViewPager.getAdapter().getCount();
			result = (int) (getPaddingLeft() + getPaddingRight() + (count * 2 * mRadius) + (count - 1) * mRadius + 1);
			// Respect AT_MOST value if that was what is called for by measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// We were told how big to be
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		// Measure the height
		else {
			result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
			// Respect AT_MOST value if that was what is called for by measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mCurrentPage = savedState.currentPage;
		mSnapPage = savedState.currentPage;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPage = mCurrentPage;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPage;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPage = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPage);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
