package com.investigatorsapp.network;

import com.google.gson.Gson;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.FileUploadInfo;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.MD5;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;

import cz.msebera.android.httpclient.Header;

public class FileUploaderAsyncHttp {

	public static final String TAG = FileUploaderAsyncHttp.class.getSimpleName();

	private String url;

	private File file;

	public FileUploaderAsyncHttp(String url, File file) {
		this.url = url;
		this.file = file;
	}
	
	public void uploader(final UpLoaderCallback callback) {
		try {
			RequestParams params = new RequestParams();
			params.put("type", "uploadfile");
			params.put("filename", file.getName());
			params.put("md5",  MD5.md5sum(file.getAbsolutePath()));
			params.put("filesize", file.length());
			params.put("uploadfile", file);
			params.put("userid", UserSingleton.getInstance().getUser().userid);
			params.put("jobid", UserSingleton.getInstance().getUser().jobid);
			params.put("token", UserSingleton.getInstance().getUser().token);
			final AsyncHttpClient client = new AsyncHttpClient();
			client.post(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					try {
						String responseMsg = new String(responseBody, "utf-8");
						FileUploadInfo fileUploadInfo = null;
						if(statusCode == 200) {
							Gson gson = new Gson();
							fileUploadInfo = gson.fromJson(responseMsg, FileUploadInfo.class);
							if(fileUploadInfo != null && Constant.RET_SUCCESS_CODE.equals(fileUploadInfo.retcode)) {
								if(callback != null) {
									callback.onSuccess(responseMsg);
									return;
								}
							}
						}
						if(callback != null) {
							if(fileUploadInfo != null) {
								callback.onFailed(statusCode, new String(
										fileUploadInfo.retmessage.getBytes(), "utf-8"));
							}else {
								callback.onFailed(statusCode, responseMsg);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						if(callback != null) {
							callback.onFailed(statusCode, e.toString());
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {Logger.d(TAG, "onFailure, statusCode = " + statusCode);
					String responseMsg = "";
					try {
						if(responseBody != null) {
							responseMsg = new String(responseBody, "utf-8");
						}
					} catch (Exception e) {
						e.printStackTrace();
						responseMsg = e.toString();
					}
					if(callback != null) {
						callback.onFailed(statusCode, responseMsg);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			if(callback != null) {
				callback.onFailed(-1, e.toString());
			}
		}
	}

	public interface UpLoaderCallback {
		public void onSuccess(String response);
		public void onFailed(int responseCode, String failReason);
	}
	
}
