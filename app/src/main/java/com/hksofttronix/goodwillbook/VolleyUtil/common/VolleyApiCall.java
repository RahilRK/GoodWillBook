package com.hksofttronix.goodwillbook.VolleyUtil.common;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by iit_amiyo on 10/10/17.
 */

public class VolleyApiCall
{
    String TAG = this.getClass().getSimpleName();
    Context context;

    Globalclass globalclass;

    public VolleyApiCall(Context context_) {
        context=context_;
        globalclass = Globalclass.getInstance(context);
    }

    public interface VolleyCallback
    {
        void onSuccessResponse(String result);
    }

    public void Volley_POST(final String Name, final Map<String, String> params, final String url, final VolleyCallback callback)
    {
        globalclass.log(TAG,url);

        StringRequest strREQ = new StringRequest(Request.Method.POST, url, new Response.Listener < String > ()
        {
            @Override
            public void onResponse(String response)
            {
                callback.onSuccessResponse(response);

                if(params.containsKey("url"))
                {
                    params.put("url","base64 string");
                }

                if(!response.equalsIgnoreCase(context.getResources().getString(R.string.onError)))
                {
                    globalclass.sendLog(Globalclass.SuccessApiCall,Name,url,"Volley_POST_onResponse",params.toString(),response);
                }
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError e)
            {
                if (e instanceof NetworkError) {
                } else if (e instanceof ServerError) {
                } else if (e instanceof AuthFailureError) {
                } else if (e instanceof ParseError) {
                } else if (e instanceof NoConnectionError) {
                } else if (e instanceof TimeoutError) {
                    callback.onSuccessResponse(context.getResources().getString(R.string.onError));
                    String error = Log.getStackTraceString(e);
                    globalclass.toast_long(context.getResources().getString(R.string.onError_error));

                    if(params.containsKey("url"))
                    {
                        params.put("url","base64 string");
                    }

                    globalclass.sendLog(Globalclass.ErrorApiCall,Name,url,"Volley_POST_onErrorResponse",params.toString(),error);
                }
            }
        })
        {
            /**
             * Passing some request headers
             * */
/*            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                return createBasicAuthHeader("enter_username", "enter_password");
            }*/

            @Override
            protected Map<String, String> getParams()
            {
                globalclass.log(TAG,"params:"+params);
                return params;
            }
        };

        strREQ.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(context).addToRequestQueue(strREQ);
    }
}