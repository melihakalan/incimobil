package com.incimobile;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Notice extends Activity
{
	private Application Global;
	private LinearLayout bodyContainer;
	private int iPage = 0, iMaxPages = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice);
		
		Global = this.getApplication();
		bodyContainer = (LinearLayout) findViewById(R.id.notice_body);
		
		loadNotices(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.notice, menu);
		return true;
	}
	
	private static Handler sHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
		}
	};
	
	private void loadNotices(final int page)
	{
		bodyContainer.removeAllViews();
		
		String url = "http://inci.sozlukspot.com/ss_index.php?sa=yazisma";
		if(page > 1)
			url += "&p=" + String.valueOf(page);
		
		final String fulladr = url;
		Log.d("load notices", fulladr);		
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				req.addHeader("Referer", "http://inci.sozlukspot.com");
				req.setHeader("Cookie", ((App)Global).m_sPunteriz + ((App)Global).m_sPHPSESSID);
				
				try
				{
					HttpResponse httpResponse = httpClient.execute(req);
					if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					{
						final String response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
						sHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								getNotices(response);
							}
						});
					}
					else
					{
						httpResponse.getEntity().getContent().close();
						sHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								((App)Global).ShowDialog(Notice.this, "Hata", "olan biten alinamadi.");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getNotices(String response)
	{
		int spos = 0, epos;
		
		if(iMaxPages == 0)
		{
			spos = response.indexOf("sayfa_yap_ops('");
			if(spos == -1)
				return;
			
			spos = response.indexOf('(', spos);
			epos = response.indexOf(',', spos);
			String maxpages = response.substring(spos + 2, epos - 1);
			Log.d("notice pages", maxpages);
			iMaxPages = Integer.parseInt(maxpages);
			iPage = iMaxPages;
			
			spos = 0;
		}
		
		while(true)
		{
			spos = response.indexOf("msj_uye_tablo_1", spos);
			if(spos == -1)
				break;
			
			spos = response.indexOf(" >", spos);
			epos = response.indexOf('<', spos);
			String nick = response.substring(spos + 2, epos);
			
			spos = response.indexOf("<tr>", epos);
			spos = response.indexOf('>', spos + 4);
			epos = response.indexOf("</td>", spos);
			String notice = response.substring(spos + 1, epos);
			
			spos = response.indexOf("<tr>", epos);
			spos = response.indexOf('>', spos + 4);
			epos = response.indexOf("</tr>", spos);
			String date = response.substring(spos + 1, epos);
			date = date.trim();
			
			LinearLayout lNotice = new LinearLayout(Notice.this);
			LinearLayout.LayoutParams noticeparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			noticeparams.setMargins(0, 5, 0, 0);
			lNotice.setLayoutParams(noticeparams);
			lNotice.setOrientation(LinearLayout.VERTICAL);
			lNotice.setBackgroundResource(R.drawable.topic_shape);
			
			bodyContainer.addView(lNotice);
			
			TextView tNick = new TextView(Notice.this);
			tNick.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tNick.setTextSize(12);
			tNick.setGravity(Gravity.LEFT);
			tNick.setTypeface(null, Typeface.BOLD);
			tNick.setText(nick);
			tNick.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			
			lNotice.addView(tNick);
			
			TextView tNotice = new TextView(Notice.this);
			LinearLayout.LayoutParams textparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			textparams.setMargins(0, 10, 0, 0);
			tNotice.setLayoutParams(textparams);
			tNotice.setTextSize(12);
			tNotice.setGravity(Gravity.LEFT);
			tNotice.setText(Html.fromHtml(notice));
			tNotice.setTextColor(Color.rgb(0xD8, 0xD8, 0xD8));
			tNotice.setMovementMethod(LinkHandler.getInstance());
			tNotice.setClickable(true);
			
			lNotice.addView(tNotice);
			
			TextView tDate = new TextView(Notice.this);
			LinearLayout.LayoutParams dateparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			dateparams.setMargins(0, 5, 0, 0);
			tDate.setLayoutParams(dateparams);
			tDate.setTextSize(10);
			tDate.setGravity(Gravity.RIGHT);
			tDate.setText(date);
			tDate.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			
			lNotice.addView(tDate);
		}
	}
	
	public void goPreviousPage(View v)
	{
		if(iPage == iMaxPages)
			return;
		
		iPage++;
		loadNotices(iPage);
	}
	
	public void goNextPage(View v)
	{	
		if(iPage == 1)
			return;
		
		iPage--;
		loadNotices(iPage);
	}
	
	public void close(View v)
	{
		finish();
	}
}
