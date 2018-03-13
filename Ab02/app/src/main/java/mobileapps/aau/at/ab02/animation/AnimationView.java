package mobileapps.aau.at.ab02.animation;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobileapps.aau.at.ab02.R;

/**
 * Animation view.
 */
public class AnimationView extends View {

    private static final int VERTICAL_SPACE_BETWEEN_WORDS = 10;

    private static final long DELAY_MS = 500; // 500 milliseconds delay until animation starts
    private static final long ANIMATION_LENGTH = 2000; // 2sec animation time

    // paint object for drawing the text
    TextPaint textPaint;

    // attributes
    private int bgColor = Color.WHITE;
    private int textFgColor = Color.BLACK;
    private float textDimension = 0;

    // other properties
    private List<AnimatableWord> words;
    private boolean animationRunning = false;

    private final Rect clippingRect = new Rect();

    AnimatorSet animatorSet;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */

    public AnimationView(Context context) {
        super(context);
        initView(context, null, 0);
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
     * @see #AnimationView(Context, AttributeSet, int)
     */
    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
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
     * @see #AnimationView(Context, AttributeSet)
     */
    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    /**
     * Initialize the view.
     *
     * @param context
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
 *                 reference to a style resource that supplies default values for
     */
    private void initView(Context context, AttributeSet attrs, int defStyle) {
        initAttributes(attrs, defStyle);
        initPaintObjects();
        initWords(context);
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
                attrs, R.styleable.AnimationView, defStyle, 0);

        // read out attributes
        bgColor = a.getColor(R.styleable.AnimationView_bgColor, bgColor);
        textFgColor = a.getColor(R.styleable.AnimationView_textFgColor, textFgColor);
        textDimension = a.getDimension(R.styleable.AnimationView_textDimension,
                28.0f * getResources().getDisplayMetrics().scaledDensity);

        // must be recycled
        a.recycle();
    }

    /**
     * Initialize words that should be shown.
     *
     * @param context
     */
    private void initWords(Context context) {

        Intent intent = ((Activity)context).getIntent();
        String text = intent.getStringExtra("text");

        List<String> words = splitWords(text);
        this.words = new ArrayList<>(words.size());
        for (String word : words) {
            this.words.add(new AnimatableWord(word, textPaint));
        }
    }

    /**
     * Init paint objects.
     */
    private void initPaintObjects() {

        // paint object used for drawing the text
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(textDimension);
        textPaint.setColor(textFgColor);
    }

    private void updateWordLocations() {

        // update locations of words

        // first compute the total height of the words
        int totalHeight = 0;
        for (int i = 0; i < words.size(); i++) {
            totalHeight += words.get(i).getBounds().height();
            if (i < words.size() - 1) {
                // add space between words
                totalHeight += VERTICAL_SPACE_BETWEEN_WORDS * getResources().getDisplayMetrics().scaledDensity;
            }
        }

        int contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        int y = (contentHeight - totalHeight) / 2;

        // go over the words again and adjust locations
        for (int i = 0; i < words.size(); i++) {
            final AnimatableWord word = words.get(i);
            final Rect bounds = word.getBounds();
            final int x = (contentWidth - bounds.width()) / 2;

            Rect initialLocation = new Rect(x, -bounds.height(), x + bounds.width(), 0);
            Rect finalLocation = new Rect(initialLocation.left, y, initialLocation.right, y + bounds.height());
            word.setInitialLocation(initialLocation);
            word.setFinalLocation(finalLocation);

            // increase x position
            y += bounds.height();

            if (i < words.size() - 1) {
                // add space between words
                y += VERTICAL_SPACE_BETWEEN_WORDS * getResources().getDisplayMetrics().scaledDensity;
            }
        }
    }

    private void initAnimation() {
        animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setEvaluator(new LinearEaseOutEvaluator(ANIMATION_LENGTH));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                for (AnimatableWord word : words) {
                    // update current locations
                    word.onUpdateAnimation(animatedValue);
                }
                // invalidate the view and therefore force redraw
                invalidate();
            }
        });
        animatorSet.playTogether(valueAnimator);
        animatorSet.setDuration(ANIMATION_LENGTH);
        animatorSet.setStartDelay(DELAY_MS);
        animatorSet.start();
        animationRunning = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // fill entire canvas with background color
        canvas.drawColor(bgColor);

        if (!animationRunning) {
            updateWordLocations();
            initAnimation();
            return; // no drawing for the first frame, just update the animation
        }

        // save canvas state
        canvas.save();

        clippingRect.left = getPaddingLeft();
        clippingRect.top = getPaddingTop();
        clippingRect.right = getWidth() - getPaddingRight();
        clippingRect.bottom = getHeight() - getPaddingBottom();

        for (AnimatableWord word : words) {
            word.draw(canvas);
        }

        // restore previously saved canvas state
        canvas.restore();
    }

    private static List<String> splitWords(String s) {

        if (s == null || s.isEmpty()) {
            // should not happen at all
            return Collections.emptyList();
        }

        String[] words = s.split("\\s+");
        List<String> result = new ArrayList<>(words.length);
        for (String word : words) {
            if (word.trim().isEmpty()) {
                continue;
            }

            result.add(word.trim());
        }

        return result;
    }
}
