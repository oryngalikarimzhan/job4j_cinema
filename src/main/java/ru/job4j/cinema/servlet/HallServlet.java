package ru.job4j.cinema.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;
import ru.job4j.cinema.store.DbStore;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class HallServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Collection<Ticket> ticketList = DbStore.instOf().findAllTicketsBySession(1);
        resp.setContentType("application/json; charset=utf-8");
        OutputStream output = resp.getOutputStream();
        String json = GSON.toJson(ticketList);
        output.write(json.getBytes(StandardCharsets.UTF_8));
        output.flush();
        output.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String phone = req.getParameter("phone");
        String name = req.getParameter("name");
        int sessionId = Integer.parseInt(req.getParameter("session"));
        int row = Integer.parseInt(req.getParameter("row"));
        int cell = Integer.parseInt(req.getParameter("cell"));


        Account account = DbStore.instOf().findAccountByPhone(phone);
        if (account == null) {
            DbStore.instOf().save(new Account(0, name, phone));
            account = DbStore.instOf().findAccountByPhone(req.getParameter("phone"));
        }
        String msg = DbStore.instOf().save(new Ticket(0, sessionId, row, cell, account.getId()))
                ? "Спасибо за покупку, " + name
                : "Ошибка повторите попытку";
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = new PrintWriter(resp.getOutputStream());
        writer.println(msg);
        writer.flush();
        writer.close();
    }
}
