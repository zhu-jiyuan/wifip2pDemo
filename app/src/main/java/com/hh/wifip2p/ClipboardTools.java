package com.hh.wifip2p;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;

public class ClipboardTools {

    private ClipboardManager mClipboard;

    private MainActivity mActivity;

    private String older_s;

    public ClipboardTools(MainActivity activity,ClipboardManager clipboardManager){
        mActivity = activity;
        mClipboard = clipboardManager;
        older_s = "";
    }

    public String getData(){
        ClipData clipData = mClipboard.getPrimaryClip();
        String ans = null;
        if(clipData!=null&&clipData.getItemCount()>0){
            ClipData.Item item = clipData.getItemAt(0);
            ans = item.getText().toString();
            Log.d("run info", "getData: "+ans);
            mActivity.handler.obtainMessage(MessageOptions.MESSAGE_CLIP_STRING.getValue(),ans).sendToTarget();
        }
        return ans;
    }

    public void setTxt(String txt){
        older_s = txt;
        mClipboard.setPrimaryClip(ClipData.newPlainText("",txt));
    }

    public boolean check(String msg){
        return msg.equals(older_s);
    }
}
