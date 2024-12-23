package com.enginex;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Controller {

    private Map<String, String> fileLogCache = new ConcurrentHashMap<>();
    private List<String> duplicateFiles = new ArrayList<>();

    public void cleanUp() throws Exception {
        final List<String> filePaths = Files.readAllLines(Paths.get("duplicates.log"));
        final List<Path> files = filePaths.stream().map(s -> Paths.get(s)).collect(Collectors.toList());
        for (final Path file : files) {
            if (Files.deleteIfExists(file)) {
                System.out.println("[INFO] Deleting file : " + file.getFileName());
            } else {
                System.err.println("[ERROR] Cannot delete file : " + file.getFileName());
            }
        }
    }

    public void run(final String path) throws Exception {
        // boot up cache
        setupShutdownHook();
        System.out.println("[INFO] Loading up cache");
        List<String> logLines = Files.readAllLines(Paths.get("filelog.log"));
        for (final String logLine : logLines) {
            final String[] fields = logLine.split("\\|");
            fileLogCache.put(fields[0], fields[1]);
        }
        System.out.println("[INFO] Loaded " + fileLogCache.size() + " entries into cache");
        final Set<String> filePathsList = fileLogCache.values().stream().collect(Collectors.toSet());

        Path startingDirectoryPath = Paths.get(path);
        final StringBuffer sb = new StringBuffer();
        Files.walkFileTree(startingDirectoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    if (filePathsList.contains(file.toString())) {
                        System.out.println(String.format("[WARNING] %s is already scanned. Skipping...", file));
                    } else {
                        final String md5Hash = FileHashCalculator.getMD5Hash(file);
                        if (fileLogCache.containsKey(md5Hash)) {
                            // print out duplicate:
                            System.out.println(String.format("[DUPLICATE] %s", file.toString()));
                            duplicateFiles.add(file.toString());
                        } else {
                            // if not a duplicate, then update the cache
                            sb.setLength(0);
                            sb.append(file.toString());
                            fileLogCache.putIfAbsent(md5Hash, sb.toString());
                        }
                        System.out.println("File: " + file.toString());
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("Directory: " + dir.toString());
                return FileVisitResult.CONTINUE;
            }
        });

        generateDuplicateLogFile(duplicateFiles);
        generateUpdatedLogFile(fileLogCache);
    }

    private void generateUpdatedLogFile(final Map<String, String> fileLogCache) throws IOException {
        Files.deleteIfExists(Paths.get("filelog.log"));
        final List<String> list = new ArrayList<>();
        fileLogCache.forEach((k, v) -> list.add(k + "|" + v));
        Files.write(Paths.get("filelog.log"), list, StandardOpenOption.CREATE);
    }

    private void generateDuplicateLogFile(final List<String> duplicateFiles) throws Exception {
        Files.deleteIfExists(Paths.get("duplicates.log"));
        Files.write(Paths.get("duplicates.log"), duplicateFiles, StandardOpenOption.CREATE);
        System.out.println("[INFO] Log file is written");
    }


    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("[WARNING] Shutting down....");
                    generateUpdatedLogFile(fileLogCache);
                    generateDuplicateLogFile(duplicateFiles);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

}
