package com.lchli.loaderlib;

public interface ListItem<DATATYPE> {

    void bindData(DATATYPE data);


    void onLoadFail();

}