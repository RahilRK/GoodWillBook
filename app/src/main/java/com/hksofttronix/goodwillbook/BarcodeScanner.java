package com.hksofttronix.goodwillbook;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hksofttronix.goodwillbook.Pay.Pay;
import com.hksofttronix.goodwillbook.Util.CameraSource;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.regex.Pattern;

public class BarcodeScanner extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = BarcodeScanner.this;

    Globalclass globalclass;

    ImageView ivqrcode;
    FloatingActionButton fabqrscanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        init();
        binding();
        setToolbar();
        generateQRcode();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
    }

    void binding()
    {
        ivqrcode = findViewById(R.id.ivqrcode);
        fabqrscanner = findViewById(R.id.fabqrscanner);

        fabqrscanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestCameraPermission();
            }
        });
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void generateQRcode()
    {
        String text = globalclass.getmobilenumber();
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try
        {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,350,350);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivqrcode.setImageBitmap(bitmap);
        }
        catch (WriterException e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_generateQRcode",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","generateQRcode","",error);
            globalclass.toast_long(getResources().getString(R.string.generateQRcodeError));
        }
    }

    void requestCameraPermission()
    {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                       openScanner();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {
                        globalclass.log(TAG,"Camera permission Denied");
                        globalclass.snackit(activity,"Permission required to make call");
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {

                        globalclass.log(TAG+"_requestCameraPermission",error.toString());
                        globalclass.sendLog(Globalclass.TryCatchException,TAG,"","requestCameraPermission","",error.toString());
                    }
                }).check();
    }

    void openScanner()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.scanbarcode_dialogue);
        dialog.setCancelable(true);

        final SurfaceView surfaceView = dialog.findViewById(R.id.surfaceView);
        ImageView ivclose = dialog.findViewById(R.id.ivclose);
        LinearLayout flashlightlo = dialog.findViewById(R.id.flashlightlo);
        ToggleButton toggleButton = dialog.findViewById(R.id.togglebt);

        PackageManager pm = activity.getPackageManager();

        // if device support mcamera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            globalclass.toast_long("Flash not available in this device...!");
            flashlightlo.setVisibility(View.GONE);
        } else {
            flashlightlo.setVisibility(View.VISIBLE);
        }

        ivclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        final BarcodeDetector barcodeDetector;
        final CameraSource cameraSource;

        barcodeDetector = new BarcodeDetector.Builder(activity)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(activity, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
//                .setAutoFocusEnabled(true) //you should add this feature
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                try
                {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        globalclass.snackit(activity, "Camera permission required to scan barcode!");
                        return;
                    }

                    cameraSource.start(surfaceView.getHolder());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(TAG+"_surfaceCreatedException",error);
                    globalclass.toast_long(getResources().getString(R.string.errorinscanningqr));
                    globalclass.sendLog(Globalclass.TryCatchException,TAG,"","surfaceCreated","",error);
                    dialog.dismiss();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                try
                {
                    cameraSource.stop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(TAG+"_surfaceDestroyedException",error);
                    globalclass.toast_long(getResources().getString(R.string.errorinscanningqr));
                    globalclass.sendLog(Globalclass.TryCatchException,TAG,"","surfaceDestroyed","",error);
                    dialog.dismiss();
                }

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                try
                {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                    if(barcodes.size() > 0)
                    {
                        barcodeDetector.release();
                        final String scanresult = barcodes.valueAt(0).displayValue;
                        globalclass.log(TAG,scanresult);
                        dialog.dismiss();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run()
                            {
                                if(!Pattern.matches(globalclass.getMobileNoRegex(),scanresult))
                                {
                                    globalclass.toast_long(getResources().getString(R.string.invalidqrcode));
                                    return;
                                }

                                Intent intent = new Intent(activity, Pay.class);
                                intent.putExtra("mobilenumber",scanresult);
                                startActivity(intent);
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    String error = Log.getStackTraceString(e);
//                    globalClass.log("receiveDetectionsException",error);
//                    globalClass.toastishort("Something went wrong in scanning, try again later..!");
//                    mydatabase.sendLog("generateOwnBill - receiveDetectionsException",error);
                    dialog.dismiss();
                }
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                cameraSource.release();
            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                try
                {
                    if(isChecked)
                    {
                        cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    }
                    else
                    {
                        cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                }
                catch (Exception e)
                {
                    String error = Log.getStackTraceString(e);
//                    globalClass.log(tag,error);
                }
            }
        });
        dialog.show();
    }
}
