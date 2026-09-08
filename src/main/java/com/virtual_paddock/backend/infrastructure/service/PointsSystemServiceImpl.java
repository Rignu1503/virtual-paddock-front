package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.pointssystem.*;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.PointRule;
import com.virtual_paddock.backend.domain.entities.PointsSystem;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.domain.repositories.PointsSystemRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IPointsSystemService;
import com.virtual_paddock.backend.infrastructure.helper.PageResponseHelper;
import com.virtual_paddock.backend.infrastructure.mapper.PointsSystemMapper;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import com.virtual_paddock.backend.utils.exeption.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PointsSystemServiceImpl implements IPointsSystemService {

    private final PointsSystemRepository pointsSystemRepository;
    private final LeagueRepository leagueRepository;
    private final PointsSystemMapper pointsSystemMapper;

    private PointsSystem find(UUID id) {
        return this.pointsSystemRepository.findById(id).orElseThrow(() ->
                new BadRequestException(ErrorMessages.IdNotFound("PointsSystem")));
    }

    @Override
    public PointsSystemResponse create(PointsSystemRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.IdNotFound("League")));

        PointsSystem pointsSystem = pointsSystemMapper.toEntity(request);
        pointsSystem.setLeague(league);

        if (request.getRules() != null && !request.getRules().isEmpty()) {
            List<PointRule> rules = new ArrayList<>();
            for (PointRuleDto ruleDto : request.getRules()) {
                PointRule rule = pointsSystemMapper.toRuleEntity(ruleDto);
                rule.setPointsSystem(pointsSystem);
                rules.add(rule);
            }
            pointsSystem.setRules(rules);
        }

        PointsSystem saved = pointsSystemRepository.save(pointsSystem);
        return pointsSystemMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PointsSystemResponse getById(UUID id) {
        PointsSystem pointsSystem = find(id);
        return pointsSystemMapper.toResponse(pointsSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsSystemResponse> getByLeagueId(UUID leagueId) {
        List<PointsSystem> list = pointsSystemRepository.findByLeagueId(leagueId);
        return list.stream().map(pointsSystemMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PointsSystemBasicResponse> getByLeagueIdPaged(UUID leagueId, int page, int size) {
        Page<PointsSystem> paged = pointsSystemRepository.findByLeagueId(leagueId, PageRequest.of(page, size));
        return PageResponseHelper.fromPage(paged, pointsSystemMapper::toBasicResponse);
    }

    @Override
    public PointsSystemResponse update(UUID id, PointsSystemUpdate update) {
        PointsSystem pointsSystem = find(id);

        pointsSystemMapper.updateEntityFromDto(update, pointsSystem);

        if (update.getRules() != null) {
            pointsSystem.getRules().clear();
            for (PointRuleDto ruleDto : update.getRules()) {
                PointRule rule = pointsSystemMapper.toRuleEntity(ruleDto);
                rule.setPointsSystem(pointsSystem);
                pointsSystem.getRules().add(rule);
            }
        }

        PointsSystem updated = pointsSystemRepository.save(pointsSystem);
        return pointsSystemMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        PointsSystem pointsSystem = find(id);
        pointsSystemRepository.delete(pointsSystem);
    }
}
