package com.investigatorsapp.network;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.investigatorsapp.logger.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;

public class FileUploaderOld {
	
	public static final String TAG = FileUploaderOld.class.getSimpleName();
	
	private AsyncTask<Void, Void, Integer> task;
	
	private String failReason;

	private String url;

	private String fileName;

	private String postParams;

    private String response;

	public FileUploaderOld(String url, String fileName, String postParams) {
		this.url = url;
		this.fileName = fileName;
		this.postParams = postParams;
	}
	
	public void uploader(final UpLoaderCallback callBack){
		task = new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				try {
					return uploadFile();
				} catch (Exception e) {
					e.printStackTrace();
					failReason = e.toString();
				}
				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if(callBack != null) {
					if(result == HttpStatus.SC_OK) {
						callBack.onSuccess(response);
					}else {
						if(result == 0) {
							callBack.onFailed(result, failReason);
						}else {
							callBack.onFailed(result, "错误码: " + String.valueOf(result));
						}
					}
				}
			}
		};
		task.execute();
	}
	
	public boolean cancleUploader() {
		if(task != null) {
			return task.cancel(true);
		}
		return false;
	}
	
	private int uploadFile() throws Exception {
		HttpClient httpclient = null;
		try {
			Logger.d(TAG, "request uri: " + url + ", filename : " + fileName + ", params = " + postParams);
			
			File file = new File(fileName);
			if (!file.exists()) {
				throw new Exception("the file " + fileName + " not exists");
			}
			
			httpclient = new DefaultHttpClient();
			final HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Connection", "Keep-Alive");
			
			final MultipartEntity multipartEntity = new MultipartEntity();
			
			if(!TextUtils.isEmpty(postParams)) {
				multipartEntity.addPart("name",new StringBody(postParams));
			}
			
			multipartEntity.addPart("file", new FileBody(file, "application/octet-stream", HTTP.UTF_8));
			
			httpPost.setEntity(multipartEntity);

			HttpResponse httpResponse = httpclient.execute(httpPost);
			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			response = EntityUtils.toString(httpResponse.getEntity(),
					HTTP.UTF_8);
			Logger.d("Test", "got response:\n" + response);
			return statusCode;
		} finally {
			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
				httpclient = null;
			}
		}
	}
	
	public interface UpLoaderCallback {
		public void onSuccess(String response);
		public void onFailed(int responseCode, String failReason);
	}
	
}
