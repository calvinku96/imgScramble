package com.example.calvin.imgscramble;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;


public class ScramblingActivity extends ActionBarActivity {
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    ArrayList<Bitmap> imagearray;
    int[] permarray;
    boolean imagearraypresent = false;
    boolean permarraypresent = false;
    boolean scrambleboolean = false; //false when scramble, true when descramble
    String rowstring;
    String colstring;
    Bitmap outputimage;
    boolean scramblingdone = false;
    Uri imageuri;
    SeekBar seekBar;
    private ClipboardManager clipboard;
    private ClipData clipdata;
    String copystring;
    String optionstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrambling);
        // Init seekBar
        TextView seekBartext = (TextView) findViewById(R.id.scrambling_quality_text);
        seekBar = (SeekBar) findViewById(R.id.scrambling_quality_seekBar);
        seekBar.setProgress(9);
        seekBartext.setText(getString(R.string.scrambling_quality) + getSeekBarProgress(seekBar));
        //seekBar Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                TextView seekBartext = (TextView) findViewById(R.id.scrambling_quality_text);
                seekBartext.setText(getString(R.string.scrambling_quality)
                        + getSeekBarProgress(seekBar));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Receive the intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String imageuristring = extras.getString("EXTRA_IMAGE");
        String scramblepassword = extras.getString("EXTRA_PASS");
        rowstring = extras.getString("EXTRA_ROW");
        colstring = extras.getString("EXTRA_COL");
        String scramblestring = extras.getString("EXTRA_SCRAMBLE");
        String imagewidthstring = extras.getString("EXTRA_WIDTH");
        String imageheightstring = extras.getString("EXTRA_HEIGHT");
        optionstring = extras.getString("EXTRA_OPTIONS");
        if (scramblestring.equals("d")) {
            scrambleboolean = true;
        }
        imageuri = Uri.parse(imageuristring);
        LinearLayout layout = (LinearLayout) findViewById(R.id.scrambling_picture_parent_layout);
        layout.setVisibility(View.GONE);
        String[] splitimageinput = new String[]{imageuristring, rowstring, colstring,
                scramblestring, imagewidthstring, imageheightstring};
        SplitImage splitImage = new SplitImage(splitimageinput);
        String[] hashnuminput = new String[]{scramblepassword, rowstring, colstring};
        HashNum hashNum = new HashNum(hashnuminput);
        new Thread(splitImage).start();
        new Thread(hashNum).start();

        //clipboard
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    /**
     * getSeekBarProgress -- Get the progress of the seekBar in terms of the quality
     *
     * @param seekBar - the SeekBar Object
     * @return - integer 5 - 100 for the quality of the image
     */
    public int getSeekBarProgress(SeekBar seekBar) {
        return (seekBar.getProgress() + 1) * 5;
    }

    /**
     * finishHashSplit -- Called when permutation was generated and image split
     * Rearrange image and put it back together
     */
    public void finishHashSplit() {
        if (imagearraypresent && permarraypresent) {
            RearrangeImage re = new RearrangeImage(imagearray, permarray, scrambleboolean,
                    Integer.parseInt(rowstring), Integer.parseInt(colstring));
            editProgressText(R.string.scrambling_rearrange_image);
            new Thread(re).start();
        }
    }

    /**
     * RearrangeImage -- Runnable to Rearrange image
     */
    private class RearrangeImage implements Runnable {
        ArrayList<Bitmap> imagearray;
        int[] permarray;
        boolean scramblebool;
        int row;
        int col;

        public RearrangeImage(ArrayList<Bitmap> imagearray, int[] permarray,
                              boolean scrambleboolen, int row, int col) {
            //init
            this.imagearray = imagearray;
            this.permarray = permarray;
            this.scramblebool = scrambleboolean;
            this.row = row;
            this.col = col;
        }

        @Override
        public void run() {
            //Run
            Bitmap result = Algorithms.rearrangeImageMethod(imagearray, permarray,
                    scramblebool, row, col);
            PostRearrangeImage postRearrangeImage = new PostRearrangeImage(result);
            runOnUiThread(postRearrangeImage);
        }
    }

    private class PostRearrangeImage implements Runnable {
        Bitmap bitmapoutput;

        public PostRearrangeImage(Bitmap bitmapoutput) {
            this.bitmapoutput = bitmapoutput;
        }

        @Override
        public void run() {
            ImageView image = (ImageView) findViewById(R.id.scrambling_output_image);
            LinearLayout layout;
            layout = (LinearLayout) findViewById(R.id.scrambling_picture_parent_layout);
            layout.setVisibility(View.VISIBLE);
            image.setImageBitmap(bitmapoutput);
            outputimage = bitmapoutput;
            scramblingdone = true;
            RelativeLayout progresslayout;
            progresslayout = (RelativeLayout) findViewById(R.id.scrambling_loading_layout);
            progresslayout.setVisibility(View.GONE);
            TextView widthheight = (TextView) findViewById(R.id.scrambling_width_height);
            widthheight.setText(getString(R.string.scrambling_widthheight_text)
                    + outputimage.getWidth() + " "
                    + getString(R.string.scrambling_widthheight_text2) + " "
                    + outputimage.getHeight() + " "
                    + getString(R.string.scrambling_widthheight_text3));
            copystring = rowstring + " "
                    + colstring + " "
                    + Integer.toString(outputimage.getWidth()) + " "
                    + Integer.toString(outputimage.getHeight());
        }
    }

    /**
     * editProgressText -- Handles the loading screen textView
     *
     * @param text
     */
    public void editProgressText(int text) {
        TextView loadingtext = (TextView) findViewById(R.id.scrambling_loading);
        loadingtext.setText(getString(text));
    }

    /**
     * HashNum -- Runnable syncTask for Hashing
     */
    private class HashNum implements Runnable {
        String[] params;

        public HashNum(String[] params) {
            this.params = params;
        }

        @Override
        public void run() {
            int[] result = Algorithms.hashNumMethod(params);
            PostHashNum postHashNum = new PostHashNum(result);
            runOnUiThread(postHashNum);
        }
    }

    private class PostHashNum implements Runnable {
        int[] outputs;

        public PostHashNum(int[] outputs) {
            this.outputs = outputs;
        }

        @Override
        public void run() {
            permarray = outputs;
            permarraypresent = true;
            finishHashSplit();
        }
    }

    /**
     * SplitImage -- Runnable to split image
     */
    private class SplitImage implements Runnable {
        String[] params;

        public SplitImage(String[] params) {
            this.params = params;
        }

        @Override
        public void run() {
            imageuri = Uri.parse(params[0]);
            try {
                InputStream imageInputStream = getContentResolver().openInputStream(imageuri);
                ArrayList<Bitmap> result = Algorithms.splitImageMethod(params, imageInputStream);
                PostSplitImage postSplitImage = new PostSplitImage(result);
                runOnUiThread(postSplitImage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class PostSplitImage implements Runnable {
        ArrayList<Bitmap> output;

        public PostSplitImage(ArrayList<Bitmap> output) {
            this.output = output;
        }

        @Override
        public void run() {
            //Pass the chunkedimages to the scramble image AsyncTask
            imagearray = output;
            imagearraypresent = true;
            editProgressText(R.string.scrambling_split_image_finish);
            finishHashSplit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_scrambling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    /**
     * getFileName -- get the initial file name of the picture
     *
     * @param uri
     * @return
     */
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * convertImageToJPEG -- Convert image to JPEG
     *
     * @return
     */
    public byte[] convertImageToJPEG() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        outputimage.compress(Bitmap.CompressFormat.JPEG, getSeekBarProgress(seekBar), bytes);
        byte[] imagebytearray = bytes.toByteArray();
        return imagebytearray;
    }

    /**
     * scramblingShare -- Share image
     *
     * @param v
     */
    public void scramblingShare(View v) {
        byte[] imagebytearray = convertImageToJPEG();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imagebytearray);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.scrambling_send_to)));
    }

    /**
     * scramblingSaveImage -- Save the current image
     *
     * @param v
     */
    public void scramblingSaveImage(View v) {
        if (scramblingdone) {
            //Filename
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            String datestring = today.format("%Y-%m-%d-%H:%M:%S");
            String filename = getFileName(imageuri);
            filename = "imgScramble_text_" + datestring + "_q"
                    + getSeekBarProgress(seekBar) + "_"
                    + filename.substring(0, filename.length() - 4) + ".jpg";
            byte[] imagebytearray = convertImageToJPEG();
            //save image
            String sdCard;
            sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/imgScramble/";
            File newdir = new File(sdCard);
            if (!newdir.exists()) {
                newdir.mkdirs();
            }
            File f = new File(sdCard, filename);
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(imagebytearray);
                fo.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            Toast.makeText(this, getString(R.string.scrambling_scramblingSaveImage_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.scrambling_scramblingSaveImage_not_done), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * scramblingCopyImage -- Copy image details
     *
     * @param v
     */
    public void scramblingCopyImage(View v) {
        clipdata = ClipData.newPlainText("text", copystring);
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, getString(R.string.scrambling_copied),
                Toast.LENGTH_SHORT).show();
    }

}
