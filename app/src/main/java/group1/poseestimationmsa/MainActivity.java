package group1.poseestimationmsa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.autofill.AutofillManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import group1.asuforia.Asuforia;
import group1.asuforia.PoseListener;
import group1.asuforia.SetupCamera2;

public class MainActivity extends AppCompatActivity{

    TextureView textureView;
    File mReferenceImage;
    Asuforia asuforia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textureView = (TextureView)findViewById(R.id.texureSurface);

        try{
            InputStream is = getResources().openRawResource(R.raw.reference);

            File cascadeDir = getDir("ref", Context.MODE_PRIVATE);

            mReferenceImage = new File(cascadeDir, "referenceImage.jpg");
            FileOutputStream os = new FileOutputStream(mReferenceImage);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        final Surface[] surface = new Surface[1];


        PoseListener poseListener = new PoseListener() {
            @Override
            public void onPose(Image image,float rvec[],float tvec[]) {
                NativeDraw.drawPose(image,surface[0],rvec,tvec);
            }

            @Override
            public void textureAvailable() {
                surface[0] =  new Surface(textureView.getSurfaceTexture());
            }
        };
        asuforia = new Asuforia(poseListener,mReferenceImage.getAbsolutePath(),surface[0]);
        asuforia.startEstimation(textureView,MainActivity.this,poseListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        asuforia.onForeground();

    }
    @Override
    protected void onPause() {
        super.onPause();
        asuforia.onBackground();
    }

}
