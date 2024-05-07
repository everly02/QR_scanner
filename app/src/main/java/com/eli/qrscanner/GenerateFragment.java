package com.eli.qrscanner;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

public class GenerateFragment extends Fragment {
    private static final int REQUEST_IMAGE = 1;
    private Button selectImageButton, selectColorButton, generateQRButton,saveButton;
    private EditText textInput;
    private ImageView qrCodeImage;
    private Bitmap selectedImage;
    private int selectedColor = Color.BLACK;
    private String inputText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate, container, false);

        selectImageButton = view.findViewById(R.id.select_image_button);
        selectColorButton = view.findViewById(R.id.select_color_button);
        generateQRButton = view.findViewById(R.id.generate_qr_button);
        textInput = view.findViewById(R.id.text_input);
        qrCodeImage = view.findViewById(R.id.qr_code_image);
        saveButton = view.findViewById(R.id.save_to_album_button);

        selectImageButton.setOnClickListener(v -> selectImage());
        selectColorButton.setOnClickListener(v -> selectColor());
        generateQRButton.setOnClickListener(v -> generateQRCode());

        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void selectColor() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_picker, null);
        Slider redSlider = dialogView.findViewById(R.id.slider_red);
        Slider greenSlider = dialogView.findViewById(R.id.slider_green);
        Slider blueSlider = dialogView.findViewById(R.id.slider_blue);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle("选择颜色")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    int red = (int) redSlider.getValue();
                    int green = (int) greenSlider.getValue();
                    int blue = (int) blueSlider.getValue();
                    selectedColor = Color.rgb(red, green, blue);
                })
                .setNegativeButton("取消", null);
        builder.show();
    }

    private void generateQRCode() {
        if (inputText.isEmpty()) {
            return;
        }

        try {
            Bitmap qrCode = createQRCode(inputText, 400, 400, selectedColor);
            if (selectedImage != null) {
                qrCode = overlayBitmap(qrCode, selectedImage);
            }
            qrCodeImage.setImageBitmap(qrCode);
            Bitmap finalQrCode = qrCode;
            saveButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveQRCode(finalQrCode);
                        }
                    }
            );
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Bitmap createQRCode(String content, int width, int height, int color) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, String.valueOf(ErrorCorrectionLevel.H));
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = bitMatrix.get(x, y) ? color : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private Bitmap overlayBitmap(Bitmap baseBitmap, Bitmap overlayBitmap) {
        Bitmap result = Bitmap.createBitmap(baseBitmap.getWidth(), baseBitmap.getHeight(), baseBitmap.getConfig());
        android.graphics.Canvas canvas = new android.graphics.Canvas(result);
        canvas.drawBitmap(baseBitmap, 0, 0, null);
        int left = (baseBitmap.getWidth() - overlayBitmap.getWidth()) / 2;
        int top = (baseBitmap.getHeight() - overlayBitmap.getHeight()) / 2;
        canvas.drawBitmap(overlayBitmap, left, top, null);
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                // Optionally, resize the image
                selectedImage = Bitmap.createScaledBitmap(selectedImage, 100, 100, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveQRCode(Bitmap qrCode) {
        String fileName = "QRCode_" + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri)) {
                qrCode.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(getActivity(), "QR Code saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Failed to save QR Code", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
