package com.google.engedu.puzzle8;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;

public class PuzzleActivity extends AppCompatActivity {

    private static final String TAG = "PuzzleActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap = null;
    private PuzzleBoardView boardView;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        // This code programmatically adds the PuzzleBoardView to the UI.
        RelativeLayout container = (RelativeLayout) findViewById(R.id.puzzle_container);
        boardView = new PuzzleBoardView(this);
        // Some setup of the view.
        boardView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                  RelativeLayout.LayoutParams.MATCH_PARENT));
        container.addView(boardView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_puzzle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            File photo = null;
            try {
                photo = createImageFile();
            }
            catch (IOException ex) {
                Log.v(TAG, "Couldn't create File photo :", ex);
            }
            if (photo != null) {
                photoURI = Uri.fromFile(photo);
                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                imageBitmap = rotateImageIfRequired(imageBitmap, photoURI);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            // Resize to a square before scaling in boardView
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(),
                                              imageBitmap.getWidth());
            boardView.initialize(imageBitmap);
            deletePicHistory();
        }
    }

    public void shuffleImage(View view) {
        boardView.shuffle();
    }

    public void solve(View view) {
        boardView.solve();
    }

    public void useStockPic(View view) {
        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.skelly);
        boardView.initialize(imageBitmap);
    }

    private File createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("photo", ".jpg", storageDir);
    }

    // Some phones store image in landscape, some in portrait.
    // In either case, rotate them appropriately. Code taken from StackOverflow.
    private Bitmap rotateImageIfRequired(Bitmap image, Uri imageUri) throws IOException {
        ExifInterface ei = new ExifInterface(imageUri.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                             ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(image, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(image, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(image, 270);
            default:
                return image;
        }
    }

    private Bitmap rotateImage(Bitmap image, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                                                  image.getHeight(), matrix, true);
        image.recycle();
        return rotatedImage;
    }

    // Make sure photos aren't being needlessly persistently stored in phone storage:
    private void deletePicHistory() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) { return; }
        String path = dir.toString();
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File files[] = f.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (File file : files) {
            Log.d("Files", "FileName: " + file.getName());
            file.delete();
        }
    }
}
