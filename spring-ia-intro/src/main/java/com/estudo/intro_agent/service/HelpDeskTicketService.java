package com.estudo.intro_agent.service;

import com.estudo.intro_agent.entity.HelpDeskTicket;
import com.estudo.intro_agent.model.TicketRequest;
import com.estudo.intro_agent.repository.HelpDeskTicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HelpDeskTicketService {

    private final HelpDeskTicketRepository helpDeskTicketRepository;

    public HelpDeskTicketService(HelpDeskTicketRepository helpDeskTicketRepository) {
        this.helpDeskTicketRepository = helpDeskTicketRepository;
    }

    public HelpDeskTicket createTicket(TicketRequest ticket, String username) {
        HelpDeskTicket helpDeskTicket = new HelpDeskTicket(
                null,
                username,
                ticket.issue(),
                "OPEN",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );
        return helpDeskTicketRepository.save(helpDeskTicket);
    }

    public List<HelpDeskTicket> getTicketsByUsername(String username) {
        return helpDeskTicketRepository.findByUsername(username);
    }


}
