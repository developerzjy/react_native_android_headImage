package com.headimage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class HeadImageModule extends ReactContextBaseJavaModule {

    private static final String HEAD_IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HeadImage/";
    private static final String HEAD_IMAGE_NAME = "head_image.png";

    private static final int REQUEST_CODE_CAMERA = 0;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CROP = 2;

    private Promise mPromise = null;
    private Uri mUri = null;
    private String mFullPath = null;

    public HeadImageModule(ReactApplicationContext reactContext) {
        super(reactContext);
        initActivityEventListener(reactContext);
    }

    @Override
    public String getName() {
        return "HeadImageModule";
    }

    private void initActivityEventListener(ReactApplicationContext reactContext) {
        reactContext.addActivityEventListener(new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_CODE_CAMERA) {
                    if (resultCode == Activity.RESULT_OK) {
                        activity.startActivityForResult(cropImage(mUri), REQUEST_CODE_CROP);
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        mPromise.resolve(null);
                        new File(mFullPath).delete();
                    }
                } else if (requestCode == REQUEST_CODE_GALLERY) {
                    if (resultCode == Activity.RESULT_OK) {
                        activity.startActivityForResult(cropImage(data.getData()), REQUEST_CODE_CROP);
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        mPromise.resolve(null);
                        new File(mFullPath).delete();
                    }
                } else if (requestCode == REQUEST_CODE_CROP) {
                    if (resultCode == Activity.RESULT_OK) {
                        mPromise.resolve(mUri.toString());
                        saveHeadImage();
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        mPromise.resolve(null);
                        new File(mFullPath).delete();
                    }
                }
            }
        });
    }

    private boolean isPathExists() {
        File file = new File(HEAD_IMAGE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.exists();
    }

    private boolean isHeadImageExits() {
        File file = new File(HEAD_IMAGE_PATH + HEAD_IMAGE_NAME);
        return file.exists();
    }

    private void saveHeadImage() {
        try {
            File file = new File(HEAD_IMAGE_PATH + HEAD_IMAGE_NAME);
            if (file.exists()) {
                file.delete();
            }
            InputStream from = new FileInputStream(mFullPath);
            OutputStream to = new FileOutputStream(HEAD_IMAGE_PATH + HEAD_IMAGE_NAME);
            byte bt[] = new byte[1024];
            int c;
            while ((c = from.read(bt)) > 0) {
                to.write(bt, 0, c);
            }
            from.close();
            to.close();
        } catch (Exception e) {
        }
    }

    public void recursionDeleteFile() {
        File file = new File(HEAD_IMAGE_PATH);
        File[] childFile = file.listFiles();
        if (childFile == null || childFile.length == 0) {
            return;
        }
        for (File f : childFile) {
            if (f.getName().contains(HEAD_IMAGE_NAME))
                continue;
            f.delete();
        }
    }

    private Intent cropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(mFullPath)));
        intent.putExtra("outputFormat", "png");
        return intent;
    }


    @ReactMethod
    public void callCamera(Promise promise) {
        recursionDeleteFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (isPathExists()) {
            mFullPath = HEAD_IMAGE_PATH + System.currentTimeMillis() + ".png";
            mUri = Uri.fromFile(new File(mFullPath));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            Activity activity = getCurrentActivity();
            if (activity != null) {
                mPromise = promise;
                activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        }
    }

    @ReactMethod
    public void callGallery(Promise promise) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        if (isPathExists()) {
            mFullPath = HEAD_IMAGE_PATH + System.currentTimeMillis() + ".png";
            mUri = Uri.fromFile(new File(mFullPath));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            Activity activity = getCurrentActivity();
            if (activity != null) {
                mPromise = promise;
                activity.startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Log.e("", "callGallery: activity is null");
            }
        }
    }

    @ReactMethod
    public void isImageExists(Promise promise) {
        boolean isExists = isHeadImageExits();
        promise.resolve(isExists);
    }

    @ReactMethod
    public void getImageUri(Promise promise) {
        Uri uri = Uri.fromFile(new File(HEAD_IMAGE_PATH + HEAD_IMAGE_NAME));
        promise.resolve(uri.toString());
    }

}
