package mobileapps.aau.at.ab01;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Touch view.
 */
public class TouchView extends View {

    /**
     * Predefined colors used for drawing paths
     */
    private static final int[] PATH_COLORS = {
            Color.CYAN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.GREEN,
            Color.BLUE,
            Color.RED,
    };

    /**
     * List storing data needed for handling touch (add points, drawing stuff, ...).
     */
    private final List<TouchViewData> touchViewData = new LinkedList<>();

    private TextPaint textPaint;
    private Paint textBackground;
    private float textHeight = 0.0f;
    private String text = "";

    // attributes
    private int bgColor = Color.DKGRAY;
    private int textBgColor = Color.WHITE;
    private int textFgColor = Color.BLACK;
    private float textDimension = 0;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public TouchView(Context context) {
        super(context);
        initView(null, 0);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p>
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see #TouchView(Context, AttributeSet, int)
     */
    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #TouchView(Context, AttributeSet)
     */
    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr);
    }

    /**
     * Initialize the view.
     *
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
     *                 reference to a style resource that supplies default values for
     *                 the view. Can be 0 to not look for defaults.
     */
    private void initView(AttributeSet attrs, int defStyle) {
        initAttributes(attrs, defStyle);
        initPaintObjects();
    }

    /**
     * Initialize view attributes from XML.
     *
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
     *                 reference to a style resource that supplies default values for
     *                 the view. Can be 0 to not look for defaults.
     */
    private void initAttributes(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TouchView, defStyle, 0);

        // read out attributes
        bgColor = a.getColor(R.styleable.TouchView_bgColor, bgColor);
        textBgColor = a.getColor(R.styleable.TouchView_textBgColor, textBgColor);
        textFgColor = a.getColor(R.styleable.TouchView_textFgColor, textFgColor);
        textDimension = a.getDimension(R.styleable.TouchView_textDimension,
                14.0f * getResources().getDisplayMetrics().scaledDensity);

        // must be recycled
        a.recycle();
    }

    /**
     * Init paint objects.
     */
    private void initPaintObjects() {

        // paint object used for drawing the background of the text
        textBackground = new Paint();
        textBackground.setFlags(Paint.ANTI_ALIAS_FLAG);
        textBackground.setColor(textBgColor);

        // paint object used for drawing the text
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textDimension);
        textPaint.setColor(textFgColor);

        textHeight = textPaint.getFontMetrics().bottom;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // test if pointing is in our area
        if (!getContentRect().contains(Math.round(event.getX()), Math.round(event.getY()))) {
            return false;
        }

        boolean handled = true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: // FALLTHROUGH
            case MotionEvent.ACTION_POINTER_DOWN: {
                handleDownEvent(event);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                // pointer was moved
                handleMovedEvent(event);
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                // touch was released (or canceled, which is similar)
                break;
            }
            default:
                handled = false;
                break;
        }


        // force redraw of view
        if (handled) {
            invalidate();
        }

        return handled;
    }

    private void handleDownEvent(MotionEvent event) {

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        TouchViewData data = find(pointerId);
        if (data == null) {
            // new pointer
            int nextColor = PATH_COLORS[touchViewData.size() % PATH_COLORS.length];
            data = new TouchViewData(nextColor, pointerId);
            touchViewData.add(data);
        } else {
            // pointer was encountered before - start a new path
            data.moveTo();
        }

        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);
        data.addPoint(x, y);

        text = String.format(Locale.US, "%.1f / %.1f", x, y);
    }

    private void handleMovedEvent(MotionEvent event) {

        PointF lastPoint = null;
        for (int size = event.getPointerCount(), i = 0; i < size; i++) {
            TouchViewData data = find(event.getPointerId(i));
            if (data != null) {
                final float x = event.getX(i);
                final float y = event.getY(i);
                data.addPoint(x, y);
                if (lastPoint != null) {
                    lastPoint.set(x, y);
                } else {
                    lastPoint = new PointF(x, y);
                }
            }
        }
        if (lastPoint != null) {
            text = String.format(Locale.US, "%.1f / %.1f", lastPoint.x, lastPoint.y);
        }
    }

    private TouchViewData find(int pointerId) {

        for (TouchViewData data : touchViewData) {
            if (data.getPointerId() == pointerId) {
                return data;
            }
        }

        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // fill entire canvas with background color
        canvas.drawColor(bgColor);

        // calculate content with
        int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        // save canvas state
        canvas.save();

        // translate subsequent draw operations
        canvas.translate(getPaddingLeft(), getPaddingTop());

        // draw text background
        canvas.drawRect(contentWidth / 3.0f,
                contentHeight - (2.0f * textHeight + textDimension),
                (2.0f * contentWidth) / 3.0f,
                contentHeight,
                textBackground);

        // and draw the text
        canvas.drawText(text,
                contentWidth / 2.0f,
                contentHeight - textHeight,
                textPaint);

        // restore previously saved canvas state
        canvas.restore();


        // last but not least draw the paths
        for (TouchViewData data : touchViewData) {
            if (data.getNumPointsAdded() > 1) {
                canvas.drawPath(data.getPath(), data.getPaint());
            }
        }
    }

    private Rect getContentRect() {
        return new Rect(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // save instance of super class
        Parcelable superState = super.onSaveInstanceState();

        if (touchViewData.isEmpty()) {
            // nothing to save
            return superState;
        }

        return new SavedState(superState, touchViewData, text);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        if (!(state instanceof SavedState)) {
            return;
        }

        SavedState savedState = (SavedState)state;
        text = savedState.text;
        touchViewData.addAll(savedState.touchViewData);

        // force view redraw
        invalidate();
    }

    @Override
    public void clearAnimation() {
        super.clearAnimation();
    }

    /**
     * Clear the view and reset it to it's initial state.
     */
    public void clear() {

        // reset the text
        text = "";
        // clear added touch view data
        touchViewData.clear();

        // invalidate to force redraw
        invalidate();
    }

    /**
     * Class for saving state.
     *
     * <p>
     *     This is needed for correctly handling view rotation changes.
     * </p>
     */
    public static class SavedState extends BaseSavedState {

        private final List<TouchViewData> touchViewData;
        private final String text;

        SavedState(Parcelable source, List<TouchViewData> touchViewData, String text) {
            super(source);
            this.touchViewData = touchViewData;
            this.text = text;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            // marshalling of the text
            out.writeString(text);

            // marshalling of touchViewData
            // don't wanna make TouchViewData parcelable, therefore handcraft marshalling
            out.writeInt(touchViewData.size());
            for (TouchViewData data : touchViewData) {
                // write meta data
                out.writeInt(data.getPointerId());
                out.writeInt(data.getColor());
                // write raw points
                List<List<PointF>> rawPoints = data.getRawPoints();
                out.writeInt(rawPoints.size());
                for (List<PointF> points : rawPoints) {
                    out.writeTypedList(points);
                }
            }
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        /**
         * Constructor used when reading from a parcel.
         *
         * @param source parcel to read from
         */
        private SavedState(Parcel source) {
            super(source);

            text = source.readString();
            int numTouchViewData = source.readInt();
            touchViewData = new ArrayList<>(numTouchViewData);

            for (int i = 0; i < numTouchViewData; i++) {
                touchViewData.add(restoreTouchViewData(source));
            }
        }

        private TouchViewData restoreTouchViewData(Parcel source) {

            int pointerId = source.readInt();
            int color = source.readInt();

            TouchViewData result = new TouchViewData(color, pointerId);
            int numPointPaths = source.readInt();
            for (int i = 0; i < numPointPaths; i++) {
                result.moveTo();
                LinkedList<PointF> points = new LinkedList<>();
                source.readTypedList(points, PointF.CREATOR);

                for (PointF point : points) {
                    result.addPoint(point);
                }
            }

            return result;
        }
    }

    /**
     * Utility class storing some touch related data.
     */
    private static final class TouchViewData {
        /**
         * Pointer it managed by this data class.
         */
        private final int pointerId;
        /**
         * Raw points added to
         */
        private final LinkedList<List<PointF>> rawPoints = new LinkedList<>();
        /**
         * Path drawn by user
         */
        private final Path path = new Path();
        /**
         * Paint object used for drawing the path.
         */
        private final Paint paint = new Paint();
        /**
         * number of points added so far.
         *
         * <p>
         *     Could be counted by traversing the {@link #rawPoints}, but updating an integer is faster.
         * </p>
         */
        private int numPointsAdded = 0;
        /**
         * Indicating whether it's a move to or line to.
         */
        private boolean moveTo = true;


        /**
         * Create new touch view data.
         * @param color Color used for drawing the paths.
         */
        private TouchViewData(int color, int pointerId) {
            this.pointerId = pointerId;
            initPaint(color);
        }

        /**
         * Initialize the paint object.
         * @param color Color used for painting the line.
         */
        private void initPaint(int color) {
            paint.setColor(color);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(10.0f);
            paint.setStyle(Paint.Style.STROKE);
        }

        /**
         * Get the pointer id.
         */
        int getPointerId() {
            return pointerId;
        }

        void addPoint(PointF point) {
            addPoint(point.x, point.y);
        }

        /**
         * Add a point.
         * @param x X-coordinate
         * @param y Y-coordinate
         */
        void addPoint(float x, float y) {
            if (moveTo) {
                // update new list for raw points
                rawPoints.add(new LinkedList<PointF>());
                // apply move command
                path.moveTo(x, y);
                // next time it's not necessarily move any more
                moveTo = false;
            } else {
                // move line to
                path.lineTo(x, y);
            }
            // update raw points & total number of points
            rawPoints.getLast().add(new PointF(x, y));
            numPointsAdded += 1;
        }

        /**
         * Next point added is a "move to" operation instead of "line to".
         */
        void moveTo() {
            moveTo = true;
        }

        /**
         * Get total number of points added so far.
         * @return Total number of points added using {@link #addPoint(float, float)}.
         */
        int getNumPointsAdded() {
            return numPointsAdded;
        }

        /**
         * Get the path object.
         */
        Path getPath() {
            return path;
        }

        /**
         * Get paint object.
         */
        Paint getPaint() {
            return paint;
        }

        /**
         * Get the color.
         */
        int getColor() {
            return getPaint().getColor();
        }

        /**
         * Get all added raw points.
         */
        List<List<PointF>> getRawPoints() {
            return rawPoints;
        }
    }
}
