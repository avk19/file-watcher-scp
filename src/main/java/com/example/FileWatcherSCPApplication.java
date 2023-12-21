package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class FileWatcherSCPApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FileWatcherSCPApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.err.println("Please provide the path to the configuration file.");
            return;
        }

        String configFilePath = args[0];
        Properties properties = loadProperties(configFilePath);

        String localFolder = properties.getProperty("local.folder");
        String remoteFolder = properties.getProperty("remote.folder");
        String username = properties.getProperty("username");
        String hostname = properties.getProperty("hostname");
        String password = properties.getProperty("password");

        // Validate and use the obtained properties for the service
        if (localFolder != null && remoteFolder != null && username != null && hostname != null && password != null) {
            FileWatcherSCPService fileWatcherSCPService = new FileWatcherSCPService();
            fileWatcherSCPService.watchAndCopyFiles(localFolder, remoteFolder, username, hostname, password);
        } else {
            System.err.println("Configuration properties missing or incomplete.");
        }
    }

    private Properties loadProperties(String filename) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("Configuration file not found.");
            }
        }
        return properties;
    }
}

