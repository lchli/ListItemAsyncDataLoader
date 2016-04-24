package com.lchli.loaderlibrary.base;

import android.view.View;

public  abstract class AbsViewHolder {
        public final View itemView;

        public AbsViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
    }