package fr.abes.item.core.configuration;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.HashMap;

public abstract class AbstractConfig {
    protected void configHibernate(LocalContainerEntityManagerFactoryBean em, boolean showsql, String ddlAuto, boolean generateDdl, String initMode, boolean enableLazyLoad) {
        HibernateJpaVendorAdapter vendorAdapter
                = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(generateDdl);
        vendorAdapter.setShowSql(showsql);
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("logging.level.org.hibernate", "DEBUG");
        properties.put("hibernate.type", "trace");
        properties.put("spring.sql.init.mode", initMode);
        properties.put("hibernate.enable_lazy_load_no_trans", enableLazyLoad);
        em.setJpaPropertyMap(properties);
    }

}
