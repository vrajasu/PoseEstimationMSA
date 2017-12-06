package group1.asuforia;

import android.media.Image;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

// PoseListener is the interface between the library and
// the app.
// The interface provides 2 methods that must be implemented
// in the app:
//      onPose           -> Tasks to be completed once the pose is available
//      textureAvailable -> Callback method
    
public interface PoseListener {

    public void onPose(Image image,float[] rvecs,float[] tvces);
    public void textureAvailable();
}
