package com.incimobile;

import java.net.URLEncoder;
import java.util.ArrayList;

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
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class User extends Activity
{
	private Application Global;
	
	private TextView tTitle;
	private String sTitle, sLink;
	private LinearLayout bodyContainer, panelGeneral, panelLastSuku, panelLastCuku, panelMostSuku, panelLast, panelFirst;
	private ArrayList<String> sLinkList1 = new ArrayList<String>();
	private ArrayList<String> sLinkList2 = new ArrayList<String>();
	private ArrayList<String> sLinkList3 = new ArrayList<String>();
	private ArrayList<String> sLinkList4 = new ArrayList<String>();
	private ArrayList<String> sLinkList5 = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		
		Global = this.getApplication();
		
		bodyContainer = (LinearLayout) findViewById(R.id.user_container);
		panelGeneral = (LinearLayout) findViewById(R.id.user_general_panel);
		panelLastSuku = (LinearLayout) findViewById(R.id.user_lastsuku_panel);
		panelLastCuku = (LinearLayout) findViewById(R.id.user_lastcuku_panel);
		panelMostSuku = (LinearLayout) findViewById(R.id.user_mostsuku_panel);
		panelLast = (LinearLayout) findViewById(R.id.user_last_panel);
		panelFirst = (LinearLayout) findViewById(R.id.user_first_panel);
		
		sTitle = getIntent().getExtras().getString("title");
		tTitle = (TextView) findViewById(R.id.user_title);
		tTitle.setText(Html.fromHtml("<b>" + sTitle + "</b>")); 
		
		loadGeneral();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.user, menu);
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		finish();	//	clear when go back.
	}
	
	private static Handler sHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
		}
	};
	
	private void loadGeneral()
	{	
		final String fulladr = "http://inci.sozlukspot.com/ss_index.php?sa=yzr&y=" + encodeNick(sTitle);
		sLink = fulladr;
		Log.d("load user", fulladr);
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				req.setHeader("Cookie", ((App)Global).m_sPunteriz + ((App)Global).m_sPHPSESSID);
				req.addHeader("Referer", "http://inci.sozlukspot.com");
				
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
								getGeneral(response);
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
								((App)Global).ShowDialog(User.this, "Hata", "bilgiler alinamadi");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getGeneral(String response)
	{
		int spos = response.indexOf("</b>");
		if(spos == -1)
		{
			Log.d("user error", response);
			return;
		}
		
		Log.d("user get", sTitle);
		
		spos += 8;
		int epos = response.indexOf("<br>", spos);
		String nesil = response.substring(spos, epos - 6);
		
		tTitle.append("\n" + nesil);
		
		spos = response.indexOf("color=", epos);
		spos = response.indexOf('>', spos);
		epos = response.indexOf('<', spos);
		String status = response.substring(spos + 1, epos);
		
		String generaldata[] = new String[4];
		spos = response.indexOf("yzrinfo_table_ic", epos);
		spos = response.indexOf("nbsp;", spos);
		epos = response.indexOf('<', spos);
		generaldata[0] = response.substring(spos + 5, epos);
		
		spos = response.indexOf("nbsp;", epos);
		epos = response.indexOf('<', spos);
		generaldata[1] = response.substring(spos + 5, epos);
		
		spos = response.indexOf("nbsp;", epos);
		epos = response.indexOf('<', spos);
		generaldata[2] = response.substring(spos + 5, epos);
		
		spos = response.indexOf("nbsp;", epos);
		epos = response.indexOf('<', spos);
		generaldata[3] = response.substring(spos + 5, epos);
		
		TextView tStatus = new TextView(User.this);
		LinearLayout.LayoutParams statusparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		statusparams.setMargins(0, 10, 0, 0);
		tStatus.setLayoutParams(statusparams);
		tStatus.setGravity(Gravity.RIGHT);
		tStatus.setTextSize(12);
		tStatus.setText(status);
		if(status.equals("online"))
			tStatus.setTextColor(Color.rgb(0x31, 0xB4, 0x04));	//#31B404
		else
			tStatus.setTextColor(Color.rgb(0xB4, 0x04, 0x04));	//#B40404
		panelGeneral.addView(tStatus);
		
		TextView tData = new TextView(User.this);
		LinearLayout.LayoutParams dataparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		dataparams.setMargins(5, 0, 0, 0);
		tData.setLayoutParams(dataparams);
		tData.setGravity(Gravity.LEFT);
		tData.setTextSize(12);
		tData.setText("bugün:\t" + generaldata[0] + "\nbu hafta:\t" + generaldata[1] + "\ntoplam entry:\t" + generaldata[2] + "\ntoplam baþlýk:\t" + generaldata[3]);
		tData.setTextColor(Color.rgb(0xBD, 0xBD, 0xBD));
		panelGeneral.addView(tData);
		
		getAllData(response);
	}
	
	private void getAllData(String response)
	{
		int spos = response.indexOf("uid=");
		int epos = response.indexOf('&', spos);
		String uid = response.substring(spos + 4, epos);
		
		spos = response.indexOf("sif=");
		epos = response.indexOf('&', spos);
		String sif = response.substring(spos + 4, epos);
		
		for(int i = 1; i <= 5; i++)
			loadData(i, uid, sif);
	}
	
	private void loadData(final int i, String param1, String param2)
	{
		String param = null;
		switch(i)
		{
		case 1:	param = "sonbeg"; break;
		case 2: param = "sonkot"; break;
		case 3: param = "enbeg"; break;
		case 4: param = "son"; break;
		case 5: param = "ilk"; break;
		}
		
		final String fulladr = "http://inci.sozlukspot.com/soz_uye_yazarinfo_xml.php?uid=" + param1 + "&sif=" + param2 + "&ne=" + param;
		Log.d("load data", fulladr);
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				req.addHeader("Referer", sLink);
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
								getData(i, response);
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
								((App)Global).ShowDialog(User.this, "Hata", "bilgiler alinamadi");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getData(final int i, String response)
	{
		int spos = response.indexOf("href");
		int epos;
		String link, title;
		int idx = 0;
		
		while(spos != -1)
		{
			epos = response.indexOf(' ', spos);
			link = response.substring(spos + 6, epos - 1);
			
			spos = response.indexOf('>', epos);
			epos = response.indexOf('<', spos);
			title = response.substring(spos + 1, epos);
			
			TextView tData = new TextView(User.this);
			LinearLayout.LayoutParams dataparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			dataparams.setMargins(5, 0, 0, 0);
			tData.setLayoutParams(dataparams);
			tData.setGravity(Gravity.LEFT);
			tData.setTextSize(12);
			tData.setText(Html.fromHtml("<b>" + title + "</b>"));
			tData.setTextColor(Color.rgb(0xBD, 0xBD, 0xBD));
			tData.setClickable(true);
			
			final int cidx = idx;
			tData.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(User.this, Entry.class);
					Bundle b = new Bundle();
					switch(i)
					{
					case 1:
						b.putString("link", sLinkList1.get(cidx));
						break;
					case 2:
						b.putString("link", sLinkList2.get(cidx));
						break;
					case 3:
						b.putString("link", sLinkList3.get(cidx));
						break;
					case 4:
						b.putString("link", sLinkList4.get(cidx));
						break;
					case 5:
						b.putString("link", sLinkList5.get(cidx));
						break;
					}
					intent.putExtras(b);
					startActivity(intent);
					finish();
				}
			});
			
			switch(i)
			{
			case 1:	sLinkList1.add(link); panelLastSuku.addView(tData); break;
			case 2:	sLinkList2.add(link); panelLastCuku.addView(tData); break;
			case 3:	sLinkList3.add(link); panelMostSuku.addView(tData); break;
			case 4:	sLinkList4.add(link); panelLast.addView(tData); break;
			case 5:	sLinkList5.add(link); panelFirst.addView(tData); break;
			}
			
			spos = response.indexOf("href", epos);
			idx++;
		}
		
	}
	
	public void toggleGeneral(View v)
	{
		if(panelGeneral.getVisibility() == LinearLayout.VISIBLE)
			panelGeneral.setVisibility(LinearLayout.GONE);
		else
			panelGeneral.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void toggleLastSuku(View v)
	{
		if(panelLastSuku.getVisibility() == LinearLayout.VISIBLE)
			panelLastSuku.setVisibility(LinearLayout.GONE);
		else
			panelLastSuku.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void toggleLastCuku(View v)
	{
		if(panelLastCuku.getVisibility() == LinearLayout.VISIBLE)
			panelLastCuku.setVisibility(LinearLayout.GONE);
		else
			panelLastCuku.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void toggleMostSuku(View v)
	{
		if(panelMostSuku.getVisibility() == LinearLayout.VISIBLE)
			panelMostSuku.setVisibility(LinearLayout.GONE);
		else
			panelMostSuku.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void toggleLast(View v)
	{
		if(panelLast.getVisibility() == LinearLayout.VISIBLE)
			panelLast.setVisibility(LinearLayout.GONE);
		else
			panelLast.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void toggleFirst(View v)
	{
		if(panelFirst.getVisibility() == LinearLayout.VISIBLE)
			panelFirst.setVisibility(LinearLayout.GONE);
		else
			panelFirst.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void clickTitle(View v)
	{
		goUserTopic(sTitle);
	}
	
	private void goUserTopic(String nick)
	{
		final String encoded = nick.replace(' ', '-');
		final String fulladr = "http://inci.sozlukspot.com/w/" + encoded + "/";
		
		Intent intent = new Intent(User.this, Topic.class);
		Bundle b = new Bundle();
		b.putString("link", fulladr);
		b.putString("title", nick);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
	
	private String encodeNick(String param)
	{
		//	iso:	nickler standart
		String ret = null;
		try{ ret = URLEncoder.encode(param, "ISO-8859-1"); } catch(Exception e){}
		return ret;
	}
	
	public void close(View v)
	{
		finish();
	}

}
