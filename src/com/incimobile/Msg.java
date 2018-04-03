package com.incimobile;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Msg extends Activity
{
	private Application Global;
	private LinearLayout msgBody;
	private int iPage = 0, iMaxPages = 0;
	
	private EditText eMsg, eUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg);
		
		Global = this.getApplication();
		msgBody = (LinearLayout) findViewById(R.id.msg_body);
		eMsg = (EditText) findViewById(R.id.msg_text);
		eUser = (EditText) findViewById(R.id.msg_user);
		
		String user = getIntent().getExtras().getString("user");
		if(!user.equals(""))
			eUser.setText(user);
		
		loadMsg(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.msg, menu);
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
	
	public void sendMsg(View v)
	{
		final String sMsg = eMsg.getText().toString();
		final String sUser = eUser.getText().toString();
		
		if(sMsg.length() == 0 || sUser.length() == 0)
		{
			Toast.makeText(getApplicationContext(), "mesaj ve kullanici yazin.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		final String fulladr = "http://inci.sozlukspot.com/index.php?sa=msj&ne=gonder&ne2=yap&kac=";
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpPost req = new HttpPost(fulladr);
				req.addHeader("Referer", "http://inci.sozlukspot.com");
				req.setHeader("Cookie", ((App)Global).m_sPunteriz + ((App)Global).m_sPHPSESSID);
				
				ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("metin", sMsg));
				pairs.add(new BasicNameValuePair("kimlere", sUser));
				pairs.add(new BasicNameValuePair("gonder", "      gönder      "));
				
				try
				{
					req.setEntity(new UrlEncodedFormEntity(pairs,"UTF-8"));
					HttpResponse httpResponse = httpClient.execute(req);
					
					if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					{
						final String response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
						sHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								((App)Global).ShowDialog(Msg.this, "mesaj", "mesaj gönderildi.");
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
								((App)Global).ShowDialog(Msg.this, "Hata", "mesaj gonderilemedi.");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void loadMsg(int page)
	{
		msgBody.removeAllViews();
		
		String url = "http://inci.sozlukspot.com/ss_index.php?sa=msj";
		if(page > 1)
			url += "&p=" + String.valueOf(page);
		
		final String fulladr = url;
		Log.d("load msg", fulladr);		
		
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
								getMsg(response);
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
								((App)Global).ShowDialog(Msg.this, "Hata", "mesajlar alinamadi.");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getMsg(String response)
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
			Log.d("msg pages", maxpages);
			iMaxPages = Integer.parseInt(maxpages);
			iPage = iMaxPages;
			
			spos = 0;
		}
		
		while(true)
		{
			spos = response.indexOf("msj_ozel_tablo_1", spos);
			if(spos == -1)
				break;
			
			spos = response.indexOf(" >", spos);
			epos = response.indexOf('<', spos);
			String nick = response.substring(spos + 2, epos);
			
			spos = response.indexOf("<tr>", epos);
			spos = response.indexOf('>', spos + 4);
			epos = response.indexOf("</td>", spos);
			String msg = response.substring(spos + 1, epos);
			
			spos = response.indexOf(">Sil<", epos);
			spos = response.indexOf("t>", spos);
			epos = response.indexOf('<', spos);
			String date = response.substring(spos + 2, epos);
			date = date.trim();
			
			LinearLayout lMsg = new LinearLayout(Msg.this);
			LinearLayout.LayoutParams msgparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			msgparams.setMargins(0, 5, 0, 0);
			lMsg.setLayoutParams(msgparams);
			lMsg.setOrientation(LinearLayout.VERTICAL);
			lMsg.setBackgroundResource(R.drawable.topic_shape);
			
			msgBody.addView(lMsg);
			
			TextView tNick = new TextView(Msg.this);
			tNick.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tNick.setTextSize(12);
			tNick.setGravity(Gravity.LEFT);
			tNick.setTypeface(null, Typeface.BOLD);
			tNick.setText(nick);
			tNick.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			
			lMsg.addView(tNick);
			
			TextView tMsgText = new TextView(Msg.this);
			LinearLayout.LayoutParams textparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			textparams.setMargins(0, 10, 0, 0);
			tMsgText.setLayoutParams(textparams);
			tMsgText.setTextSize(12);
			tMsgText.setGravity(Gravity.LEFT);
			tMsgText.setText(Html.fromHtml(msg));
			tMsgText.setTextColor(Color.rgb(0xD8, 0xD8, 0xD8));
			tMsgText.setMovementMethod(LinkHandler.getInstance());
			tMsgText.setClickable(true);
			
			lMsg.addView(tMsgText);
			
			LinearLayout lFooter = new LinearLayout(Msg.this);
			LinearLayout.LayoutParams footerparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			footerparams.setMargins(0, 10, 0, 0);
			lFooter.setLayoutParams(footerparams);
			lFooter.setGravity(Gravity.RIGHT);
			lFooter.setOrientation(LinearLayout.HORIZONTAL);
			
			lMsg.addView(lFooter);
			
			TextView tReply = new TextView(Msg.this);
			tReply.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tReply.setTextSize(10);
			tReply.setTextColor(Color.rgb(0xDF, 0x74, 0x01));	//#DF7401
			tReply.setText("cevap   ");
			tReply.setTypeface(null, Typeface.BOLD);
			
			final String touser = nick;
			tReply.setClickable(true);
			tReply.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					eUser.setText(touser);
					eMsg.setText(null);
					
					ScrollView sc = (ScrollView) findViewById(R.id.msg_scroll);
					sc.fullScroll(ScrollView.FOCUS_UP);
				}
			});
			
			lFooter.addView(tReply);
			
			TextView tDate = new TextView(Msg.this);
			tDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tDate.setTextSize(10);
			tDate.setText(date);
			tDate.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			
			lFooter.addView(tDate);
		}
	}
	
	public void goPreviousPage(View v)
	{
		if(iPage == iMaxPages)
			return;
		
		iPage++;
		loadMsg(iPage);
	}
	
	public void goNextPage(View v)
	{	
		if(iPage == 1)
			return;
		
		iPage--;
		loadMsg(iPage);
	}

}
