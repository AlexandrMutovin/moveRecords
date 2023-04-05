package ru.voxlink;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

@Setter
@Getter
public class Parameters {
    private Path srcPath;
    private Path dstPath;
    private String botId;
    private boolean isLogToTelegram;
    private String chatId;
    private List<String> tags;

    public Parameters(Path srcPath, Path dstPath){
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.isLogToTelegram = false;
    }
    public Parameters(Path srcPath, Path dstPath, String botid, String chatid){
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.isLogToTelegram = true;
        this.botId = botid;
        this.chatId = chatid;
    }
    public Parameters(Path srcPath, Path dstPath, String botid, String chatid, List<String> tags){
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.isLogToTelegram = true;
        this.botId = botid;
        this.chatId = chatid;
        this.tags = tags;
    }

}
