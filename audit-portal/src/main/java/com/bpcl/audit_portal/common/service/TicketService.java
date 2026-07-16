package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.constants.Priority;
import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.constants.TicketType;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.TicketDtoMapper;
import com.bpcl.audit_portal.common.mapper.UserDtoMapper;
import org.springframework.stereotype.Service;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.model.*;
import com.bpcl.audit_portal.common.repository.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    public TicketService(TicketRepository ticketRepository, TicketHistoryRepository historyRepository, ApplicationRepository applicationRepository, UserRepository userRepository, UserDtoMapper userDtoMapper) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.userDtoMapper = userDtoMapper;
    }

    public TicketResponse createTicket(CreateTicketRequest request, Long id) {

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BAMPException(Errors.APPLICATION_NOT_FOUND));

        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BAMPException(Errors.MANDATORY_FIELD_MISSING);
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .assignedTo(request.getAssignedTo())
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .actualCompletionDate(request.getActualCompletionDate())
                .deploymentDate(request.getDeploymentDate())
                .storyPoint(request.getStoryPoint())
                .status(request.getStatus())
                .type(request.getType())
                .priority(request.getPriority())
                .application(application)
                .createdBy(currentUser)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketDtoMapper.toDto(
                savedTicket
        );
    }

    public TicketResponse updateTicket(Long ticketId,Long id,AppRole role, UpdateTicketRequest request) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BAMPException(Errors.TICKET_NOT_FOUND));

        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        for (FieldUpdateRequest update : request.getUpdates()) {

            String fieldName = update.getFieldName();
            String newValue = update.getNewValue();
            String oldValue = null;

            switch (fieldName) {

                case "description" -> {
                    oldValue = ticket.getDescription();
                    ticket.setDescription(newValue);
                }

                case "title" -> {
                    oldValue = ticket.getTitle();
                    ticket.setTitle(newValue);
                }

                case "priority" -> {
                    oldValue = ticket.getPriority()!=null ? ticket.getPriority().name() : null;

                    Boolean validPriority = false;
                    for (Priority priority : Priority.values()) {
                        if (priority.name().equals(newValue)) {
                            validPriority = true;
                            break;
                        }
                    }
                    if (!validPriority) {
                        throw new BAMPException(Errors.INVALID_PRIORITY);
                    }
                    ticket.setPriority(Priority.valueOf(newValue));
                }

                case "assignedTo" -> {
                    oldValue = ticket.getAssignedTo();
                    ticket.setAssignedTo(newValue);
                }

                case "startDate" -> {
                    oldValue = ticket.getStartDate();
                    ticket.setStartDate(newValue);
                }

                case "targetDate" -> {
                    oldValue = ticket.getTargetDate();
                    ticket.setTargetDate(newValue);
                }

                case "actualCompletionDate" -> {
                    oldValue = ticket.getActualCompletionDate();
                    ticket.setActualCompletionDate(newValue);
                }

                case "deploymentDate" -> {
                    oldValue = ticket.getDeploymentDate();
                    ticket.setDeploymentDate(newValue);
                }

                case "storyPoint" -> {

                    oldValue = ticket.getStoryPoint() != null
                            ? ticket.getStoryPoint().toString()
                            : null;

                    if (newValue == null || newValue.isBlank()) {
                        throw new BAMPException(Errors.INVALID_STORY_POINT);
                    }

                    boolean validStoryPoint = true;

                    for (char ch : newValue.toCharArray()) {
                        if (!Character.isDigit(ch)) {
                            validStoryPoint = false;
                            break;
                        }
                    }

                    if (!validStoryPoint) {
                        throw new BAMPException(Errors.INVALID_STORY_POINT);
                    }

                    ticket.setStoryPoint(Integer.parseInt(newValue));
                }

                case "status" -> {

                    oldValue = ticket.getStatus() != null
                            ? ticket.getStatus().name()
                            : null;

                    boolean validStatus = false;

                    for (TicketStatus status : TicketStatus.values()) {
                        if (status.name().equals(newValue)) {
                            validStatus = true;
                            break;
                        }
                    }

                    if (!validStatus) {
                        throw new BAMPException(Errors.INVALID_STATUS);
                    }

                    ticket.setStatus(TicketStatus.valueOf(newValue));
                }

                case "type" -> {

                    oldValue = ticket.getType() != null
                            ? ticket.getType().name()
                            : null;

                    boolean validType = false;

                    for (TicketType type : TicketType.values()) {
                        if (type.name().equals(newValue)) {
                            validType = true;
                            break;
                        }
                    }

                    if (!validType) {
                        throw new BAMPException(Errors.INVALID_TYPE);
                    }

                    ticket.setType(TicketType.valueOf(newValue));
                }

                case "headComment" -> {

                    if (role != AppRole.HEAD) {
                        throw new BAMPException(Errors.UNAUTHORIZED);
                    }

                    oldValue = ticket.getHeadComment();
                    ticket.setHeadComment(newValue);
                }

                case "spocComment" -> {

                    if (role != AppRole.SPOC) {
                        throw new BAMPException(Errors.UNAUTHORIZED);
                    }

                    oldValue = ticket.getSpocComment();
                    ticket.setSpocComment(newValue);
                }

                default -> throw new BAMPException(Errors.INVALID_FIELD_NAME);
            }

            TicketHistory history = TicketHistory.builder()
                    .ticket(ticket)
                    .fieldName(fieldName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .updatedBy(currentUser)
                    .build();

            historyRepository.save(history);
        }
        Ticket updatedTicket = ticketRepository.save(ticket);

        return TicketDtoMapper.toDto(updatedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByApplication(Long applicationId) {
        return ticketRepository.findByApplicationIdOrdered(applicationId)
                .stream()
                .map(ticket -> {
                    List<TicketHistory> histories = historyRepository.findByTicketIdOrderByUpdatedAtDesc(ticket.getId());
                    return TicketDtoMapper.toDto(ticket);
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public TicketSummaryResponse getTicketSummaryByApplication(Long applicationId) {

        return TicketSummaryResponse.builder()
                .totalTickets(
                        ticketRepository.countByApplicationId(applicationId)
                )
                .inProgressTickets(
                        ticketRepository.countByApplicationIdAndStatus(
                                applicationId,
                                TicketStatus.IN_PROGRESS
                        )
                )
                .notStartedTickets(
                        ticketRepository.countByApplicationIdAndStatus(
                                applicationId,
                                TicketStatus.NOT_STARTED
                        )
                )
                .holdTickets(
                        ticketRepository.countByApplicationIdAndStatus(
                                applicationId,
                                TicketStatus.HOLD
                        )
                )
                .closedTickets(
                        ticketRepository.countByApplicationIdAndStatus(
                                applicationId,
                                TicketStatus.CLOSED
                        )
                )
                .build();
    }
    @Transactional(readOnly = true)
    public TicketSummaryResponse getTicketSummary() {

        return TicketSummaryResponse.builder()
                .totalTickets(ticketRepository.count())
                .inProgressTickets(
                        ticketRepository.countByStatus(TicketStatus.IN_PROGRESS)
                )
                .notStartedTickets(
                        ticketRepository.countByStatus(TicketStatus.NOT_STARTED)
                )
                .holdTickets(
                        ticketRepository.countByStatus(TicketStatus.HOLD)
                )
                .closedTickets(
                        ticketRepository.countByStatus(TicketStatus.CLOSED)
                )
                .build();
    }
}
