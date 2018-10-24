package hxzcmall.com.amfc.activity;

import android.content.Intent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.gyf.barlibrary.ImmersionBar;

import hxzcmall.com.amfc.R;


/**
 * author：captain
 * time:   2018/8/22 15:32
 * describe：
 */
public class SplashActivity  extends BaseActivity{
    private ImageView mImageView;
    @Override
    public int getLayoutResource() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        ImmersionBar.with(this).init();
        mImageView = findViewById(R.id.imgSplash);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 1.0f);
        animation.setDuration(2000);
        animation.setFillAfter(true);
        mImageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy(); //必须调用该方法，防止内存泄漏
    }
}
