package com.jkt.tdialog;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import static com.jkt.tdialog.TDialog.Style.DownSheet;


/**
 * Created by 天哥哥 at 2017/5/19 9:58
 */
public class TDialog {

    private TextView mCancelTV;
    private DialogAdapter mAdapter;
    private LinearLayout mLl;
    private Animation mInAnim;
    private Animation mOutAnim;
    private boolean mIsShowing;
    private LinearLayout mCenterLL;
    private LinearLayout mDownSheetLL;
    private boolean mNormalHidden = true;

    public enum Style {
        Center, DownSheet
    }

    private ViewGroup mDecorView;
    private ViewGroup mRootView;
    private ViewGroup mContentView;
    private TextView mTitleTV;
    private TextView mMsgTV;
    private Activity mActivity;
    private Style mStyle;
    private List<String> mItems;
    private String mTitle;
    private String mMsg;
    private onItemClickListener mItemClickListener;
    private onDismissListener mDismissListener;
    private boolean mCancelable;


    public TDialog(@NonNull Activity activity, Style style, String[] items, String title, String msg, onItemClickListener onItemClickListener) {
        initParams(activity, style, Arrays.asList(items), title, msg, onItemClickListener);
        initViews();
        initContentView();
        initAnim();
    }


    private void initParams(Activity activity, Style style, List<String> items, String title, String msg, onItemClickListener anInterface) {
        mActivity = activity;
        mStyle = style == null ? Style.Center : style;
        mItems = items;
        mTitle = title;
        mMsg = msg;
        mItemClickListener = anInterface;
        mCancelable = mStyle == Style.Center;

    }

