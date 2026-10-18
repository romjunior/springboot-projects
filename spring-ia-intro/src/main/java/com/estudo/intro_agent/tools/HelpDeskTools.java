package com.estudo.intro_agent.tools;

import com.estudo.intro_agent.entity.HelpDeskTicket;
import com.estudo.intro_agent.model.TicketRequest;
import com.estudo.intro_agent.service.HelpDeskTicketService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HelpDeskTools {

    private final HelpDeskTicketService  helpDeskTicketService;

    public HelpDeskTools(HelpDeskTicketService helpDeskTicketService) {
        this.helpDeskTicketService = helpDeskTicketService;
    }

    //return direct usado quando a resposta da tool já serve e não precisa passar pela LLM
    @Tool(name = "createTicket", description = "Criação de ticket com a descrição e o usuário necessários", returnDirect = true)
    String createTicket(@ToolParam(description = "Descrição do problema para abrir o ticket") String issue,
                        ToolContext toolContext) {
        final var username = (String) toolContext.getContext().get("username");
        final var ticket = helpDeskTicketService.createTicket(new TicketRequest(issue), username);
        return "Ticket #" + ticket.getId() + " created for user " + ticket.getUsername();
    }

    @Tool(name = "getTicketsByUsername", description = "Buscar o status de tickets baseado no username")
    List<HelpDeskTicket> getTicketsByUsername(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        return helpDeskTicketService.getTicketsByUsername(username);
    }
}
