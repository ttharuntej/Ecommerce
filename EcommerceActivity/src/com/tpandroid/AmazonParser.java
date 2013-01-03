package com.tpandroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
 
public class AmazonParser 
{
	 private static final String AWS_ACCESS_KEY_ID = "PUT YOUR AWS_ACCESS_KEY_ID";

	    
	    private static final String AWS_SECRET_KEY = "PUT YOUR AWS_SECRET_KEY";

	   
	    private static final String ENDPOINT = "ecs.amazonaws.com";

	    private static final String ITEM_ID = "0545010225";
	    private static SimpleDateFormat dateFormat;
		
		private static final String TAG = "Amazon";
	    private  ArrayList<String> itemId =new ArrayList<String>();
	    private Resources resources;

	public AmazonParser(Context context)
	{
		synchronized(this)
		{
			if(dateFormat==null)
			{
				dateFormat=new SimpleDateFormat("[\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"]");
			}
		}
		this.resources=context.getResources();
	}
	
    @SuppressWarnings("unused")
	public ArrayList<String> fetchAmazonProductId(String searchTerm, String price) 
    {
    	System.out.println("Search Term Amazon"+ searchTerm);
    	System.out.println("Price Amazon:"+ price);
    	 URL url;
    	 String requestUrl = null;
         String title = null;
         SignedRequestsHelper helper = null;
         int counter = 0;
         try 
         {
             helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
         }
         catch (Exception e) 
         {
             e.printStackTrace();
             
         }
    	System.out.println("In fetchAmazonResults ");
		 try
		 {
			 	itemId = new ArrayList<String>();
			  Map<String, String> params = new HashMap<String, String>();
		        params.put("Service", "AWSECommerceService");
		        params.put("Version", "2011-11-18");
		        params.put("Operation", "ItemSearch");
		        params.put("SearchIndex","All");
		        params.put("Keywords", searchTerm);
		        params.put("AssociateTag", "digitalsteps-20");
		        params.put("MaximumPrice", price);
		        requestUrl = helper.sign(params);
		        System.out.println("Signed Request is \"" + requestUrl + "\"");
		        String amazonFeed = requestUrl;
			 	System.out.println("amazonFeed"+ amazonFeed);
			 	url = new URL(amazonFeed);
	    		URLConnection connection;
	    		connection = url.openConnection();
	    		Log.v("fetchAmazonProductId()", "connection opened...");
	    		HttpURLConnection httpConnection = (HttpURLConnection)connection;
	    		int responseCode = httpConnection.getResponseCode();
	    		System.out.println("responseCode::"+responseCode);
	    		if ( responseCode == HttpURLConnection.HTTP_OK ) 
	    		{
		    			Log.v("fetchAmazonProductId()", "response HTTP_OK");
		    			InputStream in = httpConnection.getInputStream();
		    			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    			DocumentBuilder db = dbf.newDocumentBuilder();
		    			Document dom = db.parse(in);
		    			Element docEle = dom.getDocumentElement();
		    			NodeList nl = docEle.getElementsByTagName("Item");
		    			if( nl != null && nl.getLength() > 0)
		    			{
		    				for(int i = 0; i < nl.getLength(); i++)
		    				{
		    					if(counter >=15)
		    						break;
		    					Element mathematician = (Element)nl.item(i);
		    					Element asin = (Element)mathematician.getElementsByTagName("ASIN").item(0);
		    					String asinStr = asin.getFirstChild().getNodeValue();
		    					counter++;
		    					itemId.add(asinStr);
		    	 				
		    				}
		    				 
		    			}
	    			}
	    		}
		 catch ( MalformedURLException e ) 
	    	{
	    		Log.v("fetchAmazonProductId()", "MalformedURLException...");
	    		e.printStackTrace();
	    	}
	    	catch ( IOException e ) 
	    	{
	    		Log.v("fetchAmazonProductId()", "IOException...");
	    		e.printStackTrace();
	    	}
	    	catch ( ParserConfigurationException e ) 
	    	{
	    		Log.v("fetchAmazonProductId()", "ParserConfigurationException...");
	    		e.printStackTrace();
	    	}
	    	catch ( SAXException e ) 
	    	{
	    		Log.v("fetchAmazonProductId()", "SAXException...");
	    		e.printStackTrace();
	    	}
	    	finally 
	    	{
	    	
	    	}
		return itemId;
	}
    public Listing fetchAmazonProductDetails(String item) throws ParseException
    {
    	URL url;
    	Listing listing=new Listing();
    	 String requestUrl = null;
    	 String priceString;
         SignedRequestsHelper helper = null;
         try 
         {
             helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
         }
         catch (Exception e) 
         {
             e.printStackTrace();
             
         }
		 try
		 {
			 
			 Map<String, String> params = new HashMap<String, String>();
		        params.put("Service", "AWSECommerceService");
		        params.put("Version", "2011-11-18");
		        params.put("Operation", "ItemLookup");
		        params.put("ItemId", item);
		        params.put("AssociateTag", "digitalsteps-20");
		        params.put("ResponseGroup", "Images,ItemAttributes");
		         requestUrl = helper.sign(params);
		        System.out.println("Signed Request is \"" + requestUrl + "\"");
	 		 String amazonFeed = requestUrl; 
	 	 	url = new URL(amazonFeed);
	   		URLConnection connection;
	   		connection = url.openConnection();
	   		Log.v("fetchAmazonProductDetails()", "connection opened...");
	   		HttpURLConnection httpConnection = (HttpURLConnection)connection;
	   		int responseCode = httpConnection.getResponseCode();
	   		System.out.println("responseCode::"+responseCode);
			   		if ( responseCode == HttpURLConnection.HTTP_OK ) 
			   		{
			  	    			Log.v("fetchAmazonProductDetails()", "response HTTP_OK");
				    			InputStream in = httpConnection.getInputStream();
				    			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				    			DocumentBuilder db = dbf.newDocumentBuilder();
				    			Document dom = db.parse(in);
				    			Element docEle = dom.getDocumentElement();
  				    			NodeList nl = docEle.getElementsByTagName("Items");
 				    			if( nl != null && nl.getLength() > 0)
				    			{
				    				for(int i = 0; i < nl.getLength(); i++)
				    				{
				    				 	Element items = (Element)nl.item(i);
				    					Element asin = (Element)items.getElementsByTagName("ASIN").item(0);
					    				Element detailedPageUrl = (Element)items.getElementsByTagName("DetailPageURL").item(0);
				    					Element imageSets = (Element)items.getElementsByTagName("ImageSets").item(0);
				    					Element tumbnail = (Element)imageSets.getElementsByTagName("ThumbnailImage").item(0);
				    					Element tumbnailUrl = (Element) tumbnail.getElementsByTagName("URL").item(0);
				    					Element itemAttributes = (Element)items.getElementsByTagName("ItemAttributes").item(0);
				    					if((Element)itemAttributes.getElementsByTagName("ListPrice").item(0)!= null)
				    					{
				    						Element listPrice = (Element)itemAttributes.getElementsByTagName("ListPrice").item(0);
				    						Element price = (Element) listPrice.getElementsByTagName("FormattedPrice").item(0);
				    						priceString= price.getFirstChild().getNodeValue();
				    					}
				    					else
				    					{
				    						priceString ="0";
				    					}
					    					
					    				Element title = (Element)items.getElementsByTagName("Title").item(0);
					    				String id = asin.getFirstChild().getNodeValue();
				    					String detailePageUrlStr = detailedPageUrl.getFirstChild().getNodeValue();
				    					String imgUrl = tumbnailUrl.getFirstChild().getNodeValue();
				    				 	String titleString = title.getFirstChild().getNodeValue(); 
				    				 	Date date  = new Date();
				    				 	listing.setAuctionSource(this.resources.getString(R.string.amazon_source_name));
				    				 	listing.setId(id);
				    				 	listing.setTitle(titleString);
				    				 	listing.setCurrentPrice(priceString);
				    				 	listing.setListingUrl(detailePageUrlStr);
				    				 	listing.setShippingCost("Not listed");
				    				 	String nowYYYYMMDD = new String( dateFormat.format( date ) );
				    				 	listing.setStartTime(dateFormat.parse(nowYYYYMMDD));
				    					listing.setEndTime(dateFormat.parse(nowYYYYMMDD));
				    				 	try
				    					{
				    						listing.setImageUrl(imgUrl);
				    					}
				    					catch( Exception jx)
				    					{
				    						Log.e(TAG,"parseListing: parsing image URL",jx);
				    						listing.setImageUrl(null);
				    					}
				    				 System.out.println("detailePageUrlStr"+ detailePageUrlStr);
				    				 System.out.println("imgUrl"+ imgUrl);
				    				 System.out.println("titleString"+ titleString);
				    				 System.out.println("priceString"+ priceString);
				    				}
				    			}
			   			}
   		}
	 catch ( MalformedURLException e1 ) 
   	{
   		Log.v("fetchAmazonProductId()", "MalformedURLException...");
   		e1.printStackTrace();
   	}
   	catch ( IOException e2 ) 
   	{
   		Log.v("fetchAmazonProductId()", "IOException...");
   		e2.printStackTrace();
   	}
   	catch ( ParserConfigurationException e3 ) 
   	{
   		Log.v("fetchAmazonProductId()", "ParserConfigurationException...");
   		e3.printStackTrace();
   	}
   	catch ( SAXException e4 ) 
   	{
   		Log.v("fetchAmazonProductId()", "SAXException...");
   		e4.printStackTrace();
   	}
   	finally 
   	{
   	
   	}
		return listing;
	 
    }
 


}
 
 
