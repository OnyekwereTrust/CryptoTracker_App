package codebits.tjah;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.bumptech.glide.Glide;


import java.util.List;

/**
 * Created by tjah on 18/10/17.
 */

public class Currency_Adapter extends RecyclerView.Adapter<Currency_Adapter.MyViewHolder> {

    private Context mContext;
    private List<Currency> currencyList;
    SharedPreferences sharedPref = null;
    SharedPreferences.Editor editor = null;
    public int ITEM_POSITION = 0;

    ///api for getting the price rate of bitcoins
    public String BTC_CRYPTO_URL = "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=";

    //api for getting the price rate of ethereum
    public String ETH_CRYPTO_URL = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=";
    Resources res;
    Handler handler = new Handler();
    Currency currency;
    private int card_count = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public Currency_Adapter(Context mContext, List<Currency> currencyList) {
        this.mContext = mContext;
        this.currencyList = currencyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.currency_card, parent, false);
        sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.shared_crypto_pref), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        res = mContext.getResources();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        //initialize 0.00 text with currency name for easy identification
        currency = currencyList.get(position);
        holder.title.setText(currency.getName()+" 0.00");
        card_count = 0;
        //to keep updating values on each card, we need to run it
        Runnable runnable = new Runnable() {
            public void run() {
                if (card_count!=currencyList.size()) {
                    //to limit the number of requests made, let's stop the runnable once the list size is reached
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        public void run() {
                            currency = currencyList.get(position);
                            String SELECT_BASE = "";//switch between btc and eth urls depending on base currency on the tab
                            if(currency.getThumbnail()== R.drawable.btc_black){
                                SELECT_BASE = BTC_CRYPTO_URL;
                            }else{
                                SELECT_BASE = ETH_CRYPTO_URL;
                            }

                            new HttpRequestTask(
                                    new HttpRequest(SELECT_BASE+currency.getName(), HttpRequest.POST, "{ \"currency\": \"value\" }"),
                                    new HttpRequest.Handler() {
                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                String name = response.body.replaceAll("\"", "")
                                                        .replace("{", "").replace("}", "").split(":")[0];
                                                String value = response.body.replaceAll("\"", "")
                                                        .replace("{", "").replace("}", "").split(":")[1];
                                               holder.title.setText(name+" "+value);
                                                card_count++;
                                             //   Toast.makeText(mContext, card_count+""+currencyList.size() +response.body, Toast.LENGTH_LONG).show();

                                            } else {
                                                Log.e(this.getClass().toString(), "Request unsuccessful: " + response);
                                                Toast.makeText(mContext, "error, check your internet connection!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }).execute();

                        }
                    });
                }
            }
        };
        new Thread(runnable).start();

        // loading currency cover using Glide library
        Glide.with(mContext).load(currency.getThumbnail()).into(holder.thumbnail);

         /* The user can click anywhere on the card; thumbnail, view, etc...
    * It's optional but just to improve userbility we want to open the conversion page on any point clicked*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent theIntent = new Intent(mContext, Conversion_Activity.class);
                theIntent.putExtra("crypto_position", position);
                mContext.startActivity(theIntent);
                Home_Activity homeActivity = new Home_Activity();
                homeActivity.finish();

            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, holder.title.getText()+" is the title", Toast.LENGTH_LONG).show();
                Intent theIntent = new Intent(mContext, Conversion_Activity.class);
                theIntent.putExtra("crypto_position", position);
                mContext.startActivity(theIntent);
                Home_Activity homeActivity = new Home_Activity();
                homeActivity.finish();

            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
                //record the position of clicked item to access it from the PopupMenu
                ITEM_POSITION = position;
            }
        });


    }


    // Showing popup menu when tapping on 3 dots

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_currency, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }


   //  Click listener for popup menu items

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_conversion:

                    Intent theIntent = new Intent(mContext, Conversion_Activity.class);
                    theIntent.putExtra("crypto_position", ITEM_POSITION);
                    mContext.startActivity(theIntent);
                    Home_Activity homeActivity = new Home_Activity();
                    homeActivity.finish();
                    return true;

                case R.id.action_remove:


                    //the shared preferenceListener method in Home_Activity will refresh UI once an item has been deleted;
                    int count  = currencyList.size()-1;//our new List_size will be initial size - 1;
                    currencyList.remove(ITEM_POSITION);

                    for(int i = 0; i< currencyList.size(); i++)
                    {
                            editor.remove("List_" + i);
                            editor.putString("List_" + i, currencyList.get(i).getName() + "#" +currencyList.get(i).getSymbol() + "#" + currencyList.get(i).getThumbnail());


                    }
                    editor.putInt("List_size", count);
                    editor.commit();
                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }
}
