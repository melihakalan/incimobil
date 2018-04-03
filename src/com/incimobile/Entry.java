package com.incimobile;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Entry extends Activity
{
	private Application Global;
	
	private LinearLayout bodyContainer;
	private TextView tTopic;
	private String sTitle, sTopicLink, sEntryId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
		
		Global = this.getApplication();
		
		bodyContainer = (LinearLayout) findViewById(R.id.entry_body);
		tTopic = (TextView) findViewById(R.id.entry_topic);
		
		String elink = getIntent().getExtras().getString("link");
		loadEntry(elink);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.entry, menu);
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
	
	private void loadEntry(String link)
	{
		bodyContainer.removeAllViews();
		sTitle = null;
		sTopicLink = null;
		tTopic.setText(null);
		
		final String fulladr = link;
		
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
								getEntry(response);
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
								((App)Global).ShowDialog(Entry.this, "Hata", "entry alinamadi");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();		
	}
	
	private void getEntry(String response)
	{
		int si = 0, ei;
		
		si = response.indexOf("<body");
		si = response.indexOf("<title", si);
		si = response.indexOf('>', si);
		ei = response.indexOf('<', si);
		sTitle = response.substring(si + 1, ei);
		tTopic.setText(sTitle);
		
		Log.d("entry title", sTitle);
		
		si = response.indexOf("ol style");
		if(si == -1)
			return;
		si = response.indexOf("li  id", si);
		si = response.indexOf("li_", si);
		ei = response.indexOf(' ', si);
		sEntryId = response.substring(si + 3, ei - 1);
		
		Log.d("entry id", sEntryId);
		
		si = response.indexOf("et=", ei);
		ei = response.indexOf(' ', si);
		String num = response.substring(si + 4, ei - 1);
		
		Log.d("entry num", num);
		
		si = response.indexOf('>', ei);
		ei = response.indexOf("<div", si);
		String entry = response.substring(si + 1, ei - 4);
		entry = entry.trim();
		
		Log.d("entry entry", entry);
		
		si = response.indexOf(" >", ei);
		ei = response.indexOf('<', si);
		String nick = response.substring(si + 2, ei);
		
		Log.d("entry nick", nick);
		
		si = response.indexOf(">,",ei);
		ei = response.indexOf(')', si);
		String date = response.substring(si + 3, ei);
		
		Log.d("entry date", date);
		
		si = response.indexOf("entry_td", ei);
		si = response.indexOf("http", si);
		ei = response.indexOf("'", si);
		sTopicLink = response.substring(si, ei);
		if(sTopicLink.contains("&p"))
		{
			ei = sTopicLink.indexOf("&p");
			sTopicLink = sTopicLink.substring(0, ei);
		}
		
		Log.d("entry topic", sTopicLink);
		
		TextView tEntry = new TextView(Entry.this);
		LinearLayout.LayoutParams entryparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		entryparams.setMargins(0, 10, 0, 0);
		tEntry.setLayoutParams(entryparams);
		tEntry.setGravity(Gravity.LEFT);
		tEntry.setTextSize(12);
		tEntry.setText(Html.fromHtml(String.valueOf(num) + ". " +  entry));
		tEntry.setTextColor(Color.rgb(0xBD, 0xBD, 0xBD));
		tEntry.setMovementMethod(LinkHandler.getInstance());
		tEntry.setClickable(true);
		tEntry.setLongClickable(true);
		tEntry.setOnLongClickListener(new OnLongClickListener()
		{
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public boolean onLongClick(View v)
			{
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				{
				    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				    clipboard.setText(((TextView)v).getText().toString());
				}
				else
				{
				    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				    android.content.ClipData clip = android.content.ClipData.newPlainText("entry",((TextView)v).getText().toString());
				    clipboard.setPrimaryClip(clip);
				}

				Toast.makeText(getApplicationContext(), "entry kopyalandi!", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		bodyContainer.addView(tEntry);
		
		LinearLayout infos = new LinearLayout(Entry.this);
		infos.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		infos.setGravity(Gravity.RIGHT);
		
		bodyContainer.addView(infos);
		
		TextView tNick = new TextView(Entry.this);
		tNick.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		tNick.setTextSize(12);
		tNick.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
		tNick.setTypeface(null, Typeface.BOLD);
		tNick.setText(nick);
		tNick.setLongClickable(true);
		tNick.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				Bundle b = new Bundle();
				b.putString("title", ((TextView)v).getText().toString());
				Intent intent = new Intent(Entry.this, User.class);
				intent.putExtras(b);
				startActivity(intent);
				finish();
				return true;
			}
		});
		tNick.setClickable(true);
		tNick.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				goUserTopic(((TextView)v).getText().toString());
			}
		});
		
		infos.addView(tNick);
		
		TextView tDate = new TextView(Entry.this);
		tDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		tDate.setTextSize(12);
		tDate.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
		tDate.setText(" - " + date);
		
		infos.addView(tDate);
		
		LinearLayout idlayout = new LinearLayout(Entry.this);
		idlayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		idlayout.setGravity(Gravity.RIGHT);
		
		bodyContainer.addView(idlayout);
		
		TextView tNum = new TextView(Entry.this);
		tNum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		tNum.setTextSize(10);
		tNum.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
		tNum.setText("#" + sEntryId);
		tNum.setLongClickable(true);
		tNum.setOnLongClickListener(new OnLongClickListener()
		{
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public boolean onLongClick(View v)
			{
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				{
				    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				    clipboard.setText(((TextView)v).getText().toString());
				}
				else
				{
				    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				    android.content.ClipData clip = android.content.ClipData.newPlainText("num",((TextView)v).getText().toString());
				    clipboard.setPrimaryClip(clip);
				}

				Toast.makeText(getApplicationContext(), "numara kopyalandi!", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		if( ((App)Global).m_bLoggedIn )
		{
			TextView tRate = new TextView(Entry.this);
			tRate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tRate.setTextSize(10);
			tRate.setTextColor(Color.rgb(0xDF, 0x74, 0x01));	//#DF7401
			tRate.setText("oyla   ");
			tRate.setTypeface(null, Typeface.BOLD);
			
			tRate.setClickable(true);
			final String entry_id = sEntryId;
			tRate.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					rateEntry(entry_id);
				}
			});
			
			TextView tMsg = new TextView(Entry.this);
			tMsg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tMsg.setTextSize(10);
			tMsg.setTextColor(Color.rgb(0xDF, 0x74, 0x01));	//#DF7401
			tMsg.setText("mesaj   ");
			tMsg.setTypeface(null, Typeface.BOLD);
			
			tMsg.setClickable(true);
			final String entry_user = nick;
			tMsg.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent_msg = new Intent(Entry.this, Msg.class);
					Bundle b = new Bundle();
					b.putString("user", entry_user);
					intent_msg.putExtras(b);
					startActivity(intent_msg);
				}
			});
			
			idlayout.addView(tRate);
			idlayout.addView(tMsg);
			
			if(entry_user.equals( ((App)Global).m_sID ))
			{
				TextView tEdit = new TextView(Entry.this);
				tEdit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				tEdit.setTextSize(10);
				tEdit.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));	//#DF7401
				tEdit.setText("duzelt   ");
				tEdit.setTypeface(null, Typeface.BOLD);
				
				tEdit.setClickable(true);
				final String fentry = entry;
				tEdit.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent intent_edit = new Intent(Entry.this, Edit.class);
						Bundle b = new Bundle();
						b.putString("link", sTopicLink);
						b.putString("id", entry_id);
						b.putString("entry", fentry);
						intent_edit.putExtras(b);
						startActivity(intent_edit); 
						finish();
					}
				});
				
				TextView tDelete = new TextView(Entry.this);
				tDelete.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				tDelete.setTextSize(10);
				tDelete.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));	//#DF7401
				tDelete.setText("sil   ");
				tDelete.setTypeface(null, Typeface.BOLD);
				
				tDelete.setClickable(true);
				tDelete.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(Entry.this);                      
					    dlgAlert.setTitle(null); 
					    dlgAlert.setMessage("entry silinsin mi?"); 
					    
					    dlgAlert.setPositiveButton("evet",new DialogInterface.OnClickListener()
					    {
					        public void onClick(DialogInterface dialog, int whichButton)
					        {	        	
					        	deleteEntry(entry_id);
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
				});
				
				idlayout.addView(tEdit);
				idlayout.addView(tDelete);
			}
		}
		idlayout.addView(tNum);
	}
	
	private void deleteEntry(final String entryid)
	{
		final String fulladr = "http://inci.sozlukspot.com/soz_uye_kelime.php?sa=sil&id=" + entryid;
		Log.d("entry deleted", fulladr);		
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				req.addHeader("Referer", sTopicLink);
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
								Toast.makeText(getApplicationContext(), "entry silindi!", Toast.LENGTH_SHORT).show();
								finish();
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
								((App)Global).ShowDialog(Entry.this, "Hata", "entry silinemedi.");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void rateEntry(final String entryid)
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
	    dlgAlert.setTitle(null); 
	    dlgAlert.setMessage("oy ver"); 
	    
	    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
	    {
		    dlgAlert.setPositiveButton(":)",new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int whichButton)
		        {
		        	sendRate(entryid, 1);
		        }
		   });
		    
		    dlgAlert.setNeutralButton(":o",new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int whichButton)
		        {
		        	sendRate(entryid, 0);
		        }
		   });
		    
		    dlgAlert.setNegativeButton(":(", new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int whichButton)
		        {
		        	sendRate(entryid, -1);
		        }
		   });
	    }
	    else
	    {
		    dlgAlert.setPositiveButton(":(",new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int whichButton)
		        {
		        	sendRate(entryid, -1);
		        }
		   });
		    
		    dlgAlert.setNeutralButton(":o",new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int whichButton)
		        {
		        	sendRate(entryid, 0);
		        }
		   });
		    
		    dlgAlert.setNegativeButton(":)", new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int whichButton)
		        {
		        	sendRate(entryid, 1);
		        }
		   });	
	    }
	    
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
	}
	
	private void sendRate(final String entryid, final int val)
	{
		final String fulladr = "http://inci.sozlukspot.com/soz_uye_oylamax.php?id=" + entryid + "&pu=" + String.valueOf(val) + "&ne=yap";
		Log.d("entry rated", fulladr);		
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				req.addHeader("Referer", sTopicLink);
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
								Toast.makeText(getApplicationContext(), "entry oylandi!", Toast.LENGTH_SHORT).show();
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
								((App)Global).ShowDialog(Entry.this, "Hata", "oy verilemedi.");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void goUserTopic(String nick)
	{
		final String encoded = nick.replace(' ', '-');
		final String fulladr = "http://inci.sozlukspot.com/w/" + encoded + "/";
		
		Intent intent = new Intent(Entry.this, Topic.class);
		Bundle b = new Bundle();
		b.putString("link", fulladr);
		b.putString("title", nick);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
	
	public void goTopic(View v)
	{
		if(sTopicLink == null)
			return;
		
		Intent intent = new Intent(Entry.this, Topic.class);
		Bundle b = new Bundle();
		b.putString("link", sTopicLink);
		b.putString("title", sTitle);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

}
