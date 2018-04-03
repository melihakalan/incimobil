package com.incimobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;

public class LinkHandler extends LinkMovementMethod
{
	private static String TAG = "LinkHandler";
	private static LinkHandler linkMovementMethod = new LinkHandler();

	public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, android.view.MotionEvent event)
	{
		int action = event.getAction();

		if (action == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
			if (link.length != 0)
			{
				String url = link[0].getURL();
				handleURL(url, widget);
				return true;
			}
		}

		return super.onTouchEvent(widget, buffer, event);
	}

	public static android.text.method.MovementMethod getInstance()
	{
		return linkMovementMethod;
	}
	
	private void handleURL(String url, android.widget.TextView widget)
	{
		Log.d("link handler", url);
		Intent intent;
		Bundle b;
		
		if(url.substring(0,3).equals("/e/"))
		{
			if(widget.getContext().getClass() == Entry.class)
				((Activity)widget.getContext()).finish();
			
			String _url = "http://inci.sozlukspot.com" + url;
			intent = new Intent(widget.getContext(), Entry.class);
			b = new Bundle();
			b.putString("link", _url);
			intent.putExtras(b);
			widget.getContext().startActivity(intent);
		}
		else if(url.contains("inci.sozlukspot.com/e/"))
		{
			if(widget.getContext().getClass() == Entry.class)
				((Activity)widget.getContext()).finish();
			
			intent = new Intent(widget.getContext(), Entry.class);
			b = new Bundle();
			b.putString("link", url);
			intent.putExtras(b);
			
			widget.getContext().startActivity(intent);
		}
		else if(url.contains("inci.sozlukspot.com/w/"))
		{
			if(widget.getContext().getClass() == Topic.class)
				((Activity)widget.getContext()).finish();
			
			int spos = url.indexOf("w/");
			String title = url.substring(spos + 2);
			title = title.replace("-", " ").replace("/", "");
			intent = new Intent(widget.getContext(), Topic.class);
			b = new Bundle();
			b.putString("link", url);
			b.putString("title", title);
			intent.putExtras(b);
			
			widget.getContext().startActivity(intent);
		}
		else
		{
			intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
			widget.getContext().startActivity(intent);
		}
	}
}
