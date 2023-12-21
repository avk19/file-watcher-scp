package com.example;

import com.jcraft.jsch.*;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileWatcherSCPService {
	public void watchAndCopyFiles(String localFolder, String remoteFolder, String username, String hostname,
			String password) throws JSchException, IOException, InterruptedException {
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(localFolder);
		path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

		JSch jsch = new JSch();
		Session session = jsch.getSession(username, hostname, 22);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();

		while (true) {
			WatchKey key = watchService.take();

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();

					ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
					channelExec.setCommand("scp " + localFolder + "/" + fileName + " " + username + "@" + hostname + ":"
							+ remoteFolder);
					channelExec.connect();
					channelExec.disconnect();
				}
			}

			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}

		session.disconnect();
	}
}
