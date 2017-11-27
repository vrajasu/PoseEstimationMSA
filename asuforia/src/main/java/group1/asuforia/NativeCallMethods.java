package group1.asuforia;

import java.nio.ByteBuffer;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

public class NativeCallMethods {

    static {
        System.loadLibrary("asuforia");
    }

    public static native void generateReferenceImage(String path);
    public static native float[] nativePoseEstimation(int width, int height, ByteBuffer buffer);
}

