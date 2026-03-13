package com.rml.system.util;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String store(MultipartFile file, String filename) throws IOException;
    Resource load(String filename) throws IOException;
    void delete(String filename);
}
