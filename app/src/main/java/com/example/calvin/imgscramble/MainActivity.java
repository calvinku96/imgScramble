package com.example.calvin.imgscramble;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Random;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private static final int SELECT_PICTURE = 1;
    boolean selected = false;
    private ClipboardManager clipboard;
    private ClipData clipdata;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //prevent keyboard automatically popup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);

            switch (position) {
                case 0:
                    return new ScrambleFragment();
                case 1:
                    return new AboutFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.tab_scramble).toUpperCase(l);
                case 1:
                    return getString(R.string.tab_about).toUpperCase(l);
            }
            return null;
        }
    }

    //Scramble Tab
    public void scrambleGetImage(View v) {
        //Toast.makeText(this, "Getting Image", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we are reponding to
        ImageView image = (ImageView) findViewById(R.id.scrambleImageView);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            image.setImageURI(selectedImageUri);
            selected = true;
        }

    }

    public void scrambleStart(View v) {
        //Toast.makeText(this, "Scrambling", Toast.LENGTH_SHORT).show();
        if (selected) {
            //ImageUri to String
            EditText scramblepassword = (EditText) findViewById(R.id.scramble_password);
            EditText rowtext = (EditText) findViewById(R.id.scramble_row);
            EditText coltext = (EditText) findViewById(R.id.scramble_cols);
            //boolean rowtextfilled = true;
            //boolean coltextfilled = true;
            //boolean scramblepasswordfilled = true;
            int errcounter = 0;
            if (rowtext.getText().toString()==""){
                //rowtextfilled = false;
                errcounter++;
            }
            if (coltext.getText().toString()==""){
                //coltextfilled = false;
                errcounter++;
            }
            if (scramblepassword.getText().toString()==""){
                //scramblepasswordfilled = false;
                errcounter++;
            }
            if (errcounter>0){
                Toast.makeText(this, "Please fill in the appropriate information", Toast.LENGTH_SHORT).show();
            }else{
                String imageuristring = selectedImageUri.toString();
                Intent intent = new Intent(this, ScramblingActivity.class);
                Bundle extras = new Bundle();
                extras.putString("EXTRA_IMAGE", imageuristring);
                extras.putString("EXTRA_PASS", scramblepassword.getText().toString());
                extras.putString("EXTRA_ROW", rowtext.getText().toString());
                extras.putString("EXTRA_COL", coltext.getText().toString());
                intent.putExtras(extras);
                startActivity(intent);
            }

        }
        else{
            Toast.makeText(this, "Image not yet selected",Toast.LENGTH_SHORT).show();
        }
    }

    public void scrambleRandom(View v) {
        //Toast.makeText(this, "Random", Toast.LENGTH_SHORT).show();
        Random rand = new Random();
        BigInteger passint = new BigInteger(100, rand);
        String pass = passint.toString(36);
        int row = rand.nextInt(50) + 20;
        int col = rand.nextInt(50) + 20;
        EditText scramblepassword = (EditText) findViewById(R.id.scramble_password);
        EditText rowtext = (EditText) findViewById(R.id.scramble_row);
        EditText coltext = (EditText) findViewById(R.id.scramble_cols);
        scramblepassword.setText(pass);
        rowtext.setText(Integer.toString(row));
        coltext.setText(Integer.toString(col));
    }

    public void scrambleCopyPassword(View v) {
        EditText scramblepassword = (EditText) findViewById(R.id.scramble_password);
        String cliptext = scramblepassword.getText().toString();
        clipdata = ClipData.newPlainText("text", cliptext);
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    boolean showpasswordstatus = true;

    public void scrambleShowPassword(View v) {
        //Toast.makeText(this, "Show Password", Toast.LENGTH_SHORT).show();
        EditText scramblepassword = (EditText) findViewById(R.id.scramble_password);
        ImageView showpasswordimage = (ImageView) findViewById(R.id.scramble_show_password);
        if (showpasswordstatus) {
            //Change imageView
            scramblepassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showpasswordimage.setImageResource(R.drawable.ic_visibility_black_24dp);
            showpasswordstatus = false;
        } else {
            scramblepassword.setInputType(129);
            showpasswordimage.setImageResource(R.drawable.ic_visibility_off_black_24dp);
            showpasswordstatus = true;
        }
    }

}
