package group1.poseestimationmsa;

import android.media.Image;
import android.view.Surface;

import java.nio.ByteBuffer;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

public class NativeDraw {
    static{
        System.loadLibrary("opendraw");
    }

    public static native String getHello();

    public static void draw(Image img, Surface surface)
    {
        drawNative(img.getWidth(), img.getHeight(), img.getPlanes()[0].getBuffer(),surface);
    }
    public static void drawPose(Image img, Surface surface, float[] rvec, float[] tvec) {
        drawNativePose(img.getWidth(), img.getHeight(), img.getPlanes()[0].getBuffer(),surface,rvec,tvec);
    }

    public static native void drawNative(int width, int height, ByteBuffer buffer, Surface surface);
    public static native void drawNativePose(int width, int height, ByteBuffer buffer, Surface surface,float[] rvec, float[] tvec);


}
