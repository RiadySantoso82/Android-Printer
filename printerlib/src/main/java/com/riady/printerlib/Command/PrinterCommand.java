package com.riady.printerlib.Command;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.riady.printerlib.Common;
import com.riady.printerlib.Service.BluetoothService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Hashtable;

public class PrinterCommand {
    private static int[] p0 = new int[]{0, 128};
    private static int[] p1 = new int[]{0, 64};
    private static int[] p2 = new int[]{0, 32};
    private static int[] p3 = new int[]{0, 16};
    private static int[] p4 = new int[]{0, 8};
    private static int[] p5 = new int[]{0, 4};
    private static int[] p6 = new int[]{0, 2};

    public static void SendDataString(Context context, String content) {
        if (Common.mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, "Printer not Connected", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        byte[] bytes = POS_Print_Text(content,"GBK",0,0,0,0);
        SendDataByte(context, bytes);
    }

    public static void SendDataByte(Context context,byte[] data) {
        if (Common.mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, "not Connected To Printer", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Common.mService.write(data);
    }

    public static void PrintPDF147Code(Context context, String content, int sizeW, int sizeH) {
        if (Common.mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, "not Connected To Printer", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.PDF_417,sizeW,sizeH);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            byte[] data = POS_PrintBarcode(bitmap, sizeW, sizeH, 0, 0);
            SendDataByte(context,data);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    public static byte[] POS_Print_Text(String pszString, String encoding, int codepage,
                                        int nWidthTimes, int nHeightTimes, int nFontType) {
        if (codepage < 0 || codepage > 255 || pszString == null || "".equals(pszString) || pszString.length() < 1) {
            return null;
        }

        byte[] pbString = null;
        try {
            pbString = pszString.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        byte[] intToWidth = {0x00, 0x10, 0x20, 0x30};
        byte[] intToHeight = {0x00, 0x01, 0x02, 0x03};
        byte[] gsExclamationMark = Arrays.copyOf(Command.GS_ExclamationMark, Command.GS_ExclamationMark.length);
        gsExclamationMark[2] = (byte) (intToWidth[nWidthTimes] + intToHeight[nHeightTimes]);
        byte[] escT = Arrays.copyOf(Command.ESC_t, Command.ESC_t.length);
        escT[2] = (byte) codepage;
        byte[] escM = Arrays.copyOf(Command.ESC_M, Command.ESC_M.length);
        escM[2] = (byte) nFontType;
        byte[] data = null;
        if (codepage == 0) {
            data = concatAll(gsExclamationMark, escT, Command.FS_and, escM, pbString);
        } else {
            data = concatAll(gsExclamationMark, escT, Command.FS_dot, escM, pbString);
        }
        return data;
    }

    public static byte[] POS_PrintBarcode(Bitmap mBitmap, int nWidth, int nHeight, int nMode, int leftPadding) {
        int width = ((nWidth + 7) / 8) * 8;
        int height = ((nHeight + 7) / 8) * 8;
        int left = leftPadding == 0 ? 0 : ((leftPadding+7) / 8) * 8;

        Bitmap rszBitmap = mBitmap;
        if (mBitmap.getWidth() != width) {
            rszBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, true);
        }
        Bitmap grayBitmap = toGrayscale(rszBitmap);
        if(left>0){
            grayBitmap = pad(grayBitmap,left,0);
        }
        byte[] dithered = thresholdToBWPic(grayBitmap);
        byte[] data = eachLinePixToCmd(dithered, width+left, nMode);

        return data;
    }

    public static byte[] POS_PrintBMP(Bitmap mBitmap, int nWidth, int nMode, int leftPadding) {
        int width = ((nWidth + 7) / 8) * 8;
        int height = mBitmap.getHeight() * width / mBitmap.getWidth();
        height = ((height + 7) / 8) * 8;
        int left = leftPadding == 0 ? 0 : ((leftPadding+7) / 8) * 8;

        Bitmap rszBitmap = mBitmap;
        if (mBitmap.getWidth() != width) {
            rszBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, true);
        }
        Bitmap grayBitmap = toGrayscale(rszBitmap);
        if(left>0){
            grayBitmap = pad(grayBitmap,left,0);
        }
        byte[] dithered = thresholdToBWPic(grayBitmap);
        byte[] data = eachLinePixToCmd(dithered, width+left, nMode);

        return data;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap pad(Bitmap Src, int padding_x, int padding_y) {
        Bitmap outputimage = Bitmap.createBitmap(Src.getWidth() + padding_x,Src.getHeight() + padding_y, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(outputimage);
        can.drawARGB(255,255,255,255); //This represents White color
        can.drawBitmap(Src, padding_x, padding_y, null);
        return outputimage;
    }

    public static byte[] thresholdToBWPic(Bitmap mBitmap) {
        int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
        byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
        mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        format_K_threshold(pixels, mBitmap.getWidth(), mBitmap.getHeight(), data);
        return data;
    }

    private static void format_K_threshold(int[] orgpixels, int xsize, int ysize, byte[] despixels) {
        int graytotal = 0;
        boolean grayave = true;
        int k = 0;

        int i;
        int j;
        int gray;
        for (i = 0; i < ysize; ++i) {
            for (j = 0; j < xsize; ++j) {
                gray = orgpixels[k] & 255;
                graytotal += gray;
                ++k;
            }
        }

        int var10 = graytotal / ysize / xsize;
        k = 0;

        for (i = 0; i < ysize; ++i) {
            for (j = 0; j < xsize; ++j) {
                gray = orgpixels[k] & 255;
                if (gray > var10) {
                    despixels[k] = 0;
                } else {
                    despixels[k] = 1;
                }

                ++k;
            }
        }

    }

    public static byte[] eachLinePixToCmd(byte[] src, int nWidth, int nMode) {
        int nHeight = src.length / nWidth;
        int nBytesPerLine = nWidth / 8;
        byte[] data = new byte[nHeight * (8 + nBytesPerLine)];
        boolean offset = false;
        int k = 0;

        for (int i = 0; i < nHeight; ++i) {
            int var10 = i * (8 + nBytesPerLine);
            //GS v 0 m xL xH yL yH d1....dk 打印光栅位图
            data[var10 + 0] = 29;//GS
            data[var10 + 1] = 118;//v
            data[var10 + 2] = 48;//0
            data[var10 + 3] = (byte) (nMode & 1);
            data[var10 + 4] = (byte) (nBytesPerLine % 256);//xL
            data[var10 + 5] = (byte) (nBytesPerLine / 256);//xH
            data[var10 + 6] = 1;//yL
            data[var10 + 7] = 0;//yH

            for (int j = 0; j < nBytesPerLine; ++j) {
                data[var10 + 8 + j] = (byte) (p0[src[k]] + p1[src[k + 1]] + p2[src[k + 2]] + p3[src[k + 3]] + p4[src[k + 4]] + p5[src[k + 5]] + p6[src[k + 6]] + src[k + 7]);
                k += 8;
            }
        }

        return data;
    }

    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
