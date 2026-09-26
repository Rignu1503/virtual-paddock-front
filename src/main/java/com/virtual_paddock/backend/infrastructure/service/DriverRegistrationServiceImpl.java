package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.driver.DriverResponse;
import com.virtual_paddock.backend.api.dtos.driver_registration.*;
import com.virtual_paddock.backend.domain.entities.*;
import com.virtual_paddock.backend.domain.repositories.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IDriverRegistrationService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.DriverMapper;
import com.virtual_paddock.backend.utils.enums.DriverRegistrationStatus;
import com.virtual_paddock.backend.utils.enums.DriverStatus;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverRegistrationServiceImpl implements IDriverRegistrationService {

    private final DriverRegistrationRepository registrationRepository;
    private final DriverRepository driverRepository;
    private final LeagueRepository leagueRepository;
    private final ChampionshipRepository championshipRepository;
    private final TeamRepository teamRepository;
    private final DriverMapper driverMapper;

    @Override
    public DriverRegistrationResponse register(DriverRegistrationRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));

        Championship championship = null;
        if (request.getChampionshipId() != null) {
            championship = championshipRepository.findById(request.getChampionshipId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Championship")));
        }

        Team preferredTeam = null;
        if (request.getPreferredTeamId() != null) {
            preferredTeam = teamRepository.findById(request.getPreferredTeamId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Team")));
        }

        DriverRegistration registration = DriverRegistration.builder()
                .name(request.getName().trim())
                .gamertag(request.getGamertag() != null ? request.getGamertag().trim() : null)
                .nationality(request.getNationality() != null ? request.getNationality().trim().toUpperCase() : null)
                .carNumber(request.getCarNumber() != null ? request.getCarNumber().trim() : null)
                .carModel(request.getCarModel() != null ? request.getCarModel().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .discordTag(request.getDiscordTag() != null ? request.getDiscordTag().trim() : null)
                .contactType(request.getContactType() != null ? request.getContactType().trim().toUpperCase() : "DISCORD")
                .phoneWhatsapp(request.getPhoneWhatsapp() != null ? request.getPhoneWhatsapp().trim() : null)
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .status(DriverRegistrationStatus.PENDING)
                .league(league)
                .championship(championship)
                .preferredTeam(preferredTeam)
                .build();

        DriverRegistration saved = registrationRepository.save(registration);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverRegistrationResponse getById(UUID id) {
        DriverRegistration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("DriverRegistration")));
        return toResponse(reg);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DriverRegistrationResponse> getFiltered(
            UUID leagueId,
            UUID championshipId,
            DriverRegistrationStatus status,
            int page,
            int size
    ) {
        Page<DriverRegistration> pageResult = registrationRepository.findByFilter(
                leagueId,
                championshipId,
                status,
                PageRequest.of(page, size)
        );
        return PageResponseHelper.fromPage(pageResult, this::toResponse);
    }

    @Override
    public DriverResponse approve(UUID id, DriverApprovalRequest approval) {
        DriverRegistration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("DriverRegistration")));

        if (reg.getStatus() != DriverRegistrationStatus.PENDING) {
            throw new BadRequestException("Esta solicitud ya ha sido procesada previamente con estado: " + reg.getStatus());
        }

        Team assignedTeam = teamRepository.findById(approval.getAssignedTeamId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Team")));

        String finalCarNumber = (approval.getCarNumber() != null && !approval.getCarNumber().isBlank())
                ? approval.getCarNumber().trim()
                : reg.getCarNumber();

        String finalCarModel = (approval.getCarModel() != null && !approval.getCarModel().isBlank())
                ? approval.getCarModel().trim()
                : (reg.getCarModel() != null ? reg.getCarModel() : assignedTeam.getCarModel());

        // Crear piloto oficial activo
        Driver driver = Driver.builder()
                .name(reg.getName())
                .gamertag(reg.getGamertag())
                .nationality(reg.getNationality())
                .carNumber(finalCarNumber)
                .carModel(finalCarModel)
                .team(assignedTeam)
                .status(DriverStatus.ACTIVE)
                .build();

        Driver savedDriver = driverRepository.save(driver);

        // Actualizar solicitud a aprobada
        reg.setStatus(DriverRegistrationStatus.APPROVED);
        reg.setAssignedTeam(assignedTeam);
        reg.setReviewedAt(LocalDateTime.now());
        registrationRepository.save(reg);

        return driverMapper.toResponse(savedDriver);
    }

    @Override
    public DriverRegistrationResponse reject(UUID id, DriverRejectionRequest rejection) {
        DriverRegistration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("DriverRegistration")));

        if (reg.getStatus() != DriverRegistrationStatus.PENDING) {
            throw new BadRequestException("Esta solicitud ya ha sido procesada previamente con estado: " + reg.getStatus());
        }

        reg.setStatus(DriverRegistrationStatus.REJECTED);
        reg.setRejectionReason(rejection != null ? rejection.getReason() : null);
        reg.setReviewedAt(LocalDateTime.now());

        DriverRegistration saved = registrationRepository.save(reg);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending(UUID leagueId) {
        if (leagueId != null) {
            return registrationRepository.countByLeagueIdAndStatus(leagueId, DriverRegistrationStatus.PENDING);
        }
        return registrationRepository.countByStatus(DriverRegistrationStatus.PENDING);
    }

    private DriverRegistrationResponse toResponse(DriverRegistration reg) {
        return DriverRegistrationResponse.builder()
                .id(reg.getId())
                .name(reg.getName())
                .gamertag(reg.getGamertag())
                .nationality(reg.getNationality())
                .carNumber(reg.getCarNumber())
                .carModel(reg.getCarModel())
                .email(reg.getEmail())
                .discordTag(reg.getDiscordTag())
                .contactType(reg.getContactType())
                .phoneWhatsapp(reg.getPhoneWhatsapp())
                .notes(reg.getNotes())
                .status(reg.getStatus())
                .leagueId(reg.getLeague() != null ? reg.getLeague().getId() : null)
                .leagueName(reg.getLeague() != null ? reg.getLeague().getName() : null)
                .championshipId(reg.getChampionship() != null ? reg.getChampionship().getId() : null)
                .championshipName(reg.getChampionship() != null ? reg.getChampionship().getGameName() : null)
                .preferredTeamId(reg.getPreferredTeam() != null ? reg.getPreferredTeam().getId() : null)
                .preferredTeamName(reg.getPreferredTeam() != null ? reg.getPreferredTeam().getName() : null)
                .assignedTeamId(reg.getAssignedTeam() != null ? reg.getAssignedTeam().getId() : null)
                .assignedTeamName(reg.getAssignedTeam() != null ? reg.getAssignedTeam().getName() : null)
                .createdAt(reg.getCreatedAt())
                .reviewedAt(reg.getReviewedAt())
                .rejectionReason(reg.getRejectionReason())
                .build();
    }
}
