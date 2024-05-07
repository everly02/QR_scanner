package com.eli.qrscanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eli.qrscanner.room.ScanRecord;
import com.eli.qrscanner.room.ScanRecordDao;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScanFragment extends Fragment implements ImageAnalysis.Analyzer, ActivityCompat.OnRequestPermissionsResultCallback {
    @Inject
    ScanRecordDao scanRecordDao;
    @Inject
    Executor executor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private String previousResult = "";
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

    private PreviewView previewView;
    private TextView resultTextView;

    private boolean handled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        previewView = view.findViewById(R.id.preview_view);
        resultTextView = view.findViewById(R.id.result_text_view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS, REQUEST_CAMERA_PERMISSION);
        } else {
            resetHandledFlag();
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), this);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class) @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        if (imageProxy.getImage() == null) {
            return;
        }

        Image image = imageProxy.getImage();
        AtomicReference<String> category = new AtomicReference<>("");
        InputImage inputImage = InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees());
        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty() && !handled) {
                        Barcode barcode = barcodes.get(0);
                        String result = barcode.getRawValue();

                        if (result != null && !result.equals(previousResult)) {
                            int valueType = barcode.getValueType();
                            resultTextView.setText(result);
                            switch (valueType) {
                                case Barcode.TYPE_URL:
                                    handleUrl(result);
                                    category.set("url");
                                    break;
                                case Barcode.TYPE_TEXT:
                                    handleText(result);
                                    category.set("text");
                                    break;
                                case Barcode.TYPE_PHONE:
                                    handlePhoneNumber(result);
                                    category.set("phone");
                                    break;
                                case Barcode.TYPE_EMAIL:
                                    handleEmail(result);
                                    category.set("email");
                                    break;
                                default:
                                    handleUnknown(result);
                                    category.set("none");
                                    break;
                            }
                            handled = true;
                            previousResult = result;
                            ScanRecord scanRecord = new ScanRecord(result, category.get(),false);
                            executor.execute(() -> {new Runnable(){
                                    @Override
                                    public void run() {
                                        scanRecordDao.insert(scanRecord);
                                    }
                                };
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                });
    }

    private void handleUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void handleText(String text) {
        resultTextView.setText(text);
    }

    private void handlePhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void handleEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        startActivity(intent);
    }

    private void handleUnknown(String result) {
        resultTextView.setText("未知类型: " + result);
    }
    public void resetHandledFlag() {
        handled = false;
        previousResult = "";
    }
}