package com.example.calvin.imgscramble;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


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
    static final int REQUEST_TAKE_PHOTO = 2;
    String photofilepath;

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
        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
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
            EditText imagewidth = (EditText) findViewById(R.id.scramble_image_width);
            EditText imageheight = (EditText) findViewById(R.id.scramble_image_height);
            RadioButton descrambleradio = (RadioButton) findViewById(R.id.scramble_radio_descramble);
            String scrambleradiostring = "s";
            String scrambleimagewidth = imagewidth.getText().toString();
            String scrambleimageheight = imageheight.getText().toString();
            boolean descrambleradioisChecked = descrambleradio.isChecked();
            if (descrambleradioisChecked) {
                scrambleradiostring = "d";
            }
            //boolean rowtextfilled = true;
            //boolean coltextfilled = true;
            //boolean scramblepasswordfilled = true;
            int errcounter = 0;
            if (rowtext.getText().toString() == "") {
                //rowtextfilled = false;
                errcounter++;
            }
            if (coltext.getText().toString() == "") {
                //coltextfilled = false;
                errcounter++;
            }
            if (scramblepassword.getText().toString() == "") {
                //scramblepasswordfilled = false;
                errcounter++;
            }
            if (errcounter > 0) {
                Toast.makeText(this, "Please fill in the appropriate information", Toast.LENGTH_SHORT).show();
            } else {
                String imageuristring = selectedImageUri.toString();
                Intent intent = new Intent(this, ScramblingActivity.class);
                Bundle extras = new Bundle();
                extras.putString("EXTRA_IMAGE", imageuristring);
                extras.putString("EXTRA_PASS", scramblepassword.getText().toString());
                extras.putString("EXTRA_ROW", rowtext.getText().toString());
                extras.putString("EXTRA_COL", coltext.getText().toString());
                extras.putString("EXTRA_WIDTH", scrambleimagewidth);
                extras.putString("EXTRA_HEIGHT", scrambleimageheight);
                extras.putString("EXTRA_SCRAMBLE", scrambleradiostring);
                intent.putExtras(extras);
                startActivity(intent);
            }

        } else {
            Toast.makeText(this, "Image not yet selected", Toast.LENGTH_SHORT).show();
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

    public void openCamera(View v) {
        PackageManager packageManager = this.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                //Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                selectedImageUri = Uri.fromFile(photoFile);
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }

            }
        } else {
            Toast.makeText(this, getString(R.string.scramble_no_camera), Toast.LENGTH_SHORT).show();
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/imgScramble/Source/";
        File storageDir = new File(sdCard);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        selectedImageUri = Uri.fromFile(image);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(selectedImageUri);
        this.sendBroadcast(mediaScanIntent);
        return image;
    }

}
