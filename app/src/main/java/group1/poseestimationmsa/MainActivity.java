package group1.poseestimationmsa;

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
import java.nio.ByteBuffer;

import group1.asuforia.PoseListener;
import group1.asuforia.SetupCamera2;

public class MainActivity extends AppCompatActivity{

    TextureView textureView;
    SetupCamera2 s= new SetupCamera2();
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textureView = (TextureView)findViewById(R.id.texureSurface);

        final Surface[] surface = new Surface[1];


        PoseListener poseListener = new PoseListener() {
            @Override
            public void onPose(Image image) {
                NativeDraw.draw(image,surface[0]);
            }

            @Override
            public void textureAvailable() {
                surface[0] =  new Surface(textureView.getSurfaceTexture());
            }
        };
        s.setup(textureView,MainActivity.this,poseListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        s.onForeground();

    }
    @Override
    protected void onPause() {
        super.onPause();
        s.onBackground();
    }

}
