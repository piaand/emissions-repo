package com.piaand.co2emissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Class sets up the LogConfiguration and creates the handlers
 * for the logger. The configuration path is loaded from
 * application.properties.
 *
 * In any errors when reading log file or log configurations, systems exits.
 */
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogConfigurations {
    private static final LogManager LOGMANAGER = LogManager.getLogManager();
    private static final Logger LOGGER = Logger.getLogger(Co2EmissionsApplication.class.getName());

    @Value("${logging.path}")
    private String configFileName;

    public void setConfig() {
        try {
            FileInputStream configFile = new FileInputStream(configFileName);
            LOGMANAGER.getLogManager().readConfiguration(configFile);
            LOGGER.log(Level.INFO, "Read the logger configurations");
        } catch (IOException | NullPointerException exception) {
            LOGGER.log(Level.SEVERE, "Error in loading configuration", exception);
            System.exit(1);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Unexpected error in loading configuration", exception);
            System.exit(1);
        }
    }
}
