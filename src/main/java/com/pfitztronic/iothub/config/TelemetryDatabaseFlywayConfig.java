package com.pfitztronic.iothub.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TelemetryDatabaseFlywayConfig {
    @Bean(initMethod = "migrate")
    public Flyway timescaleFlyway(
            @Qualifier("timeseriesDataSource") DataSource ds
    ) {
         return Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/timeseries")
                .baselineOnMigrate(true)
                .load();

    }
}
