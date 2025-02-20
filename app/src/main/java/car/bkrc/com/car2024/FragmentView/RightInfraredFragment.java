package car.bkrc.com.car2024.FragmentView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import car.bkrc.com.car2024.R;
import car.bkrc.com.car2024.ViewAdapter.InfrareAdapter;
import car.bkrc.com.car2024.ViewAdapter.Infrared_Landmark;

/**
 * 红外通信标志物控制页面
 */
public class RightInfraredFragment extends Fragment {

    public static final String TAG = "RightInfraredFragment";
    private Infrared_Landmark[]  infrareds = {
            new Infrared_Landmark("智能报警台标志物", R.mipmap.alarm),
            new Infrared_Landmark("智能路灯标志物", R.mipmap.gear_position),
            new Infrared_Landmark("智能立体显示标志物", R.mipmap.stereo_display)
    };

    private List<Infrared_Landmark> InfraredList = new ArrayList<>();
    private static RightInfraredFragment mInstance =null;

    public  static RightInfraredFragment getInstance() {
        if(mInstance ==null) {
            synchronized (RightInfraredFragment.class) {
                if(mInstance ==null) {
                    mInstance =new RightInfraredFragment();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.right_infrared_fragment, container, false);
        initInfrared();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        InfrareAdapter adapter = new InfrareAdapter(InfraredList, getActivity());
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void initInfrared() {
        InfraredList.clear();
        for (int i = 0; i < infrareds.length; i++) {
            InfraredList.add(infrareds[i]);
        }
    }

}
