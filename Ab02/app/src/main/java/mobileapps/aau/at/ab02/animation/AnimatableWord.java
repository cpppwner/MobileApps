package mobileapps.aau.at.ab02.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;

/**
 * Special class representing an animatable word.
 */
class AnimatableWord {

    private final String word;
    private final TextPaint textPaint;
    private final Rect bounds;

    private Rect initialLocation;
    private Rect finalLocation;
    private Rect currentLocation;

    AnimatableWord(String word, TextPaint textPaint) {
        this.word = word;
        this.textPaint = textPaint;
        this.bounds = new Rect();

        // get bounds of the word
        textPaint.getTextBounds(word, 0, word.length(), bounds);
    }

    Rect getBounds() {
        return bounds;
    }

    void setInitialLocation(Rect initialLocation) {
        this.initialLocation = initialLocation;
        currentLocation = new Rect(initialLocation);
    }

    void setFinalLocation(Rect finalLocation) {
        this.finalLocation = finalLocation;
    }

    void onUpdateAnimation(float value) {

        // clamp value in range 0.0f - 1.0f
        value = Math.max(0.0f, Math.min(1.0f, value));

        float deltaY = finalLocation.top - initialLocation.top;
        currentLocation.top = initialLocation.top + Math.round(deltaY * value);
        currentLocation.bottom = currentLocation.top + bounds.height();

        float deltaX = finalLocation.left - initialLocation.left;
        currentLocation.left = initialLocation.left + Math.round(deltaX * value);
        currentLocation.right = currentLocation.left + bounds.width();
    }

    void draw(Canvas canvas) {
        Rect clipBounds = canvas.getClipBounds();
        int canvasHeight = clipBounds.height();
        int canvasWidth = clipBounds.width();

        canvas.drawText(word, currentLocation.left, currentLocation.top, textPaint);
    }
}
