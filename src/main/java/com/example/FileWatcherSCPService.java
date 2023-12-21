package com.example;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Service
public class FileWatcherSCPService {

    private final String backupFolder;

    public FileWatcherSCPService() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        backupFolder = properties.getProperty("backup.folder");
    }

    public void watchAndCopyFiles(String localFolder, String remoteFolder, String username, String hostname, String password)
            throws JSchException, IOException, InterruptedException {

        var watchService = FileSystems.getDefault().newWatchService();
        var path = Paths.get(localFolder);
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        var jsch = new JSch();
        var session = jsch.getSession(username, hostname, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        while (true) {
            var key = watchService.take();

            for (var event : key.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    @SuppressWarnings("unchecked")
					var ev = (WatchEvent<Path>) event;
                    var fileName = ev.context();

                    var sourceFilePath = Paths.get(localFolder, fileName.toString());
                    var backupFolderPath = Paths.get(backupFolder);

                    if (!Files.exists(backupFolderPath)) {
                        Files.createDirectories(backupFolderPath);
                    }

                    var timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    var backupFileName = fileName.toString().replace(".", "_" + timestamp + ".");

                    var backupFilePath = Paths.get(backupFolder, backupFileName);

                    Files.copy(sourceFilePath, backupFilePath, StandardCopyOption.REPLACE_EXISTING);

                    var channelExec = (ChannelExec) session.openChannel("exec");
                    //channelExec.setCommand("scp " + sourceFilePath + " " + username + "@" + hostname + ":" + remoteFolder);
                    channelExec.setCommand("scp -c aes256 " + sourceFilePath + " " + username + "@" + hostname + ":" + remoteFolder);
                    channelExec.connect();
                    channelExec.disconnect();
                }
            }

            var valid = key.reset();
            if (!valid) {
                break;
            }
        }

        session.disconnect();
    }
}
