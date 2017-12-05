# PoseEstimationMSA
Pre-requisites:
1) Android studio with ndk packages installed
2) The app has dependencies on Android openCV sdk(version 3.2)
    Link: 
    https://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.2.0/opencv-3.2.0-android-sdk.zip/download

Build the app:
1) Change the path in Android.mk in below two files to the place where you extracted Android openCV sdk.
   (If you can't find these files, try building the app without below changes. You should find links of these files in the error window of android studio)
    ./app/src/main/jni/Android.mk:OPENCV_ROOT:=/home/gunman/Downloads/software/OpenCV-android-sdk/
    ./asuforia/src/main/jni/Android.mk:OPENCV_ROOT:=/home/gunman/Downloads/software/OpenCV-android-sdk/
2) If you want to change the reference image, place replace the reference image in the below path. 
   Make sure name of the file is reference.jpg and file format is jpg.
    ./app/src/main/res/raw/reference.jpg
    
 
    
