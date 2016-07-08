package com.tiger.quicknews.activity;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.baifendian.mobile.BfdAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.tiger.quicknews.R;
import com.tiger.quicknews.bean.NewDetailModle;
import com.tiger.quicknews.bean.NewModle;
import com.tiger.quicknews.http.HttpUtil;
import com.tiger.quicknews.http.json.NewDetailJson;
import com.tiger.quicknews.utils.Options;
import com.tiger.quicknews.utils.StringUtils;
import com.tiger.quicknews.wedget.ProgressPieView;
import com.tiger.quicknews.wedget.htmltextview.HtmlTextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_details)
public class DetailsActivity extends BaseActivity implements
		ImageLoadingListener, ImageLoadingProgressListener {

	@ViewById(R.id.new_title)
	protected TextView newTitle;
	@ViewById(R.id.new_time)
	protected TextView newTime;
	@ViewById(R.id.wb_details)
	protected WebView webView;
	// @ViewById(R.id.progressBar)
	// protected ProgressBar progressBar;
	@ViewById(R.id.progressPieView)
	protected ProgressPieView mProgressPieView;
	@ViewById(R.id.new_img)
	protected ImageView newImg;
	@ViewById(R.id.img_count)
	protected TextView imgCount;
	@ViewById(R.id.play)
	protected ImageView mPlay;
	private String newUrl;
	private NewModle newModle;
	private String newID;
	protected ImageLoader imageLoader;
	private String imgCountString;

	protected DisplayImageOptions options;

	private NewDetailModle newDetailModle;

	@AfterInject
	public void init() {
		try {
			newModle = (NewModle) getIntent().getExtras().getSerializable(
					"newModle");
			newID = newModle.getDocid();
			newUrl = getUrl(newID);
			imageLoader = ImageLoader.getInstance();
			options = Options.getListOptions();

			BfdAgent.onVisit(DetailsActivity.this, newID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("JavascriptInterface")
	@AfterViews
	public void initWebView() {
		try {
			mProgressPieView.setShowText(true);
			mProgressPieView.setShowImage(false);
			WebSettings settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);// 设置可以运行JS脚本
			settings.setDefaultFontSize(16);
			settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			settings.setSupportZoom(false);// 用于设置webview放大
			settings.setBuiltInZoomControls(false);
			webView.setBackgroundResource(R.color.transparent);
			webView.setWebViewClient(new MyWebViewClient());
			showProgressDialog();
			loadData(newUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadData(String url) {
		if (hasNetWork()) {
			loadNewDetailData(url);
		} else {
			dismissProgressDialog();
			showShortToast(getString(R.string.not_network));
			String result = getCacheStr(newUrl);
			if (!StringUtils.isEmpty(result)) {
				getResult(result);
			}
		}
	}

	@Background
	public void loadNewDetailData(String url) {
		String result;
		try {
			// result = HttpUtil.getByHttpClient(this, url);
			result = getRecData("iid");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取推荐
//	private boolean isComplete = false;
	private String result = null;

	public String getRecData(String url) {
		// String result = null;
		Map<String, String> params = new HashMap<String, String>();
		params.put("fmt", "{\"iid\":\"$iid\",\"contents\":\"$html\"}");
		params.put("iid", newID);
		BfdAgent.recommend(DetailsActivity.this,
				"rec_C6613205_93B6_61A6_9FEC_180B70F91B94", params,
				new BfdAgent.Runnable() {

					@Override
					public void run(String arg0, JSONArray arg1) {
						System.out.println(arg1.toString());
						if (arg1 != null && arg1.length() > 0) {
							for (int i = 0; i < arg1.length(); i++) {
								try {
									JSONObject js = (JSONObject) arg1.get(i);
									String str = js.getString("contents");
									if(str != null && !"null".equals(str) && !"".equals(str)){
										result = str;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							getResult(result);
						}
					}
				});
	
		return result;
	}

	@UiThread
	public void getResult(String result) {
		setCacheStr(newUrl, result);
		String content = getContent(result);
		
		dismissProgressDialog();
		webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
	}
	private String getContent(String content){
		String data = "<body>" ;
	
		/*		+
				"<center><h2 style='font-size:16px;'>" + newModle.getTitle() + "</h2></center>";
		data = data + "<p align='left' style='margin-left:10px'>" 
				+ "<span style='font-size:10px;'>" 
				+ newModle.getPtime()
				+ "</span>" 
				+ "</p>";*/
		data = data+"<P align=center>" + content +"</p>";
		data = data + "<hr size='1' />";
		data = data + "</body>";
		
		return data;
	}
	@UiThread
	/*
	 * public void showResult(String content){ webView }
	 */
	@Click(R.id.new_img)
	public void imageMore(View view) {
		try {
			Bundle bundle = new Bundle();
			bundle.putSerializable("newDetailModle", newDetailModle);
			if (!"".equals(newDetailModle.getUrl_mp4())) {
				bundle.putString("playUrl", newDetailModle.getUrl_mp4());
				openActivity(VideoPlayActivity_.class, bundle, 0);
			} else {
				openActivity(ImageDetailActivity_.class, bundle, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 监听
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			view.getSettings().setJavaScriptEnabled(true);
			super.onPageFinished(view, url);
			// html加载完成之后，添加监听图片的点击js函数
			// progressBar.setVisibility(View.GONE);
			dismissProgressDialog();
			webView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			view.getSettings().setJavaScriptEnabled(true);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// progressBar.setVisibility(View.GONE);
			dismissProgressDialog();
			super.onReceivedError(view, errorCode, description, failingUrl);
		}
	}

	@Override
	public void onLoadingStarted(String imageUri, View view) {
		mProgressPieView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoadingFailed(String imageUri, View view,
			FailReason failReason) {
		mProgressPieView.setVisibility(View.GONE);
	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		if (!"".equals(newDetailModle.getUrl_mp4())) {
			mPlay.setVisibility(View.VISIBLE);
		} else {
			imgCount.setVisibility(View.VISIBLE);
			imgCount.setText(imgCountString);
		}
		mProgressPieView.setVisibility(View.GONE);
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		mProgressPieView.setVisibility(View.GONE);
	}

	@Override
	public void onProgressUpdate(String imageUri, View view, int current,
			int total) {
		int currentpro = (current * 100 / total);
		if (currentpro == 100) {
			mProgressPieView.setVisibility(View.GONE);
			mProgressPieView.setShowText(false);
		} else {
			mProgressPieView.setVisibility(View.VISIBLE);
			mProgressPieView.setProgress(currentpro);
			mProgressPieView.setText(currentpro + "%");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
