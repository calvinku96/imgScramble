package com.example.calvin.imgscramble;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;


public class ScramblingActivity extends ActionBarActivity {
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrambling);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String imageuristring = extras.getString("EXTRA_IMAGE");
        String scramblepassword = extras.getString("EXTRA_PASS");
        String rowstring = extras.getString("EXTRA_ROW");
        String colstring = extras.getString("EXTRA_COL");
        String scramblestring = extras.getString("EXTRA_SCRAMBLE");
        Uri imageuri = Uri.parse(imageuristring);
        //ImageView image = (ImageView)findViewById(R.id.imageView);
        //image.setImageURI(imageuri);
        new SplitImage().execute(imageuristring, rowstring, colstring);
        new HashNum().execute(scramblepassword, rowstring, colstring);
    }

    private class HashNum extends AsyncTask<String, Void, int[]> {
        @Override
        protected int[] doInBackground(String... params) {
            String password = params[0];
            int row = Integer.parseInt(params[1]);
            int col = Integer.parseInt(params[2]);
            int n = row * col;
            //Generates the lehmer code in base-16 using hashing functions.
            //Calculate the number of digits we should have
            //Then calculate n! using the Factorial Class
            //If the string is greater than n!, then find some way to reproducibly reduce the string
            //Give out string as a hex representation of BigInteger
            Factorial fact = new Factorial(0);
            int numdigits = fact.factorial(n).toString(16).length();
            //Give initial hash
            byte[] hash = password.getBytes(StandardCharsets.UTF_8);
            hash = getHashMultiple(2000, hash);
            String output = bytesToHex(hash);
            //Find how long is the lehmer code
            int multiplier = (numdigits / 64);
            for (int e = 0; e < multiplier; e++) {
                int num = Integer.parseInt(output.substring(output.length() - 1), 16);
                num = num + 1;
                //Hash it num times
                hash = getHashMultiple(num, hash);
                //Add it to the output
                output = output + bytesToHex(hash);
            }
            output = output.substring(0, numdigits - 1);

            //Lehmer
            BigInteger d = new BigInteger (output,16);
            int[] code = encode(n,d);
            int[] perm = ffs2seq(n,code);
            return perm;
        }

        @Override
        protected void onPostExecute(int[] outputs) {
            TextView text = (TextView)findViewById(R.id.scrambling_progress_dialog);
            text.setText(Arrays.toString(outputs));
        }
    }

    private class SplitImage extends AsyncTask<String, Void, ArrayList<Bitmap>> {
        @Override
        protected void onPreExecute() {
            TextView text = (TextView) findViewById(R.id.scrambling_progress_dialog);
            text.setText("In Progress...");
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(String... params) {
            Uri imageuri = Uri.parse(params[0]);
            int row = Integer.parseInt(params[1]);
            int col = Integer.parseInt(params[2]);
            //Store all the image chunks
            ArrayList<Bitmap> chunkedimages = new ArrayList<Bitmap>(row * col);
            //Convert Uri to Bitmap
            try {
                InputStream imageInputStream = getContentResolver().openInputStream(imageuri);
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageInputStream, false);
                int chunkHeight = decoder.getHeight() / row;
                int chunkWidth = decoder.getWidth() / col;

                int yCoord = 0;
                for (int x = 0; x < row; x++) {
                    int xCoord = 0;
                    for (int y = 0; y < col; y++) {
                        Bitmap tempimage = decoder.decodeRegion(new Rect(xCoord, yCoord, xCoord + chunkWidth, yCoord + chunkHeight), null);
                        chunkedimages.add(tempimage);
                        xCoord += chunkWidth;
                    }
                    yCoord += chunkHeight;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return chunkedimages;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> chunkedimages) {
            //Pass the chunkedimages to the scramble image AsyncTask
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrambling, menu);
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

    //Methods for HasHnum

    public static byte[] getHashMultiple(int mul, byte[] pass) {
        for (int i = 0; i < mul; i++) {
            pass = getHash(pass);
        }
        return pass;
    }

    public static byte[] getHash(byte[] pass) {
        //Do as SHA-256 hash once
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            return digest.digest(pass);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        //Convert byte[] array from getHash to hex String
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //Lehmer methods
    public static int[] encode(int n, BigInteger d) {
        //Turn the lehmer code d into the falling factorial sequence of number
        Factorial fact = new Factorial(n);
        int[] ans = new int[n];
        for (int i = n - 1; i >= 0; i--) {
            BigInteger fi = fact.factorial(i);
            BigInteger[] ansarr = d.divideAndRemainder(fi);
            ans[n - 1 - i] = ansarr[0].intValue();
            d = ansarr[1];
        }
        return ans;
    }

    public static int[] ffs2seq(int n, int[] input) {
        //Convert the falling factorial sequence to the combination sequence
        for (int i = n - 2; i >= 0; i--) {
            for (int j = i + 1; j < n; j++) {
                if (input[j] >= input[i]) {
                    input[j] = input[j] + 1;
                }
            }
        }
        return input;
    }

}
