package group1.asuforia;

import android.content.Context;
import android.view.Surface;
import android.view.TextureView;


/**
 * Created by vrajdelhivala on 11/26/17.
 */

// The main library module of the Asuforia library.
// This class implements the pose-estimation method
// which returns the rotation and translation vectors.
// This information is then used by the app to draw the
// cube on the marker-image

// Asuforia class has four member objects which are initialized
// with the default constructor
// poselistener -> This is the interface between library and app
//                 Implementation to be provided by developer
// refImagePath -> Path to the reference image
// Surface      -> A Surface object
// camera2      -> A camera2 API object

public class Asuforia {
    PoseListener poseListener;
    String refImagePath = "";
    Surface surface;
    SetupCamera2 camera2 = new SetupCamera2();

    //default constructor - poselistener is implemented in the application developer's main activity
    public Asuforia(PoseListener poseListener,String refImagePath,Surface surface)
    {
        this.poseListener = poseListener;
        this.refImagePath=refImagePath;
        this.surface=surface;

        NativeCallMethods.generateReferenceImage(refImagePath);
    }
    public void startEstimation(TextureView textureView, Context context,PoseListener poseListener)
    {
        camera2.setup(textureView,context,poseListener);
    }
    public void onForeground()
    {
        camera2.onForeground();
    }
    public void onBackground()
    {
        camera2.onBackground();
    }

}
