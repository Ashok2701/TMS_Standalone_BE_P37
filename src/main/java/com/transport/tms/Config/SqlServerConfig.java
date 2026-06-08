package com.transport.tms.Config;


import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;



@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.transport.tms.transport.sqlserver.repository"
        },
        entityManagerFactoryRef = "sqlServerEntityManagerFactory",
        transactionManagerRef = "sqlServerTransactionManager"
)
public class SqlServerConfig {



    private final JpaProperties jpaProperties;



    public SqlServerConfig(
            JpaProperties jpaProperties
    ){

        this.jpaProperties =
                jpaProperties;

    }



    // ==========================
    // SQL SERVER DATASOURCE
    // ==========================


    @Bean
    @ConfigurationProperties(
            "sqlserver.datasource"
    )
    public DataSourceProperties
    sqlServerDataSourceProperties(){

        return new DataSourceProperties();

    }





    @Bean(
            name="sqlServerDataSource"
    )
    public DataSource
    sqlServerDataSource(){


        return sqlServerDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();

    }





    // ==========================
    // SQL SERVER JDBC TEMPLATE
    // Used for X3 Sync Queries
    // ==========================


    @Bean(
            name="sqlServerJdbcTemplate"
    )
    public JdbcTemplate sqlServerJdbcTemplate(

            @Qualifier(
                    "sqlServerDataSource"
            )
            DataSource dataSource

    ){


        return new JdbcTemplate(
                dataSource
        );


    }






    // ==========================
    // SQL SERVER JPA
    // ==========================


    @Bean(
            name="sqlServerEntityManagerFactory"
    )
    public LocalContainerEntityManagerFactoryBean
    sqlServerEntityManagerFactory(

            EntityManagerFactoryBuilder builder

    ){



        Map<String,Object> properties =
                new HashMap<>();


        properties.putAll(
                jpaProperties.getProperties()
        );



        return builder

                .dataSource(
                        sqlServerDataSource()
                )

                .packages(

                        "com.transport.tms.transport.sqlserver.Entity"

                )

                .persistenceUnit(
                        "sqlserver"
                )

                .properties(
                        properties
                )

                .build();

    }






    @Bean(
            name="sqlServerTransactionManager"
    )
    public PlatformTransactionManager
    sqlServerTransactionManager(


            @Qualifier(
                    "sqlServerEntityManagerFactory"
            )
            EntityManagerFactory entityManagerFactory


    ){


        return new JpaTransactionManager(
                entityManagerFactory
        );


    }


}