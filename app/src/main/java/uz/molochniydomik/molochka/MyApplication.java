package uz.molochniydomik.molochka;

import android.app.Application;

public class MyApplication extends Application {

    private String userName;
    private String orderSum;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrderSum() {
        return orderSum;
    }

    public void setOrderSum(String orderSum) {
        this.orderSum = orderSum;
    }
}
