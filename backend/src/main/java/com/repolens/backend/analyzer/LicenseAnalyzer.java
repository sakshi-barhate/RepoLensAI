package com.repolens.backend.analyzer;

import com.repolens.backend.dto.LicenseInfo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;

@Component
public class LicenseAnalyzer {

    public LicenseInfo analyze(File repository) {

        File licenseFile = findLicenseFile(repository);

        if (licenseFile == null) {
            return LicenseInfo.builder()
                    .exists(false)
                    .type("Unknown")
                    .build();
        }

        try {

            String content = Files.readString(licenseFile.toPath()).toLowerCase();

            String type = detectLicense(content);

            return LicenseInfo.builder()
                    .exists(true)
                    .type(type)
                    .build();

        } catch (Exception e) {

            return LicenseInfo.builder()
                    .exists(true)
                    .type("Unknown")
                    .build();
        }
    }

    private File findLicenseFile(File repository) {

        String[] names = {
                "LICENSE",
                "LICENSE.txt",
                "LICENSE.md",
                "COPYING"
        };

        for (String name : names) {
            File file = new File(repository, name);
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    private String detectLicense(String content) {

        if (content.contains("mit license")) {
            return "MIT";
        }

        if (content.contains("apache license")) {
            return "Apache-2.0";
        }

        if (content.contains("gnu general public license")) {
            return "GPL";
        }

        if (content.contains("bsd")) {
            return "BSD";
        }

        if (content.contains("mozilla public license")) {
            return "MPL-2.0";
        }

        return "Unknown";
    }
}