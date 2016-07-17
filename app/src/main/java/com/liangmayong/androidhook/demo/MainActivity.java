package com.liangmayong.androidhook.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.liangmayong.androidhook.demo.patchs.DemoPatch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        demo();
    }

    private void demo() {
        DemoPatch patch = new DemoPatch(new DemoImpl());
        Log.d("TAG", patch.getProxyObject().getCount() + "");
    }
}
