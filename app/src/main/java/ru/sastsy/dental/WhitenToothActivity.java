package ru.sastsy.dental;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import ru.sastsy.dental.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import ru.sastsy.dental.R;

public class WhitenToothActivity extends AppCompatActivity {

    ImageView imageView;
    TextView details;
    Button gallery, camera;
    File mImageFile;
    float mFinalProb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitentooth);
        details = findViewById(R.id.details);

        imageView = findViewById(R.id.imageView);

        gallery = findViewById(R.id.gallery);
        camera = findViewById(R.id.camera);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Opens camera dialog
                EasyImage.openCameraForImage(WhitenToothActivity.this, 100);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Opens gallery picker
                EasyImage.openGallery(WhitenToothActivity.this, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(WhitenToothActivity.this, "Image picker error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {

                mImageFile = imageFiles.get(0);

                Bitmap bitmap = new BitmapFactory().decodeFile(imageFiles.get(0).getAbsolutePath());
                InputImage image = InputImage.fromBitmap(bitmap, 0);
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);
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

                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                for (Face face : faces) {
                                                    Rect bounds = face.getBoundingBox();
                                                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                    Paint paint = new Paint();
                                                    paint.setColor(Color.WHITE);
                                                    paint.setStyle(Paint.Style.STROKE);
                                                    canvas.drawRect(bounds, paint);

                                                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                    // nose available):
                                                    FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                                                    FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                                                    FaceLandmark mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
                                                    FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
                                                    /*if (mouthLeft != null && mouthBottom != null && mouthRight != null && noseBase != null) {
                                                        float startX = mouthRight.getPosition().x;
                                                        float endX = mouthLeft.getPosition().x;
                                                        float startY = noseBase.getPosition().y;
                                                        float endY = mouthBottom.getPosition().y;
                                                    }*/
                                                    int startX = (int) mouthLeft.getPosition().x;
                                                    int endX = (int) mouthRight.getPosition().x;
                                                    int startY = (int) noseBase.getPosition().y;
                                                    int endY = (int) mouthBottom.getPosition().y;
                                                    System.out.println(startX + " " + endX);
                                                    System.out.println(startY + " " + endY);

                                                    List<PointF> mouth_upper_points = face.getContour(FaceContour.LOWER_LIP_TOP).getPoints();
                                                    List<PointF> mouth_lower_points = face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                    List<PointF> mouth_points = new ArrayList<PointF>();
                                                    mouth_points.addAll(mouth_lower_points);
                                                    mouth_points.addAll(mouth_upper_points);

                                                    Path mouth_shape = new Path();
                                                    mouth_shape.moveTo(mouth_points.get(0).x, mouth_points.get(0).y);
                                                    for (PointF point: mouth_points) {
                                                        mouth_shape.lineTo(point.x, point.y);
                                                    }
                                                    mouth_shape.lineTo(mouth_points.get(mouth_points.size() - 1).x, mouth_points.get(mouth_points.size() - 1).y);
                                                    mouth_shape.close();

                                                    Bitmap maskBitmap = getMaskBitmap(mutableBitmap, mouth_shape);

                                                    doBrightness(mutableBitmap, maskBitmap, 50, startX, endX, startY, endY);

                                                    // If classification was enabled:
                                                    if (face.getSmilingProbability() != null) {
                                                        float smileProb = face.getSmilingProbability();
                                                        float finalProb = smileProb * 100;
                                                        mFinalProb = finalProb;
                                                        String prob = "";
                                                        if (smileProb != 0) {
                                                            prob = String.valueOf(finalProb) + "%" + "Happy";
                                                        } else {
                                                            Toast.makeText(WhitenToothActivity.this, "Put up a smile :) ", Toast.LENGTH_SHORT).show();
                                                        }
                                                        imageView.setImageBitmap(mutableBitmap);
                                                        details.setText(String.valueOf(finalProb) + "%" + "Happy");

                                                        showAlertDialog();

                                                    }
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                                Toast.makeText(WhitenToothActivity.this, "Firebase failed to detect face", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                Face faces;

            }

        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WhitenToothActivity.this);
        LayoutInflater inflater = (LayoutInflater) WhitenToothActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialog = inflater.inflate(R.layout.dialog_face, null, false);

        builder.setView(dialog)
                .setTitle("Be happy :)");

        ImageView smilingFace = dialog.findViewById(R.id.image_face);
        TextView smileCoefficient = dialog.findViewById(R.id.happiness_text);

        smilingFace.setImageBitmap(new BitmapFactory().decodeFile(mImageFile.getAbsolutePath()));
        smileCoefficient.setText(" " + String.valueOf(mFinalProb) + "%" + " happy!");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private Bitmap getMaskBitmap(Bitmap givenBitmap, Path path) {
        Bitmap maskBitmap = givenBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.GREEN);
        Canvas canvas = new Canvas(maskBitmap);
        canvas.drawPath(path, paint);
        return maskBitmap;
    }

    public static void doBrightness(Bitmap src, Bitmap mask, int value, int startX, int endX, int startY, int endY) {

        //Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = startX; x <= endX; ++x) {
            for(int y = startY; y <= endY; ++y) {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                if (mask.getPixel(x, y) == Color.GREEN && gray > 100) {

                    // increase/decrease each channel
                    R += value;
                    if (R > 255) {
                        R = 255;
                    } else if (R < 0) {
                        R = 0;
                    }

                    G += value;
                    if (G > 255) {
                        G = 255;
                    } else if (G < 0) {
                        G = 0;
                    }

                    B += value;
                    if (B > 255) {
                        B = 255;
                    } else if (B < 0) {
                        B = 0;
                    }

                    // apply new pixel color to output bitmap
                    src.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }
        }
    }
}