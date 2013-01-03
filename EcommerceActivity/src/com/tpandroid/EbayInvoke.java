package com.tpandroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

public class EbayInvoke
{
	private static String appID;
	private static String ebayURL;
	private Resources resources;

	public EbayInvoke(Context context)
	{
		this.resources=context.getResources();
		if(Build.PRODUCT.toLowerCase().indexOf("sdk")>-1)
		{
			 appID=this.resources.getString(R.string.ebay_appid_production);
			ebayURL=this.resources.getString(R.string.ebay_wsurl_production);
		}
		else
		{
			appID=this.resources.getString(R.string.ebay_appid_production);
			ebayURL=this.resources.getString(R.string.ebay_wsurl_production);
		}
	}
	
	public String search(String keyword, String price) throws Exception
	{
		String jsonResponse=null;
		jsonResponse=invokeEbayRest(keyword,price);
		if((jsonResponse==null)||(jsonResponse.length()<1))
		{
			throw(new Exception("No result received from invokeEbayRest("+keyword+") and ("+price+")"));
		}
		return(jsonResponse);
	}
	
	private String getRequestURL(String keyword, String price)
	{
		CharSequence requestURL=TextUtils.expandTemplate(this.resources.getString(R.string.ebay_request_template),ebayURL,appID,keyword,price);
		return(requestURL.toString());
	}
	
	private String invokeEbayRest(String keyword, String price) throws Exception
	{
		String result=null;
		HttpClient httpClient=new DefaultHttpClient();  
	 	HttpGet httpGet=new HttpGet(this.getRequestURL(keyword,price));  
		HttpResponse response=httpClient.execute(httpGet);  
		HttpEntity httpEntity=response.getEntity();  
		if(httpEntity!=null)
		{  
			InputStream in=httpEntity.getContent();  
	        BufferedReader reader=new BufferedReader(new InputStreamReader(in));
	        StringBuffer temp=new StringBuffer();
	        String currentLine=null;
	        while((currentLine=reader.readLine())!=null)
	        {
	           	temp.append(currentLine);
	        }
	        result=temp.toString();
	     	in.close();
		}
		return(result);
	}	
}