package com.ctrctr.imgscramble;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

public class Algorithms {
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    //Methods hashes

    /**
     * getHashMultiple -- calls the getHash function mul times
     *
     * @param mul  - times to hash
     * @param pass - password
     * @return byte array of hash
     */
    public static byte[] getHashMultiple(int mul, byte[] pass) {
        for (int i = 0; i < mul; i++) {
            pass = getHash(pass);
        }
        return pass;
    }

    /**
     * getHash -- do SHA-256 hash
     *
     * @param pass - password
     * @return byte array of the SHA-256 hash
     */
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

    /**
     * byteToHex -- turn byte[] to Hex String
     *
     * @param bytes byte array of hash
     * @return String - Hex String
     */
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

    /**
     * encode -- Turn the lehmer code d into the falling factorial sequence of numbers
     *
     * @param n - length of sequence
     * @param d - Lehmer Code
     * @return int[] - falling factorial sequence
     */
    public static int[] encode(int n, BigInteger d) {
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

    /**
     * ffs2seq -- Convert the falling factorial sequence to the combination sequence
     *
     * @param n     - length of sequence
     * @param input - falling factorial sequence
     * @return int[] falling factorial sequence
     */
    public static int[] ffs2seq(int n, int[] input) {
        for (int i = n - 2; i >= 0; i--) {
            for (int j = i + 1; j < n; j++) {
                if (input[j] >= input[i]) {
                    input[j] = input[j] + 1;
                }
            }
        }
        return input;
    }


    /**
     * Methods for RearrangeImage Runnable
     */

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

    /**
     * Methods for HashNum Runnable
     */
    public static int[] hashNumMethod(String password, int row, int col) {
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
        return ffs2seq(n, code);
    }

    /**
     * Methods for SplitImage
     */
    public static ArrayList<Bitmap> splitImageMethod(int row, int col, boolean scrambleboolean,
                                                     String imagewidthstring,
                                                     String imageheightstring,
                                                     InputStream imageInputStream) {
        //Store all the image chunks
        ArrayList<Bitmap> chunkedimages = new ArrayList<>(row * col);
        if (!(imagewidthstring.equals("") && imageheightstring.equals("")) &&
                scrambleboolean) {
            try {
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageInputStream,
                        false);
                Matrix matrixtranslate = new Matrix();
                matrixtranslate.postTranslate(0f, 0f);
                int chunkWidth = Integer.parseInt(imagewidthstring) / col;
                int chunkHeight = Integer.parseInt(imageheightstring) / row;
                Bitmap wholeimage = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(),
                        decoder.getHeight()), null);
                Bitmap scaledimage = Bitmap.createScaledBitmap(wholeimage,
                        Integer.parseInt(imagewidthstring),
                        Integer.parseInt(imageheightstring), false);

                int yCoord = 0;
                for (int y = 0; y < row; y++) {
                    int xCoord = 0;
                    for (int x = 0; x < col; x++) {
                        Bitmap tempimage = Bitmap.createBitmap(scaledimage, xCoord, yCoord,
                                chunkWidth, chunkHeight);
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
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageInputStream,
                        false);
                int largerwidth = (int) Math.ceil((double) decoder.getWidth() / (double) col) * col;
                int largerheight = (int) Math.ceil((double) decoder.getHeight() / (double) row) * row;

                int chunkHeight = largerheight / row;
                int chunkWidth = largerwidth / col;

                Bitmap originalbitmap = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(),
                        decoder.getHeight()), null);

                Bitmap newbitmap = Bitmap.createBitmap(largerwidth, largerheight,
                        Bitmap.Config.ARGB_8888);
                Canvas newcanvas = new Canvas(newbitmap);
                newcanvas.drawBitmap(originalbitmap, 0f, 0f, null);

                int yCoord = 0;
                for (int y = 0; y < row; y++) {
                    int xCoord = 0;
                    for (int x = 0; x < col; x++) {
                        Bitmap tempimage = Bitmap.createBitmap(newbitmap, xCoord, yCoord,
                                chunkWidth, chunkHeight);
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

    public static int autoadd(String intstring) {
        int num = Integer.parseInt(intstring, 16);
        if (num <= 4) {
            return 1;
        } else if (num <= 8) {
            return 2;
        } else if (num <= 12) {
            return 3;
        } else if (num <= 16) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * getrowcol -- method for option 0 and 1
     *
     * @param password password
     * @param addwidth addition by seekBar/password
     * @return row and col
     */
    public static int[] getrowcol(String password, int addwidth) {
        byte[] passbyte;
        try {
            passbyte = password.getBytes("UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        String hexpass = bytesToHex(getHash(passbyte));
        int row = Integer.parseInt(hexpass.substring(0, 1), 16) + autoadd(hexpass.substring(1, 2));
        int col = Integer.parseInt(hexpass.substring(2, 3), 16) + autoadd(hexpass.substring(3, 4));
        return new int[]{row + addwidth, col + addwidth};
    }
}
