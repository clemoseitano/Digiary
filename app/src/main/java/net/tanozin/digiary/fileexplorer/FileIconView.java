package net.tanozin.digiary.fileexplorer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import net.tanozin.digiary.R;

/**
 * TODO: document your custom view class.
 */
public class FileIconView extends View {
    private String mText="";
    private int mTextColor = Color.RED;
    private float mTextSize = 25;
    private Drawable mDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public FileIconView(Context context) {
        super(context);
        init(null, 0);
    }

    public FileIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FileIconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.FileIconView, defStyle, 0);

        mText = a.getString(
                R.styleable.FileIconView_text);
        mTextColor = a.getColor(
                R.styleable.FileIconView_fiv_textColor,
                mTextColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(
                R.styleable.FileIconView_fiv_textSize,
                mTextSize);

        if (a.hasValue(R.styleable.FileIconView_drawable)) {
            mDrawable = a.getDrawable(
                    R.styleable.FileIconView_drawable);
            if(mDrawable!=null)
            mDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextWidth = mTextPaint.measureText(mText);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    int paddingLeft;
    int paddingTop;
    int paddingRight;
    int paddingBottom;

    int contentWidth;
    int contentHeight;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the example drawable on top of the text.
        if (mDrawable != null) {
            mDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mDrawable.draw(canvas);
        }

        // Draw the text.
        canvas.drawText(mText,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight/2) / 2,
                mTextPaint);
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getText() {
        return mText;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setText(String exampleString) {
        mText = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setTextColor(int exampleColor) {
        mTextColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setTextSize(float exampleDimension) {
        mTextSize = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getDrawableIcon() {
        return mDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setDrawableIcon(Drawable exampleDrawable) {
        mDrawable = exampleDrawable;
    }
}
