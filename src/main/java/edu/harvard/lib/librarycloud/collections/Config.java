/**********************************************************************
 * Copyright (c) 2012 by the President and Fellows of Harvard College
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 * Contact information
 *
 * Office for Information Systems
 * Harvard University Library
 * Harvard University
 * Cambridge, MA  02138
 * (617)495-3724
 * hulois@hulmail.harvard.edu
 **********************************************************************/
package edu.harvard.lib.librarycloud.collections;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import javax.sql.DataSource;
import edu.harvard.lib.librarycloud.collections.dao.CollectionDAO;
import edu.harvard.lib.librarycloud.collections.CollectionsWorkflow;
/**
*
* Config is the standard LTS class for accessing properties from a properties file,
*
*/

@Configuration
@PropertySource("classpath:librarycloud.collections.env.properties")
@EnableTransactionManagement
public class Config {

    public String SQS_ENVIRONMENT;
    public String AWS_KEY;
    public String AWS_SECRET;
    private static Config conf;
    public static String propFile = "librarycloud.collections.env.properties";

    @Value( "${db_url}" )
    String dbUrl;

    @Value( "${db_user}" )
    String dbUser;

    @Value( "${db_password}" )
    String dbPassword;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Autowired
    public PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(factory);
        return txManager;
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[] { "edu.harvard.lib.librarycloud.collections.model" });
        em.setPersistenceProvider(new PersistenceProviderImpl());
        em.setPersistenceUnitName("production");

        return em;
    }


    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }


    @Bean
    public CollectionDAO collectionDao() {
        CollectionDAO collectionDao = new CollectionDAO();
        return collectionDao;
    }

    @Bean
    public CollectionsWorkflow collectionsWorkflow() {
        CollectionsWorkflow collectionsWorkflow = new CollectionsWorkflow();
        return collectionsWorkflow;
    }



    public Config() {
        Properties props = new Properties();

        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream(Config.propFile));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't load project configuration!", e);
        }

        SQS_ENVIRONMENT = props.getProperty("librarycloud.sqs.environment");
        AWS_KEY = props.getProperty("aws.access.key");
        AWS_SECRET = props.getProperty("aws.secret.key");
    }

    public static synchronized Config getInstance() {
        if (conf == null)
            conf = new Config();
        return conf;
    }

}
