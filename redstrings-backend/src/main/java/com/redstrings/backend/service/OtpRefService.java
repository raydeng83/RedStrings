package com.redstrings.backend.service;


import com.redstrings.backend.model.OtpRef;

public interface OtpRefService {
    OtpRef save(OtpRef otpRef);

    OtpRef findById(Long otpId);

    void deleteById(Long otpId);
}
