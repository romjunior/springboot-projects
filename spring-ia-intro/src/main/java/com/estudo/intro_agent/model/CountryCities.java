package com.estudo.intro_agent.model;

import java.util.List;

public record CountryCities(
        String country,
        List<String> cities
) {
}
