package org.example.lib;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

// 1st part
public interface ORManager {
    // let it work with ids:
    // - Long (autogenerated at DB side)  (HIGH)
    // - UUID (autogenerated at ORM side) (MEDIUM)
    // - String                           (OPTIONAL)
    // The fields may be of types:
    // - int/Integer                      (HIGH)
    // - long/Long                        (HIGH)
    // - double/Double                    (OPTIONAL)
    // - boolean/Boolean                  (OPTIONAL)
    // - String                           (HIGH)
    // - LocalDate                        (MEDIUM)
    // - LocalTime                        (MEDIUM)
    // - LocalDateTime/Instant            (MEDIUM)
    // - BigDecimal                       (OPTIONAL)
    // - Enum +                           (OPTIONAL)
    //   @Enumerated(EnumType.ORDINAL/EnumType.STRING)

    // initialize connection factory for the DB
    // read the jdbc url, username and password from
    //  the given property file
    static ORManager withPropertiesFrom(String filename) {

       Properties prop = new Properties();
        try (InputStream input = new FileInputStream(filename)) {
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from file: " + filename, e);
        }

        JdbcDataSource datasource = new JdbcDataSource();

        datasource.setURL(prop.getProperty("jdbc-url"));
        datasource.setUser(prop.getProperty("jdbc-username"));
        datasource.setPassword(prop.getProperty("jdbc-password"));
        DBTypes type = DBTypes.valueOf(prop.getProperty("jdbc-typeDB"));
        return withDataSource(DataSourceFactory.getDataSource(prop, type));
    }

    // initialize connection factory for the DB based on the DataSource
    static ORManager withDataSource(DataSource dataSource) {
        return new ORManagerImpl(dataSource);
    }


    // generate the schema in the DB
    // for given list of entity classes (and all related
    //  by OneToMany/ManyToOne) create a schema in DB
    void register(Class... entityClasses);

    // CREATE
    // save a new object to DB, set id if autogenerated
    // or merge into DB if id is present
    <T> T save(T o);

    // save a new object to DB, set id if autogenerated
    // throw if the object has id already set (except for String)
    void persist(Object o);

    // READ
    <T> Optional<T> findById(Serializable id, Class<T> cls);

    // READ ALL
    <T> List<T> findAll(Class<T> cls);

    // READ ALL LAZY
    <T> Iterable<T> findAllAsIterable(Class<T> cls); // (MEDIUM)

    <T> Stream<T> findAllAsStream(Class<T> cls);     // (OPTIONAL)

    // UPDATE
    <T> T merge(T o);   // send o -> DB row (to table)

    <T> T refresh(T o); // send o <- DB row (from table)

    // DELETE
    // set autogenerated id to null
    // return true if successfully deleted
    boolean delete(Object o);
}

