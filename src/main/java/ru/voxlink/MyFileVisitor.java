package ru.voxlink;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.voxlink.telegram.SendMessageToTelegram;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MyFileVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = LogManager.getLogger(MyFileVisitor.class);

    private Path srcDir;
    private Path dstDir;
    private SendMessageToTelegram sendMessageToTelegram;

    public MyFileVisitor(Path srcDir, Path dstDir, SendMessageToTelegram sendMessageToTelegram) {
        this.srcDir = srcDir;
        this.dstDir = dstDir;
        this.sendMessageToTelegram = sendMessageToTelegram;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        LOGGER.info("Сканирую папку " + dir);
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) throws IOException {

        Path dstPath = dstDir.resolve(srcDir.relativize(srcFile));

        try {
            if (!FileUtils.isSymlink(srcFile.toFile()) && srcFile.toString().endsWith(".mp3")) {
                BasicFileAttributes attr = Files.readAttributes(srcFile, BasicFileAttributes.class);
                long lastModifiedTime = attr.lastModifiedTime().toMillis();
                long diff = System.currentTimeMillis() - lastModifiedTime;
                long diffMinutes = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
                if (diffMinutes > 10) {

                    FileUtils.copyFile(srcFile.toFile(), dstPath.toFile());
                    if (Files.deleteIfExists(srcFile)) {

                        Files.createSymbolicLink(srcFile, dstPath);
                        LOGGER.info("File " + srcFile + " move to " + dstPath);
                    }
                } else {
                    LOGGER.info("File " + srcFile + " is skipped, last modification time is less than " + diffMinutes);
                }
            }
        } catch (Exception e) {
            if (sendMessageToTelegram != null) {
                sendMessageToTelegram.send("Ошибка копирования. Копирование завершилось на файле " + srcFile + "Возможно проблема с шарой. Код ошибки " + e);
            }

            LOGGER.error("Ошибка копирования. Копирование завершилось на файле " + srcFile + " Возможно проблема с шарой. Код ошибки " + e);
            return FileVisitResult.TERMINATE;
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
