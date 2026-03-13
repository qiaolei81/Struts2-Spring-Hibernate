package com.rml.system.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.base-dir:/tmp/uploads}")
    private String baseDir;

    private Path basePath;

    @PostConstruct
    public void init() {
        basePath = Paths.get(baseDir);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            log.warn("Could not create upload directory: {}", baseDir);
        }
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        Path dest = basePath.resolve(filename).normalize();
        if (!dest.startsWith(basePath)) {
            throw new IOException("Filename outside allowed directory: " + filename);
        }
        Files.copy(file.getInputStream(), dest,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    @Override
    public Resource load(String filename) throws IOException {
        Path file = basePath.resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) {
            throw new IOException("File not found: " + filename);
        }
        return resource;
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = basePath.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", filename);
        }
    }
}
