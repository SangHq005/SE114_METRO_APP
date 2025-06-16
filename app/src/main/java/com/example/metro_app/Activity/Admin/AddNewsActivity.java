package com.example.metro_app.Activity.Admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
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
    private boolean isBoldEnabled = false;
    private boolean isItalicEnabled = false;
    private boolean isUnderlineEnabled = false;
    private boolean isTextChanging = false;

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

        try {
            MediaManager.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupListeners();
        setupTextWatcher();
    }

    private void setupListeners() {
        binding.selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Toggle Button Đậm
        binding.boldBtn.setOnClickListener(v -> {
            isBoldEnabled = !isBoldEnabled;
            updateButtonState(binding.boldBtn, isBoldEnabled);
            applySelectedOrNewStyles();
            Log.d(TAG, "Bold toggled to: " + isBoldEnabled);
        });

        // Toggle Button Nghiêng
        binding.italicBtn.setOnClickListener(v -> {
            isItalicEnabled = !isItalicEnabled;
            updateButtonState(binding.italicBtn, isItalicEnabled);
            applySelectedOrNewStyles();
            Log.d(TAG, "Italic toggled to: " + isItalicEnabled);
        });

        // Toggle Button Gạch chân
        binding.underlineBtn.setOnClickListener(v -> {
            isUnderlineEnabled = !isUnderlineEnabled;
            updateButtonState(binding.underlineBtn, isUnderlineEnabled);
            applySelectedOrNewStyles();
            Log.d(TAG, "Underline toggled to: " + isUnderlineEnabled);
        });

        binding.saveBtn.setOnClickListener(v -> {
            if (validateInput()) {
                uploadImageAndSaveNews();
            }
        });

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupTextWatcher() {
        binding.contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 && !isTextChanging) {
                    isTextChanging = true;
                    applyCurrentStylesToNewText(start, count);
                    isTextChanging = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateButtonState(Button button, boolean isEnabled) {
        button.setEnabled(!isEnabled);
        button.setBackgroundColor(isEnabled ? getResources().getColor(android.R.color.holo_blue_light) : getResources().getColor(android.R.color.white));
        button.setTextColor(isEnabled ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.black));
    }

    private void applySelectedOrNewStyles() {
        int start = binding.contentEditText.getSelectionStart();
        int end = binding.contentEditText.getSelectionEnd();
        int length = binding.contentEditText.getText().length();
        if (start == end) { // Không có văn bản được chọn, áp dụng từ con trỏ
            isTextChanging = true;
            SpannableString spannable = new SpannableString(binding.contentEditText.getText());
            if (!isBoldEnabled && !isItalicEnabled && !isUnderlineEnabled) {
                // Khi toggle off tất cả, xóa định dạng từ con trỏ đến cuối
                applyStyles(spannable, start, length);
            } else {
                applyStyles(spannable, start, length);
            }
            binding.contentEditText.setText(spannable);
            binding.contentEditText.setSelection(start); // Giữ con trỏ tại vị trí hiện tại
            isTextChanging = false;
        } else if (start >= 0 && end > start) { // Có văn bản được chọn
            isTextChanging = true;
            SpannableString spannable = new SpannableString(binding.contentEditText.getText());
            applyStyles(spannable, start, end);
            binding.contentEditText.setText(spannable);
            binding.contentEditText.setSelection(end);
            isTextChanging = false;
        }
    }

    private void applyCurrentStyles() {
        int start = binding.contentEditText.getSelectionStart();
        int end = binding.contentEditText.getSelectionEnd();
        if (start >= 0 && end > start) {
            isTextChanging = true;
            SpannableString spannable = new SpannableString(binding.contentEditText.getText());
            applyStyles(spannable, start, end);
            binding.contentEditText.setText(spannable);
            binding.contentEditText.setSelection(end);
            isTextChanging = false;
        }
    }

    private void applyCurrentStylesToNewText(int start, int count) {
        int end = start + count;
        SpannableString spannable = new SpannableString(binding.contentEditText.getText());
        applyStyles(spannable, start, end);
        binding.contentEditText.setText(spannable);
        binding.contentEditText.setSelection(end);
    }

    private void applyStyles(SpannableString spannable, int start, int end) {
        Log.d(TAG, "Applying styles: start=" + start + ", end=" + end + ", bold=" + isBoldEnabled + ", italic=" + isItalicEnabled + ", underline=" + isUnderlineEnabled);
        // Xóa định dạng cũ trong phạm vi từ start đến end
        StyleSpan[] boldSpans = spannable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : boldSpans) spannable.removeSpan(span);
        UnderlineSpan[] underlineSpans = spannable.getSpans(start, end, UnderlineSpan.class);
        for (UnderlineSpan span : underlineSpans) spannable.removeSpan(span);

        // Áp dụng hoặc hủy định dạng dựa trên trạng thái toggle
        if (isBoldEnabled) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (isItalicEnabled) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (isUnderlineEnabled) {
            spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    private void uploadImageAndSaveNews() {
        isUploading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveBtn.setEnabled(false);

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

        Map<String, Object> news = new HashMap<>();
        news.put("title", title);
        news.put("date", date);
        news.put("pic", imageUrl);
        news.put("description", contentHtml);

        db.collection("news")
                .add(news)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddNewsActivity.this, "Thêm tin tức thành công", Toast.LENGTH_SHORT).show();
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
        binding.progressBar.setVisibility(View.GONE);
        binding.saveBtn.setEnabled(true);
    }
}