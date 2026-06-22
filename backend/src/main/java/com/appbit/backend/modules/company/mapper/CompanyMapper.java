package com.appbit.backend.modules.company.mapper;

import com.appbit.backend.modules.company.dto.CompanyRequest;
import com.appbit.backend.modules.company.entity.Company;
import org.apache.catalina.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequest dto){
        return Company.builder()
                .name(dto.name())
                .industrySector(dto.industrySector())
                .esgGoals(dto.esgGoals())
                .build();
    }

}
