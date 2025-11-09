package com.couponpop.couponservice.common.config;

import com.couponpop.couponservice.common.datasource.ReplicationType;
import com.couponpop.couponservice.common.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.master.hikari")
    public DataSource masterDataSource() {
        log.debug("Master DataSource 생성");
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave.hikari")
    public DataSource slaveDataSource() {
        log.debug("Slave DataSource 생성");
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * 읽기와 쓰기 데이터 소스를 ReplicationType에 따라 매핑하고,
     * 작성했던 ReplicationDataSourceRouter 클래스로 인스턴스를 생성한 뒤
     * 생성한 맵을 라우터에 설정하여, 런타임에 적절한 데이터 소스를 선택하도록 설정하고
     * 적절한 데이터 소스를 결정할 수 없을 때 사용할 기본 데이터 소스를 선택한다.
     */
    @Bean
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource
    ) {
        log.debug("RoutingDataSource 생성");
        HashMap<Object, Object> dataSources = new HashMap<>();
        dataSources.put(ReplicationType.WRITE, masterDataSource);
        dataSources.put(ReplicationType.READ, slaveDataSource);

        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(dataSources);
        routing.setDefaultTargetDataSource(masterDataSource);
        return routing;
    }

    // 실제 데이터베이스 연결이 필요할 때까지 DataSource 결정을 지연시키고, 데이터베이스 작업이 필요한 시점(예:SQL 실행)에 실제 연결을 생성한다.
    @Bean
    @Primary
    public DataSource dataSource(DataSource routingDataSource) {
        log.debug("LazyConnectionDataSourceProxy 생성");
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

}
