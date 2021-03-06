package com.lchli.loaderlibrary.example.netPictureList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.apkfuns.logutils.LogUtils;
import com.lchli.loaderlibrary.R;
import com.lchli.loaderlibrary.example.phoneInfoList.PhoneInfoListActivity;
import com.lchli.loaderlibrary.okhttpWraper.OkHttpRequestUtils;
import com.lchli.loaderlibrary.okhttpWraper.OkUiThreadCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lchli on 2016/4/23.
 */
public class NetPictureListActivity extends Activity {

    private NetPictureListAdapter mNetPictureListAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_picture_list);
        listView = (ListView) findViewById(R.id.listView);
        findViewById(R.id.btOpenPhoneList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), PhoneInfoListActivity.class);
                NetPictureListActivity.this.startActivity(it);
            }
        });
        mNetPictureListAdapter = new NetPictureListAdapter(null);
        listView.setAdapter(mNetPictureListAdapter);

        Map<String, String> params = new HashMap<>();
        params.put("query", "grassland");
        params.put("start", "10");
        params.put("reqType", "ajax");
        OkHttpRequestUtils.post("http://pic.sogou.com/pics", params, new OkUiThreadCallback<NetPicturesResponse>() {
            @Override
            public void onSuccess(int code, NetPicturesResponse o) {
                mNetPictureListAdapter.refresh(o.items);
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                LogUtils.e("onFailure");

            }
        });

    }


}
