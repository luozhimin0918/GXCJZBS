package com.jyh.gxcjzbs.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.gensee.common.ServiceType;
import com.gensee.entity.InitParam;
import com.gotye.live.core.Code;
import com.gotye.live.core.GLCore;
import com.gotye.live.core.model.AuthToken;
import com.gotye.live.core.model.RoomIdType;
import com.jyh.gxcjzbs.GenseeActivity;
import com.jyh.gxcjzbs.GotyeLiveActivity;
import com.jyh.gxcjzbs.Login_One;
import com.jyh.gxcjzbs.R;
import com.jyh.gxcjzbs.WebActivity;
import com.jyh.gxcjzbs.bean.KXTApplication;
import com.jyh.gxcjzbs.common.constant.SpConstant;
import com.jyh.gxcjzbs.common.utils.LoginInfoUtils;
import com.jyh.gxcjzbs.common.utils.SPUtils;
import com.jyh.gxcjzbs.common.utils.ToastView;
import com.jyh.gxcjzbs.common.utils.dialogutils.BaseAnimatorSet;
import com.jyh.gxcjzbs.common.utils.dialogutils.BounceTopEnter;
import com.jyh.gxcjzbs.common.utils.dialogutils.NormalDialog;
import com.jyh.gxcjzbs.common.my_interface.OnBtnClickL;
import com.jyh.gxcjzbs.common.utils.dialogutils.SlideBottomExit;

import static android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;

/**
 * @author Administrator
 */
public class fragment_zb extends Fragment implements OnClickListener {

    private ImageView imgJianjie, imgKufu;
    private SimpleDraweeView live_bg;
    private Intent intent2;

    // Gotye视频所需参数
    private boolean isCancel = false;
    private LoginThread mLoginThread;
    private ProgressDialog loginDialog;

