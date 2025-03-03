package car.bkrc.com.car2024;

import org.opencv.core.Scalar;

public class HSVbeen {
    private int H=0;
    private int S=0;
    private int V=0;
    public int getH() {
        return H;
    }

    public void setH(int h) {
        H = h;
    }

    public int getS() {
        return S;
    }

    public void setS(int s) {
        S = s;
    }

    public int getV() {
        return V;
    }

    public void setV(int v) {
        V = v;
    }

    public Scalar get(){
        return new Scalar(H,S,V);
    }

    @Override
    public String toString() {
        return "HSVbeen{" +
                "H=" + H +
                ", S=" + S +
                ", V=" + V +
                '}';
    }
}
