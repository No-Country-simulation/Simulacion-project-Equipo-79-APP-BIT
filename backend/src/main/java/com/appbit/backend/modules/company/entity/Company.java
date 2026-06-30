package com.appbit.backend.modules.company.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "industry_sector")
    private String industrySector;

    @Column(name = "esg_goals", length = 1000)
    private String esgGoals;

    @Column(name = "diversity_goal", length = 500)
    private String diversityGoal;

    @Column(name = "priority_regions", length = 500)
    private String priorityRegions;

    @Column(name = "interest_groups", length = 500)
    private String interestGroups;

    @Column(length = 20)
    private String nit;

    @Column(length = 50)
    private String size;

    @Column(length = 100)
    private String city;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
