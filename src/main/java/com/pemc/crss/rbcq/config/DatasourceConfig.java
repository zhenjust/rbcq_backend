package com.pemc.crss.rbcq.config;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
@EntityScan(basePackages = "com.pemc.crss.rbcq.entity")
public class DatasourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("settlement")
    public DataSourceProperties settlementDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("registration")
    public DataSourceProperties registrationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public JdbcTemplate settlementJdbcTemplate(@Qualifier("settlementDataSourceProperties") DataSourceProperties settlementDataSourceProperties) {
        var dataSource = DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .url(settlementDataSourceProperties.getUrl())
                .username(settlementDataSourceProperties.getUsername())
                .password(settlementDataSourceProperties.getPassword())
                .build();
        dataSource.setAutoCommit(true);
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public JdbcTemplate registrationJdbcTemplate(@Qualifier("registrationDataSourceProperties") DataSourceProperties registrationDataSourceProperties) {
        var dataSource = DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .url(registrationDataSourceProperties.getUrl())
                .username(registrationDataSourceProperties.getUsername())
                .password(registrationDataSourceProperties.getPassword())
                .build();
        dataSource.setAutoCommit(true);
        return new JdbcTemplate(dataSource);
    }

}
