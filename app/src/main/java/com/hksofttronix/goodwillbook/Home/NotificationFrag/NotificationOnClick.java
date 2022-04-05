package com.hksofttronix.goodwillbook.Home.NotificationFrag;

import com.hksofttronix.goodwillbook.VolleyUtil.common.NotificationModel;

public interface NotificationOnClick {
    public void viewDetail(int position, NotificationModel model);
    public void onLongPress(int position, NotificationModel model);
}
