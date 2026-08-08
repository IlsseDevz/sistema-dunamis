package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.AdminDashboardStats;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final InscricaoRepository inscricaoRepository;

    public AdminDashboardService(UserRepository userRepository, InscricaoRepository inscricaoRepository) {
        this.userRepository = userRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        AdminDashboardStats stats = new AdminDashboardStats();
        stats.setTotalMembers(userRepository.count());
        stats.setActiveMembers(userRepository.countByChurchSituation(ChurchSituation.MEMBRO_ATIVO));
        stats.setNewConverts(userRepository.countByChurchSituation(ChurchSituation.NOVO_CONVERTIDO));
        stats.setVisitors(userRepository.countByChurchSituation(ChurchSituation.VISITANTE));
        stats.setBaptized(userRepository.countByBaptizedTrue());
        stats.setNotBaptized(userRepository.countByBaptizedFalse());
        stats.setTotalStudents(inscricaoRepository.countByStatus(EnrollmentStatus.ACTIVE));
        return stats;
    }
}
