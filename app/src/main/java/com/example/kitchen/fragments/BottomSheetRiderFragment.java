package com.example.kitchen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.kitchen.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mTAG;

    public static BottomSheetRiderFragment newInstance(String tag) {
        BottomSheetRiderFragment fragment = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("TAG", tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTAG = getArguments().getString("TAG");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottem_sheet_rider, container, false);
        return view;
    }
}
