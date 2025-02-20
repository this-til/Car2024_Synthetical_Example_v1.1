package car.bkrc.com.car2024.MessageBean;

public class DataRefreshBean {

    /**
     * eventbus广播是否刷新数据的接口
     */
    private int refreshState;

    public DataRefreshBean(int refreshState){
        this.refreshState = refreshState;
    }

    public int getRefreshState() {
        return refreshState;
    }

    public void setRefreshState(int refreshState) {
        this.refreshState = refreshState;
    }
}
