package com.ctrctr.imgscramble;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;


public class ScramblingActivity extends AppCompatActivity {
    ArrayList<Bitmap> imagearray;
    int[] permarray;
    boolean imagearraypresent = false;
    boolean permarraypresent = false;
    boolean scrambleboolean = false; //false when scramble, true when descramble
    int row;
    int col;
    Bitmap outputimage;
    boolean scramblingdone = false;
    Uri imageuri;
    SeekBar seekBar;
    private ClipboardManager clipboard;
    int options;
    int[] copylist;
    String qualityseekbar;

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
        row = extras.getInt("EXTRA_ROW");
        col = extras.getInt("EXTRA_COL");
        String imagewidthstring = extras.getString("EXTRA_WIDTH");
        String imageheightstring = extras.getString("EXTRA_HEIGHT");
        qualityseekbar = extras.getString("EXTRA_SEEKBAR");
        options = extras.getInt("EXTRA_OPTIONS");
        scrambleboolean = extras.getBoolean("EXTRA_SCRAMBLE_RADIO");
        imageuri = Uri.parse(imageuristring);
        LinearLayout layout = (LinearLayout) findViewById(R.id.scrambling_picture_parent_layout);
        layout.setVisibility(View.GONE);

        //Find row and columns
        if (options == 0 || options == 1) {
            int qualityadder = Integer.parseInt(qualityseekbar) * 20;
            if (options == 0) {
                qualityadder = 60;
            }
            int[] rowcol = Algorithms.getrowcol(scramblepassword, qualityadder);
            row = rowcol[0];
            col = rowcol[1];
        }
        SplitImage splitImage = new SplitImage(imageuri, row, col, scrambleboolean,
                imagewidthstring, imageheightstring);
        HashNum hashNum = new HashNum(scramblepassword, row, col);
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
                    row, col);
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
                              boolean scrambleboolean, int row, int col) {
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
            copylist = new int[]{row, col, outputimage.getWidth(), outputimage.getHeight()};
        }
    }

    /**
     * editProgressText -- Handles the loading screen textView
     *
     * @param text - text to be put to the loading string
     */
    public void editProgressText(int text) {
        TextView loadingtext = (TextView) findViewById(R.id.scrambling_loading);
        loadingtext.setText(getString(text));
    }

    /**
     * HashNum -- Runnable syncTask for Hashing
     */
    private class HashNum implements Runnable {
        String password;
        int row, col;

        public HashNum(String scramblepassword, int row, int col){
            this.password = scramblepassword;
            this.row = row;
            this.col = col;
        }

        @Override
        public void run() {
            int[] result = Algorithms.hashNumMethod(password, row, col);
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
        Uri imageuri;
        int row, col;
        boolean scrambleboolean;
        String imagewidthstring, imageheightstring;

        public SplitImage(Uri imageuri, int row, int col, boolean scrambleboolean,
                          String imagewidthstring, String imageheightstring) {
            this.imageuri = imageuri;
            this.row = row;
            this.col = col;
            this.scrambleboolean = scrambleboolean;
            this.imagewidthstring = imagewidthstring;
            this.imageheightstring = imageheightstring;
        }

        @Override
        public void run() {
            try {
                InputStream imageInputStream = getContentResolver().openInputStream(imageuri);
                ArrayList<Bitmap> result = Algorithms.splitImageMethod(row, col, scrambleboolean,
                        imagewidthstring, imageheightstring, imageInputStream);
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
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    /**
     * getFileName -- get the initial file name of the picture
     *
     * @param uri - uri of image
     * @return filename of image
     */
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)){
                    if (cursor != null && cursor.moveToFirst()){
                        result = cursor.getString(cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            else {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
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
     * @return byte array of JPEG image
     */
    public byte[] convertImageToJPEG(Bitmap image, int seekbarprogress) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, seekbarprogress, bytes);
        return bytes.toByteArray();
    }

    /**
     * scramblingShare -- Share image
     *
     * @param v View
     */
    public void scramblingShare(View v) {
        String filename = getFileName(imageuri);
        int seekbarprogress = getSeekBarProgress(seekBar);
        Toast.makeText(this, getString(R.string.scrambling_scramblingShareImage_sharing),
                Toast.LENGTH_SHORT).show();
        ScramblingSaveImageAsyncTask sa = new ScramblingSaveImageAsyncTask(filename,
                outputimage, seekbarprogress, true);
        sa.execute("");
    }

    /**
     * scramblingSaveImage -- Save the current image
     *
     * @param v View
     */
    public void scramblingSaveImage(View v) {
        if (scramblingdone) {
            Toast.makeText(this, getString(R.string.scrambling_scramblingSaveImage_saving),
                    Toast.LENGTH_SHORT).show();
            String filename = getFileName(imageuri);
            int seekbarprogress = getSeekBarProgress(seekBar);
            ScramblingSaveImageAsyncTask sc = new ScramblingSaveImageAsyncTask(filename,
                    outputimage, seekbarprogress, false);
            sc.execute("");
        } else {
            Toast.makeText(this, getString(R.string.scrambling_scramblingSaveImage_not_done), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ScramblingSaveImageAsyncTask -- AsyncTask for saving image
     */
    private class ScramblingSaveImageAsyncTask extends AsyncTask<String, Void, File> {
        String filename;
        Bitmap imageoutput;
        int seekbarprogress;
        boolean share;

        public ScramblingSaveImageAsyncTask(String filename, Bitmap imageoutput,
                                            int seekbarprogress, boolean share) {
            this.filename = filename;
            this.imageoutput = imageoutput;
            this.seekbarprogress = seekbarprogress;
            this.share = share;
        }

        @Override
        protected File doInBackground(String... params) {
            //Filename
            GregorianCalendar today = new GregorianCalendar();
            String datestring = new SimpleDateFormat("yyyy-mm-dd-HH:mm:ss").format(today);
            filename = "imgScramble_ouput_" + datestring + "_q"
                    + getSeekBarProgress(seekBar) + "_"
                    + filename.substring(0, Math.min(filename.length() - 4, 10)) + ".jpg";
            byte[] imagebytearray = convertImageToJPEG(imageoutput, seekbarprogress);
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
            return f;
        }

        @Override
        protected void onPostExecute(File f) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            if (share) {
                //share
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent,
                        getResources().getText(R.string.scrambling_send_to)));
            } else {
                mediaScanIntent.setData(contentUri);
                getApplicationContext().sendBroadcast(mediaScanIntent);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.scrambling_scramblingSaveImage_saved),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * scramblingCopyImage -- Copy image details
     *
     * @param v View
     */
    public void scramblingCopyImage(View v) {
        String copystring;
        String copiedstring;
        switch (options) {
            case 0:
                copystring = Integer.toString(copylist[2]) + " "
                        + Integer.toString(copylist[3]);
                copiedstring = getString(R.string.scrambling_copied_0);
                break;
            case 1:
                String qualitytext = "";
                switch (Integer.parseInt(qualityseekbar)) {
                    case 0:
                        qualitytext = getString(R.string.scramble_option_0);
                        break;
                    case 1:
                        qualitytext = getString(R.string.scramble_option_1);
                        break;
                    case 2:
                        qualitytext = getString(R.string.scramble_option_2);
                        break;
                    case 3:
                        qualitytext = getString(R.string.scramble_option_3);
                        break;
                    case 4:
                        qualitytext = getString(R.string.scramble_option_4);
                        break;
                }
                copystring = qualitytext + " "
                        + Integer.toString(copylist[2]) + " "
                        + Integer.toString(copylist[3]);
                copiedstring = getString(R.string.scrambling_copied_1);
                break;
            case 2:
                copystring = Integer.toString(copylist[0]) + " "
                        + Integer.toString(copylist[1]) + " "
                        + Integer.toString(copylist[2]) + " "
                        + Integer.toString(copylist[3]);
                copiedstring = getString(R.string.scrambling_copied_2);
                break;
            default:
                copystring = Integer.toString(copylist[0]) + " "
                        + Integer.toString(copylist[1]) + " "
                        + Integer.toString(copylist[2]) + " "
                        + Integer.toString(copylist[3]);
                copiedstring = getString(R.string.scrambling_copied_2);
                break;
        }
        ClipData clipdata = ClipData.newPlainText("text", copystring);
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, copiedstring,
                Toast.LENGTH_SHORT).show();
    }

}
