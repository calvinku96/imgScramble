package com.ctrctr.imgscramble;

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
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

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
    Uri selectedImageUri;
    static final int REQUEST_TAKE_PHOTO = 2;
    boolean showpasswordstatus = true;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //prevent keyboard automatically popup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //clipboard
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        //No Options Menu
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

    /**
     * Scramble Tab
     */
    public void scrambleGetImage(View v) {
        //Toast.makeText(this, "Getting Image", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    /**
     * onActivityResult -- Receive the image after it is selected and post in ImageView
     * @param requestCode SELECT_PICTURE - Take from storage, REQUEST_TAKE_PHOTO - Get from camera
     * @param resultCode RESULT_OK - Only accepted otherwise ignore
     * @param data Intent
     */
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

    /**
     * scrambleStart -- Checks for input fields, Make intent
     * @param v View
     */
    public void scrambleStart(View v) {
        //Toast.makeText(this, "Scrambling", Toast.LENGTH_SHORT).show();
        //Image Selected
        if (selected) {
            //ImageUri to String
            //Get all the fields to check
            EditText scramblepassword = (EditText) findViewById(R.id.scramble_password);
            EditText rowtext = (EditText) findViewById(R.id.scramble_row);
            EditText coltext = (EditText) findViewById(R.id.scramble_cols);
            EditText imagewidth = (EditText) findViewById(R.id.scramble_image_width);
            EditText imageheight = (EditText) findViewById(R.id.scramble_image_height);
            RadioButton descrambleradio;
            descrambleradio = (RadioButton) findViewById(R.id.scramble_radio_descramble);
            String scrambleimagewidthstring = imagewidth.getText().toString();
            String scrambleimageheightstring = imageheight.getText().toString();
            Spinner spinner = (Spinner) findViewById(R.id.scramble_option_spinner);
            SeekBar imagequalityseekbar;
            imagequalityseekbar = (SeekBar) findViewById(R.id.scramble_image_quality_seekBar);
            //Maybe Included in Upcomming Feature
            //boolean rowtextfilled = true;
            //boolean coltextfilled = true;
            //boolean scramblepasswordfilled = true;
            int errcounter = 0;
            //If we pick numbers of rows and columns options
            if (spinner.getSelectedItemPosition() == 2) {
                if (rowtext.getText().toString().equals("")) {
                //rowtextfilled = false;
                errcounter++;
            }
                if (coltext.getText().toString().equals("")) {
                //coltextfilled = false;
                errcounter++;
                }
            }
            //Check password filled
            if (scramblepassword.getText().toString().equals("")) {
                //scramblepasswordfilled = false;
                errcounter++;
            }
            if (errcounter > 0) {
                Toast.makeText(this,
                        "Please fill in the appropriate information",
                        Toast.LENGTH_SHORT).show();
            } else {
                //Bundle all the info to the intent
                String imageuristring = selectedImageUri.toString();
                Intent intent = new Intent(this, ScramblingActivity.class);
                Bundle extras = new Bundle();
                extras.putString("EXTRA_IMAGE", imageuristring);
                extras.putString("EXTRA_PASS", scramblepassword.getText().toString());
                extras.putInt("EXTRA_ROW", (rowtext.getText().toString().length() > 0)
                        ? Integer.parseInt(rowtext.getText().toString()) : 0);
                extras.putInt("EXTRA_COL", (coltext.getText().toString().length() > 0)
                        ? Integer.parseInt(coltext.getText().toString()) : 0);
                extras.putString("EXTRA_WIDTH", scrambleimagewidthstring);
                extras.putString("EXTRA_HEIGHT", scrambleimageheightstring);
                extras.putBoolean("EXTRA_SCRAMBLE_RADIO", descrambleradio.isChecked());
                extras.putString("EXTRA_SEEKBAR",
                        Integer.toString(imagequalityseekbar.getProgress()));
                extras.putInt("EXTRA_OPTIONS", spinner.getSelectedItemPosition());
                intent.putExtras(extras);
                startActivity(intent);
            }

        } else {
            //Toast no image selected
            Toast.makeText(this, "Image not yet selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * scrambleRandom -- Randomize the inputs
     * @param v View
     */
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

    /**
     * scrambleCopyPassword -- Copy the password field
     * @param v View
     */
    public void scrambleCopyPassword(View v) {
        EditText scramblepassword = (EditText) findViewById(R.id.scramble_password);
        String cliptext = scramblepassword.getText().toString();
        ClipData clipdata = ClipData.newPlainText("text", cliptext);
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    /**
     * scrambleShowPassword -- Toggle the password field to show the password
     * @param v View
     */
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

    /**
     * openCamera -- Open camera and send the intent
     * @param v View
     */
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

    /**
     * createImageFile -- Save the image from the intent from camera
     * @return File -- Return the image file
     * @throws IOException - If cannot create image file
     */
    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String sdCard;
        sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + "imgScramble" + File.separator + "Source" + File.separator;
        File storageDir = new File(sdCard);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        boolean a = false;
        if (!storageDir.exists()) {
            a = storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        selectedImageUri = Uri.fromFile(image);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(selectedImageUri);
        this.sendBroadcast(mediaScanIntent);
        return image;
    }

    /**
     * scrambleScrambleOnClick -- Turn option to scramble; disable EditText on the Width and Height
     * @param v View
     */
    public void scrambleScrambleOnClick (View v){
        EditText widthedittext = (EditText) findViewById(R.id.scramble_image_width);
        EditText heightedittext = (EditText) findViewById(R.id.scramble_image_height);
        widthedittext.setEnabled(false);
        heightedittext.setEnabled(false);
    }

    /**
     * scrambleDescrambleOnClick -- Turn option to descramble; enable EditText on the
     * Width and Height
     * @param v View
     */
    public void scrambleDescrambleOnClick (View v){
        EditText widthedittext = (EditText) findViewById(R.id.scramble_image_width);
        EditText heightedittext = (EditText) findViewById(R.id.scramble_image_height);
        widthedittext.setEnabled(true);
        heightedittext.setEnabled(true);
    }
}
