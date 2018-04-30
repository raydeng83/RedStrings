package com.redstrings.backend.model;


import javax.persistence.*;

@Entity
public class OtpRef {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    private String otpString;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOtpString() {
        return otpString;
    }

    public void setOtpString(String otpString) {
        this.otpString = otpString;
    }
}
