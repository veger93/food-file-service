package com.service.fileservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        // Валидация — принимаем только картинки
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Разрешены только изображения");
        }

        // Генерируем уникальное имя файла чтобы не было конфликтов
        String publicId = "freshdrop/" + UUID.randomUUID();

        // Загружаем файл в Cloudinary
        // file.getBytes() — читаем содержимое файла в байты
        // ObjectUtils.asMap() — создаёт Map с настройками загрузки
        Map result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", "freshdrop",
                        // transformation — автоматически изменяем размер
                        // до 500x500 при загрузке, экономим место
                        "transformation", "w_500,h_500,c_fill,g_auto"
                )
        );

        // Cloudinary возвращает Map с данными загруженного файла
        // secure_url — https ссылка на картинку
        String url = (String) result.get("secure_url");
        log.debug("Файл загружен: {}", url);

        return url;
    }

    public void deleteImage(String imageUrl) throws IOException {
        // Извлекаем public_id из URL чтобы удалить файл
        // URL выглядит так: https://res.cloudinary.com/cloud/image/upload/v123/freshdrop/uuid.jpg
        // Нам нужна часть: freshdrop/uuid
        String publicId = extractPublicId(imageUrl);

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.debug("Файл удалён: {}", publicId);
    }

    private String extractPublicId(String url) {
        // Находим часть URL после /upload/ и до расширения файла
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) return url;

        String afterUpload = url.substring(uploadIndex + 8);
        // Убираем версию (v1234567/) если она есть
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // Убираем расширение файла
        int dotIndex = afterUpload.lastIndexOf(".");
        return dotIndex > 0 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}