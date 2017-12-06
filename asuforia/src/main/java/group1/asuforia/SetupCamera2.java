package group1.asuforia;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by vrajdelhivala on 11/14/17.
 */

//some try catch exceptions yet to done and the application assumes that you have given permissions 
public class SetupCamera2
{
    CameraManager cameraManager;
    Context ctx;
    Size previewSize;
    String cameraId;
    Handler backgroundHandler;
    HandlerThread backgroundThread;
    TextureView.SurfaceTextureListener surfaceTextureListener;
    CameraDevice.StateCallback stateCallback;
    CameraDevice mCameraDevice;
    CameraCaptureSession mCameraCaptureSession;
    TextureView textureView;
    CaptureRequest.Builder captureRequestBuilder;
    CaptureRequest captureRequest;
    ImageReader imageReader;
    int imageFormat = ImageFormat.YUV_420_888;
    PoseListener poseListener;

    Size imageSize = new Size(640,480);

    static {
        System.loadLibrary("asuforia");
    }


    public void setup(TextureView textureView, Context ctx, final PoseListener poseListener)
    {
        this.textureView=textureView;
        this.ctx =ctx;
        this.poseListener = poseListener;

        cameraManager = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);

        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                poseListener.textureAvailable();
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                mCameraDevice = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                cameraDevice.close();
                mCameraDevice = null;
            }
        };
    }
    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    Size[] sizes = streamConfigurationMap.getOutputSizes(imageFormat);
                    for(int i=0;i<sizes.length;i++)
                    {
                        Log.d("Output Sizes ",""+sizes[i].getWidth()+" "+sizes[i].getHeight());
                    }
                    imageReader = ImageReader.newInstance(imageSize.getWidth(),imageSize.getHeight(),imageFormat,5);
                    imageReader.setOnImageAvailableListener(onImageAvailableListener,backgroundHandler);
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        Log.d("Permissions","Here1");
        try {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions","Here1");
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            }
            else
                Log.d("Permissions","Here2");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void createBackgroundThread() {
        backgroundThread = new HandlerThread("thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(imageSize.getWidth(),imageSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (mCameraDevice == null) {
                                return;
                            }

                            try {
                                captureRequest = captureRequestBuilder.build();
                                mCameraCaptureSession = cameraCaptureSession;
                                mCameraCaptureSession.setRepeatingRequest(captureRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    //camera 2 API to access images.

    float runningAverage = 0;
    int count = 0;

    private final ImageReader.OnImageAvailableListener onImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            try {

                Image img = reader.acquireLatestImage();
                long startTime = System.currentTimeMillis();
                long stopTime = 0;
                long totalTime = 0;

                float[] vectors = NativeCallMethods.nativePoseEstimation(img.getWidth(), img.getHeight(), img.getPlanes()[0].getBuffer());

                float rvecs[] = null;
                float tvecs[] = null;

                if (vectors != null) {
                    rvecs = new float[3];
                    tvecs = new float[3];
                    rvecs[0] = vectors[0];
                    rvecs[1] = vectors[1];
                    rvecs[2] = vectors[2];
                    tvecs[0] = vectors[3];
                    tvecs[1] = vectors[4];
                    tvecs[2] = vectors[5];

                }
                poseListener.onPose(img, rvecs, tvecs);

                img.close();
                stopTime=System.currentTimeMillis();

                totalTime=stopTime-startTime;

                count ++;
                Log.d("ImageCount",""+count);
                runningAverage=(runningAverage*1.00f*(count-1)+totalTime)/count;
                Log.d("ExecutionTime",""+runningAverage);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }



        }

    };


     public void onForeground() {
        createBackgroundThread();
        if (textureView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    public void onBackground() {
        closeCamera();
        killBackgroundThread();
    }

    private void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if(imageReader!=null){
            imageReader.close();
            imageReader=null;
        }
    }

    private void killBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }




}
