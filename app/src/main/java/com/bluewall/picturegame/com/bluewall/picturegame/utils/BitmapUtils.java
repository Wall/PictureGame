package com.bluewall.picturegame.com.bluewall.picturegame.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    private static final int WIDTH = 0;
    private static final int HEIGHT = 1;
    private static final String RESIZED_IMAGE_FILE_NAME = "question.png";

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Resize an image to the given dimensions
     * Maintains aspect ratio on resize
     *
     * @param width
     * @param height
     * @param imagePath
     * @param context
     * @return Uri of the re-sized image
     */
    public static Uri resizeImage(int width, int height, Uri imagePath, Context context) {
        String filePath = getImagePathFromUri(imagePath, context);
        File imageFile = new File(filePath);
        String resultPath = "";

        if (imageFile.exists()) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            Bitmap tmpBitmap = BitmapFactory.decodeFile(filePath, options);
            int[] scaledDimensions = fitImageDimensionsToSize(width, height, tmpBitmap);

            Bitmap tmpBitmap2 = Bitmap.createScaledBitmap(tmpBitmap, scaledDimensions[WIDTH], scaledDimensions[HEIGHT], false);

            FileOutputStream fos = null;

            try {
                fos = context.openFileOutput(RESIZED_IMAGE_FILE_NAME, Context.MODE_PRIVATE);
                tmpBitmap2.compress(Bitmap.CompressFormat.PNG, 90, fos);
                if(fos!=null){
                    fos.close();
                }
                File tmpFile = new File(context.getFilesDir(), RESIZED_IMAGE_FILE_NAME);
                resultPath = tmpFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Uri.parse(resultPath);
    }

    /**
     * work out dimensions to fit the selected image within
     * the selected bounds maintaining aspect ratio
     *
     * @param width
     * @param height
     * @param image
     * @return array with 2 values, scaledDimensions[0] = width, scaledDimensions[1] = height
     */
    private static int[] fitImageDimensionsToSize(int width, int height, Bitmap image) {
        int[] scaledDimensions = new int[2];
        Log.d(TAG, image.getWidth()+"x"+image.getHeight());

        if(image.getWidth()>width){
            scaledDimensions[WIDTH] = width;
            scaledDimensions[HEIGHT] = (scaledDimensions[WIDTH]*image.getHeight())/image.getWidth();
        }
        if(scaledDimensions[HEIGHT] > height){
            scaledDimensions[HEIGHT] = height;
            scaledDimensions[WIDTH] = (scaledDimensions[HEIGHT]*image.getWidth())/image.getHeight();
        }

        Log.d(TAG, scaledDimensions[WIDTH]+"x" +scaledDimensions[HEIGHT]);
        return scaledDimensions;
    }


    public static String getImagePathFromUri(Uri uri, Context context) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
