package group1.asuforia;

import java.nio.ByteBuffer;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

// This class has native methods that are implemented
// using OpenCV

public class NativeCallMethods {

    static {
        System.loadLibrary("asuforia");
    }

    // Method: generateReferenceImage
    // Arguments -> Input image path
    //
    // This method loads the image at the given path.
    // Then we find the features in the given image and
    // descriptors for these features using ORB
    public static native void generateReferenceImage(String path);

    // Method: nativePoseEstimation
    // Arguments -> width, height of the image and imagebuffer
    //
    // This method takes input image as an argument and detects
    // features and descriptors in this image. The descriptors are
    // then used to identify corresponding points in the image.
    // Using these points, we find out rotation and translation
    // vectors.
    public static native float[] nativePoseEstimation(int width, int height, ByteBuffer buffer);
}

