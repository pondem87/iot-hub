package com.pfitztronic.iothub.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TelemetryDatabaseConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.timeseries")
    public DataSource timeseriesDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager timeseriesTransactionManager(
            @Qualifier("timeseriesDataSource") DataSource ds
    ) {
        return new DataSourceTransactionManager(ds);
    }
}
