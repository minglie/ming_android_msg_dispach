package com.hanny.listenpushmsg.utils;

import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class HttpUtil {
    private static Handler handler=new Handler();
    public static void get(final String strUrl, final Map strMap, final HttpCallBack callBack){
       new Thread(){
            @Override
            public void run() {
                HttpURLConnection connection=null;
                InputStream is=null;
                try {
                    StringBuilder stringBuffer=new StringBuilder(strUrl);

                    if(strMap !=null){
                        stringBuffer.append("?");
                        for (Object key:strMap.keySet()) {
                            stringBuffer.append(key+"="+strMap.get(key)+"&");
                        }
                        stringBuffer.deleteCharAt(stringBuffer.length()-1);
                    }
                    URL url=new URL(stringBuffer.toString());
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10*1000);
                    if (connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                        is=connection.getInputStream();
                        final String result=InputStreamToString(is);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onSuccess(result);
                            }
                        });

                    }else{
                        throw new Exception("ResponseCode:"+connection.getResponseCode());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onError(e);
                        }
                    });

                }finally {
                    if (connection!=null)connection.disconnect();
                    if (is!=null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onFinish();
                        }
                    });
                }
            }
        }.start();
    }
    public static void post( final String strUrl,final Map strMap, final HttpCallBack callBack){
        Thread thread=new Thread(){
            @Override
            public void run() {
                HttpURLConnection connection=null;
                OutputStream os=null;
                InputStream is=null;
                try {
                    StringBuilder stringBuilder=new StringBuilder();
                    for (Object key:strMap.keySet()){
                        stringBuilder.append(key+"="+strMap.get(key)+"&");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length()-1);
                    URL url=new URL(strUrl);
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(10*1000);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Charset","utf-8");
                    connection.connect();
                    os=connection.getOutputStream();
                    os.write(stringBuilder.toString().getBytes());
                    os.flush();
                    if (connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                        is=connection.getInputStream();
                        final String result=InputStreamToString(is);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onSuccess(result);
                            }
                        });
                    }else{
                        throw new Exception("ResponseCode:"+connection.getResponseCode());
                    }
                }catch (final Exception e){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onError(e);
                        }
                    });
                }finally {
                    if (connection!=null)connection.disconnect();
                    try {
                        if (is!=null) is.close();
                        if(os!=null)os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onFinish();
                        }
                    });
                }
            }
        };
        thread.start();
    }
    public static String InputStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        byte[] data=new byte[1024];
        int len=-1;
        while((len=is.read(data))!=-1){
            os.write(data,0,len);
        }
        os.flush();
        os.close();
        is.close();
        byte[] lens = os.toByteArray();
        String result=new String(lens,"UTF-8");
        return result;
    }
    public interface HttpCallBack{
        public void onSuccess(String result);
        public void onError(Exception e);
        public void onFinish();
    }
}

