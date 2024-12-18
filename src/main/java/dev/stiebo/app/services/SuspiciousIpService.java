package dev.stiebo.app.services;

import dev.stiebo.app.data.SuspiciousIp;
import dev.stiebo.app.data.SuspiciousIpRepository;
import dev.stiebo.app.data.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SuspiciousIpService {

    private final SuspiciousIpRepository suspiciousIpRepository;

    public SuspiciousIpService(SuspiciousIpRepository suspiciousIpRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
    }

    public Page<SuspiciousIp> list(Pageable pageable, Specification<SuspiciousIp> filter) {
        return suspiciousIpRepository.findAll(filter, pageable);
    }

    public SuspiciousIp create(SuspiciousIp ip) {
        if (suspiciousIpRepository.existsByIp(ip.getIp())) {
            throw new RuntimeException("IP Address already in list.");
        }
        return suspiciousIpRepository.save(ip);
    }

    public void delete(SuspiciousIp suspiciousIp) {
        if (!suspiciousIpRepository.existsByIp(suspiciousIp.getIp())) {
            throw new RuntimeException("IP Address not in list.");
        }
        suspiciousIpRepository.delete(suspiciousIp);
    }


}
