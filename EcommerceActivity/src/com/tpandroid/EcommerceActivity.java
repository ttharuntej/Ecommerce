package com.tpandroid;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EcommerceActivity extends ListActivity {
	private final static String TAG = "EcommerceActivity";
	private static EbayInvoke ebayInvoke;
	private static EbayParser ebayParser;
	private static AmazonParser amazonParser;
	private static ProgressDialog progressDialog;
	private String searchTerm = ""; // intial value for demo
	private String price = "";
	private SearchResult listings;
	private ListingArrayAdapter adapter;
	private Listing selectedListing;
	private int selectedPosition;
	// listing detail dialog
	private AlertDialog listingDetailDialog;
	private ImageView imageViewImage;
	private TextView textViewStartTime;
	private TextView textViewEndTime;
	private TextView textViewListingType;
	private TextView textViewPrice;
	private TextView textViewShipping;
	private TextView textViewLocation;
	private TextView textViewLink;
	// filter dialog
	private AlertDialog keywordDialog;
	private EditText keywordTextbox;
	private EditText priceTextbox;
	// menu constants
	private final static int MENU_KEYWORD = Menu.FIRST;
	private static final int MENU_PREFERENCES = Menu.FIRST + 1;
	private static final int SHOW_PREFERENCES = 1;

	// Used for default search key and price.
	private final String DEFAULT_SEARCH_TERM = "Guitar";
	private final String DEFAULT_PRICE = "1000";
	
	ArrayList<String> amazonItemIds = new ArrayList<String>();
	ArrayList<Listing> listingsArrayList = new ArrayList<Listing>();
	ArrayList<Listing> ebayListingsArrayList = new ArrayList<Listing>();


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			ListView listView = this.getListView();
			listView.setTextFilterEnabled(true);
			listView.setOnItemClickListener(selectItemListener);
			this.updateFromPreferences();
		} catch (Exception x) {
			Log.e(TAG, "onCreate", x);
			this.showErrorDialog(x);
		}
	}

	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_KEYWORD, Menu.NONE, "Search Term");
		menu.add(0, MENU_PREFERENCES, Menu.NONE, "Preferences");
		return true;

	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case MENU_KEYWORD: {
				this.showKeywordDialog();
				return true;
			}
			case MENU_PREFERENCES: {
				System.out.println("Yes entered");
				Intent i = new Intent(this, PreferencesAct.class);
				startActivityForResult(i, SHOW_PREFERENCES);
				return true;
			}

			}
			return false;
		} catch (Exception x) {
			Log.e(TAG, "onOptionsItemSelected", x);
			return (false);
		}
	}

	
	protected void onResume() {
		super.onResume();
		if ((this.listingDetailDialog != null)
				&& (this.listingDetailDialog.isShowing())) {
			return;
		}
		System.out.println("Entered in onResume:");
		this.refreshListings();
	}


	OnClickListener onKeywordDialogCancelListener = new OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {

		}
	};

	private void showKeywordDialog() {
		try {
			if (this.keywordDialog == null) {
				LayoutInflater inflater = (LayoutInflater) this
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.searchdialog,
						(ViewGroup) findViewById(R.id.searchdialog_root));
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				builder.setTitle("Search Keyword");
				builder.setPositiveButton("OK", onKeywordDialogPositiveListener);
				builder.setNegativeButton("Cancel",
						onKeywordDialogCancelListener);
				this.keywordTextbox = (EditText) layout
						.findViewById(R.id.searchdialog_keyword);
				this.priceTextbox = (EditText) layout
						.findViewById(R.id.searchdialog_price);
				this.keywordDialog = builder.create();
			}
			this.keywordDialog.show();
		}

		catch (Exception x) {
			Log.e(TAG, "showFilterDialog", x);
		}
	}

	OnClickListener onListingDetailDialogCloseListener = new OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
		}
	};

	OnClickListener onKeywordDialogPositiveListener = new OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			String newSearchTerm = keywordTextbox.getText().toString()
					.replace(" ", "+");
			String newPriceTerm = priceTextbox.getText().toString();
			System.out.println("newSearchTerm" + newSearchTerm);
			System.out.println("newPriceTerm" + newPriceTerm);
			if (!newSearchTerm.equals(searchTerm)
					|| !newPriceTerm.equals(price)) {
				if (!newSearchTerm.equals(""))
					searchTerm = newSearchTerm;
				if (!newPriceTerm.equalsIgnoreCase(""))
					price = newPriceTerm;

				System.out.println("PPrice:" + price);
				System.out.println("SSearchTerm" + searchTerm);
				refreshListings();
			}
		}
	};

	private void showListingDetailDialog() {
		try {
			
			if (this.selectedListing == null) {
				return;
			}

			if (this.listingDetailDialog == null) {
				LayoutInflater inflater = (LayoutInflater) this
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater
						.inflate(
								R.layout.listingdetail,
								(ViewGroup) findViewById(R.id.listingdetaildialog_root));
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				builder.setTitle(this.selectedListing.getTitle());
				builder.setPositiveButton("Close",
						onListingDetailDialogCloseListener);
				this.imageViewImage = (ImageView) layout
						.findViewById(R.id.listingdetail_image);
				this.textViewStartTime = (TextView) layout
						.findViewById(R.id.listingdetail_starttime);
				this.textViewEndTime = (TextView) layout
						.findViewById(R.id.listingdetail_endtime);
				this.textViewListingType = (TextView) layout
						.findViewById(R.id.listingdetail_listingtype);
				this.textViewPrice = (TextView) layout
						.findViewById(R.id.listingdetail_price);
				this.textViewShipping = (TextView) layout
						.findViewById(R.id.listingdetail_shipping);
				this.textViewLocation = (TextView) layout
						.findViewById(R.id.listingdetail_location);
				this.textViewLink = (TextView) layout
						.findViewById(R.id.listingdetail_link);
				this.listingDetailDialog = builder.create();
			}
			this.textViewStartTime.setText(Html
					.fromHtml("<b>Start Time:</b>&nbsp;&nbsp;"
							+ this.selectedListing.getStartTime()
									.toLocaleString()));
			this.textViewEndTime.setText(Html
					.fromHtml("<b>End Time:</b>&nbsp;&nbsp;"
							+ this.selectedListing.getEndTime()
									.toLocaleString()));
			this.textViewPrice.setText(Html
					.fromHtml("<b>Price:</b>&nbsp;&nbsp;"
							+ this.selectedListing.getCurrentPrice()));
			this.textViewShipping.setText(Html
					.fromHtml("<b>Shipping Cost:</b>&nbsp;&nbsp;"
							+ this.selectedListing.getShippingCost()));
			this.textViewLocation.setText(Html
					.fromHtml("<b>Location</b>&nbsp;&nbsp;"
							+ this.selectedListing.getLocation()));
			String listingType = new String("<b>Listing Type:</b>&nbsp;&nbsp;");
			if (this.selectedListing.isAuction()) {
				listingType = listingType + "Auction";
				if (this.selectedListing.isBuyItNow()) {
					listingType = listingType + ", " + "Buy it now";
				}
			} else if (this.selectedListing.isBuyItNow()) {
				listingType = listingType + "Buy it now";
			} else {
				listingType = listingType + "Not specified";
			}
			this.textViewListingType.setText(Html.fromHtml(listingType));
			StringBuffer html = new StringBuffer("<a href='");
			html.append(this.selectedListing.getListingUrl().replace("\\", ""));
			html.append("'>");
			html.append("View original listing on ");
			html.append(this.selectedListing.getAuctionSource());
			html.append("</a>");
			this.textViewLink.setText(Html.fromHtml(html.toString()));
			this.textViewLink.setOnClickListener(urlClickedListener);
			this.imageViewImage.setImageDrawable(this.adapter
					.getImage(this.selectedPosition));
			this.listingDetailDialog.setTitle(this.selectedListing.getTitle());
			this.listingDetailDialog.show();
		} catch (Exception x) {
			if ((this.listingDetailDialog != null)
					&& (this.listingDetailDialog.isShowing())) {
				this.listingDetailDialog.dismiss();
			}
			Log.e(TAG, "showListingDetailDialog", x);
		}
	}

	private void showErrorDialog(Exception x) {
		try {
			new AlertDialog.Builder(this).setTitle(R.string.app_name)
					.setMessage(x.getMessage())
					.setPositiveButton("Close", null).show();
		} catch (Exception reallyBadTimes) {
			Log.e(TAG, "showErrorDialog", reallyBadTimes);
		}
	}


	OnItemClickListener selectItemListener = new OnItemClickListener() {
		
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			try {
				selectedPosition = position;
				selectedListing = (Listing) adapter.getItem(position);
				showListingDetailDialog();
			} catch (Exception x) {
				Log.e(TAG, "selectItemListener.onItemClick", x);
			}
		}
	};

	View.OnClickListener urlClickedListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			launchBrowser();
		}
	};

	private void launchBrowser() {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(this.selectedListing.getListingUrl().replace(
							"\\", "")));
			this.startActivity(browserIntent);
		} catch (Exception x) {
			Log.e(TAG, "launchBrowser", x);
			this.showErrorDialog(x);
		}
	}


	private final Handler loadListHandler = new Handler() {
		public void handleMessage(Message message) {
			loadListAdapter();
		}
	};

	private void loadListAdapter() {
		
		this.adapter = new ListingArrayAdapter(this, R.layout.listviewitem,listings);
		this.setListAdapter(this.adapter);
		if (progressDialog != null) {
			progressDialog.cancel();
		}
	}

	private class LoadListThread extends Thread {
		private Handler handler;
		private Context context;

		public LoadListThread(Handler handler, Context context) {
			this.handler = handler;
			this.context = context;
		}

		public void run() {
			int counter = 0;
			String searchResponse = "";
			String amazaonResponse = "";
			amazonItemIds = new ArrayList<String>();
			listingsArrayList = new ArrayList<Listing>();
			try {
				if (ebayInvoke == null) {
					ebayInvoke = new EbayInvoke(this.context);
				}
				if (ebayParser == null) {
					ebayParser = new EbayParser(this.context);
				}
				searchResponse = ebayInvoke.search(searchTerm, price);
				if(amazonParser == null)
				{
					amazonParser = new AmazonParser(this.context);
				}
				amazonItemIds=	amazonParser.fetchAmazonProductId(searchTerm, price);
				
				for(int i =0;i <amazonItemIds.size();i++)
				{
					if(counter >=10)
						break;
					listingsArrayList.add(amazonParser.fetchAmazonProductDetails(amazonItemIds.get(i)));
				counter++;	
				}
				if (listings == null) {
					listings = new SearchResult();
					
				}
				
				
				ebayListingsArrayList = ebayParser.parseListings(searchResponse);
				for(int i =0;i <ebayListingsArrayList.size();i++){
					listingsArrayList.add(ebayListingsArrayList.get(i));
				}
				 
				listings.setListings(listingsArrayList);
				this.handler.sendEmptyMessage(RESULT_OK);
			}

			catch (Exception x) {
				Log.e(TAG, "LoadListThread.run(): searchResponse="
						+ searchResponse, x);
				listings.setError(x);
				if ((progressDialog != null) && (progressDialog.isShowing())) {
					progressDialog.dismiss();
				}
				showErrorDialog(x);
			}
		}
	}

	private void refreshListings() 
	{
		try 
		{
			if (progressDialog == null) 
			{
				progressDialog = new ProgressDialog(this);
			}
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Searching Store Inventory...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			LoadListThread loadListThread = new LoadListThread(
					this.loadListHandler, this.getApplicationContext());
			loadListThread.start();
		} 
		catch (Exception x) 
		{
			Log.e(TAG, "onResume", x);
			if ((progressDialog != null) && (progressDialog.isShowing())) 
			{
				progressDialog.dismiss();
			}
			this.showErrorDialog(x);
		}
	}

	int minimumMagnitude = 0;
	boolean autoUpdate = false;
	int updateFreq = 0;

 	private void updateFromPreferences()
 	{
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		searchTerm = prefs.getString(PreferencesAct.PREF_KEYWORD,
				"guitar");
		price = prefs.getString(PreferencesAct.PREF_MAX_PRICE, "550");
		System.out.println(" Price is::" + price);
		System.out.println("Search Term is :: " + searchTerm);

		if (price.equals("")) // When the user doesnt enter the maximum price
								// then we are considering 500$ as Max Price.
			price = DEFAULT_PRICE;

		if (searchTerm.equals(""))
			searchTerm = DEFAULT_SEARCH_TERM;
	 	int freqIndex = prefs.getInt(PreferencesAct.PREF_UPDATE_FREQ, 0);

		if (freqIndex < 0)
			freqIndex = 0;

	}

	
	public void onActivityResult(int reqCode, int rsltCode, Intent data) {
		super.onActivityResult(reqCode, rsltCode, data);

		if (reqCode == SHOW_PREFERENCES) {
			if (rsltCode == Activity.RESULT_OK) {
				updateFromPreferences();
				refreshListings();
			}
		}
	}
}