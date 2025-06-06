package com.example.metro_app.Activity.User;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class StationBottomSheet extends BottomSheetDialogFragment {
    private Station station;

    public StationBottomSheet(Station station) {
        this.station = station;
    }
    public interface OnGotoClickListener {
        void onGotoClicked(String Name, double lat, double lng);
    }
    private OnGotoClickListener gotoClickListener;

    public StationBottomSheet(Station station, OnGotoClickListener listener) {
        this.station = station;
        this.gotoClickListener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_station, container, false);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtZone = view.findViewById(R.id.txtZone);
        TextView txtStreet = view.findViewById(R.id.txtStreet);
        TextView txtWard = view.findViewById(R.id.txtWard);
        ImageView ivGoto = view.findViewById(R.id.ivGoto);

        txtName.setText("Tên trạm: " + station.Name);
        txtZone.setText("Khu vực: " + station.Zone);
        txtStreet.setText("Đường: " + station.Street);
        txtWard.setText("Phường: " + station.Ward);
        ivGoto.setOnClickListener(v -> {
            Context context = getContext();
            if (context != null) {
                Intent intent = new Intent(context, FindPathActivity.class);
                intent.putExtra("goto_name", station.Name);
                intent.putExtra("goto_lat", station.Lat);
                intent.putExtra("goto_lng", station.Lng);
                startActivity(intent);
                dismiss();
            }
        });


        return view;
    }

}
