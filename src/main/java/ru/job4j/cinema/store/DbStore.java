package ru.job4j.cinema.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DbStore implements Store {

    private final BasicDataSource pool = new BasicDataSource();
    private static final Logger LOG = LoggerFactory.getLogger(DbStore.class.getName());

    private DbStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new InputStreamReader(
                        DbStore.class.getClassLoader()
                                .getResourceAsStream("db.properties")
                )
        )) {
            cfg.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        createDataSource(cfg);
    }

    private DbStore(Properties cfg) {
        createDataSource(cfg);
    }

    private void createDataSource(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new DbStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    public static Store instOf(Properties cfg) {
        return new DbStore(cfg);
    }

    @Override
    public Optional<Ticket> save(Ticket ticket) {
        Optional<Ticket> rsl = Optional.empty();
        if (ticket.getId() == 0) {
            rsl = Optional.of(create(ticket));
        }
        return rsl;
    }

    private Ticket create(Ticket ticket) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(
                     "INSERT INTO ticket(session_id, row, cell, account_id) VALUES (?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, ticket.getSessionId());
            ps.setInt(2, ticket.getRow());
            ps.setInt(3, ticket.getCell());
            ps.setInt(4, ticket.getAccountId());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    ticket.setId(id.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOG.error("Exception while executing sql query using jdbc", e);
            return null;
        }
        return ticket;
    }

    @Override
    public Ticket deleteTicket(int id) {
        Ticket ticket = findTicketById(id);
        if (ticket != null) {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps = cn.prepareStatement("DELETE FROM ticket WHERE id = ?")
            ) {
                ps.setInt(1, id);
                ps.execute();
            } catch (SQLException e) {
                LOG.error("Exception while executing sql query using jdbc", e);
            }
        }
        return ticket;
    }

    @Override
    public Ticket findTicketById(int id) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM ticket WHERE id = ?")
        ) {
            ps.setInt(1, id);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    return createTicketObj(it);
                }
            }
        } catch (SQLException e) {
            LOG.error("Exception while executing sql query using jdbc", e);
        }
        return null;
    }

    private Ticket createTicketObj(ResultSet it) throws SQLException {
        return new Ticket(
                it.getInt("id"),
                it.getInt("session_id"),
                it.getInt("row"),
                it.getInt("cell"),
                it.getInt("account_id"));
    }

    @Override
    public Collection<Ticket> findAllTicketsBySession(int sessionId) {
        List<Ticket> ticketList = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM ticket WHERE session_id = (?);")
        ) {
            ps.setInt(1, sessionId);
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    ticketList.add(createTicketObj(it));
                }
            }
        } catch (SQLException e) {
            LOG.error("Exception while executing sql query using jdbc", e);
        }
        return ticketList;
    }

    @Override
    public void save(Account account) {
        if (account.getId() == 0) {
            create(account);
        }
    }

    private Account create(Account account) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(
                     "INSERT INTO account(username, phone) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, account.getName());
            ps.setString(2, account.getPhone());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    account.setId(id.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOG.error("Exception while executing sql query using jdbc", e);
        }
        return account;
    }

    @Override
    public Account findAccountByPhone(String phone) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM account WHERE phone = ?")
        ) {
            ps.setString(1, phone);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    return new Account(
                            it.getInt("id"),
                            it.getString("username"),
                            it.getString("phone")
                    );
                }
            }
        } catch (SQLException e) {
            LOG.error("Exception while executing sql query using jdbc", e);
        }
        return null;
    }

    @Override
    public Account deleteAccount(String phone) {
        Account account = findAccountByPhone(phone);
        if (account != null) {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps = cn.prepareStatement("DELETE FROM account WHERE phone = ?")
            ) {
                ps.setString(1, phone);
                ps.execute();
            } catch (SQLException e) {
                LOG.error("Exception while executing sql query using jdbc", e);
            }
        }
        return account;
    }
}
