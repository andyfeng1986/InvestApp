package com.investigatorsapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.investigatorsapp.R;
import com.investigatorsapp.activity.StoreListActivity;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreFragmentOld extends Fragment implements View.OnClickListener{

    private Button commitListBtn;
    private Button unCommitLitBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);
        commitListBtn = (Button) view.findViewById(R.id.commitListBtn);
        unCommitLitBtn = (Button) view.findViewById(R.id.unCommitListBtn);
        commitListBtn.setOnClickListener(this);
        unCommitLitBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(commitListBtn)) {
            Intent intent = new Intent(getActivity(), StoreListActivity.class);
            intent.putExtra("uncommit", false);
            startActivity(intent);
        }else if(v.equals(unCommitLitBtn)) {
            Intent intent = new Intent(getActivity(), StoreListActivity.class);
            intent.putExtra("uncommit", true);
            startActivity(intent);
        }
    }

}
