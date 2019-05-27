
package com.mephi2AF.android.apps.authenticator;

import com.mephi2AF.android.apps.authenticator2.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class CountdownIndicator extends View {
  private final Paint mRemainingSectorPaint;
  private final Paint mBorderPaint;
  private static final int DEFAULT_COLOR = 0xff3060c0;


  private double mPhase;

  public CountdownIndicator(Context context) {
    this(context, null);
  }

  public CountdownIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);

    int color = DEFAULT_COLOR;
    Resources.Theme theme = context.getTheme();
    TypedArray appearance = theme.obtainStyledAttributes(
        attrs, R.styleable.CountdownIndicator, 0, 0);
    if (appearance != null) {
      int n = appearance.getIndexCount();
      for (int i = 0; i < n; i++) {
        int attr = appearance.getIndex(i);

        switch (attr) {
        case R.styleable.CountdownIndicator_color:
          color = appearance.getColor(attr, DEFAULT_COLOR);
          break;
        }
      }
    }

    mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBorderPaint.setStrokeWidth(0); // hairline
    mBorderPaint.setStyle(Style.STROKE);
    mBorderPaint.setColor(color);
    mRemainingSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mRemainingSectorPaint.setColor(mBorderPaint.getColor());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    float remainingSectorSweepAngle = (float) (mPhase * 360);
    float remainingSectorStartAngle = 270 - remainingSectorSweepAngle;

    // Draw the sector/filled arc
    // We need to leave the leftmost column and the topmost row out of the drawingRect because
    // in anti-aliased mode drawArc and drawOval use these areas for some reason.
    RectF drawingRect = new RectF(1, 1, getWidth() - 1, getHeight() - 1);
    if (remainingSectorStartAngle < 360) {
      canvas.drawArc(
          drawingRect,
          remainingSectorStartAngle,
          remainingSectorSweepAngle,
          true,
          mRemainingSectorPaint);
    } else {
      // 360 degrees is equivalent to 0 degrees for drawArc, hence the drawOval below.
      canvas.drawOval(drawingRect, mRemainingSectorPaint);
    }

    // Draw the outer border
    canvas.drawOval(drawingRect, mBorderPaint);
  }

  /**
   * Sets the phase of this indicator.
   *
   * @param phase phase {@code [0, 1]}: {@code 1} when the maximum amount of time is remaining,
   *        {@code 0} when no time is remaining.
   */
  public void setPhase(double phase) {
    if ((phase < 0) || (phase > 1)) {
      throw new IllegalArgumentException("phase: " + phase);
    }

    mPhase = phase;
    invalidate();
  }
}
