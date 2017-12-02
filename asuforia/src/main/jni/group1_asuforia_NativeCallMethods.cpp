#include <jni.h>
#include <group1_asuforia_NativeCallMethods.h>
#include <android/log.h>
#include <algorithm>
#include <opencv2/opencv.hpp>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "PoseEstimationMSA", __VA_ARGS__)


using namespace cv;
using namespace std;

using namespace cv;
using namespace std;

vector<KeyPoint> keypointsReference;
Mat descriptorsReference;
vector<Point3f>  p3d;
int numFeautresReference = 500;
int numFeautresDest = 500;

JNIEXPORT jstring JNICALL Java_group1_asuforia_NativeCallMethods_getHelloNative
  (JNIEnv *, jclass){

    return jstring("");
}
//executes when the application starts.
JNIEXPORT void JNICALL
Java_group1_asuforia_NativeCallMethods_generateReferenceImage(JNIEnv *env, jclass type,jstring path1) {

    const char *path = env->GetStringUTFChars(path1, 0);

    Mat referenceImage = imread(path,0);

    Ptr<FeatureDetector> detector = ORB::create(numFeautresReference,1.2f,8,31,0,2,ORB::HARRIS_SCORE,31,20);


    //extracting keypoints and descriptors once for the refernce image and storing them as global variables.
    detector->detect(referenceImage, keypointsReference);
    detector->compute(referenceImage, keypointsReference, descriptorsReference);

    LOGE("Size %d %d %d",keypointsReference.size(),referenceImage.rows,referenceImage.cols);

    const double heightAbove = 25.0;
    const double cx= referenceImage.rows/2;
    const double cy = referenceImage.cols/2;
    const double fx = cx*1.73;
    const double fy = cy*1.73;

    for (int i = 0; i<keypointsReference.size(); i++) {
        float x = keypointsReference[i].pt.x;	// 2D location in image
        float y = keypointsReference[i].pt.y;
        float X = (heightAbove / fx)*(x - cx);
        float Y = (heightAbove / fy)*(y - cy);
        float Z = 0;
        p3d.push_back(cv::Point3f(X, Y, Z));
    }
    env->ReleaseStringUTFChars(path1, path);

    return;
}
//this executes on every image frame. Note that we already have the values from the reference image stored as global variables.
JNIEXPORT jfloatArray JNICALL
Java_group1_asuforia_NativeCallMethods_nativePoseEstimation(JNIEnv *env, jclass type, jint srcWidth,
                                                            jint srcHeight, jobject srcBuffer) {

    uint8_t *srcLumaPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));


    cv::Mat mYuvGray(srcHeight, srcWidth, CV_8UC1, srcLumaPtr); //only getting the luma channel


    const double cx = mYuvGray.rows / 2;
    const double cy = mYuvGray.cols / 2;
    const double fx = 1.73 * cx;
    const double fy = 1.73 * cy;
    //adding our homography code here
    //starting with extracting feature points from the destination image
    std::vector<cv::KeyPoint> keypointsDest;
    cv::Mat descriptorsDest;

    //extracting keypoints from the incoming image frame
    Ptr<FeatureDetector> detector = ORB::create(numFeautresDest, 1.2f, 8, 31, 0, 2,
                                                ORB::HARRIS_SCORE, 31, 20);
    detector->detect(mYuvGray, keypointsDest);
    detector->compute(mYuvGray, keypointsDest, descriptorsDest);

    //extracting keypoints done
    cv::Mat distances;
    int k = 2;
    std::vector<std::vector<cv::DMatch> > knnMatches;

    if (descriptorsReference.type() == CV_8U) { //if orb


        //using a FLANN based matcher to match keypoints from Reference Image and Input Image
        cv::FlannBasedMatcher matcher(new flann::LshIndexParams(20, 10, 2));


        if (descriptorsDest.rows > 20) {
            matcher.knnMatch(
                    descriptorsDest,
                    descriptorsReference,
                    knnMatches,
                    k);
        }

    } else {
        //if SIFT
        cv::FlannBasedMatcher matcher;

        if (descriptorsDest.rows > 20) {
            matcher.knnMatch(
                    descriptorsDest,
                    descriptorsReference,
                    knnMatches,
                    k);
        }
    }

    if (distances.type() == CV_32S) {
        cv::Mat temp;
        distances.convertTo(temp, CV_32F);
        distances = temp;
    }

    std::vector<cv::KeyPoint> kp2;
    std::vector<cv::DMatch> goodMatches;

    //pass the matches through a ratio test to determine the best matches
    for (size_t i = 0; i < knnMatches.size(); i++) {
        const cv::DMatch &match1 = knnMatches[i][0];
        const cv::DMatch &match2 = knnMatches[i][1];


        if (match1.distance < 0.8 * match2.distance)
            goodMatches.push_back(match1);
    }


    if (goodMatches.size() < 4) {
        //findHomography needs a minimum of 4 points.
    } else {
        std::vector<cv::Point2f> pts1(goodMatches.size());
        std::vector<cv::Point2f> pts2(goodMatches.size());
        for (size_t i = 0; i < goodMatches.size(); i++) {
            pts1[i] = keypointsReference[goodMatches[i].trainIdx].pt;
            pts2[i] = keypointsDest[goodMatches[i].queryIdx].pt;
        }


        std::vector<unsigned char> inliersMask(pts1.size());
        cv::Mat homography = cv::findHomography(pts1, pts2, cv::FM_RANSAC, 5, inliersMask);

        std::vector<cv::DMatch> inliers;
        for (size_t i = 0; i < inliersMask.size(); i++) {
            if (inliersMask[i])
                inliers.push_back(goodMatches[i]);
        }

        if (inliers.size() > 5) {

            std::vector<cv::Point2f> p2D;
            std::vector<cv::Point3f> p3D;
            for (unsigned int i = 0; i < inliers.size(); i++) {

                int i1 = inliers[i].trainIdx;
                p3D.push_back(p3d[i1]);
                int i2 = inliers[i].queryIdx;
                p2D.push_back(keypointsDest[i2].pt);
            }

            double data[9] = {fx, 0, cx, 0, fy, cy, 0, 0, 1};

            //make the camera intrinsic parameters matrix
            cv::Mat K = cv::Mat(3, 3, CV_64F, data);

            cv::Mat rotVec, transVec;
            bool foundPose = cv::solvePnP(p3D, p2D, K, cv::Mat::zeros(5, 1, CV_64F), rotVec,
                                          transVec);

            if(foundPose)
            {
                float result[6];
                result[0] = float(rotVec.at<double>(0,0));
                result[1] = float(rotVec.at<double>(1,0));
                result[2] = float(rotVec.at<double>(2,0));
                result[3] = float(transVec.at<double>(0,0));
                result[4] = float(transVec.at<double>(1,0));
                result[5] = float(transVec.at<double>(2,0));

                jfloatArray output = env->NewFloatArray(6);
                env->SetFloatArrayRegion( output, 0, 6, &result[0] );


                return output;
            }
            else{
                return NULL;
            }
        }
    }

    return NULL;
}


