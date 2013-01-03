package com.tpandroid;

import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListingArrayAdapter extends ArrayAdapter<Listing>
{
	private final static String TAG="ListingArrayAdapter";
	private Context context;
	private SearchResult listings;
	private int resourceId;
	private Drawable images[];
	
	public ListingArrayAdapter(Context context,int resourceId,SearchResult listings)
	{
		super(context,resourceId,listings.getListings());
		this.images=new Drawable[listings.getListings().size()];
		this.context=context;
		this.listings=listings;
		this.resourceId=resourceId;
	} 
	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		LayoutInflater inflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout=inflater.inflate(resourceId,null);
		Listing listing=this.listings.getListings().get(position);
		TextView titleField=(TextView)layout.findViewById(R.id.listviewitem_title);
		titleField.setText(listing.getTitle());
		TextView priceField=(TextView)layout.findViewById(R.id.listviewitem_price);
		priceField.setText("Current price: "+listing.getCurrentPrice());
		TextView shippingField=(TextView)layout.findViewById(R.id.listviewitem_shipping);
		shippingField.setText("Shipping: "+listing.getShippingCost());
		if(listing.getListingUrl()!=null)
		{
			try
			{
				URL url=new URL(listing.getImageUrl().replace("\\", ""));
			 	InputStream is=(InputStream)url.getContent();
				Drawable image=Drawable.createFromStream(is,"src");
			 	ImageView imageView=new ImageView(context);
				imageView=(ImageView)layout.findViewById(R.id.listviewitem_image);
				imageView.setImageDrawable(image);
				this.images[position]=image;
			}
			catch(Exception x)
			{
				Log.e(TAG,"getView - in if(listing.getListingUrl()!=null)",x);
			}
		}
		return(layout);  	
	}
	
	public Drawable getImage(int position){
		return(this.images[position]);
	}
}