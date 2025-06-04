package com.dacs.quanlyhocvien.Controllers;

import com.dacs.quanlyhocvien.Utils.ExcelExporter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/download")
public class DownloadController {

    private final Map<String, byte[]> fileCache = new ConcurrentHashMap<>();

    @PostMapping("/excel")
    public ResponseEntity<String> createExcel(@RequestBody List<Map<String, Object>> data) {
        try {
            byte[] excelFile = ExcelExporter.exportToExcel(data);
            String fileId = UUID.randomUUID().toString();
            fileCache.put(fileId, excelFile);
            return ResponseEntity.ok("/download/excel?file=" + fileId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi tạo Excel: " + e.getMessage());
        }
    }

    @GetMapping("/excel")
    public ResponseEntity<ByteArrayResource> getExcel(@RequestParam("file") String fileId) {
        byte[] file = fileCache.get(fileId);
        if (file == null) return ResponseEntity.notFound().build();

        ByteArrayResource resource = new ByteArrayResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=result.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length)
                .body(resource);
    }
}
