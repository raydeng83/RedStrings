package com.redstrings.backend.service.impl;

import com.redstrings.backend.model.OtpRef;
import com.redstrings.backend.repository.OtpRefRepository;
import com.redstrings.backend.service.OtpRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
public class OtpRefServiceImpl implements OtpRefService {
    @Autowired
    private OtpRefRepository otpRefRepository;

    @Override
    public OtpRef save(OtpRef otpRef) {
        return otpRefRepository.save(otpRef);
    }

    @Override
    public OtpRef findById(Long otpId) {
        return otpRefRepository.findById(otpId).get();
    }

    @Override
    public void deleteById(Long otpId) {
        otpRefRepository.deleteById(otpId);
    }
}
