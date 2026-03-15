package com.pfitztronic.iothub.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CoreDatabaseFlywayConfig {
    @Bean(initMethod = "migrate")
    public Flyway coreFlyway(
            @Qualifier("coreDataSource") DataSource ds
    ) {
         return Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/core")
                .baselineOnMigrate(true)
                .load();

    }
}
