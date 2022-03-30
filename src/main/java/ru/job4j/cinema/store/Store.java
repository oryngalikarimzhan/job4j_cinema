package ru.job4j.cinema.store;

import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;

import java.util.Collection;
import java.util.Optional;

public interface Store {
    Optional<Ticket> save(Ticket ticket);
    Collection<Ticket> findAllTicketsBySession(int sessionId);
    Ticket deleteTicket(int id);
    Ticket findTicketById(int id);

    void save(Account account);
    Account findAccountByPhone(String phone);
    Account deleteAccount(String phone);
}
