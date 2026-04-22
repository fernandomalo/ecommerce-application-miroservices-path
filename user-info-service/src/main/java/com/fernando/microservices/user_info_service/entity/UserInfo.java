package com.fernando.microservices.user_info_service.entity;

import java.math.BigInteger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
    @Index(columnList = "userId")
})
public class UserInfo {
    
    @Id
    private Long userId;

    private String country;
    private String region;
    private String city;
    private String location;

    @Size(min = 10, max = 10)
    private String phoneNumber;
    private BigInteger purchases;

    @OneToOne(mappedBy = "userInfo")
    private Company company;
}
