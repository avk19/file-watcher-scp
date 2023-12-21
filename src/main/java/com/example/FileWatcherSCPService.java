package com.example;

import com.jcraft.jsch.*;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileWatcherSCPService {

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
                    var ev = (WatchEvent<Path>) event;
                    var fileName = ev.context();

                    var channelExec = (ChannelExec) session.openChannel("exec");
                    channelExec.setCommand("scp " + localFolder + "/" + fileName + " " + username + "@" + hostname + ":" + remoteFolder);
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
