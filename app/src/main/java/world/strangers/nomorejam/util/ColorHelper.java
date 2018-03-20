package world.strangers.nomorejam.util;

import android.animation.ArgbEvaluator;

/**
 * Created by luhonghai on 3/19/18.
 */

public class ColorHelper {

    public static int getColorOfDegradate(int colorStart, int colorEnd, int percent){
        return (int) new ArgbEvaluator().evaluate(percent / 100.0f, colorStart, colorEnd);
    }

    private static int getColorOfDegradateCalculation(int colorStart, int colorEnd, int percent){
        return ((Math.min(colorStart, colorEnd)*(100-percent)) + (Math.max(colorStart, colorEnd)*percent)) / 100;
    }
}
