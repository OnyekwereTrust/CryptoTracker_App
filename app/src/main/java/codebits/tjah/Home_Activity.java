package codebits.tjah;

/**
 * Created by tjah on 17/10/17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cryptosasa.R;

import java.util.ArrayList;
import java.util.List;

public class Home_Activity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView recyclerView;
    private Currency_Adapter adapter;
    private List<Currency> currencyList;
    RadioButton radio_btc, radio_eth;
    SharedPreferences sharedPref = null;
    SharedPreferences.Editor editor = null;
    Spinner base_spinner;
    private float sD;
    Resources res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        //let's have an exit back button in our homePage
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        currencyList = new ArrayList<>();
        adapter = new Currency_Adapter(this, currencyList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


        Button btn_Add = (Button) findViewById(R.id.add_button);
        radio_btc = (RadioButton) findViewById(R.id.radio_button_btc);
        radio_eth = (RadioButton) findViewById(R.id.radio_button_eth);


        sharedPref = getSharedPreferences(getString(R.string.shared_crypto_pref), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        loadArray();

        base_spinner = (Spinner) findViewById(R.id.quote_currency);
        //Spinner counter_spinner = (Spinner) findViewById(R.id.counter_currency);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_list, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        base_spinner.setAdapter(adapter);


        //get dimensions to size our tips window
        res = getResources();
        sD = res.getDisplayMetrics().density;
        sD = res.getDisplayMetrics().density;


        //Implement a listener to monitor changes in shared preferences
        sharedPref.registerOnSharedPreferenceChangeListener(this);


        //Add card in the home page
        btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this filters the string format to extract name and symbol of the selected currency
                String name = base_spinner.getSelectedItem().toString().split(",")[0].replace(" ", "");
                String symbol = base_spinner.getSelectedItem().toString().split(",")[1].replace(" ", "");

                if (radio_eth.isChecked()) {
                    if (!checkIfExists(R.drawable.eth_logo, name)) {
                        addCard(radio_eth.getText().toString(), name, symbol);

                    }
                    //this checks if a card has already been added
                    else {
                        Toast.makeText(getApplicationContext(), "This card already exists!", Toast.LENGTH_LONG).show();
                    }
                } else if (radio_btc.isChecked()) {
                    if (!checkIfExists(R.drawable.btc_black, name)) {
                        addCard(radio_btc.getText().toString(), name, symbol);
                    } else {
                        Toast.makeText(getApplicationContext(), "This card already exists!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //Initialize cover image CollapsingToolbarLayout background
        ImageView img_cover = (ImageView) findViewById(R.id.backdrop);
        try {
            Glide.with(this).load(R.drawable.stock_backgrd).into(img_cover);

        } catch (Exception e) {
        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here i.e on delete
        loadArray();
        saveArray();
        adapter.notifyDataSetChanged();

    }


    // Initializing collapsing toolbar on scroll

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    // Adding/Removing currency comparison menu

    private void addCard(String base_currency, String quote_currency_name, String quote_currency_symbol) {
        int[] base_type = new int[]{
                R.drawable.btc_black, R.drawable.eth_logo};

        if (base_currency.contains("BTC")) {
            Currency a = new Currency(quote_currency_name, quote_currency_symbol, base_type[0]);
            currencyList.add(a);
            saveArray();
        }
        else if (base_currency.contains("ETH")) {
            Currency a = new Currency(quote_currency_name, quote_currency_symbol, base_type[1]);
            currencyList.add(a);
            saveArray();
        }

        adapter.notifyDataSetChanged();
    }


    public void saveArray() {
    /* List_i is an array
    * Save all our currencyList items in sharedPreference*/
        editor.putInt("List_size", currencyList.size());
        for (int i = 0; i < currencyList.size(); i++) {
            editor.remove("List_" + i);
            editor.putString("List_" + i, currencyList.get(i).getName() + "#" + currencyList.get(i).getSymbol() + "#" + currencyList.get(i).getThumbnail());
        }

        editor.commit();
    }


    public void loadArray() {

        // Extract all items from sharedPreference to the currencyList*/
        currencyList.clear();
        int size = sharedPref.getInt("List_size", 0);

        for (int i = 0; i < size; i++) {

            if (!sharedPref.getString("List_" + i, null).equals(null)) {
                Currency a = new Currency(sharedPref.getString("List_" + i, null).split("#")[0],
                        sharedPref.getString("List_" + i, null).split("#")[1],
                        Integer.parseInt(sharedPref.getString("List_" + i, null).split("#")[2]));
                currencyList.add(a);
            }
        }
        adapter.notifyDataSetChanged();
    }


    public boolean checkIfExists(int thumbnail, String quote_currency)

    //Check if an item already exists in sharedPreference before saving to avoid duplication
    {
        boolean checked = false;
        int size = sharedPref.getInt("List_size", 0);

        for (int i = 0; i < size; i++) {
            if (sharedPref.getString("List_" + i, null).split("#")[2].contains(String.valueOf(thumbnail))
                    && sharedPref.getString("List_" + i, null).split("#")[0].contains(quote_currency)) {
                checked = true;
            }

        }
        return checked;

    }

    //RecyclerView item decoration - give equal margin around grid item

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }


    // Converting dp to pixel

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
