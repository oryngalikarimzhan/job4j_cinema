package ru.job4j.cinema.store;

import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;

import java.util.Collection;

public interface Store {
    boolean save(Ticket ticket);
    Collection<Ticket> findAllTicketsBySession(int sessionId);
    Ticket deleteTicket(int id);
    Ticket findTicketById(int id);

    void save(Account account);
    Account findAccountByPhone(String phone);
    Account deleteAccount(String phone);
}
