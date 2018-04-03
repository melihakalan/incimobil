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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Topic extends ActionBarActivity
{
	private Application Global;
	
	private LinearLayout mainContainer, entriesContainer, entryPanel;
	private TextView tTitle;
	private String sTitle;
	ProgressDialog pProgress;
	
	private String sLink;
	private int iMaxPages = 1;
	private int iEntryPage = 1;
	
	private ArrayList<String> listEntries = new ArrayList<String>();
	private ArrayList<String> listEntryIds = new ArrayList<String>();
	private ArrayList<String> listEntryDates = new ArrayList<String>();
	private ArrayList<String> listNicks = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topic);
		
		Global = this.getApplication();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mainContainer = (LinearLayout) findViewById(R.id.topic_container);
		entriesContainer = (LinearLayout) findViewById(R.id.topic_entries);
		entryPanel = (LinearLayout) findViewById(R.id.topic_entrypanel);
		
		if( !((App)Global).m_bLoggedIn )	// giris yapilmamis
			entryPanel.setVisibility(LinearLayout.GONE);
				
		sTitle = getIntent().getExtras().getString("title");
		tTitle = (TextView) findViewById(R.id.topic_title);
		tTitle.setText(sTitle); 
		
		sLink = getIntent().getExtras().getString("link");
		loadEntries(sLink, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.topic, menu);
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
	
	private void loadEntries(String url, int page)
	{
		listEntries.clear();
		listEntryIds.clear();
		listNicks.clear();
		entriesContainer.removeAllViews();
		
		String _url = url;
		if(page > 1)
			_url += String.valueOf(page) + "/";
		
		final String fulladr = _url;
		
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
								getEntries(response);
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
								((App)Global).ShowDialog(Topic.this, "Hata", "entry'ler alinamadi");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getEntries(String response)
	{
		int si = 0, ei;
		
		int spage = response.indexOf("> / <");
		if(spage != -1)
		{
			spage = response.indexOf("9px", spage);
			int epage = response.indexOf('<', spage);
			String pages = response.substring(spage + 6, epage);
			iMaxPages = Integer.parseInt(pages);
			si = spage;
		}
		else
			iMaxPages = 1;
		
		si = response.indexOf("ol style");
		if(si == -1)
			return;
		
		while(true)
		{
			si = response.indexOf("li  id", si);
			if(si == -1)
				break;
			
			si = response.indexOf("li_", si);
			ei = response.indexOf(' ', si);
			String id = response.substring(si + 3, ei - 1);
			
			listEntryIds.add(id);
			Log.d("entry id", id);
			
			si = response.indexOf('>', ei);
			ei = response.indexOf("<div", si);
			String entry = response.substring(si + 1, ei - 4);
			entry = entry.trim();
			
			listEntries.add(entry);
			Log.d("entry", entry);
			
			si = response.indexOf(" >", ei);
			ei = response.indexOf('<', si);
			String nick = response.substring(si + 2, ei);
			
			listNicks.add(nick);
			Log.d("entry nick", nick);
			
			si = response.indexOf(">,",ei);
			ei = response.indexOf(')', si);
			String date = response.substring(si + 3, ei);
			
			listEntryDates.add(date);
			Log.d("entry date", date);
			
			si = ei;
		}
		
		tTitle.setText(sTitle + " - " + String.valueOf(iEntryPage) + "/" + String.valueOf(iMaxPages));
		updateEntryList();
	}
	
	private void updateEntryList()
	{
		if( listEntries.isEmpty() || listEntryIds.isEmpty() || listNicks.isEmpty() || listEntryDates.isEmpty() )
			return;
		
		int size = listEntries.size();
		
		for(int i = 0; i < size; i++)
		{
			TextView tEntry = new TextView(Topic.this);
			LinearLayout.LayoutParams entryparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			entryparams.setMargins(0, 10, 0, 0);
			tEntry.setLayoutParams(entryparams);
			tEntry.setGravity(Gravity.LEFT);
			tEntry.setTextSize(12);
			final String entryno = String.valueOf((iEntryPage - 1) * 25 + (i + 1));
			tEntry.setText( Html.fromHtml(entryno + ". " +  listEntries.get(i)));
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
			
			entriesContainer.addView(tEntry);
			
			LinearLayout infos = new LinearLayout(Topic.this);
			infos.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			infos.setGravity(Gravity.RIGHT);
			
			entriesContainer.addView(infos);
			
			TextView tNick = new TextView(Topic.this);
			tNick.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tNick.setTextSize(12);
			tNick.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			tNick.setTypeface(null, Typeface.BOLD);
			tNick.setText(listNicks.get(i));
			tNick.setLongClickable(true);
			tNick.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					Bundle b = new Bundle();
					b.putString("title", ((TextView)v).getText().toString());
					Intent intent = new Intent(Topic.this, User.class);
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
			
			TextView tDate = new TextView(Topic.this);
			tDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tDate.setTextSize(12);
			tDate.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			tDate.setText(" (" + listEntryDates.get(i) + ") ");
			
			infos.addView(tDate);
			
			LinearLayout idlayout = new LinearLayout(Topic.this);
			idlayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			idlayout.setGravity(Gravity.RIGHT);
			
			entriesContainer.addView(idlayout);
			
			TextView tNum = new TextView(Topic.this);
			tNum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tNum.setTextSize(10);
			tNum.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));
			tNum.setText("#" + listEntryIds.get(i));
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
				final int entryidx = i;
				
				TextView tReply = new TextView(Topic.this);
				tReply.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				tReply.setTextSize(10);
				tReply.setTextColor(Color.rgb(0xDF, 0x74, 0x01));	//#DF7401
				tReply.setText("@   ");
				tReply.setTypeface(null, Typeface.BOLD);
				tReply.setClickable(true);
				tReply.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
						eEntry.setText("@" + entryno + " ");
						eEntry.requestFocus();
						eEntry.setSelection(eEntry.length());
						ScrollView sc = (ScrollView) findViewById(R.id.topic_scroll);
						sc.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				
				TextView tRate = new TextView(Topic.this);
				tRate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				tRate.setTextSize(10);
				tRate.setTextColor(Color.rgb(0xDF, 0x74, 0x01));	//#DF7401
				tRate.setText("oyla   ");
				tRate.setTypeface(null, Typeface.BOLD);
				tRate.setClickable(true);
				tRate.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						rateEntry(listEntryIds.get(entryidx));
					}
				});
				
				TextView tMsg = new TextView(Topic.this);
				tMsg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				tMsg.setTextSize(10);
				tMsg.setTextColor(Color.rgb(0xDF, 0x74, 0x01));	//#DF7401
				tMsg.setText("mesaj   ");
				tMsg.setTypeface(null, Typeface.BOLD);
				tMsg.setClickable(true);
				tMsg.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent intent_msg = new Intent(Topic.this, Msg.class);
						Bundle b = new Bundle();
						b.putString("user", listNicks.get(entryidx));
						intent_msg.putExtras(b);
						startActivity(intent_msg);
					}
				});
				
				idlayout.addView(tReply);
				idlayout.addView(tRate);
				idlayout.addView(tMsg);
				
				if(listNicks.get(entryidx).equals( ((App)Global).m_sID ))
				{
					TextView tEdit = new TextView(Topic.this);
					tEdit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
					tEdit.setTextSize(10);
					tEdit.setTextColor(Color.rgb(0xA4, 0xA4, 0xA4));	//#DF7401
					tEdit.setText("duzelt   ");
					tEdit.setTypeface(null, Typeface.BOLD);
					tEdit.setClickable(true);
					tEdit.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Intent intent_edit = new Intent(Topic.this, Edit.class);
							Bundle b = new Bundle();
							b.putString("link", sLink);
							b.putString("id", listEntryIds.get(entryidx));
							b.putString("entry", listEntries.get(entryidx));
							intent_edit.putExtras(b);
							startActivity(intent_edit);
							finish();
						}
					});
					
					TextView tDelete = new TextView(Topic.this);
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
							AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(Topic.this);                      
						    dlgAlert.setTitle(null); 
						    dlgAlert.setMessage("entry silinsin mi?"); 
						    
						    dlgAlert.setPositiveButton("evet",new DialogInterface.OnClickListener()
						    {
						        public void onClick(DialogInterface dialog, int whichButton)
						        {	        	
						        	deleteEntry(listEntryIds.get(entryidx));
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
								Toast.makeText(getApplicationContext(), "entry silindi!", Toast.LENGTH_SHORT).show();
								iEntryPage = 1;
								tTitle.setText(sTitle);
								loadEntries(sLink, 0);
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
								((App)Global).ShowDialog(Topic.this, "Hata", "entry silinemedi.");
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
								((App)Global).ShowDialog(Topic.this, "Hata", "oy verilemedi.");
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
		
		iEntryPage = 1;
		sTitle = nick;
		tTitle.setText(nick);
		loadEntries(fulladr, 0);
	}
	
	public void goFirstPage(View v)
	{
		if(iEntryPage == 1)
			return;
		
		iEntryPage = 1;
		tTitle.setText(sTitle + " - " + String.valueOf(iEntryPage) + "/" + String.valueOf(iMaxPages));
		loadEntries(sLink, iEntryPage);
	}
	
	public void goPreviousPage(View v)
	{
		if(iEntryPage == 1)
			return;
		
		iEntryPage--;
		tTitle.setText(sTitle + " - " + String.valueOf(iEntryPage) + "/" + String.valueOf(iMaxPages));
		loadEntries(sLink, iEntryPage);
	}
	
	public void goNextPage(View v)
	{
		if(iEntryPage == iMaxPages)
			return;
		
		iEntryPage++;
		tTitle.setText(sTitle + " - " + String.valueOf(iEntryPage) + "/" + String.valueOf(iMaxPages));
		loadEntries(sLink, iEntryPage);
	}
	
	public void goLastPage(View v)
	{
		if(iEntryPage == iMaxPages)
			return;
		
		iEntryPage = iMaxPages;
		tTitle.setText(sTitle + " - " + String.valueOf(iEntryPage) + "/" + String.valueOf(iMaxPages));
		loadEntries(sLink, iEntryPage);
	}
	
	public void addEntry(View v)
	{
		EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
		if(eEntry.getText().length() < 7)
		{
			Toast.makeText(getApplicationContext(), "entry cok kisa.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		sendEntry( eEntry.getText().toString() );
	}
	
	private void sendEntry(final String entry)
	{
		pProgress = new ProgressDialog(Topic.this);
		pProgress.setMessage("entry giriliyor...");
		pProgress.show();
		
		final String fulladr = "http://inci.sozlukspot.com/soz_uye_kelime.php?sa=yeni&ne=yap";
		
		String title = sTitle;
		int etitle = sTitle.indexOf('(');
		
		if(etitle != -1)
			title = sTitle.substring(0, etitle - 1);
		
		final String fulltitle = title;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpPost req = new HttpPost(fulladr);
				req.addHeader("Referer", sLink);
				req.setHeader("Cookie", ((App)Global).m_sPunteriz + ((App)Global).m_sPHPSESSID);
				
				ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("entry", entry));
				pairs.add(new BasicNameValuePair("baslik", fulltitle));
				pairs.add(new BasicNameValuePair("kac", String.valueOf(iMaxPages)));
				pairs.add(new BasicNameValuePair("isimsiz", "1286897213"));
				
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
								getSendResponse(response);
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
								pProgress.dismiss();
								((App)Global).ShowDialog(Topic.this, "Hata", "cevap hatali");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getSendResponse(String response)
	{
		pProgress.dismiss();
		
		if( response.contains("entry girdin") )
		{
			iEntryPage = iMaxPages;
			tTitle.setText(sTitle);
			loadEntries(sLink + iMaxPages + "/", 0);
			Log.d("entry girdin", "=1");
			
			EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
			eEntry.setText(null);
		}
		else
		{
			Toast.makeText(getApplicationContext(), "entry girilemedi, tekrar yollayin.", Toast.LENGTH_SHORT).show();
			Log.d("entry girdin", "=0");
		}
	}
	
	public void addBkz(View v)
	{
		EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
		if(eEntry.getText().toString().length() == 0)
		{
			eEntry.setText("(bkz: )");
			eEntry.setSelection(6);
		}
		else
		{
			String entry = eEntry.getText().toString();
			int ssel = eEntry.getSelectionStart();
			int esel = eEntry.getSelectionEnd();
			
			if(ssel == -1 || ssel == esel)
			{
				int plen = entry.length();
				eEntry.append("(bkz: )");
				eEntry.setSelection(plen + 6);
			}
			else
			{
				String sel = entry.substring(ssel, esel);
				String insert = "(bkz: " + sel + ")";
				String newentry = entry.substring(0, ssel) + insert + entry.substring(esel);
				eEntry.setText(newentry);
				eEntry.setSelection(eEntry.length());
			}
		}
	}
	
	public void addGorunmezBkz(View v)
	{
		EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
		if(eEntry.getText().toString().length() == 0)
		{
			eEntry.setText("``");
			eEntry.setSelection(1);
		}
		else
		{
			String entry = eEntry.getText().toString();
			int ssel = eEntry.getSelectionStart();
			int esel = eEntry.getSelectionEnd();
			
			if(ssel == -1 || ssel == esel)
			{
				int plen = entry.length();
				eEntry.append("``");
				eEntry.setSelection(plen + 1);
			}
			else
			{
				String sel = entry.substring(ssel, esel);
				String insert = "`" + sel + "`";
				String newentry = entry.substring(0, ssel) + insert + entry.substring(esel);
				eEntry.setText(newentry);
				eEntry.setSelection(eEntry.length());
			}
		}
	}
	
	public void addSpoiler(View v)
	{
		EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
		if(eEntry.getText().toString().length() == 0)
		{
			eEntry.setText("--`spoiler`--\n\n--`spoiler`--");
			eEntry.setSelection(14);
		}
		else
		{
			String entry = eEntry.getText().toString();
			int ssel = eEntry.getSelectionStart();
			int esel = eEntry.getSelectionEnd();
			
			if(ssel == -1 || ssel == esel)
			{
				int plen = entry.length();
				eEntry.append("\n--`spoiler`--\n\n--`spoiler`--");
				eEntry.setSelection(plen + 14);
			}
			else
			{
				String sel = entry.substring(ssel, esel);
				String insert = "\n--`spoiler`--\n" + sel + "\n--`spoiler`--\n";
				String newentry = entry.substring(0, ssel) + insert + entry.substring(esel);
				eEntry.setText(newentry);
				eEntry.setSelection(eEntry.length());
			}
		}
	}
	
	public void addYildizBkz(View v)
	{
		EditText eEntry = (EditText) findViewById(R.id.topic_newentry);
		if(eEntry.getText().toString().length() == 0)
		{
			eEntry.setText("~~");
			eEntry.setSelection(1);
		}
		else
		{
			String entry = eEntry.getText().toString();
			int ssel = eEntry.getSelectionStart();
			int esel = eEntry.getSelectionEnd();
			
			if(ssel == -1 || ssel == esel)
			{
				int plen = entry.length();
				eEntry.append("~~");
				eEntry.setSelection(plen + 1);
			}
			else
			{
				String sel = entry.substring(ssel, esel);
				String insert = "~" + sel + "~";
				String newentry = entry.substring(0, ssel) + insert + entry.substring(esel);
				eEntry.setText(newentry);
				eEntry.setSelection(eEntry.length());
			}
		}
	}
}
