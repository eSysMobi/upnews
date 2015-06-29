package mobi.esys.unl_new_api;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.orhanobut.logger.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.IntegerRes;
import org.androidannotations.annotations.res.StringRes;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.esys.unl_new_api.eventbus.GetFirstVideoEvent;
import mobi.esys.unl_new_api.eventbus.NextVideoPlayEvent;
import mobi.esys.unl_new_api.filesystem.FoldersHelper;
import mobi.esys.unl_new_api.helpers.TimeHelper;
import mobi.esys.unl_new_api.model.UNPlaylist;
import mobi.esys.unl_new_api.model.UNVideo;
import mobi.esys.unl_new_api.playback.UNPlayback;
import mobi.esys.unl_new_api.un_api.UNApi;
import okio.BufferedSink;
import okio.Okio;


@EActivity(R.layout.activity_videofullscreen)
public class VideoActivity extends UNBaseActivity implements
        LocationListener,
        SurfaceHolder.Callback,
        android.hardware.Camera.PictureCallback {

    @ViewById
    FrameLayout camHolder;
    @ViewById
    VideoView video;

    @SystemService
    TelephonyManager tel;
    @SystemService
    LocationManager locationManager;

    @IntegerRes
    int maxFaces;

    @StringRes
    String baseDir;
    @StringRes
    String photoDir;


    private transient int cameraId = 0;

    private transient EventBus bus;
    private transient UNPhoneStateListener unPhoneStateListener;
    private transient String locationProvider;
    private transient SharedPreferences unPref;
    private transient UNPlaylist playlists;


    private static final int NO_FRONT_CAMERA = -1;
    private transient Camera camera;
    private transient boolean previewIsRunning = false;
    private transient boolean isTakingPicture = false;
    private transient String videoName;

    private transient MediaController mController;

    private transient double lat;
    private transient double lng;
    private transient UNApp unApp;

    private transient boolean isCharging = false;
    private transient int chargeLevel;

    private transient int sigStr;
    private int faceCount;
    private transient UNPlayback playback;


    @AfterViews
    void init() {
        unApp = (UNApp) getApplicationContext();
        bus = new EventBus();
        UNApi.setCurrentContext(VideoActivity.this, bus);


        SurfaceView surfaceView = new SurfaceView(VideoActivity.this);
        camHolder.addView(surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(VideoActivity.this);

        unPref = getSharedPreferences("unPref", MODE_PRIVATE);


        bus.register(this);


        tel.listen(unPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(locationProvider);

        if (location != null) {
            onLocationChanged(location);
        }

        mController = new MediaController(this);
        video.setMediaController(mController);
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LinearLayout ll = (LinearLayout) mController.getChildAt(0);

                for (int i = 0; i < ll.getChildCount(); i++) {

                    if (ll.getChildAt(i) instanceof LinearLayout) {
                        LinearLayout llC = (LinearLayout) ll.getChildAt(i);
                        for (int j = 0; j < llC.getChildCount(); j++) {
                            if (llC.getChildAt(j) instanceof SeekBar) {
                                SeekBar seekBar = (SeekBar) llC.getChildAt(j);
                                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.seekbartheme_scrubber_progress_horizontal_holo_dark));
                                seekBar.setThumb(getResources().getDrawable(R.drawable.seekbartheme_scrubber_control_selector_holo_dark));
                            }
                        }
                    }

                }
            }
        });


        getBatteryData();
        getSignal();
        UNApi.getPlaylists();

    }


    private void getBatteryData() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = VideoActivity.this.registerReceiver(null,
                ifilter);

        chargeLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;


    }


    private void getSignal() {
        PhoneStateListener phoneStateListener = new UNPhoneStateListener();
        tel.listen(phoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }


    public void onEvent(GetFirstVideoEvent event) {
        playback = new UNPlayback(video, VideoActivity.this, event.playlist, event.firstVideo);

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(VideoActivity.this, "Play End", Toast.LENGTH_SHORT).show();
                playEnd();

                playback.nextVideo();
                startPreview();
                UNApi.getPlaylistVideos(String.valueOf(playback.getPlaylist().getUnPlaylistID()));

                playback.restartDownload();
            }
        });
    }


    public void onEvent(NextVideoPlayEvent event) {
    }


    public void playEnd() {
        takePhoto();
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
        tel.listen(unPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(locationProvider, 400, 1, this);
        tel.listen(unPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }


    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startPreview();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        isTakingPicture = false;
        Toast.makeText(VideoActivity.this, "Take photo 2", Toast.LENGTH_SHORT).show();
        FoldersHelper foldersHelper = new FoldersHelper(photoDir, baseDir);

        foldersHelper.clearFolder();
        String timeStamp = TimeHelper.getCurrentTimeStamp();


        File picFile = new File(foldersHelper.getFolderInstance().getAbsolutePath(), "upnews".concat(timeStamp).concat(".jpg"));
        Logger.d(picFile.getAbsolutePath());

        String videoPref = unPref.getString("video", "");
        try {


            BufferedSink sink = Okio.buffer(Okio.sink(picFile));
            sink.write(data);
            sink.close();


            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();

            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap_options.inSampleSize = 2;
            bitmap_options.inMutable = false;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bitmap_options.inPremultiplied = true;
            }

            Bitmap bitMapFile = BitmapFactory.
                    decodeFile(picFile.getAbsolutePath(), bitmap_options);


            if (videoPref != null) {
                Logger.d("videoName".concat(videoPref));
            }


            FaceDetector face_detector = new FaceDetector(

                    bitMapFile.getWidth(), bitMapFile.getHeight(),

                    maxFaces);

            FaceDetector.Face[] faces = new FaceDetector.Face[maxFaces];


            int faceCount = face_detector.findFaces(bitMapFile, faces);
            Logger.d("faces: ".concat(String.valueOf(faceCount)));
            Toast.makeText(VideoActivity.this, "faces: ".concat(String.valueOf(faceCount)), Toast.LENGTH_SHORT).show();
            Logger.d("videoNamePref", videoPref);
            UNApi.sendPhoto(picFile, faceCount, videoPref);
        } catch (IOException e) {
            Logger.d("file pic file exception");
            e.printStackTrace();
        }


        getBatteryData();
        getSignal();
        UNApi.sendData(String.valueOf(chargeLevel), String.valueOf(sigStr),
                String.valueOf(isCharging), String.valueOf(lat), String.valueOf(lng), videoPref, unApp.getDeviceID());

        stopPreview();
    }


    private class UNPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            sigStr = signalStrength.getGsmSignalStrength();
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99) {
                    SharedPreferences.Editor editor = unPref.edit();
                    editor.putString("signalStrength", String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113));
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = unPref.edit();
                    editor.putString("signalStrength", String.valueOf(signalStrength.getGsmSignalStrength()));
                    editor.apply();
                }
            } else {
                SharedPreferences.Editor editor = unPref.edit();
                editor.putString("signalStrength", String.valueOf(signalStrength.getCdmaDbm()));
                editor.apply();
            }
        }
    }

    private int getFrontCameraId() {
        final int numberOfCameras = Camera.getNumberOfCameras();
        boolean isFrontExist = false;

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                isFrontExist = true;
                return i;
            }
        }

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }

        return NO_FRONT_CAMERA;
    }

    private void initCamera(SurfaceHolder holder) {
        int cameraId = getFrontCameraId();
        if (cameraId != NO_FRONT_CAMERA) {
            try {
                camera = Camera.open(cameraId);

                Camera.Parameters parameters = camera.getParameters();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    ;

                List<String> flashModes = parameters.getSupportedFlashModes();
                if (flashModes != null
                        && flashModes
                        .contains(Camera.Parameters.FLASH_MODE_OFF))
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                List<String> whiteBalance = parameters
                        .getSupportedWhiteBalance();
                if (whiteBalance != null
                        && whiteBalance
                        .contains(Camera.Parameters.WHITE_BALANCE_AUTO))
                    parameters
                            .setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null
                        && focusModes
                        .contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                if (sizes != null && sizes.size() > 0) {
                    Camera.Size size = sizes.get(0);
                    parameters.setPictureSize(size.width, size.height);
                }

                List<Camera.Size> previewSizes = parameters
                        .getSupportedPreviewSizes();
                if (previewSizes != null) {
                    Camera.Size previewSize = previewSizes.get(previewSizes
                            .size() - 1);
                    parameters.setPreviewSize(previewSize.width,
                            previewSize.height);
                }

                camera.setParameters(parameters);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    camera.enableShutterSound(false);
            } catch (RuntimeException e) {
                return;
            }
        } else {
            return;
        }

        try {
            camera.setPreviewDisplay(holder);

        } catch (IOException ioe) {
            Logger.e(ioe.getMessage(), "i/o exception");
        }
    }

    public void startPreview() {
        Log.d("is preview", String.valueOf(previewIsRunning));
        if (!previewIsRunning && camera != null) {
            Log.d("photo", "photo");

        }
    }

    private void takePhoto() {
        Toast.makeText(VideoActivity.this, "Take photo 1", Toast.LENGTH_SHORT).show();
        try {
            camera.startPreview();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    if (!isTakingPicture) {
                        try {

                            isTakingPicture = true;
                            camera.setPreviewCallback(null);
                            camera.takePicture(null, null,
                                    VideoActivity.this);
                        } catch (RuntimeException ignored) {
                        }
                    }
                }
            });
            previewIsRunning = true;


        } catch (Exception e) {
            Log.d("async", "except");
            Logger.d("cam".concat(e.getMessage()));
        }
    }

    private void stopPreview() {
        if (!isTakingPicture && previewIsRunning && camera != null) {
            camera.stopPreview();
            previewIsRunning = false;
            Log.d("async", "stop");
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            Log.d("async", "release");
        }
    }

    public UNVideo getNextVideo(String plID, String vID, int pt, int orderNumber) {
        return UNApi.getNextVideo(plID, vID, pt, orderNumber);
    }


}
