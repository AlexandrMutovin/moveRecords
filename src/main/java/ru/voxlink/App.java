package ru.voxlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.voxlink.telegram.SendMessageToTelegram;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private static final int NO_ARG_PARAMETER = 0;
    private static final int ONE_ARG_PARAMETER = 1;
    private static final int THREE_ARG_PARAMETR = 3;

    private static String hostName = "";
    public static void main(String[] args) {
        try {
            hostName = InetAddress.getLocalHost().getHostName() + " ";
        } catch (UnknownHostException e) {
            LOGGER.info(hostName +"Не удалось определить HostName сервера");
        }
        Parameters parameters = validateAndGetParam(args);
        SendMessageToTelegram sendMessageToTelegram = null;
        Path srcDir = parameters.getSrcPath();
        Path dstDir = parameters.getDstPath();
        if (parameters.isLogToTelegram()){
            sendMessageToTelegram = new SendMessageToTelegram(parameters.getBotId(), parameters.getChatId(), parameters.getTags());
        }

        if (!Files.exists(dstDir)) {
            String message = hostName + "Папка назначения не существует- " + dstDir;
            if (sendMessageToTelegram != null) {
                sendMessageToTelegram.send(message);
            }
            LOGGER.warn(message);
            System.exit(0);
        }

        Double totalSpace = dstDir.toFile().getTotalSpace() / 1024.0 / 1024.0;
        String totalSpaceFormat = String.format("%.2f Mb", totalSpace);
        if (totalSpace > 1000) {
            totalSpaceFormat = String.format("%.2f Gb", totalSpace / 1024.0);
        }

        if (totalSpace < 0.0001) {
            String message = hostName + "Копирование невозможно, указанный диск для записи пустой или NFS монтирование отвалилось";
            if(sendMessageToTelegram != null) {
                sendMessageToTelegram.send(message);
            }
            LOGGER.error(message);
            System.exit(0);
        }

        Double freeSpace = dstDir.toFile().getFreeSpace() / 1024.0 / 1024.0;
        Double usageSpaceParsent = (freeSpace * 100 / totalSpace);
        String usageSpaceParsent2 = String.format("%.2f", usageSpaceParsent);
        LOGGER.info(hostName + "Total space - " + totalSpaceFormat + "; Free space - " + usageSpaceParsent2 + " %");

        if (usageSpaceParsent < 20) {
            String message = hostName + "Осталось " + String.format("%.2f", usageSpaceParsent) + " процентов свободного места";
            if(sendMessageToTelegram != null){
            sendMessageToTelegram.send(message);
            }
            LOGGER.warn(message);
        }

        try {
            Files.walkFileTree(srcDir, new MyFileVisitor(srcDir, dstDir, sendMessageToTelegram));
        } catch (IOException e) {
            LOGGER.warn(hostName + "Проблема в доступе к srcfile " + e.toString());
            System.exit(0);
        }

    }

    private static Parameters validateAndGetParam(String[] parameter) {
        List<String> tags = new ArrayList<String>();
        Path srcPath = null;
        Path dstPath = null;
        String botId = null;
        String chatId = null;

        if (parameter.length == NO_ARG_PARAMETER) {
            LOGGER.error(hostName + "В параметрах не переданы директории для копирования");
            System.exit(0);
        }
        if (parameter.length == ONE_ARG_PARAMETER) {
            LOGGER.error(hostName + "В параметрах передан всего один путь " + parameter[0]);
            System.exit(0);
        }
        if (parameter.length == THREE_ARG_PARAMETR) {
            LOGGER.error(hostName + "В командной строке передано " + parameter.length + " аргументов. Смотри документацию ");
            System.exit(0);
        }
        String[] firstParameterNameAndPath = parameter[0].split("=");
        if (firstParameterNameAndPath.length != 2 && !"srcdir".equals(firstParameterNameAndPath[0])) {
            LOGGER.error(hostName + "srcdir указан неверно, пример  srcdir=/var/spool/asterisk/monitor/");
            System.exit(0);
        }
        String[] secondParameterNameAndPath = parameter[1].split("=");
        if (secondParameterNameAndPath.length != 2 && !"dstdir".equals(secondParameterNameAndPath[0])) {
            LOGGER.error("dstdir указан неверно, пример  dstdir=/mnt/monitor/");
            System.exit(0);
        }

        if (parameter.length >= 4) {
            String[] theardParameterNameAndBotId = parameter[2].split("=");
            if (theardParameterNameAndBotId.length != 2 && !"botid".equals(theardParameterNameAndBotId[0])) {
                LOGGER.error("Третий параметр botid указан неверно, пример botid=bot5772911192:AAHNClxq6Ewry_o5pWfdQrA444444uM2E14");
                System.exit(0);
            }
            botId = theardParameterNameAndBotId[1];

            String[] fourthParameterNameAndChatId = parameter[3].split("=");
            if (fourthParameterNameAndChatId.length != 2 && !"chatid".equals(fourthParameterNameAndChatId[0])) {
                LOGGER.error("Четвертый параметр указан неверно, пример chatid=-611709251");
                System.exit(0);
            }
            chatId = fourthParameterNameAndChatId[1];

            for (int i = 4; i < parameter.length; i++) {
                if (parameter[i].startsWith("@")) {
                    tags.add(parameter[i]);
                }
            }

        }

        String firstParameterPAth = firstParameterNameAndPath[1];
        String secondParameterPath = secondParameterNameAndPath[1];

        try {
            srcPath = Path.of(firstParameterPAth);
        } catch (Exception e) {
            LOGGER.error(hostName + "Не удалось получить src путь, вероятно опечатка в пути к директории " + e.getMessage());
            System.out.println(hostName + "Не удалось получить src путь, вероятно опечатка в пути к директории " + e.getMessage());
            System.exit(0);
        }

        try {
            dstPath = Path.of(secondParameterPath);
        } catch (Exception e) {
            LOGGER.error(hostName + "Не удалось получить dst путь, вероятно опечатка в пути к директории " + e.getMessage());
            System.out.println(hostName + "Не удалось получить dst путь, вероятно опечатка в пути к директории " + e.getMessage());
            System.exit(0);
        }
        if (parameter.length == 2) {
            return new Parameters(srcPath, dstPath);
        } else return new Parameters(srcPath, dstPath, botId, chatId, tags);
    }
}


