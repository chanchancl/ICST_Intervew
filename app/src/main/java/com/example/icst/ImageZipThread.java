package com.example.icst;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by 大杨编 on 2016/9/4.
 */
public class ImageZipThread extends Thread {
    private Context context;
    private Handler handler;
    private List<Student> students;

    public ImageZipThread(Context context, Handler handler, List<Student> students) {
        this.context = context;
        this.handler = handler;
        this.students = students;
    }

    @Override
    public void run() {
        for (int i = 0; i < students.size(); i++) {
            String filePath = students.get(i).getPhoto();
            Message message = new Message();
            message.what = 1;
            message.arg1 = i;
            if (filePath != null && !filePath.isEmpty()) {
                message.obj = compressImage(BitmapFactory.decodeFile(filePath), 100f, 100f, 10);
            } else {
                message.obj = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_student);
            }
            handler.sendMessage(message);
        }
    }

    public static Bitmap compressImage(Bitmap image, float hh, float ww, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); //初始化
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos); //统一压缩并获取baos
        int quality = 10240 / baos.toByteArray().length;
        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        if (size == 1000) return bitmap;
        baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);
    }

}
