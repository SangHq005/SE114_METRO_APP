package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.metro_app.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.barcodes.BarcodeQRCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CreateTicketActivity extends AppCompatActivity {

    private static final String TAG = "CreateTicketActivity";
    private FirebaseFirestore db;
    private String userId;
    private Spinner typeSpinner, validitySpinner, startStationSpinner, endStationSpinner;
    private TextView validityLabel, startStationLabel, endStationLabel;
    private Button issueTicketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_ticket);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d(TAG, "Retrieved userId from SharedPreferences: " + userId);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        typeSpinner = findViewById(R.id.typeSpinner);
        validitySpinner = findViewById(R.id.validitySpinner);
        startStationSpinner = findViewById(R.id.startStationSpinner);
        endStationSpinner = findViewById(R.id.endStationSpinner);
        validityLabel = findViewById(R.id.validityLabel);
        startStationLabel = findViewById(R.id.startStationLabel);
        endStationLabel = findViewById(R.id.endStationLabel);
        issueTicketButton = findViewById(R.id.issueTicketButton);

        // Load Type vào typeSpinner
        loadTicketTypes();

        // Xử lý sự kiện chọn Type
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Selected Type: " + selectedType);

                // Ẩn tất cả Spinner phụ
                validitySpinner.setVisibility(View.GONE);
                validityLabel.setVisibility(View.GONE);
                startStationSpinner.setVisibility(View.GONE);
                startStationLabel.setVisibility(View.GONE);
                endStationSpinner.setVisibility(View.GONE);
                endStationLabel.setVisibility(View.GONE);

                if ("Vé dài hạn".equals(selectedType)) {
                    // Hiển thị Spinner thời hạn vé
                    validitySpinner.setVisibility(View.VISIBLE);
                    validityLabel.setVisibility(View.VISIBLE);
                    loadValidityOptions();
                } else if ("Vé lượt".equals(selectedType)) {
                    // Hiển thị Spinner ga đi và ga đến
                    startStationSpinner.setVisibility(View.VISIBLE);
                    startStationLabel.setVisibility(View.VISIBLE);
                    endStationSpinner.setVisibility(View.VISIBLE);
                    endStationLabel.setVisibility(View.VISIBLE);
                    loadStartStations();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
            }
        });

        // Xử lý sự kiện chọn ga đi
        startStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStartStation = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Selected StartStation: " + selectedStartStation);
                loadEndStations(selectedStartStation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
            }
        });

        // Sự kiện nút xuất vé
        issueTicketButton.setOnClickListener(v -> {
            String selectedType = (String) typeSpinner.getSelectedItem();
            if (selectedType == null) {
                Toast.makeText(CreateTicketActivity.this, "Vui lòng chọn loại vé!", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("Vé dài hạn".equals(selectedType)) {
                String selectedName = (String) validitySpinner.getSelectedItem();
                if (selectedName == null) {
                    Toast.makeText(CreateTicketActivity.this, "Vui lòng chọn thời hạn vé!", Toast.LENGTH_SHORT).show();
                    return;
                }
                issueLongTermTicket(selectedName);
            } else if ("Vé lượt".equals(selectedType)) {
                String selectedStartStation = (String) startStationSpinner.getSelectedItem();
                String selectedEndStation = (String) endStationSpinner.getSelectedItem();
                if (selectedStartStation == null || selectedEndStation == null) {
                    Toast.makeText(CreateTicketActivity.this, "Vui lòng chọn ga đi và ga đến!", Toast.LENGTH_SHORT).show();
                    return;
                }
                issueSingleTripTicket(selectedStartStation, selectedEndStation);
            }
        });
    }

    private void loadTicketTypes() {
        db.collection("TicketType")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> typeSet = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String type = document.getString("Type");
                            if (type != null) {
                                typeSet.add(type);
                            }
                        }
                        List<String> typeList = new ArrayList<>(typeSet);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateTicketActivity.this,
                                android.R.layout.simple_spinner_item, typeList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        typeSpinner.setAdapter(adapter);
                        Log.d(TAG, "Loaded ticket types: " + typeList);
                    } else {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi tải loại vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error loading ticket types: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void loadValidityOptions() {
        db.collection("TicketType")
                .whereEqualTo("Type", "Vé dài hạn")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> nameList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("Name");
                            if (name != null) {
                                nameList.add(name);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateTicketActivity.this,
                                android.R.layout.simple_spinner_item, nameList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        validitySpinner.setAdapter(adapter);
                        Log.d(TAG, "Loaded validity options: " + nameList);
                    } else {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi tải thời hạn vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error loading validity options: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void loadStartStations() {
        db.collection("TicketType")
                .whereEqualTo("Type", "Vé lượt")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> startStationSet = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String startStation = document.getString("StartStation");
                            if (startStation != null) {
                                startStationSet.add(startStation);
                            }
                        }
                        List<String> startStationList = new ArrayList<>(startStationSet);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateTicketActivity.this,
                                android.R.layout.simple_spinner_item, startStationList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        startStationSpinner.setAdapter(adapter);
                        Log.d(TAG, "Loaded start stations: " + startStationList);

                        // Load EndStation cho StartStation đầu tiên (nếu có)
                        if (!startStationList.isEmpty()) {
                            loadEndStations(startStationList.get(0));
                        }
                    } else {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi tải ga đi: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error loading start stations: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void loadEndStations(String selectedStartStation) {
        db.collection("TicketType")
                .whereEqualTo("Type", "Vé lượt")
                .whereEqualTo("StartStation", selectedStartStation)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> endStationList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String endStation = document.getString("EndStation");
                            if (endStation != null) {
                                endStationList.add(endStation);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateTicketActivity.this,
                                android.R.layout.simple_spinner_item, endStationList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        endStationSpinner.setAdapter(adapter);
                        Log.d(TAG, "Loaded end stations for " + selectedStartStation + ": " + endStationList);
                    } else {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi tải ga đến: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error loading end stations: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void issueLongTermTicket(String selectedName) {
        getTicketTypeId("Vé dài hạn", selectedName, null, null, (ticketTypeId, ticketTypeData) -> {
            if (ticketTypeId == null) {
                Toast.makeText(CreateTicketActivity.this, "Không tìm thấy loại vé!", Toast.LENGTH_LONG).show();
                return;
            }
            generateTicket(ticketTypeId, ticketTypeData);
        });
    }

    private void issueSingleTripTicket(String selectedStartStation, String selectedEndStation) {
        getTicketTypeId("Vé lượt", null, selectedStartStation, selectedEndStation, (ticketTypeId, ticketTypeData) -> {
            if (ticketTypeId == null) {
                Toast.makeText(CreateTicketActivity.this, "Không tìm thấy loại vé!", Toast.LENGTH_LONG).show();
                return;
            }
            generateTicket(ticketTypeId, ticketTypeData);
        });
    }

    private void getTicketTypeId(String type, String name, String startStation, String endStation,
                                 TicketTypeCallback callback) {
        var query = db.collection("TicketType").whereEqualTo("Type", type);
        if (type.equals("Vé dài hạn") && name != null) {
            query = query.whereEqualTo("Name", name);
        } else if (type.equals("Vé lượt") && startStation != null && endStation != null) {
            query = query.whereEqualTo("StartStation", startStation).whereEqualTo("EndStation", endStation);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    callback.onResult(null, null);
                    return;
                }
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> ticketTypeData = new HashMap<>();
                    ticketTypeData.put("Name", document.getString("Name"));
                    ticketTypeData.put("Active", document.get("Active"));
                    ticketTypeData.put("AutoActive", document.get("AutoActive"));
                    ticketTypeData.put("StartStation", document.getString("StartStation"));
                    ticketTypeData.put("EndStation", document.getString("EndStation"));
                    callback.onResult(document.getId(), ticketTypeData);
                    return;
                }
            } else {
                Toast.makeText(CreateTicketActivity.this, "Lỗi truy vấn loại vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error querying TicketType: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                callback.onResult(null, null);
            }
        });
    }

    private void generateTicket(String ticketTypeId, Map<String, Object> ticketTypeData) {
        generateTicketCode((ticketCode) -> {
            if (ticketCode == null) {
                Toast.makeText(CreateTicketActivity.this, "Lỗi tạo mã vé!", Toast.LENGTH_LONG).show();
                return;
            }

            Date timestamp = new Date();
            long autoActiveDays = parseNumber(ticketTypeData.get("AutoActive"), 0);
            long activeDays = parseNumber(ticketTypeData.get("Active"), 0);

            // Tính AutoActiveDate
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timestamp);
            calendar.add(Calendar.DAY_OF_YEAR, (int) autoActiveDays);
            Date autoActiveDate = calendar.getTime();

            // Tính ExpirationDate
            calendar.setTime(autoActiveDate);
            calendar.add(Calendar.DAY_OF_YEAR, (int) activeDays);
            Date expirationDate = calendar.getTime();

            // Tạo vé mới
            String ticketId = UUID.randomUUID().toString();
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("ticketCode", ticketCode);
            ticketData.put("Status", "Chưa kích hoạt");
            ticketData.put("ticketTypeId", ticketTypeId);
            ticketData.put("timestamp", timestamp);
            ticketData.put("AutoActiveDate", autoActiveDate);
            ticketData.put("ExpirationDate", expirationDate);

            // Lưu vé vào Firestore
            db.collection("Ticket")
                    .document(ticketId)
                    .set(ticketData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Created ticket with ID: " + ticketId);
                        generatePDF(ticketData, ticketTypeData);
                        Toast.makeText(CreateTicketActivity.this, "Tạo vé thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi tạo vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error creating ticket: " + e.getMessage());
                    });
        });
    }

    private long parseNumber(Object value, long defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing number: " + e.getMessage());
            }
        }
        return defaultValue;
    }

    private void generateTicketCode(TicketCodeCallback callback) {
        DocumentReference counterRef = db.collection("Metadata").document("ticketCounter");
        db.runTransaction(transaction -> {
            var snapshot = transaction.get(counterRef);
            long lastTicketCode = snapshot.exists() && snapshot.contains("lastTicketCode")
                    ? snapshot.getLong("lastTicketCode")
                    : 0;
            String newTicketCode = String.format("%06d", lastTicketCode + 1);
            transaction.update(counterRef, "lastTicketCode", lastTicketCode + 1);
            return newTicketCode;
        }).addOnSuccessListener(newTicketCode -> {
            Log.d(TAG, "Generated ticket code: " + newTicketCode);
            callback.onResult(newTicketCode);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error generating ticket code: " + e.getMessage());
            callback.onResult(null);
        });
    }

    private void generatePDF(Map<String, Object> ticketData, Map<String, Object> ticketTypeData) {
        String ticketCode = (String) ticketData.get("ticketCode");
        String ticketTypeName = (String) ticketTypeData.get("Name");
        String startStation = (String) ticketTypeData.get("StartStation");
        String endStation = (String) ticketTypeData.get("EndStation");
        Object active = ticketTypeData.get("Active");
        Object autoActive = ticketTypeData.get("AutoActive");

        String activeStr = active != null ? String.valueOf(active) : "0";
        String autoActiveStr = autoActive != null ? String.valueOf(autoActive) : "0";

        boolean isSingleTrip = startStation != null && endStation != null;

        try {
            // Tạo thư mục tickets trong Internal Storage
            File ticketsDir = new File(getFilesDir(), "tickets");
            if (!ticketsDir.exists()) {
                ticketsDir.mkdirs();
            }

            // Tạo file PDF
            File pdfFile = new File(ticketsDir, "ticket_" + ticketCode + ".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A5);

            // Nhúng font hỗ trợ tiếng Việt
            byte[] fontBytes = readFontFromAssets("fonts/SVN-Times New Roman.ttf");
            PdfFont font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);

            // Tiêu đề
            Paragraph title = new Paragraph("VÉ TÀU METRO")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Thông tin vé (căn giữa)
            document.add(new Paragraph("Loại vé: " + ticketTypeName)
                    .setTextAlignment(TextAlignment.CENTER));
            if (isSingleTrip) {
                document.add(new Paragraph("Ga đi: " + startStation)
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Ga đến: " + endStation)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            document.add(new Paragraph("HSD: " + activeStr + " ngày sau khi kích hoạt")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Lưu ý: Tự động kích hoạt sau " + autoActiveStr + " ngày")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Mã vé: " + ticketCode)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Tạo mã QR
            BarcodeQRCode qrCode = new BarcodeQRCode(ticketCode);
            Image qrImage = new Image(qrCode.createFormXObject(pdfDoc));
            qrImage.setWidth(100);
            qrImage.setHeight(100);
            qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(qrImage);

            document.close();
            writer.close();

            Log.d(TAG, "Generated PDF file at: " + pdfFile.getAbsolutePath());
            Toast.makeText(this, "Đã tạo file PDF: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();

            // Mở file PDF
            openPDF(pdfFile);
        } catch (IOException e) {
            Log.e(TAG, "Error creating PDF file: " + e.getMessage());
            Toast.makeText(this, "Lỗi tạo file PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPDF(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(this, "com.example.metro_app.fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
            Toast.makeText(this, "Không thể mở file PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private interface TicketTypeCallback {
        void onResult(String ticketTypeId, Map<String, Object> ticketTypeData);
    }

    private interface TicketCodeCallback {
        void onResult(String ticketCode);
    }
    private byte[] readFontFromAssets(String fileName) throws IOException {
        try (AssetManager.AssetInputStream inputStream = (AssetManager.AssetInputStream) getAssets().open(fileName)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
}