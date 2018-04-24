package shatarupa.cuidate;
 
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
 
public class WelcomeActivity extends AppCompatActivity {
 
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private PrefManager prefManager;
    private boolean Contacts_Added;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = this.getSharedPreferences("pref_settings", Context.MODE_PRIVATE); //Shared preferences allow us to store simple default variables like int, float, long, boolean in memory (on
        //internal storage/sd card etc. A new shared pref settings have been created with name pref_settings
        // Checking for first time launch - before calling setContentView()
        Contacts_Added = settings.getBoolean("Contacts_Added_Status", false);
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        final Context context=getApplicationContext();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_welcome);
 
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);
 
 
        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3,
                R.layout.welcome_slide4,
                R.layout.welcome_slide5,
                R.layout.welcome_slide6};
 
        // adding bottom dots
        addBottomDots(0);
 
        // making notification bar transparent
        changeStatusBarColor();
 
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
 
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });
 
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    // If Current=5, ask for GPS
                    // IF current =6, ask for SMS

                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }
 
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
 
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);
 
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }
 
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }
 
    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }
 
    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        if (!Contacts_Added)
        {
            startActivity(new Intent(WelcomeActivity.this, Contact_Picker.class));
        }
        else
        {
            startActivity(new Intent(WelcomeActivity.this, Home.class)); //Replace with main heading activity
        }
        finish();
    }
 
    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
 
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            if(position==5){
                    //Fine Location
                    if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
                    {
                        ActivityCompat.requestPermissions(WelcomeActivity.this, //Requesting permission to send SMS
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                0);
                    }
                    //Coarse Location
                    if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
                    {
                        ActivityCompat.requestPermissions(WelcomeActivity.this, //Requesting permission to send SMS
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                0);
                    }
                }
            if(position==2){
                //Contacts
                if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
                {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, //Requesting permission to send SMS
                            new String[]{Manifest.permission.READ_CONTACTS},
                            0);
                }
            }
            if(position==4){
                //Contacts
                if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
                {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, //Requesting permission to send SMS
                            new String[]{Manifest.permission.SEND_SMS},
                            0);
                }
            }
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }
 
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
 
        }
 
        @Override
        public void onPageScrollStateChanged(int arg0) {
 
        }
    };
 
    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
 
    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
 
        public MyViewPagerAdapter() {
        }
 
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
 
            return view;
        }
 
        @Override
        public int getCount() {
            return layouts.length;
        }
 
        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }
 
 
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}