    private void initViews() {
        mDecorView = (ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        mRootView = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.dialog_root, mDecorView, false);
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (mCancelable && action == MotionEvent.ACTION_UP) {
                    if (mNormalHidden) {
                        dismiss(true);
                    }
                }
                return true;
            }
        });
    }

    private void initContentView() {
        switch (mStyle) {
            case Center:
                initCenterLayout();
                initCenterView();
                break;
            case DownSheet:
                initDownSheetLayout();
                initDownSheetView();
                break;
        }
    }

    private void initCenterLayout() {
        mContentView = (ViewGroup) mRootView.findViewById(R.id.dialog_root_content_fl);
        int marginLR = mActivity.getResources().getDimensionPixelSize(R.dimen.margin_center_left_right);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        lp.gravity = Gravity.CENTER;
        lp.setMargins(marginLR, 0, marginLR, 0);
        mContentView.setLayoutParams(lp);

    }

    private void initDownSheetLayout() {
        mContentView = (ViewGroup) mRootView.findViewById(R.id.dialog_root_content_fl);
        int marginLR = mActivity.getResources().getDimensionPixelSize(R.dimen.margin_downSheet_left_right);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        lp.gravity = Gravity.BOTTOM;
        lp.setMargins(marginLR, 0, marginLR, 0);
        mContentView.setLayoutParams(lp);
    }


    private void initCenterView() {
        mContentView = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.dialog_content_conter, mContentView);
        mCenterLL = (LinearLayout) mContentView.findViewById(R.id.center_ll);
        initHeaderView(R.id.center_title_tv, R.id.center_msg_tv);
        initRecyclerView(R.id.center_content_rv);
    }


    private void initDownSheetView() {
        mContentView = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.dialog_content_downsheet, mContentView);
        mDownSheetLL = (LinearLayout) mContentView.findViewById(R.id.downSheet_ll);
        initHeaderView(R.id.downSheet_title_tv, R.id.downSheet_msg_tv);
        initRecyclerView(R.id.downSheet_content_rv);
    }

    private void initHeaderView(@IdRes int titleId, @IdRes int msgId) {
        mTitleTV = (TextView) mRootView.findViewById(titleId);
        mMsgTV = (TextView) mRootView.findViewById(msgId);
        mTitleTV.setText(TextUtils.isEmpty(mTitle) ? "默认标题" : mTitle);
        if (TextUtils.isEmpty(mMsg)) mMsgTV.setVisibility(View.GONE);
        else mMsgTV.setText(mMsg);
    }

    private void initRecyclerView(@IdRes int rvId) {
        RecyclerView rv = (RecyclerView) mRootView.findViewById(rvId);
        mLl = (LinearLayout) mRootView.findViewById(R.id.center_content_ll);
        if (mStyle.equals(Style.Center) && mItems != null && mItems.size() == 2) {
            twoItemWork(rv, mLl);
        } else {
            otherItemWork(rv, mLl);
        }
    }


    private void twoItemWork(RecyclerView rv, LinearLayout ll) {
        rv.setVisibility(View.GONE);
        for (int i = 0; i < 2; i++) {
            View item = mActivity.getLayoutInflater().inflate(R.layout.dialog_item, null);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) item.getLayoutParams();
            TextView textView = (TextView) item.findViewById(R.id.dialog_item_tv);
            textView.setText(mItems.get(i));
            ll.addView(item, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            if (i == 0) {
                textView.setBackgroundResource(R.drawable.bg_left);
                View divider = new View(mActivity);
                divider.setBackgroundColor(mActivity.getResources().getColor(R.color.bgColor_divier));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) mActivity.getResources().getDimension(R.dimen.size_divider_line),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                ll.addView(divider, params);
            }
            if (i == 1) {
                textView.setBackgroundResource(R.drawable.bg_right);
            }
            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(TDialog.this, finalI);
                    }
                    if (mNormalHidden) {
                        dismiss(false);
                    }
                }
            });

        }
    }

    private void otherItemWork(RecyclerView rv, LinearLayout ll) {
        if (mStyle == Style.Center) {
            ll.setVisibility(View.GONE);
        }
        rv.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new DialogAdapter(mActivity);
        mAdapter.setList(mItems);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new DialogAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(TDialog.this, position);
                }
                if (mNormalHidden) {
                    dismiss(false);
                }
            }
        });
        if (mStyle == DownSheet) {
            mCancelTV = (TextView) mContentView.findViewById(R.id.downSheet_Cancel_tv);
            mCancelTV.setText("取消");
            mCancelTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNormalHidden) {
                        dismiss(true);
                    }
                }
            });
        }
    }

    private void initAnim() {
        int inAnimRes = AnimUtil.getAnimRes(mStyle, true);
        mInAnim = AnimationUtils.loadAnimation(mActivity, inAnimRes);
        int outAnimRes = AnimUtil.getAnimRes(mStyle, false);
        mOutAnim = AnimationUtils.loadAnimation(mActivity, outAnimRes);
    }

    //------------------------------显示和消失处理--------------------------------------
    public void show() {
        if (mIsShowing) {
            return;
        }
        mDecorView.addView(mRootView);
        mIsShowing = true;
        mContentView.startAnimation(mInAnim);
    }

    public void dismiss() {
        if (mNormalHidden) {
            dismiss(false);
        }
    }

    public void dismissImmediately() {
        if (mNormalHidden) {
            dismissImmediately(false);
        }
    }

    private void dismiss(final boolean cancelListener) {
        if (!mIsShowing) {
            return;
        }
        mOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismissImmediately(cancelListener);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContentView.startAnimation(mOutAnim);
    }

    private void dismissImmediately(boolean cancelListener) {
        if (!mIsShowing) {
            return;
        }
        mDecorView.removeView(mRootView);
        mIsShowing = false;
        if (mDismissListener != null && cancelListener) {
            mDismissListener.onDismissClick(this);
        }
    }
    //-------------------------------扩展设置-------------------------------------------

    public void setDismissListener(onDismissListener dismissListener) {
        mDismissListener = dismissListener;
    }

    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
    }

    //-------------------------------监听回调接口------------------------------------------------------

    public interface onItemClickListener {
        void onItemClick(Object object, int position);
    }

    public interface onDismissListener {
        void onDismissClick(Object object);
    }

    //--------------------------------样式扩展--------------------------------------------------------

    public void setTitleTextColor(@ColorInt int color) {
        mTitleTV.setTextColor(color);
    }

    public void setTitleTextSize(int dp) {
        mTitleTV.setTextSize(dp);
    }

    public void setMsgTextColor(@ColorInt int color) {
        mMsgTV.setTextColor(color);
    }

    public void setMsgTextSize(int dp) {
        mMsgTV.setTextSize(dp);
    }


    public void setCancelTextColor(@ColorInt int color) {
        if (mCancelTV != null) {
            mCancelTV.setTextColor(color);
        }
    }

    public void setCancelTextSize(int dp) {
        if (mCancelTV != null) {
            mCancelTV.setTextSize(dp);
        }
    }

    public void setItemTextColor(@ColorInt int color) {
        if (mAdapter != null) {
            mAdapter.setTextColor(color);
        }
        if (mLl != null && mLl.getChildCount() == 3) {
            ((TextView) ((LinearLayout) mLl.getChildAt(0)).getChildAt(1)).setTextColor(color);
            ((TextView) ((LinearLayout) mLl.getChildAt(2)).getChildAt(1)).setTextColor(color);
        }
    }

    public void setItemTextSize(int dp) {
        if (mAdapter != null) {
            mAdapter.setTextSize(dp);
        }
        if (mLl != null && mLl.getChildCount() == 3) {
            ((TextView) ((LinearLayout) mLl.getChildAt(0)).getChildAt(1)).setTextSize(dp);
            ((TextView) ((LinearLayout) mLl.getChildAt(2)).getChildAt(1)).setTextSize(dp);
        }
    }

    public void setItemTextColorAt(int position, @ColorInt int color) {
        if (mAdapter != null) {
            mAdapter.setTextColorAt(position, color);
        }
        if (mLl != null && mLl.getChildCount() == 3 && position <= 1) {
            if (position == 1) {
                position++;
            }
            ((TextView) ((LinearLayout) mLl.getChildAt(position)).getChildAt(1)).setTextColor(color);

        }
    }

    public void setItemTextSizeAt(int position, int dp) {
        if (mAdapter != null) {
            mAdapter.setTextSizeAt(position, dp);
        }
        if (mLl != null && mLl.getChildCount() == 3 && position <= 2) {
            ((TextView) ((LinearLayout) mLl.getChildAt(position)).getChildAt(1)).setTextSize(dp);
        }
    }

    /**
     * @param leftDP   the unit is dp
     * @param topDp    the unit is dp
     * @param rightDp  the unit is dp
     * @param bottomDp the unit is dp
     */
    public void setMargin(int leftDP, int topDp, int rightDp, int bottomDp) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        int left = DensityUtil.dp2px(mActivity, leftDP);
        int top = DensityUtil.dp2px(mActivity, topDp);
        int right = DensityUtil.dp2px(mActivity, rightDp);
        int bottom = DensityUtil.dp2px(mActivity, bottomDp);
        switch (mStyle) {
            case Center:
                lp.gravity = Gravity.CENTER;
                break;
            case DownSheet:
                lp.gravity = Gravity.BOTTOM;
                break;
        }
        lp.setMargins(left, top, right, bottom);
        mContentView.setLayoutParams(lp);
    }

    /**
     * @param bottomDp the unit is dp
     */
    public void setMarginBottom(int bottomDp) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        int bottom = DensityUtil.dp2px(mActivity, bottomDp);
        switch (mStyle) {
            case Center:
                lp.gravity = Gravity.CENTER;
                break;
            case DownSheet:
                lp.gravity = Gravity.BOTTOM;
                break;
        }
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottom);
        mContentView.setLayoutParams(lp);
    }

    /**
     * @param topDp the unit is dp
     */
    public void setMarginTop(int topDp) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        int top = DensityUtil.dp2px(mActivity, topDp);
        switch (mStyle) {
            case Center:
                lp.gravity = Gravity.CENTER;
                break;
            case DownSheet:
                lp.gravity = Gravity.BOTTOM;
                break;
        }
        lp.setMargins(lp.leftMargin, top, lp.rightMargin, lp.bottomMargin);
        mContentView.setLayoutParams(lp);
    }

    /**
     * @param specialDp the unit is dp
     */
    public void setMarginLeftAndRight(int specialDp) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        int special = DensityUtil.dp2px(mActivity, specialDp);
        switch (mStyle) {
            case Center:
                lp.gravity = Gravity.CENTER;
                break;
            case DownSheet:
                lp.gravity = Gravity.BOTTOM;
                break;
        }
        lp.setMargins(special, lp.topMargin, special, lp.bottomMargin);
        mContentView.setLayoutParams(lp);
    }

    public void setInAnim(Animation inAnim) {
        mInAnim = inAnim;
        if (mOutAnim == null) {
            mOutAnim = AnimationUtils.loadAnimation(mActivity, AnimUtil.getAnimRes(mStyle, true));
        }
    }

    public void setOutAnim(Animation outAnim) {
        mOutAnim = outAnim;
        if (mOutAnim == null) {
            mOutAnim = AnimationUtils.loadAnimation(mActivity, AnimUtil.getAnimRes(mStyle, false));
        }
    }


    //-------------------------------------addView--------------------------------
    public void addView(View view) {
        if (view == null) {
            return;
        }
        view.setBackgroundResource(R.drawable.bg_item);
        switch (mStyle) {
            case Center:
                mCenterLL.addView(view, 2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                break;
            case DownSheet:
                mDownSheetLL.addView(view, 2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                break;
        }
    }

    public void setMsgGravity(int gravity) {
        mMsgTV.setGravity(gravity);
    }

    public void setTitleGravity(int gravity) {
        mTitleTV.setGravity(gravity);
    }

    /**
     * @param leftDp   the unit is dp
     * @param topDp    the unit is dp
     * @param rightDp  the unit is dp
     * @param bottomDp the unit is dp
     */
    public void setMsgPaddingLeft(int leftDp, int topDp, int rightDp, int bottomDp) {
        mMsgTV.setPadding(DensityUtil.dp2px(mActivity, leftDp), DensityUtil.dp2px(mActivity, topDp),
                DensityUtil.dp2px(mActivity, rightDp), DensityUtil.dp2px(mActivity, bottomDp));
    }

    /**
     * @param leftDp   the unit is dp
     * @param topDp    the unit is dp
     * @param rightDp  the unit is dp
     * @param bottomDp the unit is dp
     */
    public void setTitlePaddingLeft(int leftDp, int topDp, int rightDp, int bottomDp) {
        mTitleTV.setPadding(DensityUtil.dp2px(mActivity, leftDp), DensityUtil.dp2px(mActivity, topDp),
                DensityUtil.dp2px(mActivity, rightDp), DensityUtil.dp2px(mActivity, bottomDp));
    }

    public TextView getTitleView() {
        return mTitleTV;
    }

    public TextView getMsgView() {
        return mMsgTV;
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void isNormalHidden(boolean normalHidden) {
        mNormalHidden = normalHidden;
    }
}
