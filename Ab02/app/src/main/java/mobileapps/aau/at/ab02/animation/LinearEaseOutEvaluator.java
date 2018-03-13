package mobileapps.aau.at.ab02.animation;

import android.animation.TypeEvaluator;

/**
 * Ease out evaluator.
 *
 * <p>
 *     This is required for the animation to work.
 *     The easing algorithm is based on Robert Penner's work.
 * </p>
 */
class LinearEaseOutEvaluator implements TypeEvaluator<Number> {

    private float duration;

    LinearEaseOutEvaluator(float duration) {

        if (duration < 0) {
            throw new IllegalArgumentException("delay < 0");
        }

        this.duration = duration;
    }

    @Override
    public Number evaluate(float fraction, Number startValue, Number endValue) {
        float t = duration * fraction;
        float b = startValue.floatValue();
        float c = endValue.floatValue() - startValue.floatValue();
        float d = duration;

        return linearEaseOut(t, b, c, d);
    }

    private static float linearEaseOut(float t, float b, float c, float d) {
        return c*t/d + b;
    }
}
