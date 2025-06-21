package com.example.metro_app.Activity.Admin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.metro_app.databinding.ActivityAddNewsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddNewsActivity extends AppCompatActivity {
    private static final String TAG = "AddNewsActivity";
    private ActivityAddNewsBinding binding;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private boolean isUploading = false;
    private MaterialButton saveBtn;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.selectedImageView.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(binding.selectedImageView);
                    binding.selectImageBtn.setText("Thay đổi ảnh");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        saveBtn = (MaterialButton) binding.saveBtn;
        // Remove: private CircularProgressIndicator progressIndicator;

        try {
            MediaManager.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ẩn CardView chứa Spinner status
        binding.statusCardView.setVisibility(View.GONE);

        setupListeners();
    }

    private void setupListeners() {
        binding.selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Bold Button
        binding.boldBtn.setOnClickListener(v -> {
            toggleStyle(new StyleSpan(android.graphics.Typeface.BOLD), StyleSpan.class, android.graphics.Typeface.BOLD);
        });

        // Italic Button
        binding.italicBtn.setOnClickListener(v -> {
            toggleStyle(new StyleSpan(android.graphics.Typeface.ITALIC), StyleSpan.class, android.graphics.Typeface.ITALIC);
        });

        // Underline Button
        binding.underlineBtn.setOnClickListener(v -> {
            toggleStyle(new UnderlineSpan(), UnderlineSpan.class, 0);
        });

        binding.saveBtn.setOnClickListener(v -> {
            if (validateInput()) {
                uploadImageAndSaveNews();
            }
        });

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void toggleStyle(Object style, Class<?> styleClass, int styleType) {
        int start = binding.contentEditText.getSelectionStart();
        int end = binding.contentEditText.getSelectionEnd();

        if (start >= 0 && end > start) {
            SpannableString spannable = new SpannableString(binding.contentEditText.getText());
            boolean hasStyle = false;

            Object[] spans = spannable.getSpans(start, end, styleClass);
            for (Object span : spans) {
                if (styleClass == StyleSpan.class) {
                    if (((StyleSpan) span).getStyle() == styleType &&
                            spannable.getSpanStart(span) <= start &&
                            spannable.getSpanEnd(span) >= end) {
                        hasStyle = true;
                        spannable.removeSpan(span);
                    }
                } else if (styleClass == UnderlineSpan.class) {
                    if (spannable.getSpanStart(span) <= start &&
                            spannable.getSpanEnd(span) >= end) {
                        hasStyle = true;
                        spannable.removeSpan(span);
                    }
                }
            }

            if (!hasStyle) {
                spannable.setSpan(style, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            binding.contentEditText.setText(spannable);
            binding.contentEditText.setSelection(start, end);
        } else {
            Toast.makeText(this, "Vui lòng chọn văn bản để áp dụng định dạng", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        String title = binding.titleEditText.getText().toString().trim();
        String content = binding.contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isUploading) {
            Toast.makeText(this, "Đang tải ảnh, vui lòng chờ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showLoadingOnButton(boolean show) {
        if (show) {
            saveBtn.setText("Đang đăng...");
            saveBtn.setIcon(null); // Hide icon while loading
            saveBtn.setEnabled(false);
        } else {
            saveBtn.setText("Đăng bài viết");
            saveBtn.setIconResource(com.example.metro_app.R.drawable.ic_publish);
            saveBtn.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            saveBtn.setEnabled(true);
        }
    }

    private void uploadImageAndSaveNews() {
        isUploading = true;
        showLoadingOnButton(true);
        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        saveNewsToFirestore(imageUrl);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(AddNewsActivity.this, "Tải ảnh thất bại: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        resetUploadState();
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void saveNewsToFirestore(String imageUrl) {
        String title = binding.titleEditText.getText().toString().trim();
        SpannableString contentSpannable = new SpannableString(binding.contentEditText.getText());
        String contentHtml = Html.toHtml(contentSpannable, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new Date());

        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userid = prefs.getString("UserID", null);

        if (userid == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        Map<String, Object> news = new HashMap<>();
        news.put("title", title);
        news.put("date", date);
        news.put("pic", imageUrl);
        news.put("description", contentHtml);
        news.put("userid", userid);
        news.put("status", "Đã xuất bản");

        db.collection("news")
                .add(news)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddNewsActivity.this, "Thêm tin tức thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    resetUploadState();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddNewsActivity.this, "Lưu tin tức thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetUploadState();
                });
    }

    private void resetUploadState() {
        isUploading = false;
        showLoadingOnButton(false);
    }
}