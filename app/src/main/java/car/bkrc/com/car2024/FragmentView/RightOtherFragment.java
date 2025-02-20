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
import car.bkrc.com.car2024.ViewAdapter.OtherAdapter;
import car.bkrc.com.car2024.ViewAdapter.Other_Landmark;

/**
 * 更多控制页面
 */
public class RightOtherFragment extends Fragment {

    private List<Other_Landmark> otherList = new ArrayList<Other_Landmark>();
    Context minstance =null;

    public static RightOtherFragment getInstance()
    {
        return RightZigbeeHolder.mInstance;
    }

    private static class RightZigbeeHolder
    {
        @SuppressLint("StaticFieldLeak")
        private static final RightOtherFragment mInstance =new RightOtherFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        minstance =getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_other_fragment, container, false);
        initFruits();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        OtherAdapter adapter = new OtherAdapter(otherList,getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initFruits() {
        otherList.clear();
        Other_Landmark banana = new Other_Landmark("图像检测与识别", R.mipmap.qr_crice);
        otherList.add(banana);
        Other_Landmark mainCamera = new Other_Landmark("流媒体摄像头调节", R.mipmap.new_camera_and_old);
        otherList.add(mainCamera);
        Other_Landmark robotCamera = new Other_Landmark("智能视觉摄像头俯仰调节", R.mipmap.vga_camera);
        otherList.add(robotCamera);
        Other_Landmark orange = new Other_Landmark("蜂鸣器控制", R.mipmap.buzzer);
        otherList.add(orange);
        Other_Landmark watermelon = new Other_Landmark("转向灯控制", R.mipmap.light);
        otherList.add(watermelon);
    }
}

