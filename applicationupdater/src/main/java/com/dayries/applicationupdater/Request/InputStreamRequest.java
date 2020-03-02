package com.dayries.applicationupdater.Request;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class InputStreamRequest extends Request<byte[]> {
    private final Map<String, String> headers;
    private final Response.Listener<byte[]> listener;

    public InputStreamRequest(int method, String url, Map<String, String> headers, Response.Listener<byte[]> listener1, @Nullable Response.ErrorListener listener) {
        super(method, url, listener);
        this.headers = headers;
        this.listener = listener1;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {

        //Pass the response data here
        return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(byte[] response) {
        listener.onResponse(response);
    }
}
