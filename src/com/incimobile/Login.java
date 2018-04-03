package com.incimobile;

import java.util.ArrayList;

import org.apache.http.Header;
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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;

public class Login extends Activity
{
	private StartAppAd startAppAd = new StartAppAd(this);
	
	private Application Global;
	ProgressDialog pProgress;
	
	private Button btnGo;
	private String sID, sPW;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		StartAppAd.init(this, "112710646", "201767333");
		setContentView(R.layout.activity_login);
		
		Global = this.getApplication();
		
		btnGo = (Button) findViewById(R.id.login_go);
		
		((App)Global).init();
		checkLogin();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		startAppAd.onResume();
	}
	
	@Override
	public void onBackPressed()
	{
		startAppAd.onBackPressed();
		super.onBackPressed();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		startAppAd.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
	
	private void checkLogin()
	{
		SharedPreferences fLoad = getApplicationContext().getSharedPreferences("incimobile.cfg",0);
		if(fLoad == null)
			return;
		
		int login = fLoad.getInt("login", 0);
		if(login == 0)
			return;
		
		String id = fLoad.getString("id", null);
		String pw = fLoad.getString("pw", null);
		if(id == null || pw == null)
			return;
		
		sID = id;
		sPW = pw;
		
		requestCookie(id,pw);
	}
	
	public void doLogin(View v)
	{
		EditText eid = (EditText) findViewById(R.id.login_id);
		EditText epw = (EditText) findViewById(R.id.login_pw);
		
		if(eid.length() == 0 || epw.length() == 0)
		{
			Toast.makeText(getApplicationContext(), "id sifreyi gir.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String id = eid.getText().toString();
		String pw = epw.getText().toString();
		
		sID = id;
		sPW = pw;
		
		requestCookie(id,pw);
	}
	
	private void requestCookie(final String id, final String pw)
	{
		btnGo.setClickable(false);
		btnGo.setText("...");
		
		pProgress = new ProgressDialog(Login.this);
		pProgress.setMessage("giris yapiliyor...");
		pProgress.show();
		
		final String fulladr = "http://inci.sozlukspot.com/ss_index.php?ne=login";
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				
				HttpGet req = new HttpGet(fulladr);
				req.addHeader("Referer", "http://inci.sozlukspot.com/index.php?c=soy&ce=top");
				
				try
				{
					HttpResponse httpResponse = httpClient.execute(req);
					if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					{
						Header[] hCookie = httpResponse.getHeaders("Set-Cookie");
						String cookie = hCookie[0].getValue();
						int end = cookie.indexOf(";");
						
						((App)Global).m_sPHPSESSID = cookie.substring(0, end); 
						Log.d("cookie value", ((App)Global).m_sPHPSESSID);
						
						String response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
						int srote = response.indexOf("hidden");
						srote = response.indexOf('=', srote);
						int erote = response.indexOf(' ', srote);
						((App)Global).m_sRote = response.substring(srote + 2, erote - 1);
						Log.d("rote", ((App)Global).m_sRote);
						
						sendLogin(id,pw);
					}
					else
					{
						httpResponse.getEntity().getContent().close();
						sHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								btnGo.setClickable(true);
								btnGo.setText("Giris");
								((App)Global).ShowDialog(Login.this, "Hata", "cookie alinamadi!");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void sendLogin(final String id, final String pw)
	{
		final String fulladr = "http://inci.sozlukspot.com/index.php?sa=login&ne=yap";
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
				httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
				// Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30
				
				HttpPost req = new HttpPost(fulladr);
				req.addHeader("Referer", "http://inci.sozlukspot.com/ss_index.php?ne=login");
				req.setHeader("Cookie", ((App)Global).m_sPHPSESSID);
				Log.d("login cookie", ((App)Global).m_sPHPSESSID);
				
				ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("kuladi", id));
				pairs.add(new BasicNameValuePair("sifre", pw));
				pairs.add(new BasicNameValuePair("gonder", "    login    "));
				pairs.add(new BasicNameValuePair("rote", ((App)Global).m_sRote));
															
				try
				{
					req.setEntity(new UrlEncodedFormEntity(pairs,"UTF-8"));
					final HttpResponse httpResponse = httpClient.execute(req);
					
					if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					{
						final String response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
						sHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								btnGo.setClickable(true);
								btnGo.setText("Giris");
								
								getLoginResponse(response, httpResponse);
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
								btnGo.setClickable(true);
								btnGo.setText("Giris");
								((App)Global).ShowDialog(Login.this, "Hata", "cevap hatali");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	private void getLoginResponse(String response, HttpResponse httpResponse)
	{
		pProgress.dismiss();
		
		if(response == null)
		{
			((App)Global).ShowDialog(Login.this, "Hata", "cevap yok");
			return;
		}
		
		if(response.contains("tekrar dene"))
		{
			((App)Global).ShowDialog(Login.this, "Hata", "id veya sifre yanlis. tekrar dene.");
			return;
		}
		
		if(response.contains("ss_entry.php"))
		{
			SharedPreferences fSave = getApplicationContext().getSharedPreferences("incimobile.cfg",0);
			SharedPreferences.Editor fEditor = fSave.edit();
			fEditor.clear().commit();
			
			fEditor.putInt("login", 1);
			fEditor.putString("id", sID);
			fEditor.putString("pw", sPW);
			fEditor.commit();
			
			((App)Global).m_sID = sID;
			((App)Global).m_sPW = sPW;
			((App)Global).m_bLoggedIn = true;
			
			Header[] hCookie = httpResponse.getHeaders("Set-Cookie");
			if(hCookie.length > 0)
			{
				String cookie = hCookie[0].getValue();
				int end = cookie.indexOf(";");
				
				((App)Global).m_sPunteriz = cookie.substring(0, end) + "; "; 
				Log.d("punteriz value", ((App)Global).m_sPunteriz);
			}
			else
				Log.d("punteriz value", "null");
			
			Toast.makeText(getApplicationContext(), "giris yapildi.", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(Login.this, Home.class);
			startActivity(i);
			
			finish();
		}
	}
	
	public void doSkip(View v)
	{
		Intent i = new Intent(Login.this, Home.class);
		startActivity(i);
		finish();
	}
}
