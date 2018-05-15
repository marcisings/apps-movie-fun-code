package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCrendentials(@Value("${VCAP_SERVICES}") String vcapServices) {
          return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        return dataSource;
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean(
            DataSource moviesDataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(moviesDataSource);
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        factoryBean.setPersistenceUnitName("moviesUnitName");
        return factoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean(
            DataSource albumsDataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(albumsDataSource);
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        factoryBean.setPersistenceUnitName("albumsUnitName");
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(
            EntityManagerFactory moviesEntityManagerFactoryBean) {
        return new JpaTransactionManager(moviesEntityManagerFactoryBean);
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(
            EntityManagerFactory albumsEntityManagerFactoryBean) {
        return new JpaTransactionManager(albumsEntityManagerFactoryBean);
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(
            PlatformTransactionManager moviesPlatformTransactionManager) {
        return new TransactionTemplate(moviesPlatformTransactionManager);
    }

    @Bean
    public TransactionOperations albumsTransactionOperations(
            PlatformTransactionManager albumsPlatformTransactionManager) {
        return new TransactionTemplate(albumsPlatformTransactionManager);
    }
}
