package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

import java.util.List;

/**
 * Created by DEV002 on 2016/3/25.
 */
public class MyAdapter extends HorizontalAdapter {


    public MyAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position) {
        RadioButton rbtn = new RadioButton(context.getApplicationContext());
        rbtn.setText(array.get(position));
        rbtn.setId(position);
        return rbtn;
    }

    @Override
    public void setData(List<String> str) {
        for (String s : str) {
            array.add(s);
        }
    }


}
