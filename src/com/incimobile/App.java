package com.incimobile;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;

public class App extends Application
{
	public boolean m_bLoggedIn;
	public String m_sID;
	public String m_sPW;
	public String m_sPHPSESSID;
	public String m_sPunteriz;
	public String m_sRote;
	
	public void init()
	{
		m_bLoggedIn = false;
		m_sID = null;
		m_sPW = null;
		m_sPHPSESSID = "";
		m_sPunteriz = "";
		m_sRote = null;
	}
	
	public void ShowDialog(Context context, String title, String msg)
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);                      
	    dlgAlert.setTitle(title); 
	    dlgAlert.setMessage(msg); 
	    dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton){dialog.cancel();}
	   });
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
	}
}
