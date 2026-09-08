package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.driver.*;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.Team;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.TeamRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IDriverService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.DriverMapper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements IDriverService {

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final DriverMapper driverMapper;

    private Driver find(UUID id) {
        return this.driverRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("Driver")));
    }

    @Override
    public DriverResponse create(DriverRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Team")));
        Driver driver = driverMapper.toEntity(request);
        driver.setTeam(team);
        Driver saved = driverRepository.save(driver);
        return driverMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(UUID id) {
        Driver driver = find(id);
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DriverBasicResponse> getAll(int page, int size) {
        Page<Driver> driverPage = driverRepository.findAll(PageRequest.of(page, size));
        return PageResponseHelper.fromPage(driverPage, driverMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DriverBasicResponse> getByTeamId(UUID teamId, int page, int size) {
        Page<Driver> driverPage = driverRepository.findByTeamId(teamId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(driverPage, driverMapper::toBasicResponse);
    }

    @Override
    public DriverResponse update(UUID id, DriverUpdate update) {
        Driver driver = find(id);

        if (update.getTeamId() != null) {
            Team team = teamRepository.findById(update.getTeamId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("Team")));
            driver.setTeam(team);
        }

        driverMapper.updateEntityFromDto(update, driver);
        Driver updated = driverRepository.save(driver);
        return driverMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Driver driver = find(id);
        driverRepository.delete(driver);
    }
}
