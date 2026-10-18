package com.estudo.intro_agent.repository;

import com.estudo.intro_agent.entity.HelpDeskTicket;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HelpDeskTicketRepository extends CrudRepository<HelpDeskTicket, Long> {

    List<HelpDeskTicket> findByUsername(String username);
}
