package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.constants.Priority;
import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.TicketAssignmentMapper;
import com.bpcl.audit_portal.common.mapper.TicketDtoMapper;
import com.bpcl.audit_portal.common.model.*;
import com.bpcl.audit_portal.common.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final TicketHistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final TicketAssignmentRepository ticketAssignmentRepository;

    public TicketService(TicketRepository ticketRepository,
                         TicketAssignmentRepository assignmentRepository,
                         TicketHistoryRepository historyRepository,
                         ApplicationRepository applicationRepository,
                         TicketAssignmentRepository ticketAssignmentRepository,
                         UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.assignmentRepository = assignmentRepository;
        this.historyRepository = historyRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.ticketAssignmentRepository = ticketAssignmentRepository;
    }
    @Transactional
    public TicketResponse createTicket(
            CreateTicketRequest request,
            Long currentUserId
    ) {

        Application application =
                applicationRepository.findById(
                                request.getApplicationId()
                        )
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.APPLICATION_NOT_FOUND
                                )
                        );

        User currentUser =
                userRepository.findById(
                                currentUserId
                        )
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.USER_NOT_FOUND
                                )
                        );

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(
                        request.getStatus() != null
                                ? request.getStatus()
                                : TicketStatus.NOT_STARTED
                )
                .type(request.getType())
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .actualCompletionDate(
                        request.getActualCompletionDate()
                )
                .deploymentDate(
                        request.getDeploymentDate()
                )
                .storyPoint(request.getStoryPoint())
                .application(application)
                .createdBy(currentUser)
                .build();

        Ticket savedTicket =
                ticketRepository.save(ticket);

        TicketAssignment assignment = null;

        if (request.getAssignedToId() != null) {

            User assignedTo =
                    userRepository.findById(
                                    request.getAssignedToId()
                            )
                            .orElseThrow(() ->
                                    new BAMPException(
                                            Errors.USER_NOT_FOUND
                                    )
                            );

            assignment =
                    TicketAssignment.builder()
                            .ticket(savedTicket)
                            .assignedTo(assignedTo)
                            .assignedBy(currentUser)
                            .build();

            ticketAssignmentRepository.save(
                    assignment
            );
        }

        return TicketDtoMapper.toDto(
                savedTicket,
                assignment
        );
    }

    public void assignTicket(Long ticketId, Long assignedToId, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BAMPException(Errors.TICKET_NOT_FOUND));

        User assignedTo = userRepository.findById(assignedToId)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        User assignedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        assignmentRepository.findByTicketIdAndActiveTrue(ticketId)
                .ifPresent(oldAssignment -> {
                    oldAssignment.setActive(false);
                    oldAssignment.setDeassignedAt(LocalDateTime.now());
                    oldAssignment.setDeassignedBy(assignedBy);
                });

        TicketAssignment newAssignment = TicketAssignment.builder()
                .ticket(ticket)
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .active(true)
                .build();

        assignmentRepository.save(newAssignment);
    }

    public void deassignTicket(Long ticketId, Long currentUserId) {
        TicketAssignment assignment = assignmentRepository.findByTicketIdAndActiveTrue(ticketId)
                .orElseThrow(() -> new BAMPException(Errors.TICKET_NOT_ASSIGNED));

        User deassignedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        assignment.setActive(false);
        assignment.setDeassignedAt(LocalDateTime.now());
        assignment.setDeassignedBy(deassignedBy);

        assignmentRepository.save(assignment);
    }

    @Transactional
    public TicketResponse updateTicket(
            Long ticketId,
            Long currentUserId,
            UpdateTicketRequest request
    ) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.TICKET_NOT_FOUND
                        ));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        boolean assignedToUser =
                assignmentRepository
                        .existsByTicketIdAndAssignedToIdAndActiveTrue(
                                ticketId,
                                currentUserId
                        );

        for (FieldUpdateRequest update : request.getUpdates()) {

            String fieldName = update.getFieldName();
            String newValue = update.getNewValue();
            String oldValue = null;

            switch (fieldName) {

                case "status" -> {

                    if (!assignedToUser) {
                        throw new BAMPException(
                                Errors.INSUFFICIENT_PERMISSION
                        );
                    }

                    oldValue =
                            ticket.getStatus() != null
                                    ? ticket.getStatus().name()
                                    : null;

                    ticket.setStatus(
                            TicketStatus.valueOf(newValue)
                    );
                }

                case "title" -> {
                    oldValue = ticket.getTitle();
                    ticket.setTitle(newValue);
                }

                case "description" -> {
                    oldValue = ticket.getDescription();
                    ticket.setDescription(newValue);
                }

                case "priority" -> {
                    oldValue =
                            ticket.getPriority() != null
                                    ? ticket.getPriority().name()
                                    : null;

                    ticket.setPriority(
                            Priority.valueOf(newValue)
                    );
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
                    oldValue =
                            ticket.getActualCompletionDate();

                    ticket.setActualCompletionDate(
                            newValue
                    );
                }

                case "deploymentDate" -> {
                    oldValue = ticket.getDeploymentDate();
                    ticket.setDeploymentDate(newValue);
                }

                case "storyPoint" -> {

                    oldValue =
                            ticket.getStoryPoint() != null
                                    ? ticket.getStoryPoint()
                                    .toString()
                                    : null;

                    ticket.setStoryPoint(
                            newValue != null &&
                                    !newValue.isBlank()
                                    ? Integer.parseInt(newValue)
                                    : null
                    );
                }

                case "headComment" -> {
                    oldValue = ticket.getHeadComment();
                    ticket.setHeadComment(newValue);
                }

                case "spocComment" -> {
                    oldValue = ticket.getSpocComment();
                    ticket.setSpocComment(newValue);
                }

                default ->
                        throw new BAMPException(
                                Errors.INVALID_FIELD_NAME
                        );
            }

            historyRepository.save(
                    TicketHistory.builder()
                            .ticket(ticket)
                            .fieldName(fieldName)
                            .oldValue(oldValue)
                            .newValue(newValue)
                            .updatedBy(currentUser)
                            .build()
            );
        }

        Ticket updatedTicket =
                ticketRepository.save(ticket);

        TicketAssignment assignment =
                assignmentRepository
                        .findByTicketIdAndActiveTrue(
                                ticketId
                        )
                        .orElse(null);

        return TicketDtoMapper.toDto(
                updatedTicket,
                assignment
        );
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByApplication(
            Long applicationId
    ) {

        return ticketRepository
                .findByApplicationIdOrdered(applicationId)
                .stream()
                .map(ticket -> {

                    TicketAssignment assignment =
                            assignmentRepository
                                    .findByTicketIdAndActiveTrue(
                                            ticket.getId()
                                    )
                                    .orElse(null);

                    return TicketDtoMapper.toDto(
                            ticket,
                            assignment
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUser(
            Long userId
    ) {

        return assignmentRepository
                .findByAssignedToIdAndActiveTrue(userId)
                .stream()
                .map(assignment ->
                        TicketDtoMapper.toDto(
                                assignment.getTicket(),
                                assignment
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketSummaryResponse getTicketSummaryByApplication(Long applicationId) {
        return TicketSummaryResponse.builder()
                .totalTickets(ticketRepository.countByApplicationId(applicationId))
                .inProgressTickets(ticketRepository.countByApplicationIdAndStatus(applicationId, TicketStatus.IN_PROGRESS))
                .notStartedTickets(ticketRepository.countByApplicationIdAndStatus(applicationId, TicketStatus.NOT_STARTED))
                .holdTickets(ticketRepository.countByApplicationIdAndStatus(applicationId, TicketStatus.HOLD))
                .closedTickets(ticketRepository.countByApplicationIdAndStatus(applicationId, TicketStatus.CLOSED))
                .build();
    }

    @Transactional(readOnly = true)
    public TicketSummaryResponse getTicketSummary() {
        return TicketSummaryResponse.builder()
                .totalTickets(ticketRepository.count())
                .inProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS))
                .notStartedTickets(ticketRepository.countByStatus(TicketStatus.NOT_STARTED))
                .holdTickets(ticketRepository.countByStatus(TicketStatus.HOLD))
                .closedTickets(ticketRepository.countByStatus(TicketStatus.CLOSED))
                .build();
    }

    @Transactional(readOnly = true)
    public TicketSummaryResponse getTicketSummaryByUser(Long userId) {
        return TicketSummaryResponse.builder()
                .totalTickets(ticketRepository.countByAssignedToId(userId))
                .inProgressTickets(ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.IN_PROGRESS))
                .notStartedTickets(ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.NOT_STARTED))
                .holdTickets(ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.HOLD))
                .closedTickets(ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.CLOSED))
                .build();
    }
    @Transactional(readOnly = true)
    public List<TicketAssignmentResponse>
    getAssignmentHistory(Long ticketId) {

        ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.TICKET_NOT_FOUND
                        )
                );

        return assignmentRepository
                .findByTicketIdOrderByAssignedAtDesc(
                        ticketId
                )
                .stream()
                .map(
                        TicketAssignmentMapper::toResponse
                )
                .toList();
    }
}