package com.service.fileservice.controller;

import com.service.fileservice.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Принимаем файл через multipart/form-data
    // @RequestParam("file") — имя поля в форме
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Файл не выбран"));
        }

        try {
            String url = fileService.uploadImage(file);
            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "message", "Файл успешно загружен"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Ошибка загрузки файла"));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String url) {
        try {
            fileService.deleteImage(url);
            return ResponseEntity.ok(Map.of("message", "Файл удалён"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Ошибка удаления файла"));
        }
    }
}
