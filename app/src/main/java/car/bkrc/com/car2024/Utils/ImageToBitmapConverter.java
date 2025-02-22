package car.bkrc.com.car2024.Utils;

import android.graphics.*;
import android.media.Image;
import android.renderscript.*;
import car.bkrc.com.car2024.ViewAdapter.InfrareAdapter;

import java.nio.ByteBuffer;

public class ImageToBitmapConverter {
    private static final String TAG = "ImageToBitmapConverter";

    public static Bitmap yuvToBitmap(Image image) {


        if (image == null) return null;


        int W = image.getWidth();
        int H = image.getHeight();


        Image.Plane Y = image.getPlanes()[0];
        Image.Plane U = image.getPlanes()[1];
        Image.Plane V = image.getPlanes()[2];


        int Yb = Y.getBuffer().remaining();
        int Ub = U.getBuffer().remaining();
        int Vb = V.getBuffer().remaining();


        byte[] data = new byte[Yb + Ub + Vb];


        Y.getBuffer().get(data, 0, Yb);
        V.getBuffer().get(data, Yb, Vb);
        U.getBuffer().get(data, Yb + Vb, Ub);


        RenderScript rs = RenderScript.create(InfrareAdapter.context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));


        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);


        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(W).setY(H);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);


        final Bitmap bmpout = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);


        in.copyFromUnchecked(data);


        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        out.copyTo(bmpout);

        return bmpout;
    }

    public static Bitmap jpegToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        int W = image.getWidth();
        int H = image.getHeight();
        Bitmap inBitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inBitmap = inBitmap;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
    }
}
