package org.moddingx.libx.impl;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.moddingx.libx.LibX;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ModFileHashes {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int MURMUR2_SEED = 1;
    private static final int MURMUR2_M = 0x5bd1e995;
    private static final Map<Path, FileHashes> HASHES = new ConcurrentHashMap<>();
    private static boolean STARTED = false;

    private ModFileHashes() {}

    public static void start() {
        if (STARTED) {
            return;
        }

        STARTED = true;
        Path modsDir = FMLPaths.MODSDIR.get();
        Set<Path> modPaths = new LinkedHashSet<>();

        for (IModFileInfo fileInfo : ModList.get().getModFiles()) {
            Path path = ModFileHashes.normalize(fileInfo.getFile());
            if (path.startsWith(modsDir) && Files.isRegularFile(path)) {
                modPaths.add(path);
            }
        }

        if (modPaths.isEmpty()) {
            return;
        }

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int threadCount = Math.clamp(Math.max(1, availableProcessors / 2), 1, 4);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount, new HashThreadFactory());

        for (Path path : modPaths) {
            executor.execute(() -> {
                try {
                    HASHES.put(path, ModFileHashes.hashFile(path));
                } catch (Exception e) {
                    LibX.logger.warn("Failed to hash mod file {}", path, e);
                }
            });
        }
        executor.shutdown();
    }

    // ModList#crashReport replacement with additional SHA1 and Murmur2 hashes of each mod
    // to make recreating a modpack based on crash report easier
    public static String modListReport() {
        return "\n" + ModList.get().applyForEachModFileAlphabetical(ModFileHashes::fileToLine)
                .collect(Collectors.joining("\n\t\t", "\t\t", ""));
    }

    private static String fileToLine(IModFile mf) {
        IModInfo mainMod = mf.getModInfos().getFirst();
        FileHashes hashes = HASHES.get(ModFileHashes.normalize(mf));

        return String.format(Locale.ENGLISH, "%-50.50s|%-30.30s|%-30.30s|%-20.20s|Manifest: %s|SHA1: %-40s|Murmur2: %s",
                mf.getFileName(),
                mainMod.getDisplayName(),
                mainMod.getModId(),
                mainMod.getVersion(),
                ((ModFileInfo) mf.getModFileInfo()).getCodeSigningFingerprint().orElse("NOSIGNATURE"),
                hashes == null ? "-" : hashes.sha1(),
                hashes == null ? "-" : Long.toString(hashes.murmur2()));
    }

    private static Path normalize(IModFile file) {
        return file.getFilePath().toAbsolutePath().normalize();
    }

    static FileHashes hashFile(Path path) throws IOException {
        MessageDigest firstSha1 = ModFileHashes.sha1();
        long normalizedLength = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        try (InputStream in = new BufferedInputStream(Files.newInputStream(path), BUFFER_SIZE)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                firstSha1.update(buffer, 0, read);
                for (int idx = 0; idx < read; idx++) {
                    if (!ModFileHashes.isCurseForgeWhitespace(buffer[idx])) normalizedLength++;
                }
            }
        }

        if (normalizedLength > Integer.MAX_VALUE) {
            throw new IOException("File is too large for a CurseForge Murmur2 fingerprint: " + path);
        }

        byte[] firstDigest = firstSha1.digest();
        MessageDigest verifySha1 = ModFileHashes.sha1();
        int hash = MURMUR2_SEED ^ (int) normalizedLength;
        int block = 0;
        int blockBytes = 0;
        long secondNormalizedLength = 0;

        try (InputStream in = new BufferedInputStream(Files.newInputStream(path), BUFFER_SIZE)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                verifySha1.update(buffer, 0, read);
                for (int idx = 0; idx < read; idx++) {
                    byte value = buffer[idx];
                    if (ModFileHashes.isCurseForgeWhitespace(value)) {
                        continue;
                    }

                    block |= (value & 0xff) << (blockBytes * 8);
                    blockBytes++;
                    secondNormalizedLength++;

                    if (blockBytes == 4) {
                        int k = block;
                        k *= MURMUR2_M;
                        k ^= k >>> 24;
                        k *= MURMUR2_M;
                        hash *= MURMUR2_M;
                        hash ^= k;
                        block = 0;
                        blockBytes = 0;
                    }
                }
            }
        }

        byte[] secondDigest = verifySha1.digest();
        if (normalizedLength != secondNormalizedLength || !Arrays.equals(firstDigest, secondDigest)) {
            throw new IOException("Mod file changed while it was being hashed: " + path);
        }

        if (blockBytes != 0) {
            hash ^= block;
            hash *= MURMUR2_M;
        }
        hash ^= hash >>> 13;
        hash *= MURMUR2_M;
        hash ^= hash >>> 15;

        return new FileHashes(HexFormat.of().formatHex(firstDigest), Integer.toUnsignedLong(hash));
    }

    private static MessageDigest sha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }

    private static boolean isCurseForgeWhitespace(byte value) {
        return value == 0x09 || value == 0x0a || value == 0x0d || value == 0x20;
    }

    record FileHashes(String sha1, long murmur2) {}

    private static final class HashThreadFactory implements ThreadFactory {

        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(@Nonnull Runnable runnable) {
            Thread thread = new Thread(runnable, "LibX Mod Hash #" + this.counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
