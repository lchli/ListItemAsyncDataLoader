package com.lchli.loaderlibrary.example;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lchli.loaderlib.ListItem;
import com.lchli.loaderlibrary.R;
import com.lchli.loaderlibrary.base.AbsAdapter;
import com.lchli.loaderlibrary.base.AbsViewHolder;

import java.util.List;

/**
 * Created by lchli on 2016/4/24.
 */
public class NetPictureListAdapter extends AbsAdapter<NetPicture, NetPictureListAdapter.Holder> {

    private final NetImageLoader mNetImageLoader = new NetImageLoader();


    public NetPictureListAdapter(List<NetPicture> datas) {
        super(datas);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(View.inflate(parent.getContext(), R.layout.list_item_net_picture, null));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        NetPicture picture = mDatas.get(position);
        holder.textView.setText(picture.pic_url);
        final String key = picture.pic_url;
        holder.imageView.setImageResource(R.drawable.ic_launcher);
        mNetImageLoader.load(key, position, holder, picture.pic_url);

    }

    static class Holder extends AbsViewHolder implements ListItem<Bitmap> {

        private final ImageView imageView;
        private final TextView textView;

        public Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }

        @Override
        public void bindData(Bitmap data) {
            imageView.setImageBitmap(data);
        }

        @Override
        public void onLoadFail() {
            //set fail image.
            imageView.setImageResource(R.drawable.ic_launcher);
        }
    }
}
