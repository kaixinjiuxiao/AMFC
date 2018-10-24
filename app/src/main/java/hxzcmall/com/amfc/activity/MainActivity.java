package hxzcmall.com.amfc.activity;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import hxzcmall.com.amfc.R;
import hxzcmall.com.amfc.utils.DialogUtil;
import hxzcmall.com.amfc.utils.FileUtils;
import hxzcmall.com.amfc.utils.NetworkChangeReceive;
import hxzcmall.com.amfc.utils.PermissionsChecker;
import hxzcmall.com.amfc.utils.ToastUtils;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_PERMISSION = 4;
    /**
     * 需要的权限
     */
    static final String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    public ValueCallback<Uri[]> mFilePathCallback;
    public ValueCallback<Uri> mUploadMessage;
    private WebView mWeb;
    private TextView mTitle;
    private ImageView mImgBack,networkImageView;
    private Button mRefresh;
    private PermissionsChecker mPermissionsChecker;
    private NetworkChangeReceive networkChangeReceive;
    private IntentFilter intentFilter;
    private String mBaseUrl;
    public String mCameraPhotoPath, picUrl;
    public boolean isLoadError,needFinish;
    private Dialog mProgressDialog;
    @Override
    public int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mBaseUrl = "http://app.amfc.io/";
        mImgBack =findViewById(R.id.titleview_btnLeft);
        mTitle = findViewById(R.id.titleview_title);
        mRefresh = findViewById(R.id.titleview_btnRight);
        mRefresh.setText("刷新");
        mRefresh.setVisibility(View.VISIBLE);
        mWeb = findViewById(R.id.webView);
        mProgressDialog = DialogUtil.showProgressDialog(this, "", true);
        networkImageView =findViewById(R.id.network);
        networkImageView.setVisibility(View.GONE);
        initWebView();
        mPermissionsChecker = new PermissionsChecker(this);
        networkChangeReceive = new NetworkChangeReceive(MainActivity.this);
        networkChangeReceive.setNetworkStatusDelegate(new NetworkChangeReceive.NetworkStatusDelegate() {
            @Override
            public void networkEnable(boolean enable) {
                if (!enable) {
                    ToastUtils.showShort(MainActivity.this, "网络断开连接");
                    networkImageView.setVisibility(View.VISIBLE);
                }else{
                    mWeb.reload();
                }
            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceive, intentFilter);
        PermissionsActivity.startActivityForResult(MainActivity.this, REQUEST_PERMISSION,
                PERMISSIONS);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {
        mImgBack.setOnClickListener(this);
        mRefresh.setOnClickListener(this);
        networkImageView.setOnClickListener(this);
        mWeb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mWeb.getUrl().contains("user_share")){
                    final WebView.HitTestResult hitTestResult = mWeb.getHitTestResult();
                    if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                            hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        picUrl = hitTestResult.getExtra();
                        showSaveDialog(picUrl);
                    }
                }
                return false;
            }
        });
    }

    private void initWebView() {
        WebSettings webSettings = mWeb.getSettings();
        webSettings.setDefaultFontSize(16);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowFileAccess(true);// 设置允许访问文件数据
        webSettings.setBuiltInZoomControls(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSavePassword(false);
        //获取触摸焦点
        mWeb.requestFocusFromTouch();
        mWeb.setWebViewClient(new MyWebViewClient());
        mWeb.setWebChromeClient(new MyWebChromClient());
        mWeb.loadUrl(mBaseUrl);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.titleview_btnLeft:
                if(mWeb.canGoBack()){
                    mWeb.goBack();
                }else{
                    finish();
                }
                break;
            case R.id.titleview_btnRight:
                mWeb.reload();
                break;
            case R.id.network:
                mWeb.reload();
                break;
                default:
                    break;
        }
    }
    private void showSaveDialog(final String picUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        builder.setMessage("保存图片到本地");
        builder.setPositiveButton("确定 ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //保存图片到相册
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                            startPermissionsActivity();
                        } else {
                            url2bitmap(picUrl);
                        }
                    }
                }).start();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION,
                PERMISSIONS);
    }

    public void url2bitmap(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = null;
                try {
                    URL iconUrl = new URL(url);
                    URLConnection conn = iconUrl.openConnection();
                    HttpURLConnection http = (HttpURLConnection) conn;
                    int length = http.getContentLength();
                    conn.connect();
                    // 获得图像的字符流
                    InputStream is = conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is, length);
                    bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                    if (bm != null) {
                        save2Album(url, bm);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void save2Album(String url, Bitmap bitmap) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "amfc");
        if (!appDir.exists()) appDir.mkdir();
        String[] str = url.split("/");
        String fileName = str[str.length - 1];
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            onSaveSuccess(file);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void onSaveSuccess(final File file) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Uri imageUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "hxzcmall.com.amfc.fileprovider", file);
                } else {
                    imageUri = Uri.fromFile(file);
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                }
                intent.setData(imageUri);
                sendBroadcast(intent);
                Toast.makeText(MainActivity.this, "保存成功" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("Login/logout")) {
                needFinish = true;
            }
            if (url.startsWith("tel:")) {
                String parseUrl = url.replace("//", "");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(parseUrl));
                startActivity(intent);
                return true;
            }
            else if (url != null) {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            isLoadError = false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (view.getTitle().contains("http://psc.one/")) {
                mTitle.setText("");
            } else {
                mTitle.setText(view.getTitle());
            }
        }


        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            isLoadError = true;
        }


    }


    private class MyWebChromClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress >= 60) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                if (!isLoadError) {
                    networkImageView.setVisibility(View.INVISIBLE);
                }
            } else {
                mProgressDialog.show();
            }
        }


        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            String reg = "[\\u4e00-\\u9fa5]+";
            if (title.matches(reg) == true) {
                mTitle.setText(title);
            }
        }

        //        //Handling input[type="file"] requests for android API 16+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), INPUT_FILE_REQUEST_CODE);
        }

        //Handling input[type="file"] requests for android API 21+
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            // get_file();
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");
            Intent[] intentArray;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = FileUtils.create_image();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    Toast.makeText(MainActivity.this, "Image file creation failed" + ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
        else if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            url2bitmap(picUrl);
        }
        else if (resultCode == RESULT_CANCELED) {

            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }

            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
            }

            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == INPUT_FILE_REQUEST_CODE) {
                    if (null == mFilePathCallback) {
                        return;
                    }
                    if (data == null) {
                        if (mCameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        } else {
                            mFilePathCallback.onReceiveValue(null);
                        }
                    } else {
                        String dataString = data.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }

                    mProgressDialog.show();

                    final Uri[] cResults = results;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            mFilePathCallback.onReceiveValue(cResults);
                            mFilePathCallback = null;

                            mProgressDialog.dismiss();
                        }
                    }, 2000);
                }
            }
        } else {
            if (requestCode == INPUT_FILE_REQUEST_CODE) {
                if (null == mUploadMessage) return;
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                mProgressDialog.show();
                if (Build.VERSION.SDK_INT < 19) {
                    String imagePath = getImagePath(result, null);
                    mUploadMessage.onReceiveValue(Uri.parse(imagePath));
                } else {
                    mUploadMessage.onReceiveValue(result);
                }
                mUploadMessage = null;
                mProgressDialog.dismiss();
            }
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection老获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWeb.clearHistory();
        mWeb.destroy();
        unregisterReceiver(networkChangeReceive);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (needFinish == true) {
                finish();
                mWeb.clearHistory();
            } else {
                if (mWeb.canGoBack()) {
                    mWeb.goBack();
                } else {
                    moveTaskToBack(true);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}
