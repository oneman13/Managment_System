package application.api;

import application.DataForDatabase;

import java.sql.*;
import java.util.Properties;

public class Database {
    private static Database database = null;
    private static Connection connection;

    // Here the constructor is responsible for connecting to database
    private Database(){
        try {
            Class.forName("org.postgresql.Driver");
            Properties authorization = new Properties();

            String password = DataForDatabase.password;
            String username = DataForDatabase.user;
            String url = DataForDatabase.url;

            authorization.put("user", username);
            authorization.put("password", password);

            connection = DriverManager.getConnection(url, authorization);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Database getInstance() {
        if (database == null) database = new Database();

        return database;
    }

    public static void create_tables() {
        try{
            Statement statement = connection.createStatement();

            String query = """
                    create table if not exists users
                    (
                        user_id        serial
                            primary key,
                        worked_hours   numeric(4, 2) default 0,
                        required_hours numeric(4, 2) default 20
                            constraint required_hours
                                check ((required_hours < (80)::numeric) AND (required_hours >= (0)::numeric)),
                        status         varchar(12) not null,
                        occupation     varchar(50),
                        salary         numeric(20, 4)
                            constraint salary
                                check (salary > (0)::numeric),
                        password       text        not null,
                        email          varchar(50) not null
                            unique,
                        full_name      varchar(50) not null,
                        company        varchar(50) not null,
                        date_of_birth  date
                            constraint date_of_birth
                                check (date_of_birth > '1922-11-20'::date),
                        photo          varchar(256)  default NULL::character varying
                    );

                    create table if not exists tasks
                    (
                        task_id         serial
                            primary key,
                        creator_id      integer not null
                            constraint creator
                                references users,
                        deadline        timestamp with time zone
                            constraint deadline
                                check (deadline >= now()),
                        description     text,
                        task_type       varchar(50) default NULL::character varying,
                        status          varchar(15),
                        completion_date timestamp with time zone
                            constraint completion_date
                                check (completion_date = CURRENT_DATE)
                    );

                    create table if not exists users_tasks
                    (
                        user_id integer not null
                            references users
                                on update cascade on delete cascade,
                        task_id integer not null
                            references tasks
                                on update cascade on delete cascade,
                        constraint user_task_pkey
                            primary key (user_id, task_id)
                    );

                    create table if not exists timeoffs
                    (
                        timeoff_id  serial
                            primary key,
                        employee_id integer not null
                            constraint employee
                                references users,
                        description text,
                        start_day   date default CURRENT_DATE
                            constraint start_day
                                check (start_day > (CURRENT_DATE - 7)),
                        end_day     date,
                        is_approved boolean,
                        constraint end_day
                            check (end_day >= start_day)
                    );

                    create table if not exists notifications
                    (
                        notification_id      serial
                            primary key,
                        task_id              integer
                            constraint task
                                references tasks,
                        sender_id            integer not null
                            constraint sender
                                references users,
                        notification_message text,
                        timeoff_id           integer
                            constraint timeoff
                                references timeoffs,
                        is_seen              boolean default false
                    );

                    create table if not exists users_notifications
                    (
                        user_id         integer not null
                            references users
                                on update cascade on delete cascade,
                        notification_id integer not null
                            references notifications
                                on update cascade on delete cascade,
                        constraint user_notification_pkey
                            primary key (user_id, notification_id)
                    );

                    create table if not exists slots
                    (
                        slot_id     serial
                            primary key,
                        task_id     integer
                            constraint task
                                references tasks,
                        hours       numeric(4, 2) default 0
                            constraint hours
                                check ((hours > (0)::numeric) AND (hours <= (16)::numeric)),
                        description text,
                        user_id     integer
                            constraint users
                                references users,
                        slot_date   date not null
                            constraint slot_date
                                check (slot_date > (CURRENT_DATE - 7))
                    );

                    create table if not exists current_users
                    (
                        id              serial
                            primary key,
                        current_user_id integer
                    );
                    """;

            statement.executeUpdate(query);
            System.out.println("Created tables in given database...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // getting values with some condition
    public ResultSet get(String select, String table, String whereCondition) {
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT " + select + " FROM " + table + " WHERE " + whereCondition;
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // getNC - get without 'WHERE' condition
    public ResultSet getNC(String select, String table) {
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT " + select + " FROM " + table;

            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet insert(String table, String columns, String values) {
        try {
            String query = "INSERT INTO " + table + columns + " VALUES " + values;
            PreparedStatement statement = connection.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate();

            // returns id of created object
            ResultSet generatedKeys = statement.getGeneratedKeys();
            return generatedKeys;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String table, String whereCondition) {
        try {
            Statement statement = connection.createStatement();
            String query = "DELETE FROM " + table + " WHERE " + whereCondition;

            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String table, String whereCondition, String values) {
        try {
            Statement statement = connection.createStatement();
            String query = "UPDATE " + table + " SET " + values + " WHERE " + whereCondition;

            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void anotherQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet anotherGetQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // this is implemented at the end of the program
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}