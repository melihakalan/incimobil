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

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.startapp.android.publish.StartAppAd;

public class Home extends ActionBarActivity
{	
	private Application Global;
	
	private StartAppAd startAppAd = new StartAppAd(this);

	private LinearLayout mainContainer, topicsContainer;
	private int iTopicPage = 1;
	private int iMaxPages = 1;
	private String sCurrentType = "bugun";
	
	private ArrayList<String> listTopics = new ArrayList<String>();
	private ArrayList<String> listTopicLinks = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		Global = this.getApplication();
		
		mainContainer = (LinearLayout) findViewById(R.id.home_container);
		topicsContainer = (LinearLayout) findViewById(R.id.home_topics);
		
		loadTopics("bugun", 0);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		startAppAd.onBackPressed();
		super.onBackPressed();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		startAppAd.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		startAppAd.onPause();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_me:
			menuMe();
			return true;
		case R.id.menu_msg:
			menuMsg();
			return true;
		case R.id.menu_notice:
			menuNotice();
			return true;
		case R.id.menu_out:
			menuOut();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private static Handler sHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
		}
	};

	private void menuMe()
	{
		if( !((App)Global).m_bLoggedIn )
		{
			((App)Global).ShowDialog(this, "hata", "giris yapmalisin.");
			return;
		}
		
		Intent intent = new Intent(Home.this, User.class);
		Bundle b = new Bundle();
		b.putString("title", ((App)Global).m_sID);
		intent.putExtras(b);
		startActivity(intent);
	}
	
	private void menuMsg()
	{
		if( !((App)Global).m_bLoggedIn )
		{
			((App)Global).ShowDialog(this, "hata", "giris yapmalisin.");
			return;
		}
		
		Intent intent = new Intent(Home.this, Msg.class);
		Bundle b = new Bundle();
		b.putString("user", "");
		intent.putExtras(b);
		startActivity(intent);
	}
	
	private void menuNotice()
	{
		if( !((App)Global).m_bLoggedIn )
		{
			((App)Global).ShowDialog(this, "hata", "giris yapmalisin.");
			return;
		}
		
		Intent intent = new Intent(Home.this, Notice.class);
		startActivity(intent);
	}
	
	private void menuOut()
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
	    dlgAlert.setTitle(null); 
	    dlgAlert.setMessage("çýkýþ yapýlýyor?"); 
	    
	    dlgAlert.setPositiveButton("evet",new DialogInterface.OnClickListener()
	    {
	        public void onClick(DialogInterface dialog, int whichButton)
	        {	        	
	    		SharedPreferences fSave = getApplicationContext().getSharedPreferences("incimobile.cfg",0);
	    		SharedPreferences.Editor fEditor = fSave.edit();
	    		fEditor.clear().commit();
	    		
	    		((App)Global).init();
	    		
	    		Intent i = new Intent(Home.this, Login.class);
	    		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    		startActivity(i);
	    		
	    		finish();
	        }
	   });
	    
	    dlgAlert.setNegativeButton("hayýr", new DialogInterface.OnClickListener()
	    {
	        public void onClick(DialogInterface dialog, int whichButton)
	        {
	        	dialog.cancel();
	        }
	   });
	    
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
	}
	
	public void goToday(View v)
	{
		iTopicPage = 1;
		loadTopics("bugun", 0);
	}
	
	public void goPopular(View v)
	{
		iTopicPage = 1;
		loadTopics("populer",0);
	}
	
	public void goSukela(View v)
	{
		Intent intent = new Intent(Home.this, Sukela.class);
		startActivity(intent);
	}
	
	private void loadTopics(String type, int page)
	{
		listTopics.clear();
		listTopicLinks.clear();
		topicsContainer.removeAllViews();
		
		sCurrentType = type;
		
		String url = "http://inci.sozlukspot.com/ss_leftframe.php?sa=" + type;
		if(page > 1)
			url += "&p=" + String.valueOf(page);
		
		final String fulladr = url;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				
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
								getTopics(response);
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
								((App)Global).ShowDialog(Home.this, "Hata", "basliklar alinamadi");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getTopics(String response)
	{
		int stopics = response.indexOf("sol_liste");
		if(stopics == -1)
			return;
		
		int spage = response.indexOf("ss_leftframe.php");
		spage = response.indexOf("p=", spage);
		int epage = response.indexOf(' ', spage);
		String pages = response.substring(spage + 2, epage - 1);
		iMaxPages = Integer.parseInt(pages);
		
		int si = stopics;
		int ei;
		String link, name, entry;
		
		while(true)
		{
			si = response.indexOf("sol_list_div", si);
			if(si == -1)
				break;
			si = response.indexOf("http", si);
			ei = response.indexOf("target", si);
			link = response.substring(si, ei - 2);
			
			listTopicLinks.add(link);
			Log.d("topic link", link);
			
			//entry sayisi
			si = response.indexOf('(', ei) + 1;
			ei = response.indexOf(')', si);
			entry = response.substring(si, ei);
			
			//baslik adi
			si = response.indexOf('>', ei) + 1;
			ei = response.indexOf('<', si);
			name = response.substring(si, ei);
			
			if(!entry.equals("1"))
				name += " (" + entry + ")";
			
			listTopics.add(name);
			Log.d("topic",name);
			
			si = ei;
		}
		
		updateTopicList();
	}
	
	private void updateTopicList()
	{
		if( listTopics.isEmpty() || listTopicLinks.isEmpty() )
			return;
		
		int size = listTopics.size();
		
		for(int i = 0; i < size; i++)
		{
			TextView tTopic = new TextView(Home.this);
			LinearLayout.LayoutParams topicparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			topicparams.setMargins(0, 5, 0, 0);
			tTopic.setLayoutParams(topicparams);
			tTopic.setGravity(Gravity.LEFT);
			tTopic.setTextSize(12);
			tTopic.setTextColor(Color.rgb(0xD8, 0xD8, 0xD8));
			tTopic.setTypeface(null, Typeface.BOLD);
			tTopic.setText(listTopics.get(i));
			tTopic.setBackgroundResource(R.drawable.topic_shape);
			tTopic.setClickable(true);
			
			final int idx = i;
			tTopic.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{	
					Intent intent = new Intent(Home.this, Topic.class);
					Bundle b = new Bundle();
					b.putString("link", listTopicLinks.get(idx));
					b.putString("title", listTopics.get(idx));
					intent.putExtras(b);
					startActivity(intent);
				}
			});
			
			topicsContainer.addView(tTopic);
		}
	}
	
	public void goFirstPage(View v)
	{
		if(iTopicPage == 1)
			return;
		
		iTopicPage = 1;
		loadTopics(sCurrentType, 1);
	}
	
	public void goPreviousPage(View v)
	{
		if(iTopicPage == 1)
			return;
		
		iTopicPage--;
		loadTopics(sCurrentType, iTopicPage);
	}
	
	public void goNextPage(View v)
	{
		if(iTopicPage == iMaxPages)
			return;
		
		iTopicPage++;
		loadTopics(sCurrentType, iTopicPage);
	}
	
	public void goLastPage(View v)
	{
		if(iTopicPage == iMaxPages)
			return;
		
		iTopicPage = iMaxPages;
		loadTopics(sCurrentType, iTopicPage);
	}
	
	public void doSearch(View v)
	{
		EditText tSearch = (EditText) findViewById(R.id.home_search);
		if(tSearch.getText().length() == 0)
			return;
		
		String title = tSearch.getText().toString();
		String link = "http://inci.sozlukspot.com/ss_entry.php?k=" + encodeParam(title);
		
		Intent intent = new Intent(Home.this, Topic.class);
		Bundle b = new Bundle();
		b.putString("link", link);
		b.putString("title", title);
		intent.putExtras(b);
		startActivity(intent);
	}
	
	private String encodeParam(String param)
	{
		// utf-8:	basliklar turkce karakterler icerebilir
		String ret = null;
		try{ ret = URLEncoder.encode(param, "UTF-8"); } catch(Exception e){}
		return ret;
	}

}
