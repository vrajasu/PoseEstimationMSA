package group1.asuforia;

import android.media.Image;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

public interface PoseListener {

    public void onPose(Image image);
    public void textureAvailable();
}
