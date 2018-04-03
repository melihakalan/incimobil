package com.incimobile;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Edit extends Activity
{
	private Application Global;
	private String sLink, sEntryId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		Global = this.getApplication();
		
		sLink = getIntent().getExtras().getString("link");
		sEntryId = getIntent().getExtras().getString("id");
		
		EditText eEntry = (EditText) findViewById(R.id.edit_entry);
		String entry = getIntent().getExtras().getString("entry");
		eEntry.setText(entry);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.edit, menu);
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
	
	public void saveEntry(View v)
	{
		final String fulladr = "http://inci.sozlukspot.com/index.php?sa=duzelt&ne=yap";
		
		EditText eEntry = (EditText) findViewById(R.id.edit_entry);
		final String entry = eEntry.getText().toString();
		
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
				pairs.add(new BasicNameValuePair("duzelt", "  düzelt   "));
				pairs.add(new BasicNameValuePair("entry_id", sEntryId));
				
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
								Toast.makeText(getApplicationContext(), "entry duzeltildi.", Toast.LENGTH_SHORT).show();
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
								((App)Global).ShowDialog(Edit.this, "Hata", "entry duzeltilemedi.");
							}
						});
					}
				}catch(Exception e){e.printStackTrace();}
			}
		}).start();
	}
	
	public void close(View v)
	{
		finish();
	}

}
