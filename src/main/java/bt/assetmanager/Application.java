package bt.assetmanager;

import bt.log.ConsoleLoggerHandler;
import bt.log.FileLoggerHandler;
import bt.log.Log;
import bt.log.LoggerConfiguration;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;
import java.util.logging.Level;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "btassetmanager", variant = Lumo.DARK)
@PWA(name = "BtAssetManager", shortName = "BtAssetManager", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator
{

    public static void main(String[] args) throws IOException
    {
        // disable logging system to force spring into using SLF4J binding
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, "none");

        // making Desktop class work to open file locations
        System.setProperty("java.awt.headless", "false");

        LoggerConfiguration config = new LoggerConfiguration().level(Level.FINE)
                                                              .invalidCallerPackages("org.apache.commons.logging",
                                                                                     "org.springframework.boot.autoconfigure.logging",
                                                                                     "org.springframework.core.log");

        Log.createDefaultLogFolder();
        Log.configureDefaultJDKLogger(new ConsoleLoggerHandler(config), new FileLoggerHandler(config,
                                                                                              FileLoggerHandler.DEFAULT_FILE_PATTERN,
                                                                                              10_000_000,
                                                                                              10,
                                                                                              true));
        SpringApplication.run(Application.class, args);
    }
}