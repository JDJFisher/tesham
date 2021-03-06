package maths;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by Jordan Fisher on 24/06/2017.
 */
public class Maths
{
    private Maths()
    {
    }

    public static float sigmoid(float f)
    {
        return 1f / (1 + (float) FastMath.pow(FastMath.E, -f));
    }

    public static float floorMod(float x, float y)
    {
        int quo = (int)(x / y);
        return x - quo * y;
    }

    public static float max(float f0, float f1, float... fs)
    {
        float result = FastMath.max(f0, f1);
        for (float f : fs)
        {
            result = FastMath.max(result, f);
        }
        return result;
    }
}
