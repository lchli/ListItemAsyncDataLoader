package com.lchli.loaderlibrary.example.phoneInfoList;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.ListView;

import com.lchli.loaderlibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * this demo show how to load async phone info from net.
 * Created by lchli on 2016/4/23.
 */
public class PhoneInfoListActivity extends Activity {

    private PhoneInfoListAdapter mPhoneInfoListAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_picture_list);
        listView = (ListView) findViewById(R.id.listView);
        findViewById(R.id.btOpenPhoneList).setVisibility(View.GONE);
        mPhoneInfoListAdapter = new PhoneInfoListAdapter(null);
        listView.setAdapter(mPhoneInfoListAdapter);

        new LoadPhoneNumbersTask().execute();

    }

    private List<String> getPhones() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " desc");
            if (cursor == null)
                return null;
            List<String> mRecordList = new ArrayList<String>();
            while (cursor.moveToNext()) {
                mRecordList.add(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
            }
            return mRecordList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class LoadPhoneNumbersTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            return getPhones();
        }

        @Override
        protected void onPostExecute(List<String> phones) {
            mPhoneInfoListAdapter.refresh(phones);
        }
    }

}
