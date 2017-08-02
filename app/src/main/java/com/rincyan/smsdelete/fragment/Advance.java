package com.rincyan.smsdelete.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.FragmentControl;

import java.util.ArrayList;

/**
 * Created by rin on 2017/08/01.
 *
 */

public class Advance extends Fragment {
    private ListView advance_method;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> methodData;
    private Advance_regex advance_regex;
    private Advance_other advance_other;
    private FragmentManager fragmentManager;
    private FragmentControl fragmentControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_advance, container, false);
        advance_method = view.findViewById(R.id.advancedmethod);
        fragmentManager = getActivity().getSupportFragmentManager();
        getActivity().setTitle("高级选项");
        fragmentControl = (FragmentControl) getActivity().getApplicationContext();
        fragmentControl.setFabIconCancle();
        fragmentControl.set_fragment_name("高级选项");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        final FragmentControl fragmentControl = (FragmentControl) getActivity().getApplicationContext();
        methodData = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getMethod());
        advance_method.setAdapter(adapter);
        advance_method.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        advance_regex = new Advance_regex();
                        fragmentControl.set_fragment_name("正则表达式模式");
                        fragmentControl.setFragment(advance_regex);
                        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, advance_regex).commit();
                        break;
                    default:
                        advance_other = new Advance_other();
                        fragmentControl.set_fragment_name("其他模式");
                        fragmentControl.setFragment(advance_other);
                        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, advance_other).commit();
                        break;
                }
            }
        });
        super.onActivityCreated(savedInstanceState);
    }
    
    private ArrayList<String> getMethod(){
        methodData.clear();
        methodData.add("正则表达式模式");
        methodData.add("其他模式");
        return methodData;
    }
}
