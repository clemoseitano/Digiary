package net.tanozin.digiary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class DemoView extends View {
    private String mDescriptionString="10 px";
    private int mShapeColor = Color.RED;
    private int mRadius = 10;

    private Paint mPaint;

    public DemoView(Context context) {
        super(context);
        init(null, 0);
    }

    public DemoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DemoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DemoView, defStyle, 0);

        mDescriptionString = a.getString(
                R.styleable.DemoView_descriptionString);
        mShapeColor = a.getColor(
                R.styleable.DemoView_shapeColor,
                mShapeColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mRadius = a.getInteger(
                R.styleable.DemoView_shapeRadius,
                mRadius);


        a.recycle();

        // Set up a default TextPaint object
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mShapeColor);
        mPaint.setStrokeWidth(mRadius);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        canvas.drawCircle(paddingLeft+contentWidth/2, paddingTop+contentHeight/2, mRadius, mPaint);
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getSDescritionString() {
        return mDescriptionString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mDescriptionString = exampleString;
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getShapeColor() {
        return mShapeColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setShapeColor(int exampleColor) {
        mShapeColor = exampleColor;
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public int getRadius() {
        return mRadius;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setRadius(int exampleDimension) {
        mRadius = exampleDimension;
        postInvalidate();
        }
}
