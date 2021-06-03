package ru.sastsy.dental;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class CameraFragment extends Fragment {

    ImageView imageView;
    ImageButton gallery, camera;
    File mImageFile;
    RadioButton radioButton1, radioButton2, radioButton3;
    CircularProgressIndicator progressBar;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        imageView = view.findViewById(R.id.imageView);
        gallery = view.findViewById(R.id.gallery);
        camera = view.findViewById(R.id.camera);
        radioButton1 = view.findViewById(R.id.radioButton1);
        radioButton2 = view.findViewById(R.id.radioButton2);
        radioButton3 = view.findViewById(R.id.radioButton3);
        progressBar = view.findViewById(R.id.progress_circular_camera);
        radioButton2.setChecked(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        camera.setOnClickListener(view -> {
            //Opens camera dialog
            EasyImage.openCameraForImage(getActivity(), 100);
        });

        gallery.setOnClickListener(view -> {
            //Opens gallery picker
            EasyImage.openGallery(getActivity(), 100);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(getContext(), "Ошибка при выборе изображения", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {

                mImageFile = imageFiles.get(0);

                // Convert chosen image into Bitmap to process it
                Bitmap bitmap = new BitmapFactory().decodeFile(imageFiles.get(0).getAbsolutePath());
                InputImage image = InputImage.fromBitmap(bitmap, 0);
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);
                // Set options for FaceDetector
                FaceDetectorOptions options =
                        new FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                                .setMinFaceSize(0.15f)
                                .enableTracking()
                                .build();

                FaceDetector detector = FaceDetection.getClient(options);


                detector.process(image)
                                .addOnSuccessListener(
                                        faces -> {
                                            // Loop through all detected faces
                                            for (Face face : faces) {
                                                // Get landmarks of mouth area
                                                FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                                                FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                                                FaceLandmark mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
                                                FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);

                                                // Checking if lips are present in the picture
                                                if (face.getContour(FaceContour.LOWER_LIP_TOP) != null && face.getContour(FaceContour.UPPER_LIP_BOTTOM) != null &&
                                                        mouthLeft != null && mouthRight != null && noseBase != null && mouthBottom != null) {
                                                    // Get contours of teeth area
                                                    List<PointF> mouth_upper_points = face.getContour(FaceContour.LOWER_LIP_TOP).getPoints();
                                                    List<PointF> mouth_lower_points = face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                    List<PointF> mouth_points = new ArrayList<>();
                                                    mouth_points.addAll(mouth_lower_points);
                                                    mouth_points.addAll(mouth_upper_points);

                                                    // Create a polygon for teeth area
                                                    Path mouthShape = createTeethPolygon(mouth_points);

                                                    // Create Bitmap with masked teeth area
                                                    Bitmap maskBitmap = getMaskBitmap(mutableBitmap, mouthShape);

                                                    // Get coordinates of rectangle to be processed
                                                    int startX = (int) mouthLeft.getPosition().x;
                                                    int endX = (int) mouthRight.getPosition().x;
                                                    int startY = (int) noseBase.getPosition().y;
                                                    int endY = (int) mouthBottom.getPosition().y;

                                                    // Get chosen level of teeth whitening
                                                    int brightness = getBrightness();

                                                    progressBar.setVisibility(View.VISIBLE);
                                                    // Process the pixels in teeth area
                                                    doBrightness(mutableBitmap, maskBitmap, brightness, startX, endX, startY, endY);
                                                    showAlertDialog(mutableBitmap); // Dialog with processed image
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                                else
                                                    Toast.makeText(getContext(), "Не удалось обнаружить область рта", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                .addOnFailureListener(
                                        e -> Toast.makeText(getContext(), "Не удалось обнаружить лицо", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /* Create Bitmap with masked teeth area */
    private Bitmap getMaskBitmap(Bitmap bitmap, Path path) {
        Bitmap maskBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.GREEN);
        Canvas canvas = new Canvas(maskBitmap);
        canvas.drawPath(path, paint);
        return maskBitmap;
    }

    /* Process pixels of teeth area */
    private static void doBrightness(Bitmap src, Bitmap mask, int value, int startX, int endX, int startY, int endY) {
        // Color information
        int A, R, G, B;
        int pixel;

        // Scan through all pixels
        for (int x = startX; x <= endX; ++x) {
            for (int y = startY; y <= endY; ++y) {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // Convert RGB values to grayscale
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                // Check if pixel is inside masked area and is closer to white than black
                if (mask.getPixel(x, y) == Color.GREEN && gray > 100) {

                    // Increase/decrease each channel
                    R += value;
                    if (R > 255) {
                        R = 255;
                    }
                    else if (R < 0) {
                        R = 0;
                    }

                    G += value;
                    if (G > 255) {
                        G = 255;
                    }
                    else if (G < 0) {
                        G = 0;
                    }

                    B += value;
                    if (B > 255) {
                        B = 255;
                    }
                    else if (B < 0) {
                        B = 0;
                    }

                    // Apply new pixel color to output Bitmap
                    src.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }
        }
    }

    /* Show AlertDialog with the processed image */
    private void showAlertDialog(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_face, null, false);
        builder.setCancelable(false);

        builder.setView(dialog).setTitle("ГОТОВО!");
        ImageView smilingFace = dialog.findViewById(R.id.image_face);
        smilingFace.setImageBitmap(bitmap);

        builder.setPositiveButton("ЗАГРУЗИТЬ", (alertDialog, which) -> {
            String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";
            File appDir = new File(storePath);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), file.getAbsolutePath(), fileName, null);
                Toast.makeText(getContext(), "Изображение сохранено!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("ЗАКРЫТЬ", (alertDialog, which) -> {
            alertDialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /* Create shape that defines teeth area */
    private Path createTeethPolygon(List<PointF> mouth_points) {
        Path mouthShape = new Path();
        mouthShape.moveTo(mouth_points.get(0).x, mouth_points.get(0).y);
        for (PointF point: mouth_points) {
            mouthShape.lineTo(point.x, point.y);
        }
        mouthShape.lineTo(mouth_points.get(mouth_points.size() - 1).x, mouth_points.get(mouth_points.size() - 1).y);
        mouthShape.close();
        return mouthShape;
    }

    // Get chosen level of brightness according to Radio Buttons
    private int getBrightness() {
        int brightness;
        if (radioButton1.isChecked()) brightness = 30;
        else if (radioButton2.isChecked()) brightness = 60;
        else brightness = 80;
        return brightness;
    }
}