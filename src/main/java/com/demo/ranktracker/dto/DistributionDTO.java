package com.demo.ranktracker.dto;

public record DistributionDTO(
    long top3,
    long top10,
    long top20,
    long top50,
    long top100,
    long notRanking
) {}
