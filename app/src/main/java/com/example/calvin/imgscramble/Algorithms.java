package com.example.calvin.imgscramble;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * Created by calvin on 6/5/15.
 */
public class Algorithms {
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    //Methods hashes
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

    // Methods for Lehmer
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


    // Methods for RearrangeImage AsyncTask
    public static Bitmap rearrangeImageMethod(ArrayList<Bitmap> imagearray, int[] permarray, boolean scrambleboolean, int row, int col) {
        Bitmap[] rearrangedimagearray = new Bitmap[imagearray.size()];
        if (scrambleboolean) {
            //find inverse permutation
            int[] invpermarray = new int[permarray.length];
            for (int i = 0; i < permarray.length; i++) {
                invpermarray[permarray[i]] = i;
            }
            permarray = invpermarray;
        }
        //rearrange
        for (int i = 0; i < imagearray.size(); i++) {
            rearrangedimagearray[permarray[i]] = imagearray.get(i);
        }
        //Combine all into one
        int indivWidth = rearrangedimagearray[0].getWidth();
        int indivHeight = rearrangedimagearray[0].getHeight();
        int totalWidth = col * indivWidth;
        int totalHeight = row * indivHeight;

        Bitmap temp = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        int yCoord = 0;
        for (int y = 0; y < row; y++) {
            int xCoord = 0;
            for (int x = 0; x < col; x++) {
                // Add to canvas
                canvas.drawBitmap(rearrangedimagearray[x + y * col], (float) xCoord, (float) yCoord, null);
                xCoord += indivWidth;
            }
            yCoord += indivHeight;
        }
        return temp;
    }

    //Methods for HashNum AsyncTask
    public static int[] hashNumMethod(String[] params) {
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
        byte[] hash;
        try {
            hash = password.getBytes("UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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
        BigInteger d = new BigInteger(output, 16);
        int[] code = encode(n, d);
        int[] perm = ffs2seq(n, code);
        return perm;
    }

    // Methods for SplitImage
    public static ArrayList<Bitmap> splitImageMethod (String[] params, InputStream imageInputStream){
        Uri imageuri = Uri.parse(params[0]);
            int row = Integer.parseInt(params[1]);
            int col = Integer.parseInt(params[2]);
            //Store all the image chunks
            ArrayList<Bitmap> chunkedimages = new ArrayList<Bitmap>(row * col);
            //Convert Uri to Bitmap
        if (!(params[4].equals("") && params[5].equals(""))&& params[3].equals("d")) {
            try {
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageInputStream, false);
                int chunkWidth = Integer.parseInt(params[4]) / col;
                int chunkHeight = Integer.parseInt(params[5]) / row;
                Bitmap wholeimage = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(), decoder.getHeight()), null);
                Bitmap scaledimage = Bitmap.createScaledBitmap(wholeimage, Integer.parseInt(params[4]), Integer.parseInt(params[5]), false);

                int yCoord = 0;
                for (int y = 0; y < row; y++) {
                    int xCoord = 0;
                    for (int x = 0; x < col; x++) {
                        Bitmap tempimage = Bitmap.createBitmap(scaledimage, xCoord, yCoord, chunkWidth, chunkHeight);
                        chunkedimages.add(tempimage);
                        xCoord += chunkWidth;
                    }
                    yCoord += chunkHeight;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageInputStream, false);
                int chunkHeight = decoder.getHeight() / row;
                int chunkWidth = decoder.getWidth() / col;

                int yCoord = 0;
                for (int y = 0; y < row; y++) {
                    int xCoord = 0;
                    for (int x = 0; x < col; x++) {
                        Bitmap tempimage = decoder.decodeRegion(new Rect(xCoord, yCoord, xCoord + chunkWidth, yCoord + chunkHeight), null);
                        chunkedimages.add(tempimage);
                        xCoord += chunkWidth;
                    }
                    yCoord += chunkHeight;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }
        return chunkedimages;
    }
}
