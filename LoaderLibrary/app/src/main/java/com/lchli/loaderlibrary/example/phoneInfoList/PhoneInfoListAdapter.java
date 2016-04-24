package com.lchli.loaderlibrary.example.phoneInfoList;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lchli.loaderlib.ListItem;
import com.lchli.loaderlibrary.R;
import com.lchli.loaderlibrary.base.AbsAdapter;
import com.lchli.loaderlibrary.base.AbsViewHolder;

import java.util.List;

/**
 * Created by lchli on 2016/4/24.
 */
public class PhoneInfoListAdapter extends AbsAdapter<String, PhoneInfoListAdapter.Holder> {

    private final PhoneInfoLoader loader = new PhoneInfoLoader();


    public PhoneInfoListAdapter(List<String> datas) {
        super(datas);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(View.inflate(parent.getContext(), R.layout.list_item_phone_info, null));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        String phoneNumber = mDatas.get(position);
        holder.tvPhoneNumber.setText(phoneNumber);
        holder.tvPhoneAddress.setText("loading...");
        final String key = phoneNumber;
        loader.load(key, position, holder, phoneNumber);

    }

    static class Holder extends AbsViewHolder implements ListItem<PhoneInfoResponse.PhoneInfo> {

        private final TextView tvPhoneNumber;
        private final TextView tvPhoneAddress;

        public Holder(View itemView) {
            super(itemView);
            tvPhoneNumber = (TextView) itemView.findViewById(R.id.tvPhoneNumber);
            tvPhoneAddress = (TextView) itemView.findViewById(R.id.tvPhoneAddress);
        }

        @Override
        public void bindData(PhoneInfoResponse.PhoneInfo data) {
            if(TextUtils.isEmpty(data.province)&&TextUtils.isEmpty(data.carrier)){
                tvPhoneAddress.setText("unknown");
            }else{
                tvPhoneAddress.setText(String.format("%s [%s]", data.province, data.carrier));
            }
        }

        @Override
        public void onLoadFail() {
            //set fail image.
            tvPhoneAddress.setText("unknown");
        }
    }
}
