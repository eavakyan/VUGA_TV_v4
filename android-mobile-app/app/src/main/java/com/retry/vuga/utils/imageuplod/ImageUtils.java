package com.retry.vuga.utils.imageuplod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 500; // Max width/height in pixels
    private static final int COMPRESSION_QUALITY = 70; // JPEG compression quality (0-100)
    
    /**
     * Converts an image URI to a base64 string with automatic resizing and compression
     */
    public static String convertImageToBase64(Context context, Uri imageUri) {
        try {
            // Read the image into a bitmap
            Bitmap originalBitmap = getBitmapFromUri(context, imageUri);
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode image from URI");
                return null;
            }
            
            // Rotate bitmap if needed based on EXIF data
            Bitmap rotatedBitmap = rotateImageIfRequired(context, originalBitmap, imageUri);
            
            // Resize the bitmap to reduce file size
            Bitmap resizedBitmap = resizeBitmap(rotatedBitmap, MAX_IMAGE_SIZE);
            
            // Convert to base64
            String base64String = bitmapToBase64(resizedBitmap, COMPRESSION_QUALITY);
            
            // Clean up bitmaps
            if (originalBitmap != rotatedBitmap) {
                originalBitmap.recycle();
            }
            if (rotatedBitmap != resizedBitmap) {
                rotatedBitmap.recycle();
            }
            resizedBitmap.recycle();
            
            Log.d(TAG, "Image converted to base64, length: " + (base64String != null ? base64String.length() : 0));
            return base64String;
            
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to base64", e);
            return null;
        }
    }
    
    /**
     * Gets a Bitmap from URI
     */
    private static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            return null;
        }
        
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        return bitmap;
    }
    
    /**
     * Rotates an image if required based on EXIF orientation data
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) {
        try {
            InputStream input = context.getContentResolver().openInputStream(selectedImage);
            ExifInterface ei = new ExifInterface(input);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading EXIF data", e);
            return img;
        }
    }
    
    /**
     * Rotates a bitmap by the specified degrees
     */
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    
    /**
     * Resizes a bitmap to fit within the specified maximum dimensions while maintaining aspect ratio
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // If image is already smaller than max size, return as is
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        
        // Calculate the scaling factor
        float scale = Math.min(((float) maxSize / width), ((float) maxSize / height));
        
        // Calculate new dimensions
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        // Create scaled bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    /**
     * Converts a bitmap to base64 string with specified compression quality
     */
    private static String bitmapToBase64(Bitmap bitmap, int quality) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to base64", e);
            return null;
        }
    }
    
    /**
     * Calculates the sample size for bitmap decoding to reduce memory usage
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
}