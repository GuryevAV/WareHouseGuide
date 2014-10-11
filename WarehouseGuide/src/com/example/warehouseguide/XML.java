package com.example.warehouseguide;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.database.Cursor;
import android.util.Log;
import android.util.Xml;

public class XML {
	
	final static String LOG_TAG = "myLogs";

	public static Boolean CheckXML(String xmlText) {
		
		Boolean result = false;
		try {
			//получаем фабрику
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			//включаем поддержку namespace (по умолчанию выключена)
			factory.setNamespaceAware(true);
			//создаем парсер
			XmlPullParser xpp = factory.newPullParser();
			//даем парсеру на вход
			xpp.setInput(new StringReader(xmlText));
						
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch (xpp.getEventType()) {
				//начало документа
				case XmlPullParser.START_DOCUMENT:
					Log.d(LOG_TAG, "START_DOCUMENT");
					break;
				//начало тэга
				case XmlPullParser.START_TAG:
					if (xpp.getName().contentEquals("check")) {
						xpp.next();
						if (xpp.getText().contentEquals("check")) {
							result = true;
							break;
						}
					}
				}
				//следующий элемент
				xpp.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String OutXML(Cursor c) {
		
		XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, "check");
            serializer.text("check");
            serializer.endTag(null, "check");
            serializer.startTag(null, "items");
            
            if (c != null) {
    			if (c.moveToFirst()) {
    				do {
    					serializer.startTag(null, "item");
    					for (String cn : c.getColumnNames()) {
    						serializer.attribute(null, cn, c.getString(c.getColumnIndex(cn)));
    					}
    					serializer.endTag(null, "item");
    				} while (c.moveToNext());
    			}
    		}
            serializer.endTag(null, "items");
            serializer.endDocument();
            //serializer.flush();
            //Log.d(LOG_TAG, writer.toString());
        } catch(Exception e) {
            Log.e("Exception", "Exception occured in wroting");
        }
        return writer.toString();
	}
	
	public static ArrayList<String[]> LoadFromXML(String xmlText) {
		
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		try {
			//получаем фабрику
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			//включаем поддержку namespace (по умолчанию выключена)
			factory.setNamespaceAware(true);
			//создаем парсер
			XmlPullParser xpp = factory.newPullParser();
			//даем парсеру на вход
			xpp.setInput(new StringReader(xmlText));
						
			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch (xpp.getEventType()) {
				//начало документа
				case XmlPullParser.START_DOCUMENT:
					Log.d(LOG_TAG, "START_DOCUMENT");
					break;
				//начало тэга
				case XmlPullParser.START_TAG:
					if (xpp.getName().contentEquals("item")) {
						String[] good = {"", "", "", "", ""};
						good[0] = xpp.getAttributeValue(1);
						good[1] = xpp.getAttributeValue(2);
						good[2] = xpp.getAttributeValue(3);
						good[3] = xpp.getAttributeValue(4);
						good[4] = xpp.getAttributeValue(5);
						result.add(good);
						xpp.next();
					}
					break;
				default:
					break;
				}
				//следующий элемент
				xpp.next();
			}
			Log.d(LOG_TAG, "END_DOCUMENT");
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
		
}
