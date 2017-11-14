package group1.poseestimationmsa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.autofill.AutofillManager;
import android.widget.TextView;

import group1.asuforia.SetupCamera2;

public class MainActivity extends AppCompatActivity{

    TextureView textureView;
    SetupCamera2 s= new SetupCamera2();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textureView = (TextureView)findViewById(R.id.texureSurface);

        s.setup(textureView,MainActivity.this);

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
