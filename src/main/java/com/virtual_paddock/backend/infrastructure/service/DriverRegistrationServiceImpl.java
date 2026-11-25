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
import java.util.List;
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
    private final SeasonRepository seasonRepository;
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

        String cleanName = request.getName().trim();
        String cleanGamertag = (request.getGamertag() != null && !request.getGamertag().isBlank())
                ? request.getGamertag().trim()
                : null;

        if (cleanGamertag != null && cleanGamertag.startsWith("@")) {
            cleanGamertag = cleanGamertag.substring(1).trim();
        }

        String cleanCarNumber = (request.getCarNumber() != null && !request.getCarNumber().isBlank())
                ? request.getCarNumber().trim()
                : null;
        if (cleanCarNumber != null && cleanCarNumber.startsWith("#")) {
            cleanCarNumber = cleanCarNumber.substring(1).trim();
        }

        Team preferredTeam = null;
        if (request.getPreferredTeamId() != null) {
            preferredTeam = teamRepository.findById(request.getPreferredTeamId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Team")));
            int limit = (preferredTeam.getMaxDrivers() != null && preferredTeam.getMaxDrivers() > 0)
                    ? preferredTeam.getMaxDrivers()
                    : (preferredTeam.getChampionship() != null && preferredTeam.getChampionship().getMaxDriversPerTeam() != null
                        ? preferredTeam.getChampionship().getMaxDriversPerTeam() : 2);
            long currentCount = driverRepository.countByTeamId(preferredTeam.getId());
            if (currentCount >= limit) {
                throw new BadRequestException("El equipo '" + preferredTeam.getName() + "' ya está completo (" + currentCount + "/" + limit + " pilotos). Por favor selecciona otra escudería.");
            }
        }

        Season season = null;
        UUID effectiveSeasonId = request.getSeasonId();
        if (effectiveSeasonId != null) {
            season = seasonRepository.findById(effectiveSeasonId).orElse(null);
        }
        if (season == null && preferredTeam != null && preferredTeam.getSeason() != null) {
            season = preferredTeam.getSeason();
            effectiveSeasonId = season.getId();
        }
        if (season == null && championship != null) {
            List<Season> seasons = seasonRepository.findByChampionshipIdOrderByIsDefaultDesc(championship.getId());
            if (!seasons.isEmpty()) {
                season = seasons.get(0);
                effectiveSeasonId = season.getId();
            }
        }

        // Si hay campeonato, validar unicidad a nivel de campeonato y temporada (permite mismo piloto en otras temporadas)
        if (championship != null) {
            UUID champId = championship.getId();
            if (driverRepository.existsByNameInChampionshipAndSeason(champId, effectiveSeasonId, cleanName) ||
                registrationRepository.existsActiveByNameInChampionshipAndSeason(champId, effectiveSeasonId, cleanName)) {
                throw new BadRequestException("El nombre de piloto '" + cleanName + "' ya se encuentra registrado o con solicitud pendiente en esta temporada.");
            }

            if (cleanGamertag != null && !cleanGamertag.isBlank()) {
                if (driverRepository.existsByGamertagInChampionshipAndSeason(champId, effectiveSeasonId, cleanGamertag) ||
                    registrationRepository.existsActiveByGamertagInChampionshipAndSeason(champId, effectiveSeasonId, cleanGamertag)) {
                    throw new BadRequestException("El gamertag '" + cleanGamertag + "' ya se encuentra registrado o con solicitud pendiente en esta temporada.");
                }
            }

            if (cleanCarNumber != null && !cleanCarNumber.isBlank()) {
                if (driverRepository.existsByCarNumberInChampionshipAndSeason(champId, effectiveSeasonId, cleanCarNumber) ||
                    registrationRepository.existsActiveByCarNumberInChampionshipAndSeason(champId, effectiveSeasonId, cleanCarNumber)) {
                    throw new BadRequestException("El dorsal #" + cleanCarNumber + " ya se encuentra registrado o con solicitud pendiente en esta temporada.");
                }
            }
        } else {
            // Sin campeonato especifico: validar a nivel de liga
            if (driverRepository.existsByNameInLeague(request.getLeagueId(), cleanName) ||
                registrationRepository.existsActiveByNameInLeague(request.getLeagueId(), cleanName)) {
                throw new BadRequestException("El nombre de piloto '" + cleanName + "' ya se encuentra registrado o con solicitud pendiente en esta liga.");
            }

            if (cleanGamertag != null && !cleanGamertag.isBlank()) {
                if (driverRepository.existsByGamertagInLeague(request.getLeagueId(), cleanGamertag) ||
                    registrationRepository.existsActiveByGamertagInLeague(request.getLeagueId(), cleanGamertag)) {
                    throw new BadRequestException("El gamertag '" + cleanGamertag + "' ya se encuentra registrado o con solicitud pendiente en esta liga.");
                }
            }

            if (cleanCarNumber != null && !cleanCarNumber.isBlank()) {
                if (driverRepository.existsByCarNumberInLeague(request.getLeagueId(), cleanCarNumber) ||
                    registrationRepository.existsActiveByCarNumberInLeague(request.getLeagueId(), cleanCarNumber)) {
                    throw new BadRequestException("El dorsal #" + cleanCarNumber + " ya se encuentra registrado o con solicitud pendiente en esta liga.");
                }
            }
        }

        String cleanSteamId = (request.getSteamId() != null && !request.getSteamId().isBlank())
                ? request.getSteamId().trim()
                : null;

        if (championship != null && Boolean.TRUE.equals(championship.getRequireSteamId())) {
            if (cleanSteamId == null || cleanSteamId.isBlank()) {
                throw new BadRequestException("El Steam ID es obligatorio para inscribirse en este campeonato.");
            }
        }

        DriverRegistration registration = DriverRegistration.builder()
                .name(cleanName)
                .gamertag(cleanGamertag)
                .nationality(request.getNationality() != null ? request.getNationality().trim().toUpperCase() : null)
                .carNumber(request.getCarNumber() != null ? request.getCarNumber().trim() : null)
                .carModel(request.getCarModel() != null ? request.getCarModel().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .discordTag(request.getDiscordTag() != null ? request.getDiscordTag().trim() : null)
                .contactType(request.getContactType() != null ? request.getContactType().trim().toUpperCase() : "DISCORD")
                .phoneWhatsapp(request.getPhoneWhatsapp() != null ? request.getPhoneWhatsapp().trim() : null)
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .steamId(cleanSteamId)
                .status(DriverRegistrationStatus.PENDING)
                .league(league)
                .championship(championship)
                .season(season)
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

        int limit = (assignedTeam.getMaxDrivers() != null && assignedTeam.getMaxDrivers() > 0)
                ? assignedTeam.getMaxDrivers()
                : (assignedTeam.getChampionship() != null && assignedTeam.getChampionship().getMaxDriversPerTeam() != null
                    ? assignedTeam.getChampionship().getMaxDriversPerTeam() : 2);
        long currentDrivers = driverRepository.countByTeamId(assignedTeam.getId());
        if (currentDrivers >= limit) {
            throw new BadRequestException("El equipo '" + assignedTeam.getName() + "' ya ha alcanzado el limite maximo de pilotos (" + currentDrivers + "/" + limit + ").");
        }

        String finalCarNumber = (approval.getCarNumber() != null && !approval.getCarNumber().isBlank())
                ? approval.getCarNumber().trim()
                : reg.getCarNumber();

        String finalCarModel = (approval.getCarModel() != null && !approval.getCarModel().isBlank())
                ? approval.getCarModel().trim()
                : (reg.getCarModel() != null ? reg.getCarModel() : assignedTeam.getCarModel());

        Driver driver = Driver.builder()
                .name(reg.getName())
                .gamertag(reg.getGamertag())
                .nationality(reg.getNationality())
                .carNumber(finalCarNumber)
                .carModel(finalCarModel)
                .steamId(reg.getSteamId())
                .team(assignedTeam)
                .status(DriverStatus.ACTIVE)
                .build();

        Driver savedDriver = driverRepository.save(driver);

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
    public DriverAvailabilityResponse checkAvailability(UUID leagueId, UUID championshipId, UUID seasonId, String name, String gamertag, String carNumber) {
        UUID effectiveSeasonId = seasonId;
        if (effectiveSeasonId == null && championshipId != null) {
            List<Season> seasons = seasonRepository.findByChampionshipIdOrderByIsDefaultDesc(championshipId);
            if (!seasons.isEmpty()) {
                effectiveSeasonId = seasons.get(0).getId();
            }
        }

        boolean nameAvail = true;
        String nameMsg = null;

        if (name != null && !name.isBlank()) {
            String cleanName = name.trim();
            if (championshipId != null) {
                if (driverRepository.existsByNameInChampionshipAndSeason(championshipId, effectiveSeasonId, cleanName) ||
                    registrationRepository.existsActiveByNameInChampionshipAndSeason(championshipId, effectiveSeasonId, cleanName)) {
                    nameAvail = false;
                    nameMsg = "El nombre '" + cleanName + "' ya esta registrado o con solicitud pendiente en esta temporada.";
                }
            } else {
                if (driverRepository.existsByNameInLeague(leagueId, cleanName) ||
                    registrationRepository.existsActiveByNameInLeague(leagueId, cleanName)) {
                    nameAvail = false;
                    nameMsg = "El nombre '" + cleanName + "' ya esta registrado o con solicitud pendiente en esta liga.";
                }
            }
        }

        boolean gamertagAvail = true;
        String gamertagMsg = null;

        if (gamertag != null && !gamertag.isBlank()) {
            String cleanGamertag = gamertag.trim();
            if (cleanGamertag.startsWith("@")) {
                cleanGamertag = cleanGamertag.substring(1).trim();
            }
            if (!cleanGamertag.isBlank()) {
                if (championshipId != null) {
                    if (driverRepository.existsByGamertagInChampionshipAndSeason(championshipId, effectiveSeasonId, cleanGamertag) ||
                        registrationRepository.existsActiveByGamertagInChampionshipAndSeason(championshipId, effectiveSeasonId, cleanGamertag)) {
                        gamertagAvail = false;
                        gamertagMsg = "El gamertag '" + cleanGamertag + "' ya esta registrado o con solicitud pendiente en esta temporada.";
                    }
                } else {
                    if (driverRepository.existsByGamertagInLeague(leagueId, cleanGamertag) ||
                        registrationRepository.existsActiveByGamertagInLeague(leagueId, cleanGamertag)) {
                        gamertagAvail = false;
                        gamertagMsg = "El gamertag '" + cleanGamertag + "' ya esta registrado o con solicitud pendiente en esta liga.";
                    }
                }
            }
        }

        boolean carNumberAvail = true;
        String carNumberMsg = null;

        if (carNumber != null && !carNumber.isBlank()) {
            String cleanCarNumber = carNumber.trim();
            if (cleanCarNumber.startsWith("#")) {
                cleanCarNumber = cleanCarNumber.substring(1).trim();
            }
            if (!cleanCarNumber.isBlank()) {
                if (championshipId != null) {
                    if (driverRepository.existsByCarNumberInChampionshipAndSeason(championshipId, effectiveSeasonId, cleanCarNumber) ||
                        registrationRepository.existsActiveByCarNumberInChampionshipAndSeason(championshipId, effectiveSeasonId, cleanCarNumber)) {
                        carNumberAvail = false;
                        carNumberMsg = "El dorsal #" + cleanCarNumber + " ya esta registrado o con solicitud pendiente en esta temporada.";
                    }
                } else {
                    if (driverRepository.existsByCarNumberInLeague(leagueId, cleanCarNumber) ||
                        registrationRepository.existsActiveByCarNumberInLeague(leagueId, cleanCarNumber)) {
                        carNumberAvail = false;
                        carNumberMsg = "El dorsal #" + cleanCarNumber + " ya esta registrado o con solicitud pendiente en esta liga.";
                    }
                }
            }
        }

        return DriverAvailabilityResponse.builder()
                .nameAvailable(nameAvail)
                .gamertagAvailable(gamertagAvail)
                .carNumberAvailable(carNumberAvail)
                .nameMessage(nameMsg)
                .gamertagMessage(gamertagMsg)
                .carNumberMessage(carNumberMsg)
                .build();
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
        UUID seasonId = null;
        String seasonName = null;
        if (reg.getSeason() != null) {
            seasonId = reg.getSeason().getId();
            seasonName = reg.getSeason().getSeasonName();
        } else if (reg.getPreferredTeam() != null && reg.getPreferredTeam().getSeason() != null) {
            seasonId = reg.getPreferredTeam().getSeason().getId();
            seasonName = reg.getPreferredTeam().getSeason().getSeasonName();
        }

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
                .steamId(reg.getSteamId())
                .status(reg.getStatus())
                .leagueId(reg.getLeague() != null ? reg.getLeague().getId() : null)
                .leagueName(reg.getLeague() != null ? reg.getLeague().getName() : null)
                .championshipId(reg.getChampionship() != null ? reg.getChampionship().getId() : null)
                .championshipName(reg.getChampionship() != null ? reg.getChampionship().getGameName() : null)
                .seasonId(seasonId)
                .seasonName(seasonName)
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
