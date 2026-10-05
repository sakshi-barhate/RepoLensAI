package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpringComponents {

    private int controllers;

    private int restControllers;

    private int services;

    private int repositories;

    private int entities;

    private int components;

    private int configurations;
}