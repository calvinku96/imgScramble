package com.example.calvin.imgscramble;

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
import android.widget.EditText;
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
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                TextView seekBartext = (TextView) findViewById(R.id.scrambling_quality_text);
                seekBartext.setText(getString(R.string.scrambling_quality) + getSeekBarProgress(seekBar));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Intent
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
        new SplitImage().execute(imageuristring, rowstring, colstring, scramblestring, imagewidthstring, imageheightstring);
        new HashNum().execute(scramblepassword, rowstring, colstring);

        //clipboard
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    public int getSeekBarProgress(SeekBar seekBar) {
        return (seekBar.getProgress() + 1) * 5;
    }

    public void finishHashSplit() {
        if (imagearraypresent && permarraypresent) {
            RearrangeImage re = new RearrangeImage(imagearray, permarray, scrambleboolean, Integer.parseInt(rowstring), Integer.parseInt(colstring));
            editProgressText(R.string.scrambling_rearrange_image);
            re.execute("");
        }
    }

    private class RearrangeImage extends AsyncTask<String, Void, Bitmap> {
        ArrayList<Bitmap> imagearray;
        int[] permarray;
        boolean scrambleboolean;
        int row;
        int col;

        public RearrangeImage(ArrayList<Bitmap> imagearray, int[] permarray, boolean scrambleboolean, int row, int col) {
            this.imagearray = imagearray;
            this.permarray = permarray;
            this.scrambleboolean = scrambleboolean;
            this.row = row;
            this.col = col;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return Algorithms.rearrangeImageMethod(imagearray, permarray, scrambleboolean, row, col);
        }

        @Override
        protected void onPostExecute(Bitmap bitmapoutput) {
            ImageView image = (ImageView) findViewById(R.id.scrambling_output_image);
            LinearLayout layout = (LinearLayout) findViewById(R.id.scrambling_picture_parent_layout);
            layout.setVisibility(View.VISIBLE);
            image.setImageBitmap(bitmapoutput);
            outputimage = bitmapoutput;
            scramblingdone = true;
            RelativeLayout progresslayout = (RelativeLayout) findViewById(R.id.scrambling_loading_layout);
            progresslayout.setVisibility(View.GONE);
            TextView widthheight = (TextView)findViewById(R.id.scrambling_width_height);
            widthheight.setText(getString(R.string.scrambling_widthheight_text)+outputimage.getWidth()+" "+getString(R.string.scrambling_widthheight_text2)+" "+outputimage.getHeight()+" "+getString(R.string.scrambling_widthheight_text3));
            copystring = rowstring + " " + colstring + " " + Integer.toString(outputimage.getWidth()) + " " + Integer.toString(outputimage.getHeight());
        }
    }


    public void editProgressText(int text) {
        TextView loadingtext = (TextView) findViewById(R.id.scrambling_loading);
        loadingtext.setText(getString(text));
    }

    private class HashNum extends AsyncTask<String, Void, int[]> {
        @Override
        protected void onPreExecute() {
            editProgressText(R.string.scrambling_get_permutation);
        }

        @Override
        protected int[] doInBackground(String... params) {
            return Algorithms.hashNumMethod(params);
        }

        @Override
        protected void onPostExecute(int[] outputs) {
            permarray = outputs;
            permarraypresent = true;
            finishHashSplit();
        }
    }

    private class SplitImage extends AsyncTask<String, Void, ArrayList<Bitmap>> {
        @Override
        protected void onPreExecute() {
            editProgressText(R.string.scrambling_split_image);
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(String... params) {
            imageuri = Uri.parse(params[0]);
            try {
                InputStream imageInputStream = getContentResolver().openInputStream(imageuri);
                return Algorithms.splitImageMethod(params, imageInputStream);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> output) {
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

    public byte[] convertImageToJPEG() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        outputimage.compress(Bitmap.CompressFormat.JPEG, getSeekBarProgress(seekBar), bytes);
        byte[] imagebytearray = bytes.toByteArray();
        return imagebytearray;
    }

    public void scramblingShare(View v) {
        byte[] imagebytearray = convertImageToJPEG();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imagebytearray);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.scrambling_send_to)));
    }

    /*
    public void scramblingSaveText(View v) {
        byte[] imagebytearray = convertImageToJPEG();
        String base64 = Base64.encodeToString(imagebytearray, Base64.DEFAULT);
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        String datestring = today.format("%Y-%m%-d-%H:%M:%S");
        String filename = getFileName(imageuri);
        filename = "imgScramble_text_" + datestring + "_q" + getSeekBarProgress(seekBar)+ "_" + filename.substring(0, filename.length() - 4)  + ".txt";
        String sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/imgScramble/";
        File newdir = new File(sdCard);
        if (!newdir.exists()) {
            newdir.mkdirs();
        }
        File f = new File(sdCard, filename);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(base64.getBytes());
            fo.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Toast.makeText(this, getString(R.string.scrambling_scramblingSaveText_saved), Toast.LENGTH_SHORT).show();
    }
    */

    public void scramblingSaveImage(View v) {
        if (scramblingdone) {
            //Filename
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            String datestring = today.format("%Y-%m-%d-%H:%M:%S");
            String filename = getFileName(imageuri);
            filename = "imgScramble_text_" + datestring + "_q" + getSeekBarProgress(seekBar)+ "_" + filename.substring(0, filename.length() - 4) + ".jpg";
            byte[] imagebytearray = convertImageToJPEG();
            //save image
            String sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/imgScramble/";
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
    public void scramblingCopyImage(View v){
        clipdata = ClipData.newPlainText("text", copystring);
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, getString(R.string.scrambling_copied), Toast.LENGTH_SHORT).show();
    }

}
