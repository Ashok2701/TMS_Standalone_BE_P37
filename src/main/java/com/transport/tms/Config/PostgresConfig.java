package com.transport.tms.Config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Primary
@EnableJpaRepositories(
        basePackages = {
                "com.transport.tms.Fleet.Repository",
                "com.transport.tms.Sync.Customer.Repository",
                "com.transport.tms.Sync.Site.Repository",
                "com.transport.tms.Sync.Repository",
                "com.transport.tms.UserManagement.Repository",
                "com.transport.tms.Configuration.Document.Repository"
        },
        entityManagerFactoryRef = "postgresEntityManagerFactory",
        transactionManagerRef = "postgresTransactionManager"
)
public class PostgresConfig {

    private final JpaProperties jpaProperties;

    public PostgresConfig(JpaProperties jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties postgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource() {

        return postgresDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean(name = "postgresEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> properties = new HashMap<>();

        properties.putAll(jpaProperties.getProperties());

        return builder
                .dataSource(postgresDataSource())
                .packages(
                        "com.transport.tms.Fleet.Entity",
                        "com.transport.tms.Sync.Entity",
                        "com.transport.tms.UserManagement.Entity",
                        "com.transport.tms.Sync.Site.Entity",
                        "com.transport.tms.Sync.Customer.Entity",
                        "com.transport.tms.Configuration.Document.Entity"
                )
                .persistenceUnit("postgres")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "postgresTransactionManager")
    public PlatformTransactionManager postgresTransactionManager(
            @Qualifier("postgresEntityManagerFactory")
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }
}