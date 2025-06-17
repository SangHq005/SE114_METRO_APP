package com.example.metro_app.Activity.Admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.metro_app.Model.Station;
import com.example.metro_app.R;
import com.example.metro_app.utils.FireStoreHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AdStationBottomSheet extends BottomSheetDialogFragment {
    private Station station;
    private String docId;

    public static AdStationBottomSheet newInstance(Station station, String docId) {
        AdStationBottomSheet fragment = new AdStationBottomSheet();
        Bundle args = new Bundle();

        args.putSerializable("station", station);
        args.putString("docId", docId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            station = (Station) getArguments().getSerializable("station");
            docId = getArguments().getString("docId");
        }
    }
    private OnStationUpdatedListener updateListener;

    public interface OnStationUpdatedListener {
        void onStationUpdated();
    }

    public AdStationBottomSheet(Station station) {
        this.station = station;
    }


    public AdStationBottomSheet(Station station, String docId) {
        this.station = station;
        this.docId = docId;
    }


    public AdStationBottomSheet() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStationUpdatedListener) {
            updateListener = (OnStationUpdatedListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_bottom_sheet_station, container, false);

        EditText etName = view.findViewById(R.id.etAdminStationName);
        EditText etLat = view.findViewById(R.id.etAdminStationLat);
        EditText etLng = view.findViewById(R.id.etAdminStationLng);
        EditText etWard = view.findViewById(R.id.etAdminStationWard);
        EditText etZone = view.findViewById(R.id.etAdminStationZone);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        if (station != null) {
            etName.setText(station.Name != null ? station.Name : "");
            etLat.setText(String.valueOf(station.Lat));
            etLng.setText(String.valueOf(station.Lng));
            etWard.setText(station.Ward != null ? station.Ward : "");
            etZone.setText(station.Zone != null ? station.Zone : "");
        } else {
            Toast.makeText(getContext(), "Không có dữ liệu trạm!", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        btnDelete.setOnClickListener(v -> {
            if (station.StopId != 0) {
                FireStoreHelper.deleteStation(station.StopId, new FireStoreHelper.DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Xoá trạm thành công", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(getContext(), "Lỗi khi xoá: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                FireStoreHelper.removePointFromRoute(docId, station.Lat, station.Lng, new FireStoreHelper.DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Xoá điểm thành công", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(getContext(), "Lỗi khi xoá điểm: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });


        applyFieldUpdater(etName, "Name");
        applyFieldUpdater(etWard, "Ward");
        applyFieldUpdater(etZone, "Zone");
        applyLatLngUpdater(etLat, etLng);
        return view;
    }

    private void applyFieldUpdater(EditText editText, String fieldName) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateField(editText, fieldName);
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                hideKeyboard(editText);
                updateField(editText, fieldName);
                return true;
            }
            return false;
        });
    }

    private void updateField(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) return;

        boolean changed = false;

        switch (fieldName) {
            case "Name":
                if (!value.equals(station.Name)) {
                    station.Name = value;
                    changed = true;
                }
                break;
            case "Ward":
                if (!value.equals(station.Ward)) {
                    station.Ward = value;
                    changed = true;
                }
                break;
            case "Zone":
                if (!value.equals(station.Zone)) {
                    station.Zone = value;
                    changed = true;
                }
                break;
        }

        if (changed) {
            if (station.StopId == 0) {
                station.StopId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                FireStoreHelper.addStation(station, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã thêm trạm mới", Toast.LENGTH_SHORT).show();
                        if (updateListener != null) updateListener.onStationUpdated();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thêm trạm", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                FireStoreHelper.updateStation(station, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã cập nhật " + fieldName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi cập nhật " + fieldName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void applyLatLngUpdater(EditText etLat, EditText etLng) {
        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if (!hasFocus) updateLatLng(etLat, etLng);
        };

        TextView.OnEditorActionListener actionListener = (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateLatLng(etLat, etLng);
                return true;
            }
            return false;
        };

        etLat.setOnFocusChangeListener(listener);
        etLng.setOnFocusChangeListener(listener);
        etLat.setOnEditorActionListener(actionListener);
        etLng.setOnEditorActionListener(actionListener);
    }

    private void updateLatLng(EditText etLat, EditText etLng) {
        try {
            double newLat = Double.parseDouble(etLat.getText().toString());
            double newLng = Double.parseDouble(etLng.getText().toString());

            if (newLat != station.Lat || newLng != station.Lng) {
                station.Lat = newLat;
                station.Lng = newLng;

                if (station.StopId == 0) {
                    station.StopId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                    FireStoreHelper.addStation(station, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã thêm trạm mới (tọa độ)", Toast.LENGTH_SHORT).show();
                            if (updateListener != null) updateListener.onStationUpdated();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi thêm trạm", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    FireStoreHelper.updateStation(station, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã cập nhật tọa độ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi cập nhật tọa độ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Tọa độ không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard(View view) {
        if (view != null && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateListener != null) {
            updateListener.onStationUpdated();
        }
    }
}
