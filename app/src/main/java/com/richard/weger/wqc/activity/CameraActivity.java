package com.richard.weger.wqc.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.GeneralPicturesProcessorScheduler;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.richard.weger.wqc.util.App.getStringResource;

@SuppressWarnings("deprecated")
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, GeneralPicturesProcessorScheduler.GeneralPicturesProcessorListener {

    private Camera mCamera;
    private SurfaceHolder surfaceHolder;
    private GeneralPicturesProcessorScheduler ex;

    List<String> newPictures = new ArrayList<>();
    List<String> processed = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        FloatingActionButton capture_image = findViewById(R.id.takeButton);
        capture_image.setOnClickListener(v -> capture());
        SurfaceView surfaceView = findViewById(R.id.surfaceView);

        ex = new GeneralPicturesProcessorScheduler(new ArrayList<>(), ProjectHelper.getProject(), this);

        surfaceView.setOnTouchListener((v, event) -> {

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                float x = event.getX();
                float y = event.getY();

                Rect touchRect = new Rect(
                        (int)(x - 100),
                        (int)(y - 100),
                        (int)(x + 100),
                        (int)(y + 100));


                final Rect targetFocusRect = new Rect(
                        touchRect.left * 2000 / surfaceView.getWidth() - 1000,
                        touchRect.top * 2000 / surfaceView.getHeight() - 1000,
                        touchRect.right * 2000 / surfaceView.getWidth() - 1000,
                        touchRect.bottom * 2000 / surfaceView.getHeight() - 1000);

                doTouchFocus(targetFocusRect);


            }
            v.performClick();

            return false;
        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CameraActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    private void capture() {
        findViewById(R.id.takeButton).setEnabled(false);
        mCamera.takePicture(null, null, null, (data, camera) -> {
            try {
                Toast.makeText(getApplicationContext(), getStringResource(R.string.okTag) + "!",
                        Toast.LENGTH_SHORT).show();
                String newPicName = StringHelper.getGeneralPictureFilename(ProjectHelper.getProject());
                String filePath = StringHelper.getPicturesFolderPath(ProjectHelper.getProject()) + newPicName;
                File f = new File(filePath);
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(data);
                    newPictures.add(newPicName);
                    ex.appendAndProccess(Collections.singletonList(newPicName));

                    TextView tvPicCount = findViewById(R.id.tvPicCount);
                    if (tvPicCount != null) {
                        tvPicCount.setText(String.valueOf(newPictures.size()));
                        tvPicCount.setVisibility(View.VISIBLE);
                    }
                    mCamera.stopPreview();
                    mCamera.startPreview();
                    findViewById(R.id.takeButton).setEnabled(true);
                } catch (Exception ex) {
                    handleException(ex);
                }
            } catch (Exception ex2) {
                handleException(ex2);
            }
        });
    }

    private void handleException(Exception e) {
        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_PHOTO_CAPTURE_SETTING_EXCEPTION, getStringResource(R.string.photoCaptureError), ErrorResult.ErrorLevel.SEVERE);
        LoggerManager.getLogger(CameraActivity.class).severe(StringHelper.getStackTraceAsString(e));
        ErrorResponseHandler.handle(err, this::finishAndRemoveTask);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.e("Surface Changed", "format   ==   " + format + ",   width  ===  "
                + width + ", height   ===    " + height);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("Surface Created", "");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("Surface Destroyed", "");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("newPictures", new ArrayList<>(newPictures));
        if(ex != null) {
            ex.stopAndGetNotProcessedPics();
            intent.putStringArrayListExtra("processedPictures", new ArrayList<>(processed));
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(surfaceHolder);

            Camera.Parameters parameters = mCamera.getParameters();
            try {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

                if (display.getRotation() == Surface.ROTATION_0) {
//                parameters.setPreviewSize(height, width);
                    parameters.setRotation(90);
                    mCamera.setDisplayOrientation(90);
                }

                if (display.getRotation() == Surface.ROTATION_90) {
//                parameters.setPreviewSize(width, height);
                }

                if (display.getRotation() == Surface.ROTATION_180) {
//                parameters.setPreviewSize(height, width);
                }

                if (display.getRotation() == Surface.ROTATION_270) {
//                parameters.setPreviewSize(width, height);
                    parameters.setRotation(180);
                    mCamera.setDisplayOrientation(180);
                }

                mCamera.setParameters(parameters);
            } catch (Exception ignored) {
                LoggerManager.getLogger(CameraActivity.class).warning("Failed to set rotation parameters of the camera!");
            }

            try {
                Camera.Size defaultSize = parameters.getSupportedPictureSizes().stream().filter(p -> p.width == 1920 && p.height == 1080).findFirst().orElse(null);
                Camera.Size mSize = null;
                if (defaultSize != null) {
                    mSize = defaultSize;
                } else {
                    for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                        if (mSize == null || (size.width > mSize.width && size.height > mSize.height && size.width < 1920 && size.height < 1080)) {
                            mSize = size;
                        }
                    }
                }
                if (mSize != null) {
                    parameters.setPictureSize(mSize.width, mSize.height);
                    parameters.setPreviewSize(mSize.width, mSize.height);
                }

                mCamera.setParameters(parameters);

            } catch (Exception ignored) {
                LoggerManager.getLogger(CameraActivity.class).warning("Failed to set size parameters of the camera!");
            }

            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    private Camera.AutoFocusCallback myAutoFocusCallback = (start, camera) -> {
        if(start) {
            mCamera.cancelAutoFocus();
        }
    };

    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            mCamera.setParameters(param);

            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("AutoFocus", "Unable to autofocus");
        }
    }

    @Override
    public void onPicturesListProcessFinish() {

    }

    @Override
    public void onPictureProcessed(String picName) {
        processed.add(picName);
    }
}