    private Bitmap bitmap;
    private GenericDraweeHierarchyBuilder builder;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        builder = new GenericDraweeHierarchyBuilder(getResources());
        builder.setPlaceholderImage(getResources().getDrawable(R.drawable.live_bg), ScalingUtils.ScaleType.FIT_XY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        view = inflater.inflate(R.layout.fragment_zb, null);
        findView(view);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                joinLive();
            }
        });
        setBg();
        return view;
    }

    /**
     * 设置背景图
     */
    private void setBg() {
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
        builder.setPlaceholderImage(getResources().getDrawable(R.drawable.live_bg), ScalingUtils.ScaleType.FIT_XY);
        live_bg.setHierarchy(builder.build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            bitmap.recycle();
            bitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findView(View view) {
        // TODO Auto-generated method stub
        imgJianjie = (ImageView) view.findViewById(R.id.img_jianjie);
        imgKufu = (ImageView) view.findViewById(R.id.img_kefu);
        live_bg = (SimpleDraweeView) view.findViewById(R.id.live_bg);

        imgJianjie.setOnClickListener(this);
        imgKufu.setOnClickListener(this);
        intent2 = new Intent(getActivity(), WebActivity.class);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.img_jianjie:
                intent2.putExtra(
                        "url",
                        SPUtils.getString(getContext(), SpConstant.APPINFO_SUMMARY_URL));
                intent2.putExtra("from", "main");
                intent2.putExtra("title", "直播室简介");
                startActivity(intent2);
                break;
            case R.id.img_kefu:
                intent2.putExtra(
                        "url",
                        SPUtils.getString(getContext(), SpConstant.APPINFO_KEFU_URL));
                intent2.putExtra("from", "main");
                intent2.putExtra("title", "联系客服");
                startActivity(intent2);
                break;
            default:
                break;
        }
    }

    /**
     * 进入直播间
     */
    private void joinLive() {
        if (LoginInfoUtils.isCanJoin(getContext())) {
            String type = SPUtils.getString(getContext(), SpConstant.VIDEO_TYPE);
            Log.i("type1", type);
            if (type != null) {
                if ("live_gensee".equals(type)) {
                    initGensee();
                } else
                    attemptLogin();
            }
        } else {
            showLoginDialog();
        }
    }

    private void initGensee() {
        loginDialog = new ProgressDialog(getActivity());
        loginDialog.setMessage("进入直播室。。。");
        loginDialog.setCancelable(true);
        loginDialog.setCanceledOnTouchOutside(true);
        loginDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                KXTApplication.initParam = null;
            }
        });

        handleProgress(true);
        KXTApplication.initParam = new InitParam();
        // domain
        KXTApplication.initParam.setDomain(SPUtils.getString(getContext(),SpConstant.VIDEO_GENSEE_SITE));
        // 编号（直播间号）,如果没有编号却有直播id的情况请使用setLiveId("此处直播id或课堂id");
        KXTApplication.initParam.setNumber(SPUtils.getString(getContext(),SpConstant.VIDEO_GENSEE_ID));
        KXTApplication.initParam.setLiveId(SPUtils.getString(getContext(),SpConstant.VIDEO_GENSEE_ROOMID));
        // 站点认证帐号，根据情况可以填""
        KXTApplication.initParam.setLoginAccount("");
        // 站点认证密码，根据情况可以填""
        KXTApplication.initParam.setLoginPwd("");
        // 昵称，供显示用
        KXTApplication.initParam.setNickName("");
        // 加入口令，没有则填""
        KXTApplication.initParam.setJoinPwd(SPUtils.getString(getContext(),SpConstant.VIDEO_GENSEE_PWD));

        // 判断serviceType类型
        // 站点类型ServiceType.ST_CASTLINE
        // 直播webcast，ServiceType.ST_MEETING
        // 会议meeting，ServiceType.ST_TRAINING 培训
        ServiceType serviceType = null;
        switch (SPUtils.getString(getContext(),SpConstant.VIDEO_GENSEE_CTXZ)) {
            case "webcast":
                serviceType = ServiceType.ST_CASTLINE;
                break;
            case "meeting":
                serviceType = ServiceType.ST_MEETING;
                break;
            case "training":
                serviceType = ServiceType.ST_TRAINING;
                break;
        }
        KXTApplication.initParam.setServiceType(serviceType);
        Intent intent = new Intent(getActivity(), GenseeActivity.class);
        intent.setFlags(FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        KXTApplication.IsOut = true;
        handleProgress(false);
        startActivity(intent);
    }

    /**
     * 显示登录Dialog
     */
    private BaseAnimatorSet bas_in;
    private BaseAnimatorSet bas_out;

    private void showLoginDialog() {
        bas_in = new BounceTopEnter();
        bas_out = new SlideBottomExit();
        final NormalDialog dialog = new NormalDialog(getContext());
        dialog.isTitleShow(true)
                // 设置背景颜色
                .bgColor(Color.parseColor("#383838"))
                // 设置dialog角度
                .cornerRadius(5)
                // 设置内容
                .content("您好,您的权限不够,请先登录").title("温馨提示")
                // 设置居中
                .contentGravity(Gravity.CENTER)
                // 设置内容字体颜色
                .contentTextColor(Color.parseColor("#ffffff"))
                // 设置线的颜色
                .dividerColor(Color.parseColor("#222222"))
                // 设置字体
                .btnTextSize(15.5f, 15.5f)
                // 设置取消确定颜色
                .btnTextColor(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"))//
                .btnPressColor(Color.parseColor("#2B2B2B"))//
                .widthScale(0.85f)//
                .showAnim(bas_in)//
                .dismissAnim(bas_out)//
                .show();

        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                if (!LoginInfoUtils.isLogin(getContext())) {
                    Intent intent = new Intent(getContext(), Login_One.class);
                    intent.putExtra("from", "zb");
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void reJoin() {
        joinLive();
    }

    private void attemptLogin() {

        KXTApplication.player.stop();
        KXTApplication.core.clearAuth();
        KXTApplication.IsOut = false;

        KXTApplication.isFirst = true;
        mLoginThread = null;
        // Store values at the time of the login attempt.
        // String roomId = "100030";
        // String password = "000000";
        // String roomId = "101639";
        // String password = "333333";
        String roomId = SPUtils.getString(getContext(), SpConstant.VIDEO_GOTYEROOMID);
        String password = SPUtils.getString(getContext(), SpConstant.VIDEO_GOTYEPASSWORD);
        String nickname = "111";

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            cancel = true;
        }

        if (TextUtils.isEmpty(roomId)) {
            cancel = true;
        }

        if (TextUtils.isEmpty(nickname)) {
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginDialog = new ProgressDialog(getActivity());
            loginDialog.setMessage("进入直播室。。。");
            loginDialog.setCancelable(true);
            loginDialog.setCanceledOnTouchOutside(true);
            loginDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mLoginThread != null) {
                        mLoginThread.cancel();
                        mLoginThread = null;

                        handleProgress(false);
                        KXTApplication.core.clearAuth();
                        return;
                    }
                }
            });
            handleProgress(true);

            mLoginThread = new LoginThread(roomId, password, nickname, RoomIdType.GOTYE);
            mLoginThread.start();
        }
    }

    private class LoginThread extends Thread {

        String roomId, password, nickaname;
        RoomIdType type;

        public LoginThread(String roomId, String password, String nickname, RoomIdType type) {
            this.roomId = roomId;
            this.password = password;
            this.nickaname = nickname;
            this.type = type;
            isCancel = false;
        }

        @Override
        public void run() {
            super.run();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 如果在登录时取消，则退出session
                    if (isCancel) {
                        KXTApplication.core.clearAuth();
                        mLoginThread = null;
                        handleProgress(false);
                        return;
                    }
                    // 首先到服务器验证session取得accessToken和role等信息
                    KXTApplication.core.auth(roomId, password, null, nickaname, type, new GLCore.Callback<AuthToken>() {
                        @Override
                        public void onCallback(int i, final AuthToken authToken) {
                            if (isCancel || i != Code.SUCCESS) {
                                // session验证失败
                                KXTApplication.core.clearAuth();
                                mLoginThread = null;
                                handleProgress(false);
                                ToastView.makeText(getActivity(), "session验证失败");
                                return;
                            }
                            handleProgress(false);
                            Intent intent = new Intent(getActivity(), GotyeLiveActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                            startActivity(intent);
                        }
                    });

                }
            });
        }

        public void cancel() {
            isCancel = true;
            KXTApplication.core.clearAuth();
        }
    }

    private boolean isPasswordValid(String password) {
        // TODO: Replace this with your own logic
        return password.length() > 1;
    }

    private void handleProgress(final boolean show) {
        if (show) {
            loginDialog.show();
        } else {
            loginDialog.dismiss();
        }
    }
}
