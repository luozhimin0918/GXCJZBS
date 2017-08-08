package com.jyh.gxcjzbs.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.jyh.gxcjzbs.R;
import com.jyh.gxcjzbs.WebActivity;
import com.jyh.gxcjzbs.bean.NavIndextEntity;
import com.jyh.gxcjzbs.bean.UserBean;

import java.util.List;

/**
 * Created by Mr'Dai on 2017/5/18.
 */

public class MarketGridAdapter extends BaseListAdapter<NavIndextEntity.DataBean.ButtonBean> {

    private Context mContext;
    private LayoutInflater mInflater;
    Intent intent2;
    public MarketGridAdapter(Context mContext, List<NavIndextEntity.DataBean.ButtonBean> dataList) {
        super(dataList);
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
        intent2 = new Intent(mContext, WebActivity.class);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_home_header_btn, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.nameTv = (TextView) convertView.findViewById(R.id.tv);
            mViewHolder.photoIv = (ImageView) convertView.findViewById(R.id.iv);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final NavIndextEntity.DataBean.ButtonBean userBean = dataList.get(position);
        mViewHolder.nameTv.setText(userBean.getTitle());
        try {
            Glide.with(mContext).load(userBean.getImage()).error(R.drawable.ic_default_adimage).placeholder(R.drawable.ic_default_adimage).into
                    (mViewHolder.photoIv);
        } catch (Exception e) {
            e.printStackTrace();
            Glide.with(mContext).load(R.drawable.ic_default_adimage).into(mViewHolder.photoIv);
        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent2.putExtra(
                        "url",
                        dataList.get(position).getUrl());
                intent2.putExtra("from", "main");
                intent2.putExtra("title", dataList.get(position).getTitle());
                mContext.startActivity(intent2);
            }
        });

        return convertView;
    }

    class ViewHolder {
        private TextView nameTv;
        private ImageView photoIv;



        public TextView getNameTv() {
            return nameTv;
        }

        public void setNameTv(TextView nameTv) {
            this.nameTv = nameTv;
        }

        public ImageView getPhotoIv() {
            return photoIv;
        }

        public void setPhotoIv(ImageView photoIv) {
            this.photoIv = photoIv;
        }
    }

}
