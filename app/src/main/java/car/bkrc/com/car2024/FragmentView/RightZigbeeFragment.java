package car.bkrc.com.car2024.FragmentView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.ViewAdapter.ZigbeeAdapter;
import car.bkrc.com.car2024.ViewAdapter.Zigbee_Landmark;

/**
 * zigbee通信的标志物控制页面
 */
public class RightZigbeeFragment extends Fragment {

    private final List<Zigbee_Landmark> ZigbeeList = new ArrayList<>();
    Context minstance = null;

    public static RightZigbeeFragment getInstance() {
        return RightZigbeeHolder.mInstance;
    }

    private static class RightZigbeeHolder {
        @SuppressLint("StaticFieldLeak")
        private static final RightZigbeeFragment mInstance = new RightZigbeeFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        minstance = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_zigbee_fragment, container, false);
        initZigbees();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        ZigbeeAdapter adapter = new ZigbeeAdapter(ZigbeeList, getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initZigbees() {
        ZigbeeList.clear();
        Zigbee_Landmark apple = new Zigbee_Landmark("智能道闸标志物", R.mipmap.barrier_gate);
        ZigbeeList.add(apple);
        Zigbee_Landmark banana = new Zigbee_Landmark("智能显示标志物", R.mipmap.led_display);
        ZigbeeList.add(banana);
        Zigbee_Landmark orange = new Zigbee_Landmark("智能公交站标志物", R.mipmap.voice_bus_station);
        ZigbeeList.add(orange);
        Zigbee_Landmark watermelon = new Zigbee_Landmark("智能无线充电标志物", R.mipmap.maglev);
        ZigbeeList.add(watermelon);
        Zigbee_Landmark pear_A = new Zigbee_Landmark("多功能信息显示标志物", R.mipmap.tft_lcd);
        ZigbeeList.add(pear_A);
        Zigbee_Landmark traffic_light_A = new Zigbee_Landmark("智能交通信号灯标志物", R.mipmap.traffic_light);
        ZigbeeList.add(traffic_light_A);
        Zigbee_Landmark stereo_garage_A = new Zigbee_Landmark("智能立体车库标志物", R.mipmap.cheku);
        ZigbeeList.add(stereo_garage_A);
        Zigbee_Landmark etc_A = new Zigbee_Landmark("智能ETC系统标志物", R.mipmap.etc_pic);
        ZigbeeList.add(etc_A);
    }

}
