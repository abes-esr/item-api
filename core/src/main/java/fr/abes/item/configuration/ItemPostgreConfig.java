package fr.abes.item.configuration;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(transactionManagerRef = "itemTransactionManager",
		entityManagerFactoryRef = "itemEntityManagerFactory",
		basePackages = "fr.abes.item.dao.item")
@NoArgsConstructor
public class ItemPostgreConfig extends AbstractConfig {
	@Value("${spring.jpa.item.database-platform}")
	protected String platform;
	@Value("${spring.jpa.item.hibernate.ddl-auto}")
	protected String ddlAuto;
	@Value("${spring.jpa.item.generate-ddl}")
	protected boolean generateDdl;
	@Value("${spring.jpa.item.properties.hibernate.dialect}")
	protected String dialect;
	@Value("${spring.jpa.item.show-sql}")
	private boolean showsql;
	@Value("${spring.sql.basexml.init.mode}")
	private String initMode;
	@Value("${spring.hibernate.item.enable_lazy_load_no_trans}")
	private boolean lazyload;

	@Primary
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.item")
	public DataSource itemDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean itemEntityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(itemDataSource());
		em.setPackagesToScan(new String[]{"fr.abes.item.entities.item"});
		configHibernate(em, platform, showsql, dialect, ddlAuto, generateDdl, initMode, lazyload);
		return em;
	}

	@Primary
	@Bean
	public JpaTransactionManager itemTransactionManager(EntityManagerFactory entityManagerFactory) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}
	
	@Primary
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean(name = "kopyaJdbcTemplate")
	public JdbcTemplate kopyaJdbcTemplate() { return new JdbcTemplate(itemDataSource());}

}
