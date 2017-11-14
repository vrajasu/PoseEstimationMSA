package group1.asuforia;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

public class NativeCallMethods {

    static {
        System.loadLibrary("asuforia");
    }

    public static native String getHelloNative();
}

