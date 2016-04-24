# ListItemAsyncDataLoader
this library is used for loading android listItem async data.unlike other imageLoader ,this lib can load any async data for listItem not just image.

## Using ListItemAsyncDataLoader in your application
First ,Download loaderlib and add dependencies to your project.
If you are building with Gradle, import loaderlib module,then add the following line to the `dependencies` section of your `build.gradle` file:

```groovy
compile project':loaderlib'
```



## 1,screenshot for loading net pictures. 
![Screenshot](https://github.com/lchli/ListItemAsyncDataLoader/raw/master/LoaderLibrary/screenshot/shot_net_picturelist.png)

## 2,screenshot for loading net phone info list. 
![Screenshot](https://github.com/lchli/ListItemAsyncDataLoader/raw/master/LoaderLibrary/screenshot/shot_phone_info_list.png)

## Usage. 
```groovy
1)Create your loader extends ListItemAsyncDataLoader.
Implements the method getDataLogic(...) to do your loadData logic.

2)You also can override these cache method to implements your cache,such as getMemoryCache(...),getDiskCache(...),etc.

3)Example:
package com.lchli.loaderlibrary.example.netPictureList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lchli.loaderlib.android.ListItemAsyncDataLoader;
import com.lchli.loaderlibrary.CacheManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * this is sample for load net pictures,and show how to use memory cache with my loader.
 * Created by lchli on 2016/4/24.
 */
public class NetImageLoader extends ListItemAsyncDataLoader<String, Bitmap> {


    @Override
    protected Bitmap getDataLogic(Object... args) {
        //if image is too large,you can resize it here.
        String url = (String) args[0];
        byte[] bmpbytes = downloadFile(url);
        if (bmpbytes != null) {
            return BitmapFactory.decodeByteArray(bmpbytes, 0, bmpbytes.length);
        }
        return null;
    }

    private static byte[] downloadFile(String urlstr) {
        try {
            URL url = new URL(urlstr);
            URLConnection con = url.openConnection();
            con.setDoInput(true);
            con.connect();
            InputStream ins = con.getInputStream();
            byte[] buffer = new byte[10240];
            int len = -1;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = ins.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            ins.close();
            bos.flush();
            byte[] ret = bos.toByteArray();
            bos.close();
            return ret;
        } catch (Error | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void putMemoryCache(String key, Bitmap value) {
        CacheManager.getBitmapMemoryCacher().put(key, value);
    }

    @Override
    protected void removeMemoryCache(String key) {
        CacheManager.getBitmapMemoryCacher().remove(key);
    }

    @Override
    protected Bitmap getMemoryCache(String cacheKey) {
        return CacheManager.getBitmapMemoryCacher().get(cacheKey);
    }
}

4)Now you can use it in your Adapter.Like this:

YourLoader yourLoader = new YourLoader();
yourLoader.load(...);

Below is a demo adapter:

package com.lchli.loaderlibrary.example.netPictureList;

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





```
