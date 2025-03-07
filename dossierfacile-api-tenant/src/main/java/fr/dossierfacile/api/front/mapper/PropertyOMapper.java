package fr.dossierfacile.api.front.mapper;

import fr.dossierfacile.api.front.model.property.PropertyOModel;
import fr.dossierfacile.common.entity.Property;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public abstract class PropertyOMapper {

    public abstract PropertyOModel toPropertyModel(Property property);
}
